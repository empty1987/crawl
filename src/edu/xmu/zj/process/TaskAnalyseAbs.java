package edu.xmu.zj.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.Tag;

import com.heaton.bot.Attribute;
import com.heaton.bot.AttributeList;
import com.heaton.bot.CookieParse;
import com.heaton.bot.HTTP;
import com.heaton.bot.HTTPSocket;

import edu.xmu.zj.dao.BaseDao;
import edu.xmu.zj.dao.impl.BaseDaoImpl;
import edu.xmu.zj.pojo.Cancer;
import edu.xmu.zj.pojo.Company;
import edu.xmu.zj.pojo.Posts;
import edu.xmu.zj.pojo.Task;
import edu.xmu.zj.pojo.Threads;
import edu.xmu.zj.pojo.User;
import edu.xmu.zj.util.JavaUtil;
import edu.xmu.zj.util.SysObject;

/**
 * 单个网页分析类抽象父类，提供运行接口。包含发送HTTP请求，注意转向。提取链接，主题内容延迟到子类。
 * 
 * @author administrator
 * 
 */
public abstract class TaskAnalyseAbs extends Thread {

	private Logger logger = Logger.getLogger(TaskAnalyseAbs.class);
	private BaseDao baseDao = new BaseDaoImpl();
	protected boolean isValidate = false;
	private String locationUrl;// 转向地址
	protected String[] forumMoudles;
	protected String[] threadMoudles;
	protected Task task;// 任务
	protected String url;// 单个URL

	protected String urlEx;// 当前URL文件夹路径
	protected String urlHost;// 当前URL根路径

	// private Header[] httpHead;
	private AttributeList httpHead;
	private String httpBody;
	private AttributeList userCookieList = new AttributeList();;

	protected Date taskLastDate;// 今天的日期
	protected Date taskNowDate;// 当前更新的日期
	protected String xxx;
	List<Threads> threads = new ArrayList<Threads>();
	List<Posts> posts = new ArrayList<Posts>();
	List<Cancer> cancers = new ArrayList<Cancer>();
	List<Company> companys = new ArrayList<Company>();

	public TaskAnalyseAbs(Task task, String url) {
		this.task = task;
		this.url = url;
		this.urlEx = this.url.substring(0, this.url.lastIndexOf("/") + 1);
		try {
			this.urlHost = this.url.substring(0, this.url.indexOf("/", 9));
		} catch (Exception e) {
			this.urlHost = this.url.substring(0, this.url.length());
		}
	}

	/**
	 * 获取主要部分的上页地址
	 * 
	 * @param bodyStr
	 */
	protected abstract void getMainPrevPage(String bodyStr);

	/**
	 * 获取次要部分的上页地址
	 * 
	 * @param bodyStr
	 */
	protected abstract void getSecondPrevPage(String bodyStr);

	protected void initHttps(Task task) {

	}

	protected abstract List<Cancer> getCancer(String bodyStr);

	/**
	 * 线程入口,控制线程数量
	 */
	@Override
	public void run() {
		logger.debug("URL:" + this.url + " 开始运行");
		SysObject.changeCurrTaskThread(1);

		try {// 主函数中有可能出现意外的错误，在此处捕捉
			mainPro();
		} catch (Exception e) {
			logger.error("TaskAnalyseAbs 运行主函数错误！！！", e);
		}

		logger.debug("URL:" + this.url + " 运行完成");

		SysObject.changeCurrTaskThread(-1);
		// System.gc();
	}

	/**
	 * 发送HTTP请求，验证是否转向，最多5次，控制线程数量。调用实际分析方法。
	 */
	private void mainPro() {
		this.getHttpBody();

		if (this.httpBody == null || this.httpBody.length() == 0) {
			logger.error("链接：" + this.url + " 最终没有获取任何内容，退出");
			return;
		}
		if (this.locationUrl == null || this.locationUrl.length() == 0) {
			// 获取了实际页面进行分析
			this.analyseHTML(this.httpBody);
		}
	}

