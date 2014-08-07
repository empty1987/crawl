package edu.xmu.zj.plugins;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
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
import edu.xmu.zj.process.TaskAnalyseAbs;
import edu.xmu.zj.process.TaskRun;
import edu.xmu.zj.util.DateUtil;
import edu.xmu.zj.util.JavaUtil;

/*
 * 
 * http://bbs.51.com/index.php?c=forum&_from=city&cat=1024
 */
public class BBS51ProcessImpl extends TaskAnalyseAbs {
	private Logger logger = Logger.getLogger(BBS51ProcessImpl.class);
	BaseDao baseDao = new BaseDaoImpl();
	private String forumMoudle = "index.php?c=forum";// 版块模板

	private String threadMoudle = "index.php?c=topic";// 帖子模板

	private String _tid = "";

	private String _pid = "";

	int pageIndex = 1;

	public BBS51ProcessImpl(Task task, String url) {
		super(task, url);
		forumMoudles = forumMoudle.split("\\|");
		threadMoudles = threadMoudle.split("\\|");
	}

	protected void createForumPagesUrl(String bodyStr) {
		logger.debug("开始createForumPagesUrl");
		try {
			Document doc = JavaUtil.getDocument(bodyStr);
			String lastThreadXpath = "//DIV[@class='body_wrap clear']/DIV[@class='right_wrap']/DIV[@class='forum']/UL/LI[not(@class)]";

			NodeList threadList = XPathAPI.selectNodeList(doc, lastThreadXpath);

			if (threadList == null || threadList.getLength() == 0) {
				logger.debug("版面分析出错，可能需要登录！");
				return;
			}
			logger.debug("帖子长为 ： " + threadList.getLength());

			Node thread = threadList.item(threadList.getLength() - 1);

			Node timeNode = XPathAPI.selectSingleNode(thread,
					"./P[@class='w4']");
			String timeStr = JavaUtil.getTextContent(timeNode);

			Date updateData = formate(timeStr);
			// 根据帖子的id，获取最后的回复帖子时间
			// 获取发帖的标题 和url 、发帖时间
			Node titleNode = XPathAPI.selectSingleNode(thread,
					"./P[@class='w1']/A");
			String title = JavaUtil.getTextContent(titleNode);

			String turl = JavaUtil.getNodeValue(titleNode, "href");
			String tid = getThreadId(turl);
			Date lastDate = null;
			if (tid != null) {
				lastDate = (Date) baseDao.getObject(
						"Select.getUpdateTimeByTid", tid);
			}
			if (lastDate == null
					|| (lastDate != null && updateData.getTime() > lastDate
							.getTime())) {
				int[] pageInfo = getPageInfo(doc);
				int iCurPage = pageInfo[0];
				int iTolPage = pageInfo[1];
				if (iCurPage < iTolPage) {
					String nextUrl = null;
					nextUrl = buildPageUrl(iCurPage + 1);
					logger.debug("NextPage:" + nextUrl);
					this.addLinks(nextUrl);
				}
			}
		} catch (Exception ex) {
			logger.debug("任务:" + this.task.getTaskid() + "解析版块页面内容出错", ex);
		}

	}

	private String buildPageUrl(int pageIndex) {
		String newUrl = null;

		if (this.url.indexOf("&page=") == -1) {
			newUrl = this.url.replaceAll("###(.*)", "").replaceAll("$", "")
					+ "&page=" + pageIndex + "$";
		} else {
			newUrl = this.url.replaceAll("&page=(\\d+)$", "&page=" + pageIndex)
					+ "$";
		}

		if (newUrl.indexOf("###") == -1)
			newUrl += "###" + this.getForumId(url);
		logger.debug("new URL:" + newUrl);
		return newUrl;
	}

