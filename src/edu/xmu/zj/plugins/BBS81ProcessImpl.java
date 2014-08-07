package edu.xmu.zj.plugins;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.dom4j.DocumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.xmu.zj.dao.BaseDao;
import edu.xmu.zj.dao.impl.BaseDaoImpl;
import edu.xmu.zj.pojo.Cancer;
import edu.xmu.zj.pojo.Company;
import edu.xmu.zj.pojo.Posts;
import edu.xmu.zj.pojo.Task;
import edu.xmu.zj.pojo.Threads;
import edu.xmu.zj.pojo.User;
import edu.xmu.zj.util.DateUtil;
import edu.xmu.zj.util.JavaUtil;

/**
 * 
 * @author Administrator
 * 
 */
public class BBS81ProcessImpl extends edu.xmu.zj.process.TaskAnalyseAbs {
	private Logger logger = Logger.getLogger(BBS81ProcessImpl.class);
	BaseDao baseDao = new BaseDaoImpl();
	// http://www.ptfish.com/forum-2-1.html
	private String forumMoudle = "http://bbs.8181.com.cn/forumdisplay.php?fid="; // 帖子页
	private String threadMoudle = "http://bbs.8181.com.cn/viewthread.php?tid=";// 回复页

	private String tid;
	private String fid;
	private String pid;
	private int pageIndex = 1;
	public BBS81ProcessImpl(Task task, String url) {
		super(task, url);
		forumMoudles = forumMoudle.split("\\|");
		threadMoudles = threadMoudle.split("\\|");
	}