	/**
	 * 获取请求URL的网页内容
	 */
	private void getHttpBody() {
		initHttps(TaskRun.task);
		String tempUrl = this.url.replaceAll("###.*", "");//
		String cookieStr = task.getUcookies();// 用户设置的cookie

		// 设置cookie
		if (null != cookieStr && !cookieStr.trim().equals("")) {
			String[] cookies = cookieStr.split(";");
			// userCookieList = new AttributeList();
			for (int i = 0; i < cookies.length; i++) {
				int _i = cookies[i].indexOf("=");
				if (_i != -1) {
					try {
						CookieParse _cookie = new CookieParse();
						_cookie.source = new StringBuffer(cookies[i]);
						_cookie.get();
						_cookie.setName(_cookie.get(0).getName());
						userCookieList.add(_cookie);
					} catch (Exception e) {
					}
				}
			}
		}

		// 验证转向
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 6;) {
				byte[] _buff1 = null;
				try {
					HTTP _http = new HTTPSocket();
					_http.setUseCookies(true, true);// 设置会话cookie为真，持久性cookie为真
					if (null != userCookieList)
						_http.cookieStore = userCookieList;// 附加已经获取的cookie
					_http.setTimeout(60 * 1000);
					_http.getClientHeaders()
							.add(new Attribute("Accept", "*/*"));
					_http.getClientHeaders().add(
							new Attribute("Accept-Language", "en-us,zh-cn"));
					_http.getClientHeaders().add(
							new Attribute("Connection", "close"));
					_http.setAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 1.1.4322)");
					if (TaskRun.task.getProxyurl() != null
							&& TaskRun.task.getProxyurl().length() > 0) {
						_http.proxyStr = "https://"
								+ TaskRun.task.getProxyurl() + ":"
								+ TaskRun.task.getProxyport() + "@"
								+ TaskRun.task.getProxyuser() + ":"
								+ TaskRun.task.getProxypwd() + "";
						_http.initProxy();
					}

					logger.debug("send begin");
					_http.send(tempUrl, null);
					logger.debug("send end");
					_buff1 = (_http.getBodyBytes());// 取得body二进制内容
					logger.debug(this.url + " before read stream");
					this.httpBody = JavaUtil.readBytes(_buff1);
					logger.debug(this.url + " end read stream");
					this.httpHead = _http.getServerHeaders();
					break;
				} catch (Exception e) {
					logger.debug("Exception   ", e);
				}

