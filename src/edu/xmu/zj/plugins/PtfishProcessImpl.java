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
public class PtfishProcessImpl extends edu.xmu.zj.process.TaskAnalyseAbs {
	private Logger logger = Logger.getLogger(PtfishProcessImpl.class);
	BaseDao baseDao = new BaseDaoImpl();
	// http://www.ptfish.com/forum-2-1.html
	private String forumMoudle = "forum-"; // 帖子页
	private String threadMoudle = "thread-";// 回复页

	private String tid;
	private String pid;
	private int pageIndex = 1;
	public PtfishProcessImpl(Task task, String url) {
		super(task, url);
		forumMoudles = forumMoudle.split("\\|");
		threadMoudles = threadMoudle.split("\\|");
	}

	public void createForumPagesUrl(String bodyStr) {
		logger.debug("开始createForumPagesUrl");
		try {
			Document doc = JavaUtil.getDocument(bodyStr);
			// 取出帖子列表

			String lastThreadsXpath = "//DIV[@id='threadlist']/DIV[@class='bm_c']//TBODY";
			NodeList threads = XPathAPI.selectNodeList(doc, lastThreadsXpath);

			if (threads == null || threads.getLength() == 0) {
				logger.debug("版面分析出错 ， 可能需要登录！");
				return;
			}
//			// 取出最后本页的最后的一个帖子
			Node thread = threads.item(threads.getLength() - 1);
//			// 发帖时间
			String dateXpath = "./TR/TD[@class='by']/EM/SPAN";
			String datelineStr = JavaUtil.getTextContent(XPathAPI
					.selectSingleNode(thread, dateXpath));
			Date dateline = DateUtil.convertStringToDate(
					"yyyy-MM-dd", datelineStr);
			String fid= getForumId(this.url);
		 
			Date lastDate = null;
			if (fid != null) {
				lastDate = (Date) baseDao.getObject("Select.getUpdateTimeByFid", fid);
			}
			if (lastDate == null
					|| (lastDate != null && dateline.getTime() > lastDate
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
		String newUrl = null;
		if (url.indexOf(".html") != -1) {
			if (url.indexOf("-") == url.lastIndexOf("-"))
				newUrl = url.substring(0, url.lastIndexOf(".")) + "-" + pageIndex + ".html";
			else
				newUrl = url.substring(0, url.lastIndexOf("-")) + "-" + pageIndex + ".html";
			return newUrl;
		}
		if (url.indexOf("&page=") != -1)
			newUrl = url.replaceFirst("&page=(\\d)+", "&page=" + pageIndex);
		else
			newUrl = url.replaceAll("###(.*)", "") + "&page=" + pageIndex;
		if (newUrl.indexOf("###") == -1)
			newUrl += "###" + this.getForumId(url);
		logger.debug("new URL:" + newUrl);
		return newUrl;
	}
	public int[] getPageInfo(Document doc) {
		try {
			String curXPath = "//DIV[@id='pgt']//DIV[@class='pg']/STRONG";
			Node curNode = XPathAPI.selectSingleNode(doc, curXPath);
			if(curNode == null){
				return new int[]{1,1};
			}
			int curPage = Integer.parseInt(JavaUtil.getTextContent(curNode));
			String totalXPath = "//DIV[@id='pgt']//DIV[@class='pg']/A[@class='last']";
			Node totalNode = XPathAPI.selectSingleNode(doc, totalXPath);
			int totalPage = curPage;
			if (totalNode == null) {
				totalXPath = "//DIV[@id='pgt']//DIV[@class='pg']/A";
				NodeList pageList = XPathAPI.selectNodeList(doc, totalXPath);
				if (JavaUtil.getTextContent(
						pageList.item(pageList.getLength() - 1)).indexOf("下一页") != -1) {
					String totalPageString = JavaUtil.getTextContent(pageList
							.item(pageList.getLength() - 2));
					String t_str = JavaUtil.match(totalPageString, "(\\d+)")[1];
					totalPage = Integer.valueOf(t_str);
				}
			} else {
				totalPage = Integer.parseInt(JavaUtil.match(JavaUtil.getTextContent(totalNode) , "(\\d+)")[1]);
			}
			return new int[] { curPage, totalPage };
		} catch (Exception e) {
			logger.debug("获取页码出错"+url, e);
			return new int[] { 1, 1 };
		}
	}

	protected String getForumId(String linkUrl) {
		return JavaUtil.match(linkUrl, "forum-(\\d+)")[1];
	}

	public String getThreadId(String linkUrl) {
		// http://www.ptfish.com/thread-384221-1-1.html
		return JavaUtil.match(linkUrl, "thread-(\\d+)")[1];
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
				logger.debug("for begin");
				Node postNode = postNodes.item(i);
				logger.debug("get postNode");
 
				Node tdateNode = XPathAPI
						.selectSingleNode(postNode,
								".//TD[@class='plc']//DIV[@class='authi']/EM");
				logger.debug("获取单个节点");

				if (tdateNode == null)
					continue;
				String tdateNodeStr = JavaUtil.match(JavaUtil
							.getTextContent(tdateNode), datePatt)[1];
				logger.debug("获取日期节点");

				Date tDateline = DateUtil.convertStringToDate(
						"yyyy-MM-dd HH:mm", tdateNodeStr);

				logger.debug("转换日期节点");
				Node userNode = XPathAPI.selectSingleNode(postNode,
						".//TD[@class='pls']//DIV[@class='authi']/A");
				String tuser = JavaUtil.getTextContent(userNode); // 发布人名称
				String turl = JavaUtil.getNodeValue(userNode, "href"); // 发布人uid
				
				String uid = JavaUtil.match(turl, "uid-(\\d+)")[1];
				
				try{
					//http://www.ptfish.com/home.php?mod=space&uid=128873&do=profile
					
					String userUrl = "http://www.ptfish.com/home.php?mod=space&uid="+uid+"&do=profile";
					
					String userBody = JavaUtil.getHttpBody(userUrl, null);
					
					Document userDoc = JavaUtil.getDocument(userBody);
					User user = new User();
					user.setUid(uid);
					user.setUname(tuser);
					user.setComeFrom(this.task.getTname());
					user.setUrl(userUrl);
					
					String axpath = "//DIV[@class='bm_c u_profile']/DIV[2]//A";
					String group = JavaUtil.getTextContent(XPathAPI.selectSingleNode(userDoc, axpath));
					user.setGroupName(group);
					
					String rxpath = "//DIV[@class='bm_c u_profile']/DIV[2]/UL[@id='pbbs']/LI[2]";
					String registdateStr = JavaUtil.getTextContent(XPathAPI.selectSingleNode(userDoc, rxpath));
					String registdateStr2 = JavaUtil.match(registdateStr, datePatt)[1];
					Date registdate = DateUtil.convertStringToDate(
							"yyyy-MM-dd HH:mm", registdateStr2);
					user.setRegistdate(registdate);
					
					
					String lxpath = "//DIV[@class='bm_c u_profile']/DIV[2]/UL[@id='pbbs']/LI[3]";
					String lastdateStr = JavaUtil.getTextContent(XPathAPI.selectSingleNode(userDoc, lxpath));
					String lastdateStr2 = JavaUtil.match(lastdateStr, datePatt)[1];
					Date lastdate = DateUtil.convertStringToDate(
							"yyyy-MM-dd HH:mm", lastdateStr2);
					user.setLastdate(lastdate);
					
					
					String laxpath = "//DIV[@class='bm_c u_profile']/DIV[2]/UL[@id='pbbs']/LI[4]";
					String lastActivityStr = JavaUtil.getTextContent(XPathAPI.selectSingleNode(userDoc, laxpath));
					String lastActivityStr2 = JavaUtil.match(lastActivityStr, datePatt)[1];
					Date lastActivity = DateUtil.convertStringToDate(
							"yyyy-MM-dd HH:mm", lastActivityStr2);
					user.setLastActivity(lastActivity);
					
					String lpxpath = "//DIV[@class='bm_c u_profile']/DIV[2]/UL[@id='pbbs']/LI[4]";
					String lastpostStr = JavaUtil.getTextContent(XPathAPI.selectSingleNode(userDoc, lpxpath));
					String lastpostStr2 = JavaUtil.match(lastpostStr, datePatt)[1];
					Date lastpost = DateUtil.convertStringToDate(
							"yyyy-MM-dd HH:mm", lastpostStr2);
					user.setLastpost(lastpost);
					
					baseDao.save("Add.addUser", user);
					
				}catch (Exception e) {
				}
				
				logger.debug("获取用户名节点");
				Node tcontextNode = XPathAPI
						.selectSingleNode(postNode,
								".//TD[@class='plc']//DIV[@class='pcb']//TD");
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
				
				 if(tDateline.getTime() <= this.taskLastDate.getTime()){
					 break;
				 }

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
					Node tempNode = XPathAPI.selectSingleNode(postNode,
									".//TD[@class='plc']//STRONG//EM");
					if (JavaUtil.getTextContent(tempNode).indexOf("楼主") != -1
							|| JavaUtil.getTextContent(tempNode).indexOf("1") != -1
							|| JavaUtil.getTextContent(tempNode).indexOf("1楼") != -1) {
						post.setIstopic(true);
					}
				}
				
				Date lastPost = (Date) baseDao.getObject("Select.getUpdateTimeByTid" , tid);
				if ( (lastPost != null && tDateline.getTime() <= lastPost.getTime())) {
					continue;
				}

				if (!returnPosts.contains(post))
					returnPosts.add(post);
				logger.debug("add post end");

			}
		} catch (Exception e) {
		}
		logger.debug("退出 getPosts");
		return returnPosts;
	}