	public void createForumPagesUrl(String bodyStr) {
		logger.debug("开始createForumPagesUrl");
		try {
			Document doc = JavaUtil.getDocument(bodyStr);
			// 取出帖子列表

			String lastThreadsXpath = "//DIV[@id='threadlist']//TABLE[@class='datatable']//TBODY[@id]";
			NodeList threads = XPathAPI.selectNodeList(doc, lastThreadsXpath);

			if (threads == null || threads.getLength() == 0) {
				logger.debug("版面分析出错 ， 可能需要登录！");
				return;
			}
			// 取出最后本页的最后的一个帖子
			Node thread = threads.item(threads.getLength() - 1);
			Node timeNode = XPathAPI.selectSingleNode(thread, ".//A[last()]");
			String text = JavaUtil.getTextContent(thread);
			String datePatt = "(\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2})";
			String timeStrTemp = JavaUtil.match(text, datePatt)[1];
			
			Node postNode = XPathAPI.selectSingleNode(thread, ".//TH//A");
			String postUrl = JavaUtil.getNodeValue(postNode, "href");

			Date updateDate = DateUtil.convertStringToDate("yyyy-MM-dd HH:mm",
					timeStrTemp);
			// 根据帖子的id，获取最后的回复帖子时间
			String tid = getThreadId(postUrl);
			Date lastDate = null;
			if (tid != null) {
				lastDate = (Date) baseDao.getObject(
						"Select.getUpdateTimeByTid", tid);
			}

			if (lastDate == null
					|| (lastDate != null && updateDate.getTime() > lastDate
							.getTime())) {
				int[] pageInfo = getPageInfo(doc);
				int iCurPage = pageInfo[0];
				int iTolPage = pageInfo[1];

				if (iCurPage < iTolPage) {
					String nextPage = null;
					nextPage = buildPage(iCurPage + 1);
					logger.debug("the next page URL is " + nextPage);
					this.addLinks(nextPage);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String buildPage(int pageIndex) {
		//http://bbs.8181.com.cn/forumdisplay.php?fid=458&page=2
		String newUrl = null;
		 
		if (url.indexOf("&page=") != -1)
			newUrl = url.replaceFirst("&page=(\\d)+", "&page=" + pageIndex);
		else
			newUrl = url.replaceAll("###(.*)", "") + "&page=" + pageIndex;
		logger.debug("new URL:" + newUrl);
		return newUrl;
	}

	public int[] getPageInfo(Document doc) {
		try {
			String curXPath = "//DIV[@class='pages']//STRONG";
			
			Node curNode = XPathAPI.selectSingleNode(doc, curXPath);
			if(curNode == null){
				return new int[]{1,1};
			}
			int curPage = Integer.parseInt(JavaUtil.getTextContent(curNode));
			String totalXPath = "//DIV[@class='pages']//A[@class='last']";
			Node totalNode = XPathAPI.selectSingleNode(doc, totalXPath);
			int totalPage = curPage;
			if (totalNode == null) {
				totalXPath = "//DIV[@class='pages']//A";
				NodeList pageList = XPathAPI.selectNodeList(doc, totalXPath);
				if (JavaUtil.getTextContent(
						pageList.item(pageList.getLength() - 1)).indexOf("下一页") != -1) {
					String totalPageString = JavaUtil.getTextContent(pageList
							.item(pageList.getLength() - 2));
					String t_str = JavaUtil.match(totalPageString, "(\\d+)")[1];
					totalPage = Integer.valueOf(t_str);
				}
			} else {
//				
				totalPage = Integer
						.parseInt(JavaUtil.match(JavaUtil.getTextContent(totalNode) , "(\\d+)")[1]);
			}
			return new int[] { curPage, totalPage };
		} catch (Exception e) {
			logger.debug("获取页码出错"+url, e);
			return new int[] { 1, 1 };
		}
	}

	protected String getForumId(String linkUrl) {
		return JavaUtil.match(linkUrl, "fid=(\\d+)")[1];
	}

	public String getThreadId(String linkUrl) {
		// http://www.ptfish.com/thread-384221-1-1.html
		return JavaUtil.match(linkUrl, "tid=(\\d+)")[1];
	}

	protected void getMainPrevPage(String bodyStr) {
		// TODO Auto-generated method stub

	}

	protected List<Posts> getPosts(String tempbodyStr) {
		logger.debug("开始 getPosts");
		List<Posts> returnPosts = new ArrayList<Posts>();
		String datePatt = "(\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2})";
		try {
			Document doc = JavaUtil.getDocument(tempbodyStr);
			if (!parsePostPage(doc))
				return returnPosts;
			NodeList postNodes = XPathAPI.selectNodeList(doc,
					"//DIV[@id='postlist']/DIV");
			logger.debug("found: " + postNodes.getLength());
			for (int i = 0; i < postNodes.getLength(); i++) {
				Node postNode = postNodes.item(i);
				
				 
				Node tdateNode = XPathAPI
						.selectSingleNode(postNode,
								".//TD[@class='postcontent']//DIV[@class='posterinfo']//EM");
			 
				if (tdateNode == null)
					continue;
				String tdateNodeStr = JavaUtil.match(JavaUtil
							.getTextContent(tdateNode), datePatt)[1];
				logger.debug("获取日期节点");
				Date tDateline = DateUtil.convertStringToDate(
						"yyyy-MM-dd HH:mm", tdateNodeStr);

				logger.debug("转换日期节点");
				Node userNode = XPathAPI.selectSingleNode(postNode,
						".//TD[@class='postauthor']/DIV[@class='postinfo']/A");
				String tuser = JavaUtil.getTextContent(userNode); // 发布人名称
				String turl = JavaUtil.getNodeValue(userNode, "href"); // 发布人uid
				
				String uid = tuser;
				
				 
				Node tcontextNode = XPathAPI
						.selectSingleNode(postNode,
								".//DIV[@class='defaultpost']//TD");
				if(tcontextNode == null){
					tcontextNode =  XPathAPI
							.selectSingleNode(postNode,
									".//DIV[@class='defaultpost']//DIV[@class='locked']");
				}
				String tNotagContext = JavaUtil.getTextContent(tcontextNode);
				String tTagContext = JavaUtil.getTagContent(tcontextNode);

				
				NodeList imgNodes = XPathAPI.selectNodeList(tcontextNode, ".//IMG");
				for(int n = 0 ; n < imgNodes.getLength() ; n++){
					String imgHref = JavaUtil.getNodeValue(imgNodes.item(n), "src");
					if(n == 0){
						tNotagContext += "<br/>包含图片:"+imgHref;
					}else{
						tNotagContext += "<br/>"+imgHref;
					}
				}
				logger.debug(i);
				logger.debug("发布人名称:" + tuser);
				logger.debug("回复日期:" + tDateline);
				logger.debug("回复内容:" + tTagContext);

				Posts post = new Posts();
				post.setDateline(tDateline);
				post.setMessage(tTagContext);
				post.setMessagenotag(tNotagContext);
				post.setTid(tid);
				post.setPid(pid + "_" + i);
				post.setPurl(url);
				post.setTurl(turl);

				
				
				User user = new User();
				user.setUid(uid);
				user.setUname(tuser);
				post.setUser(user);
				post.setUsername(tuser);
				post.setUserid(uid);
				
				

				post.setTaskid(this.task.getTaskid());

				if (pageIndex == 1 && i < 10) {
					Node tempNode = XPathAPI
							.selectSingleNode(postNode,
									".//TD[@class='postcontent']//STRONG//EM");
					if (JavaUtil.getTextContent(tempNode).indexOf("楼主") != -1
							|| JavaUtil.getTextContent(tempNode).indexOf("1") != -1
							|| JavaUtil.getTextContent(tempNode).indexOf("1楼") != -1) {
						post.setIstopic(true);
					}
				}
				Date lastPost = (Date) baseDao.getObject("Select.getUpdateTimeByTid" , tid);
				if ( (lastPost != null && tDateline.getTime() <= lastPost.getTime())&& !post.isIstopic()) {
					continue;
				}

				if (!returnPosts.contains(post))
					returnPosts.add(post);
			}
		} catch (Exception e) {
			logger.debug("获取回复失败！",e);
		}
		logger.debug("退出 getPosts");
		return returnPosts;
	}

	private boolean parsePostPage(Document doc) throws Exception {
		tid = this.getThreadId(url);
		String datePatt = "(\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2})";
//		logger.debug("最后回复时间：" + this.taskLastDate);
		int[] pageinfo = getPageInfo(doc);
		int iCurPage = pageIndex = pageinfo[0];
		int iTotalPage = pageinfo[1];
		pid = tid + "-" + iCurPage;
		
		if (iCurPage == 1) {
			String lastPageURL = buildPage(iTotalPage);
			this.addLinks(lastPageURL);
		} else if (iCurPage == 2) {
			return true;
		} else {
			try {
				
				Node postNode = XPathAPI.selectSingleNode(doc,
						"//DIV[@id='postlist']/DIV");
				 
				if (postNode == null) {
					return false;
				}
				Node tdateNode = XPathAPI
						.selectSingleNode(postNode,
								".//TD[@class='postcontent']//DIV[@class='posterinfo']//EM");
			 
				String tdateNodeStr = JavaUtil.match(JavaUtil
							.getTextContent(tdateNode), datePatt)[1];
				logger.debug("获取日期节点");
				Date tDateline = DateUtil.convertStringToDate(
						"yyyy-MM-dd HH:mm", tdateNodeStr);
				Date lastPost = (Date) baseDao.getObject("Select.getUpdateTimeByTid" , tid);
				logger.debug("---------------------------------------------"
						+ "tdateline : " + tdateNodeStr + " \n taskLastDate : "
						+ taskLastDate);
				if (lastPost == null || (lastPost != null && tDateline.getTime() >= this.taskLastDate.getTime())) {
					String prePageURL = buildPage(iCurPage - 1);
					logger.debug("上一页回复地址:" + prePageURL);
					this.addLinks(prePageURL);
				}

			} catch (Exception e) {
				logger.debug("error in parsePostPage: ", e);
				e.printStackTrace();
			}

		}
		return true;
		
	}

	protected void getSecondPrevPage(String bodyStr) {

	}

	protected List<Threads> getThreads(String bodyStr) {
		List<Threads> returnThreads = new ArrayList<Threads>();
		try {
			Document doc = JavaUtil.getDocument(bodyStr);

			logger.debug("tid : " + tid);
			//String lastThreadsXpath = "//DIV[@id='threadlist']/DIV[@class='bm_c']//TBODY";
			String threadXpath = "//DIV[@id='threadlist']//TBODY[@id]";
			NodeList threads = XPathAPI.selectNodeList(doc, threadXpath);

			if (threads == null) {
				logger.debug("没有获取帖子列表");
				return null;
			}

			for (int i = 0; i < threads.getLength(); i++) {
				try {
					Node threadNode = threads.item(i);
					
					Node imgNode = XPathAPI.selectSingleNode(threadNode, ".//TD[@class='folder']//IMG");
					String idStr = JavaUtil.getNodeValue(imgNode, "alt");
					if (idStr.indexOf("置顶") == -1) {
						continue;
					} else {
						String titleXpath = "./TR/TH/SPAN/A";
						String title = JavaUtil.getTextContent(XPathAPI
								.selectSingleNode(threadNode, titleXpath));
						String turl = JavaUtil.getNodeValue(XPathAPI
								.selectSingleNode(threadNode, titleXpath),
								"href");
						String tid = getThreadId(turl);
						
						Date lastDate = (Date) baseDao.getObject(
								"Select.getUpdateTimeByTid", tid);
						// 获取最后回复时间
						String timeXpath = "./TR/TD[@class='lastpost']/EM/A";
						Node timeNode = XPathAPI.selectSingleNode(threadNode,
								timeXpath);
						String timeStrTemp = JavaUtil.getTextContent(timeNode);
						Date updateDate = DateUtil.convertStringToDate(
								"yyyy-MM-dd HH:mm", timeStrTemp);
						// 如果晚于上次爬虫的回帖时间，则不下载
						if (lastDate != null
								&& updateDate.getTime() < lastDate.getTime()) {
							continue;
						}

						String userXpath = "./TR/TD[@class='author']/CITE/A";
						Node userNode = XPathAPI.selectSingleNode(threadNode,
								userXpath);
						String uidt = JavaUtil.getNodeValue(userNode, "href");
						String username = JavaUtil.getTextContent(userNode);
						String uid = username;

						// 发帖时间
						String dateXpath = "./TR/TD[@class='author']/EM";
						String datelineStr = JavaUtil.getTextContent(XPathAPI
								.selectSingleNode(threadNode, dateXpath));
						Date dateline = DateUtil.convertStringToDate(
								"yyyy-MM-dd", datelineStr);

						Date lastpost = dateline;// 最后回复时间

						int views = 0;
						int replies = 0;
						Node repNode = XPathAPI.selectSingleNode(threadNode,
								"./TR/TD[@class='nums']/STRONG");
						String repStr = JavaUtil.getTextContent(repNode);
						replies = Integer.parseInt(repStr);

						Node viewNode = XPathAPI.selectSingleNode(threadNode,
								"./TR/TD[@class='nums']/EM");
						String viewStr = JavaUtil.getTextContent(viewNode);
						views = Integer.parseInt(viewStr);

						turl = initUrl(turl);
						if (turl.indexOf("###") == -1)
							turl += "###" + this.getForumId(url);

						logger.debug("tid:" + tid);
						logger.debug("标题:" + title);
						logger.debug("发布人名称:" + username);
						logger.debug("发布人uid:" + uid);
						logger.debug("URL:" + turl);
						logger.debug("访问:" + views);
						logger.debug("回复:" + replies);

						Threads thread = new Threads();

						thread.setTid(tid);
						thread.setTitle(title);
						thread.setTurl(turl);
						thread.setFid(this.getForumId(url));
						thread.setDateline(dateline);
						thread.setLastpost(lastpost);
						thread.setReplies(replies);
						thread.setViews(views);
						User user = new User();
						user.setUid(uid);
						user.setUname(username);
						thread.setUser(user);
						thread.setUsername(username);
						thread.setUserid(uid);
						thread.setTaskid(this.task.getTaskid());
						thread.setUrl(uidt);
						thread.setType(task.getType());

						if (!returnThreads.contains(thread))
							returnThreads.add(thread);
						this.addLinks(turl);
					}
				} catch (Exception e) {
					logger.error(this.url + " 获取主题异常", e);
					continue;
				}
			}
		} catch (Exception e) {
			logger.error(this.url + " 获取主题异常", e);
			// e.printStackTrace();
		}
		logger.debug("退出 getThreads");
		return returnThreads;
	}

	public boolean isFirstForumPage() {
		return false;
	}
	
//	private String filepath = this.getClass().getResource("/").getPath().replaceAll("%20", " ")+"type-mapping.xml";
	public static void main(String[] args) throws DocumentException, UnsupportedEncodingException {
		String bodyStr = JavaUtil.readFile("d:/3.html");
		bodyStr = new String(bodyStr.getBytes("GBK") , "gbk");
		BBS81ProcessImpl process = new BBS81ProcessImpl(new Task(),"bbs.8181.com.cn/viewthread.php?tid=2490814&extra=page%3D1&page=1");
		process.getPosts(bodyStr);
//		
		String tid = process.getThreadId("http://bbs.8181.com.cn/viewthread.php?tid=5556681&extra=page%3D2");
		System.out.println(tid);
	}

	@Override
	protected List<Cancer> getCancer(String bodyStr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<Company> getCompanys(String bodyStr) {
		// TODO Auto-generated method stub
		return null;
	} 
}