	private int[] getPageInfo(Document doc) {
		logger.debug("开始getPageInfo");
		try {
			String pageXpath = "//DIV[@class='g_page clear']";

			Node pageNode = XPathAPI.selectSingleNode(doc, pageXpath);

			if (pageNode == null) {
				logger.debug("没有页码");
				return new int[] { 1, 1 };
			}

			Node iCurNode = XPathAPI.selectSingleNode(pageNode,
					"./A[@class='currentpage']");
			int icurPage = Integer.parseInt(JavaUtil.getTextContent(iCurNode)
					.trim());

			Node iTolNode = XPathAPI.selectSingleNode(pageNode, "./A[@href]");
			int iTolPage = 1;
			if (iTolNode == null) {
				iTolPage = icurPage;
			} else {
				String itolStr = JavaUtil.match(
						JavaUtil.getNodeValue(iTolNode, "href"),
						"&total_page=(\\d+)")[1].trim();
				iTolPage = Integer.parseInt(itolStr);
			}

			return new int[] { icurPage, iTolPage };

		} catch (Exception e) {
			logger.debug("getPageInfo出错");
			return new int[] { 1, 1 };
		}
	}

	private Date formate(String timeStr) {
		Date updateDate = null;
		Date now = new Date();
		String curYear = DateUtil.getDateTime("yyyy", now);
		String month = DateUtil.getDateTime("MM", now);
		String cmd = DateUtil.getDateTime("yyyy-MM-dd", now);
		int today = now.getDate();
		String temp = null;

		if (timeStr.indexOf("今天") != -1) {
			String tempTime = JavaUtil.match(timeStr, "(\\d{1,2}:\\d{1,2})")[1];
			temp = cmd + " " + tempTime;
		} else if (timeStr.indexOf("昨天") != -1) {
			String tempTime = JavaUtil.match(timeStr, "(\\d{1,2}:\\d{1,2})")[1];
			temp = curYear + "-" + month + "-" + String.valueOf(today - 1)
					+ " " + tempTime;
		} else if (timeStr.indexOf("前天") != -1) {
			String tempTime = JavaUtil.match(timeStr, "(\\d{1,2}:\\d{1,2})")[1];
			temp = curYear + "-" + month + "-" + String.valueOf(today - 2)
					+ " " + tempTime;
		} else {
			temp = curYear + "-" + timeStr.trim();
		}

		try {
			updateDate = DateUtil.convertStringToDate("yyyy-MM-dd HH:mm", temp);
		} catch (ParseException e) {
			logger.debug("formate出错", e);
		}

		return updateDate;
	}


	@Override
	protected String getForumId(String linkUrl) {

		if (linkUrl.indexOf(forumMoudles[0]) == -1) {
			return JavaUtil.match(linkUrl, "###(.*)")[1];
		} else {
			return JavaUtil.match(linkUrl, "&cat=(\\d+)")[1];
		}
	}

	@Override
	protected List<Posts> getPosts(String bodyStr) {
		logger.debug("开始获取posts（getPosts）");
		List<Posts> returnPosts = new ArrayList<Posts>();
		String datePatt = "(\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2})";
		try {
			Document doc = JavaUtil.getDocument(bodyStr);
			if (!parsePostPage(doc))
				return returnPosts;

			NodeList postNodes = XPathAPI
					.selectNodeList(doc,
							"//DIV[@class='topic_wrap clear']/DIV[@class='topic_main']/UL/LI");

			logger.debug("回复数为 ：" + postNodes.getLength());

			for (int i = postNodes.getLength() - 1; i >= 0; i--) {
				Node postNode = postNodes.item(i);

				// 获取回复时间
				Node timeNode = XPathAPI.selectSingleNode(postNode,
						"./DIV/P/SPAN[@class='time']");
				String tdateNodeStr = JavaUtil.match(
						JavaUtil.getTextContent(timeNode), datePatt)[1];
				Date tDateline = DateUtil.convertStringToDate(
						"yyyy-MM-dd HH:mm", tdateNodeStr);

				// 获取回复人
				String userXPath = "./DIV/P/A[2]";
				Node userNode = XPathAPI.selectSingleNode(postNode, userXPath);

				String userName = JavaUtil.getTextContent(userNode);

				String uid = JavaUtil.match(
						JavaUtil.getNodeValue(userNode, "href"), "u=(.*?)&")[1];

				// 获取内容
				String contentXpath = "./DIV[@class='txt clear']";
				Node contentNode = XPathAPI.selectSingleNode(postNode,
						contentXpath);
				String contentNoTag = JavaUtil.getTextContent(contentNode);
				String contentWithTag = JavaUtil.getTagContent(contentNode);

				logger.debug(i);
				logger.debug("发布人名称:" + userName);
				logger.debug("回复日期:" + tDateline);
				logger.debug("回复内容:" + contentWithTag);

				Posts post = new Posts();
				post.setDateline(tDateline);
				post.setMessage(contentWithTag);
				post.setMessagenotag(contentNoTag);
				post.setTid(_tid);
				post.setPid(_pid + "_" + i);
				post.setPurl(url);

				User user = new User();
				user.setUid(uid);
				user.setUname(userName);
				post.setUser(user);

				post.setTaskid(this.task.getTaskid());

				if (pageIndex == 1 && i == 0) {
					String louzhu = JavaUtil.getNodeValue(postNode, "class");
					if (louzhu.equals("louzhu")) {
						post.setIstopic(true);
					}
				}

				Date lastPost = (Date) baseDao.getObject("Select.getUpdateTimeByTid" , _tid);
				if ( (lastPost != null && tDateline.getTime() <= lastPost.getTime())&& !post.isIstopic()) {
					continue;
				}

				if (!returnPosts.contains(post))
					returnPosts.add(post);

			}

		} catch (Exception e) {
			logger.debug(e);
			e.printStackTrace();
		}
		logger.debug("退出getPosts");
		return returnPosts;
	}