				if (!isValidate) {// 当利用插件检测论坛类型时 超过6次无法识别就退出
				}
				j++;
			}
			// logger.debug("返回头为："+this.httpHead);
			if (this.httpBody == null || this.httpBody.length() < 100) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				continue;
			}

			checkLocation();
			checkSetCookie();
			if (this.locationUrl == null || this.locationUrl.length() == 0) {
				break;
			} else {
				logger.debug("链接：" + this.url + " 存在转向。当前为第 " + i + " 次。转向链接为："
						+ this.locationUrl);
				// 说明获取了转向链接，继续请求
				tempUrl = this.initUrl(this.locationUrl);
			}
		}
	}

	/**
	 * 构造完整URL
	 * 
	 * @param url
	 * @return
	 */
	protected String initUrl(String url) {
		if (!url.startsWith("http")) {
			if (url.startsWith("/") || url.startsWith("../"))
				url = this.urlHost
						+ url.replaceAll("\\.\\.", "").replaceAll("//", "/");
			else
				url = this.urlEx + url;
		}
		return url;
	}

	/**
	 * 分析网页内容。提取链接，主题内容。数据入库
	 */
	private void analyseHTML(String bodyStr) {
		if (this.task.getWebtype() == 1) {// webtype为1的时候，则是论坛
			if (isForumLink(this.url)) {
				this.createForumPagesUrl(bodyStr);
				logger.debug("完成createForumPagesUrl");
				this.threads = this.getThreads(bodyStr);
				logger.debug("完成updateTaskDates");
			} else if (this.isThreadLink(this.url)) {// 若是帖子页
				this.posts = this.getPosts(bodyStr);
			}
			if (this.threads != null && this.threads.size() > 0) {
				baseDao.insertList("Add.addThreads", this.threads);
			}
			if (this.posts != null && this.posts.size() > 0) {
				baseDao.insertList("Add.addPosts", this.posts);
			}
		} else if (this.task.getWebtype() == 2) {//人才网
			logger.debug("完成initDates");
			this.createForumPagesUrl(bodyStr);
			logger.debug("完成createForumPagesUrl");
			this.cancers = this.getCancer(bodyStr);
			logger.debug("完成updateTaskDates");
			if (this.cancers != null && this.cancers.size() > 0) {
				baseDao.insertList("Add.addCancers", this.cancers);
			}
		}else if (this.task.getWebtype() == 3) {//公司
			logger.debug("完成initDates");
			this.createForumPagesUrl(bodyStr);
			logger.debug("完成createForumPagesUrl");
			this.companys = this.getCompanys(bodyStr);
			logger.debug("完成updateTaskDates");
			if (this.companys != null && this.companys.size() > 0) {
				baseDao.insertList("Add.addCompanys", this.companys);
			}
		}
	}

	/**
	 * 获取版块ID
	 * 
	 * @param linkUrl
	 * @return
	 */
	protected abstract String getForumId(String linkUrl);

	/**
	 * 若当前页面中最早的更新比设定时间晚，则获取下一页链接，否则跳过
	 * 
	 * @param bodyStr
	 */
	protected abstract void createForumPagesUrl(String bodyStr);
	
	protected abstract List<Company> getCompanys(String bodyStr);

	protected abstract List<Posts> getPosts(String bodyStr);

	protected abstract List<Threads> getThreads(String bodyStr);

	/**
	 * 是否为帖子链接
	 * 
	 * @param linkUrl
	 * @return 是帖子链接返回TRUE，否则返回FASLE
	 */
	protected boolean isThreadLink(String linkUrl) {
		linkUrl = this.initUrl(linkUrl);
		int isThreadCount = 0;
		for (int iThread = 0; iThread < threadMoudles.length; iThread++) {
			if (threadMoudles[iThread] != null
					&& threadMoudles[iThread].length() != 0) {
				if (threadMoudles[iThread].startsWith("pico_reg:")) {
					if (JavaUtil.isMatch(linkUrl,
							threadMoudles[iThread].replaceAll("pico_reg:", "")))
						isThreadCount++;
					else
						isThreadCount--;
				} else
					isThreadCount += linkUrl.toLowerCase().indexOf(
							threadMoudles[iThread].toLowerCase());
			} else {
				isThreadCount--;
			}
		}

		if (isThreadCount != (threadMoudles.length * -1)) {
			return true;
		} else {
			return false;
		}
	}

	public abstract boolean isFirstForumPage();

	/**
	 * 是否为版块链接
	 * 
	 * @param linkUrl
	 * @return 是版块链接返回TRUE，否则返回FASLE
	 */
	protected boolean isForumLink(String linkUrl) {
		linkUrl = this.initUrl(linkUrl);
		int isForumCount = 0;
		for (int iForum = 0; iForum < forumMoudles.length; iForum++) {
			if (forumMoudles[iForum] != null
					&& forumMoudles[iForum].length() != 0) {
				if (forumMoudles[iForum].startsWith("pico_reg:")) {
					if (JavaUtil.isMatch(linkUrl,
							forumMoudles[iForum].replaceAll("pico_reg:", "")))
						isForumCount++;
					else
						isForumCount--;
				} else
					isForumCount += linkUrl.indexOf(forumMoudles[iForum]);
			} else {
				isForumCount--;
			}
		}

		if (isForumCount != (forumMoudles.length * -1)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查是否包含链接转向，3种方法<br>
	 * <ol>
	 * <li>头部包含“location:”或“content-location:”，返回代号302</li>
	 * <li>内容部分包含“meta http-equiv=refresh content="2;URL=..."”</li>
	 * <li>js脚本刷新，正则为：
	 * "(?s)<script.{0,50}?>\\s*((document)|(window)|(this))\\.location(\\.href)?\\s*="
	 * </li>
	 * <li>js脚本刷新，重新获取当前页面reload</li>
	 * </ol>
	 */
	private void checkLocation() {
		// 1.
		for (int i = 0; i < this.httpHead.length(); i++) {
			if (this.httpHead.get(i) == null)
				continue;
			if ("location".equals(this.httpHead.get(i).getName().toLowerCase())
					|| "content-location".equals(this.httpHead.get(i).getName()
							.toLowerCase())) {
				this.locationUrl = this.httpHead.get(i).getValue();
				return;
			}
		}
		// 2.
		String bodyLocationStr = "";
		if (this.httpBody.length() > 5120) {
			bodyLocationStr = this.httpBody.substring(0, 5120);// 太长则截取部分内容
		} else {
			bodyLocationStr = this.httpBody;
		}
		bodyLocationStr = bodyLocationStr.replaceAll("<!--(?s).*?-->", "")
				.replaceAll("['\"]", "");// 去除注释和引号部分

		int metaLocation = -1;
		metaLocation = bodyLocationStr.toLowerCase().indexOf(
				"http-equiv=refresh");
		if (metaLocation != -1) {
			String locationPart = bodyLocationStr.substring(metaLocation,
					bodyLocationStr.indexOf(">", metaLocation));
			metaLocation = locationPart.toLowerCase().indexOf("url");
			if (metaLocation != -1) {
				// 假定url=...是在 > 之前最后的部分
				this.locationUrl = locationPart.substring(metaLocation + 4,
						locationPart.length()).replaceAll("\\s+[^>]*", "");
				return;
			} else {
				this.locationUrl = url;
				return;
			}
		}

		// 3.
		Matcher locationMath = Pattern
				.compile(
						"(?s)<script.{0,50}?>\\s*((document)|(window)|(this))\\.location(\\.href)?\\s*=")
				.matcher(this.httpBody.toLowerCase());
		if (locationMath.find()) {
			String[] cs = this.httpBody.substring(locationMath.end()).trim()
					.split("[> ;<]");
			this.locationUrl = cs[0].replaceAll("\"|'", "");
			cs = null;
			return;
		}

		this.locationUrl = null;
	}

	/**
	 * 检查返回请求头是否要求设置COOKIE
	 * 
	 * @return
	 */
	private void checkSetCookie() {
		for (int i = 0; i < this.httpHead.length(); i++) {
			if (this.httpHead.get(i).getName().equalsIgnoreCase("set-cookie")) {
				String cookies[] = this.httpHead.get(i).getValue().split(";");
				for (int j = 0; j < cookies.length; j++) {
					try {
						CookieParse _cookie = new CookieParse();
						_cookie.source = new StringBuffer(cookies[j]);
						_cookie.get();
						_cookie.setName(_cookie.get(0).getName());
						userCookieList.add(_cookie);
					} catch (RuntimeException e) {
					}
				}
			}
		}
	}

	/**
	 * 添加给定链接。若在CRC32列表中不存在，则添加CRC32值，并写入URL文件。且不为外部域名。且不包含排除字词
	 * 
	 * @param linkUrl
	 * @return 是否添加成功
	 */
	protected boolean addLinks(String linkUrl) {
		linkUrl = linkUrl.replaceAll("&amp;", "&");
		if ((linkUrl.startsWith("http://") || linkUrl.startsWith("https://"))
				&& (!linkUrl.startsWith(this.urlHost))) {
			logger.info("非法url，不进队列：" + linkUrl);
			return false;
		}

		linkUrl = initUrl(linkUrl);

		if (SysObject.existsUrl(this.task.getTaskid(), linkUrl)) {
			try {
				SysObject.addUrl(linkUrl);
				return true;
			} catch (Exception e) {
				logger.info(linkUrl + " 写入URL文件出错 " + e.getMessage());
			}
		}
		// logger.debug(linkUrl+" 链接已存在！");
		return false;
	}

	/**
	 * 获取给定节点的指定子节点
	 * 
	 * @param parentNode
	 * @param filterStr
	 * @return
	 */
	protected Node[] getChildrenNodes(Node parentNode, String filterStr) {
		Node[] results = new Node[parentNode.getChildren().size()];
		int nIndex = 0;
		for (int j = 0; j < parentNode.getChildren().size(); j++) {
			Tag t = null;
			try {
				t = (Tag) parentNode.getChildren().elementAt(j);
			} catch (RuntimeException e) {
				continue;
			}
			if (t.getTagName().equalsIgnoreCase(filterStr)) {
				results[nIndex] = parentNode.getChildren().elementAt(j);
				nIndex++;
			}
		}
		Node[] results2 = new Node[nIndex];
		for (int j = 0; j < nIndex; j++) {
			results2[j] = results[j];
		}
		return results2;
	}

	/**
	 * 获取文字节点
	 * 
	 * @param parentNode
	 * @return
	 */
	protected String getTextNodes(Node parentNode) {
		String results = "";
		for (int j = 0; j < parentNode.getChildren().size(); j++) {
			@SuppressWarnings("unused")
			Tag t = null;
			try {
				t = (Tag) parentNode.getChildren().elementAt(j);
			} catch (RuntimeException e) {
				results += parentNode.getChildren().elementAt(j).getText();
				continue;
			}
		}
		return results;
	}

	/**
	 * 获取下一个节点
	 * 
	 * @param node
	 * @param filterStr
	 * @return
	 */
	protected Node getNextNode(Node node, String filterStr) {
		Node next = null;
		for (int i = 0; i < 10; i++) {
			node = node.getNextSibling();
			try {
				if (((Tag) node).getTagName().equalsIgnoreCase(filterStr)) {
					next = node;
					break;
				}
			} catch (RuntimeException e) {
				continue;
			}
		}
		return next;
	}

	/**
	 * 处理回复内容中的img等含有SRC属性的标签
	 * 
	 * @param postContent
	 * @return
	 */
	protected String handlePostContent(String postContent) {
		if (postContent == null)
			return null;
		// 处理img标签
		postContent = postContent
				.replaceAll("src=\"/", "src=\"" + this.urlHost).replaceAll(
						"src=\"(?!http://)", "src=\"" + this.urlEx);

		return postContent;
	}

	/**
	 * 初始化httpClient参数
	 * 
	 * @param httpClient
	 */
	@SuppressWarnings("unused")
	private void initHttpClient(HttpClient httpClient) {
		// 设置代理
		if (!JavaUtil.isNullOrEmpty(task.getProxyurl(), task.getProxyport())) {
			httpClient.getHostConfiguration().setProxy("172.16.0.246", 808);
			if (!JavaUtil
					.isNullOrEmpty(task.getProxyuser(), task.getProxypwd())) {
				UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
						task.getProxyuser(), task.getProxypwd());
				httpClient.getState().setProxyCredentials(AuthScope.ANY, creds);
			}
		}

		System.setProperty("apache.commons.httpclient.cookiespec",
				CookiePolicy.BROWSER_COMPATIBILITY);

		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(600000);
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(600000);

	}
}