	private String buildPostPageUrl(int pageIndex) {
		String newUrl = null;
		if (url.indexOf(".html") != -1) {
			newUrl = url.replaceAll("thread-" + tid + "-" + "\\d+", "thread-"
					+ tid + "-" + pageIndex);
			return newUrl;
		}
		if (url.indexOf("&page=") != -1)
			newUrl = url.replaceFirst("&page=(\\d)+", "&page=" + pageIndex);
		else
			newUrl = url.replaceAll("###(.*)", "") + "&page=" + pageIndex;
		if (newUrl.indexOf("###") == -1)
			newUrl += "###" + this.getForumId(url);
		logger.debug("new URL:" + newUrl);
		return newUrl;
	}

	private boolean parsePostPage(Document doc) throws Exception {
		String datePatt = "(\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2})";
		tid = this.getThreadId(url);
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
								".//TD[@class='plc']//DIV[@class='authi']/EM");
				logger.debug("获取单个节点");

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
			String threadXpath = "//DIV[@id='threadlist']/DIV[@class='bm_c']//TBODY[@id]";
			NodeList threads = XPathAPI.selectNodeList(doc, threadXpath);

			if (threads == null) {
				logger.debug("没有获取帖子列表");
				return null;
			}

			for (int i = 0; i < threads.getLength(); i++) {
				try {
					Node threadNode = threads.item(i);
					String idStr = JavaUtil.getNodeValue(threadNode, "id");
					if (idStr.indexOf("normalthread") == -1) {
						continue;
					} else {
						System.out.println(JavaUtil.getTextContent(threadNode));
						String titleXpath = "./TR/TH/A[2]";
						String title = JavaUtil.getTextContent(XPathAPI.selectSingleNode(threadNode, titleXpath));
						String turl = JavaUtil.getNodeValue(XPathAPI
								.selectSingleNode(threadNode, titleXpath),
								"href");
						String tid = getThreadId(turl);
						Date lastDate = (Date) baseDao.getObject("Select.getUpdateTimeByTid", tid);
						// 获取最后回复时间
						String timeXpath = "./TR/TD[4]/EM/A";
						Node timeNode = XPathAPI.selectSingleNode(threadNode,
								timeXpath);
						String timeStrTemp = JavaUtil.getTextContent(timeNode);
						
						
						Date updateDate = DateUtil.convertStringToDate("yyyy-MM-dd HH:mm", timeStrTemp);
						// 如果晚于上次爬虫的回帖时间，则不下载
						if (lastDate != null && updateDate.getTime() <= lastDate.getTime()) {
							continue;
						}

						String userXpath = "./TR/TD[@class='by']/CITE/A";
						Node userNode = XPathAPI.selectSingleNode(threadNode,
								userXpath);
						String username = JavaUtil.getTextContent(userNode);
						String uidt = JavaUtil.getNodeValue(userNode, "href");
						
						String uid = JavaUtil.match(uidt, "uid-(\\d+)")[1];

						// 发帖时间
						String dateXpath = "./TR/TD[@class='by']/EM/SPAN";
						String datelineStr = JavaUtil.getTextContent(XPathAPI
								.selectSingleNode(threadNode, dateXpath));
						Date dateline = DateUtil.convertStringToDate(
								"yyyy-MM-dd", datelineStr);

						Date lastpost = dateline;// 最后回复时间

						int views = 0;
						int replies = 0;
						Node repNode = XPathAPI.selectSingleNode(threadNode,
								"./TR/TD[@class='num']/A");
						String repStr = JavaUtil.getTextContent(repNode);
						replies = Integer.parseInt(repStr);

						Node viewNode = XPathAPI.selectSingleNode(threadNode,
								"./TR/TD[@class='num']/EM");
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
		}
		logger.debug("退出 getThreads");
		return returnThreads;
	}

	public boolean isFirstForumPage() {
		return false;
	}
	
//	private String filepath = this.getClass().getResource("/").getPath().replaceAll("%20", " ")+"type-mapping.xml";
	public static void main(String[] args) throws DocumentException, UnsupportedEncodingException {
		String bodyStr = JavaUtil.readFile("d:/1.htm");
		bodyStr = new String(bodyStr.getBytes("GBK") , "gbk");
		PtfishProcessImpl process = new PtfishProcessImpl(new Task(),"http://www.ptfish.com/thread-384221-1-2.html");
		process.getPosts(bodyStr);
	}
	
	

	@Override
	protected List<Cancer> getCancer(String bodyStr) {
		return null;
	}

	@Override
	protected List<Company> getCompanys(String bodyStr) {
		return null;
	}

	@Override
	protected boolean isForumLink(String linkUrl) {
		if(linkUrl.indexOf("forum") != -1)
			return true;
		else
			return false;
	} 
}