	private boolean parsePostPage(Document doc) {
		// TODO Auto-generated method stub
		logger.debug("-------------------------parsePost--------------------------"
				+ this.url);
		_tid = this.getThreadId(url);
		logger.debug("最后回复时间：" + this.taskLastDate);
		int[] pageinfo = this.getPageInfo(doc);
		int iCurPage = pageIndex = pageinfo[0];
		int iTotalPage = pageinfo[1];
		_pid = _tid + "-" + iCurPage;
		logger.debug("_tid = " + _tid + " , _pid = " + _pid);

		if (iTotalPage == 1) {
			logger.debug("只有一页回复");
			return true;
		}
		if (iCurPage == 1) {
			String lastPageURL = buildPageUrl(iTotalPage);
			this.addLinks(lastPageURL);
		} else if (iCurPage == 2) {
			return true;
		} else {
			try {
				Node postNode = XPathAPI
						.selectSingleNode(doc,
								"//DIV[@class='topic_wrap clear']/DIV[@class='topic_main']/UL/LI");
				if (postNode == null) {
					return false;
				}
				Node timeNode = XPathAPI.selectSingleNode(postNode,
						"./DIV/P/SPAN[@class='time']");
				String datePatt = "(\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2})";
				String tdateNodeStr = JavaUtil.match(
						JavaUtil.getTextContent(timeNode), datePatt)[1];
				Date tDateline = DateUtil.convertStringToDate(
						"yyyy-MM-dd HH:mm", tdateNodeStr);
				logger.debug("---------------------------------------------"
						+ "tdateline : " + tdateNodeStr + " \n taskLastDate : "
						+ taskLastDate);

				if (tDateline.getTime() >= this.taskLastDate.getTime()) {
					String prePageURL = buildPageUrl(iCurPage - 1);
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

	protected void getPrevPostPage(String bodyStr) {
		// TODO Auto-generated method stub

	}

	protected String getThreadId(String linkUrl) {
		String tid = JavaUtil.match(linkUrl, "&id=(.*?)&")[1];
		return tid;
	}

	@Override
	protected List<Threads> getThreads(String bodyStr) {
		logger.debug("开始获取帖子列表");
		List<Threads> returnThreads = new ArrayList<Threads>();
		try {
			Document doc = JavaUtil.getDocument(bodyStr);

			String threadsXpath = "//DIV[@class='body_wrap clear']/DIV[@class='right_wrap']/DIV[@class='forum']/UL/LI[not(@class)]";
			NodeList threads = XPathAPI.selectNodeList(doc, threadsXpath);
			logger.debug(this.url + "  总的帖子数 : " + threads.getLength());
			String timep = "(\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2})";
			for (int i = 0; i < threads.getLength(); i++) {
				try {

					Node threadNode = threads.item(i);

					// 去除置顶的帖子
					Node imgNode = XPathAPI.selectSingleNode(threadNode,
							"./P[@class='w1']/IMG");
					if (imgNode != null) {
						if (JavaUtil.getNodeValue(imgNode, "src").indexOf(
								"ding.gif") != -1) {
							continue;
						}
					}

					// 获取最后回复时间
					Node timeNode = XPathAPI.selectSingleNode(threadNode,
							"./P[@class='w4']");
					String timeStr = JavaUtil.getTextContent(timeNode);
					Date updateData = formate(timeStr);

					if (updateData.getTime() <= this.taskLastDate.getTime())
						continue;

					// 获取发帖的标题 和url 、发帖时间
					Node titleNode = XPathAPI.selectSingleNode(threadNode,
							"./P[@class='w1']/A");
					String title = JavaUtil.getTextContent(titleNode);

					String turl = JavaUtil.getNodeValue(titleNode, "href");
					String tid = getThreadId(turl);
					turl = initUrl(turl);
					if (turl.indexOf("###") == -1) {
						turl = turl + "###" + getForumId(this.url);
					}

					/*
					 * String time = JavaUtil.match(JavaUtil.getNodeValue(
					 * titleNode, "title"), timep)[1]; Date tDateline =
					 * DateUtil.convertStringToDate( "yyyy-MM-dd HH:mm", time);
					 */

					// 获取发帖人、发帖人ID
					Node userNode = XPathAPI.selectSingleNode(threadNode,
							"./P[@class='w3']/A");
					String userName = JavaUtil.getTextContent(userNode);
					String userid = JavaUtil
							.match(JavaUtil.getNodeValue(userNode, "href"),
									"u=(.*?)&")[1];

					// 获取回帖数和看帖数
					Node repAndV = XPathAPI.selectSingleNode(threadNode,
							"./P[@class='w5']");
					String[] rvStr = JavaUtil.getTextContent(repAndV)
							.split("/");

					int repNum = Integer.parseInt(rvStr[1].trim());
					int viewNum = Integer.parseInt(rvStr[0].trim());

					logger.debug("tid:" + tid);
					logger.debug("标题:" + title);
					logger.debug("发布人名称:" + userName);
					logger.debug("发布人uid:" + userid);
					logger.debug("URL:" + turl);
					logger.debug("访问:" + viewNum);
					logger.debug("回复:" + repNum);
					logger.debug("更新日期:" + updateData);

					Threads thread = new Threads();

					thread.setTid(tid);
					thread.setTitle(title);
					thread.setTurl(turl);
					thread.setFid(this.getForumId(url));
					// thread.setDateline(tDateline);
					thread.setLastpost(updateData);
					thread.setReplies(repNum);
					thread.setViews(viewNum);
					User user = new User();
					user.setUid(userid);
					user.setUname(userName);
					thread.setUser(user);
					thread.setTaskid(this.task.getTaskid());
					thread.setType(task.getType());

					if (this.isFirstForumPage()) {
						// 若是帖子列表首页，则获取当前最大日期作为该版块当前更新日期
						if (this.taskNowDate == null
								|| this.taskNowDate.getTime() < updateData
										.getTime()) {
							this.taskNowDate = updateData;
						}
					}

					if (!returnThreads.contains(thread))
						returnThreads.add(thread);
					this.addLinks(turl);
				} catch (Exception e) {
					logger.error(this.url + " 获取主题异常", e);
					continue;
				}

			}

		} catch (Exception e) {
			logger.debug(e);
		}
		return returnThreads;
	}

	public boolean isFirstForumPage() {
		if (this.url.indexOf("&page=") == -1
				|| this.url.indexOf("&page=1$") != -1)
			return true;
		return false;
	}

	protected boolean isForumPage(String bodyStr) {
		return false;
	}

	protected void getMainPrevPage(String bodyStr) {
		// TODO Auto-generated method stub

	}

	protected void getSecondPrevPage(String bodyStr) {
		// TODO Auto-generated method stub

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
