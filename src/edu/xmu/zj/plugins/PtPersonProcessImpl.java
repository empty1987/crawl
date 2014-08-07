package edu.xmu.zj.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
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
import edu.xmu.zj.util.DateUtil;
import edu.xmu.zj.util.JavaUtil;
import edu.xmu.zj.util.propertiesUtils;

/**
 * 
 * @author Administrator
 * 
 */
public class PtPersonProcessImpl extends edu.xmu.zj.process.TaskAnalyseAbs {
	private Logger logger = Logger.getLogger(PtfishProcessImpl.class);
	BaseDao baseDao = new BaseDaoImpl();
	private String forumMoudle = "http://www.pt791.com/jianli/"; // 列表页
	private String threadMoudle = "jianli";// 具体页

	private String tid;
	private String fid;
	private String pid;
	private int pageIndex = 1;

	public PtPersonProcessImpl(Task task, String url) {
		super(task, url);
		forumMoudles = forumMoudle.split("\\|");
		threadMoudles = threadMoudle.split("\\|");
	}

	public void createForumPagesUrl(String bodyStr) {
		logger.debug("开始createForumPagesUrl");
		try {
			Document doc = JavaUtil.getDocument(bodyStr.replace("//r", "")
					.replace("//n", ""));
			// 取出帖子列表

			String lastCancerXpath = "//DIV[@class='person-list']//LI";
			NodeList cancers = XPathAPI.selectNodeList(doc, lastCancerXpath);

			if (cancers == null || cancers.getLength() == 0) {
				logger.debug("版面分析出错 ， 可能需要登录！");
				return;
			}
			// 取出最后本页的最后的一个帖子
			Node thread = cancers.item(cancers.getLength() - 1);
			String text = JavaUtil.getTextContent(thread);
			// 2012年04月30日
			String datePatt = "(\\d{4}年\\d{1,2}月\\d{1,2}日)";
			String timeStrTemp = JavaUtil.match(text, datePatt)[1];

			Date updateDate = DateUtil.convertStringToDate("yyyy年MM月dd日",
					timeStrTemp);
			// 根据帖子的id，获取最后的回复帖子时间
			Date lastDate = (Date) baseDao.getObject("Select.getUpdateTime");
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
		// http://www.eptrc.com/person_list.aspx?page=2
		String newUrl = null;
		if(!url.endsWith("/")){
			url += "/";
		}
		if (url.indexOf("page") == -1) {
			newUrl = url + "page" + pageIndex+"/";
			return newUrl;
		}
		if (url.indexOf("page") != -1)
			newUrl = url.replaceFirst("page(\\d)+", "page" + pageIndex);

		logger.debug("new URL:" + newUrl);
		return newUrl;
	}

	public int[] getPageInfo(Document doc) {
		try {
			String curXPath = "//DIV[@class='bar-page']//DIV[@class='PageBar']//B[2]";

			Node curNode = XPathAPI.selectSingleNode(doc, curXPath);
			if (curNode == null) {
				return new int[] { 1, 1 };
			}
			int curPage = Integer.parseInt(JavaUtil.getTextContent(curNode));
			String totalXPath = "//DIV[@class='bar-page']//DIV[@class='PageBar']//B[last()]";
			Node totalNode = XPathAPI.selectSingleNode(doc, totalXPath);
			int totalPage = Integer.parseInt(JavaUtil.match(
					JavaUtil.getTextContent(totalNode), "(\\d+)")[1]);
			return new int[] { curPage, totalPage };
		} catch (Exception e) {
			logger.debug("获取页码出错" + url, e);
			return new int[] { 1, 1 };
		}
	}

	protected void getMainPrevPage(String bodyStr) {
		// TODO Auto-generated method stub

	}

	protected void getSecondPrevPage(String bodyStr) {

	}

	public List<Cancer> getCancer(String bodyStr) {
		List<Cancer> returnThreads = new ArrayList<Cancer>();
		try {
			Document doc = JavaUtil.getDocument(bodyStr);

			String threadXpath = "//DIV[@class='person-list']/UL//LI";
			NodeList threads = XPathAPI.selectNodeList(doc, threadXpath);

			if (threads == null) {
				logger.debug("没有获取员工列表");
				return null;
			}
			for (int i = 0; i < threads.getLength(); i++) {
				try {
					Node threadNode = threads.item(i);
					String idStr = JavaUtil.getNodeValue(threadNode, "class");
					if (idStr != null && idStr.indexOf("head") != -1) {
						continue;
					} else {
						// 获取具体页的地址
						String hrefXpath = ".//DIV[@class='c-photo']//A";
						Node urlNode = XPathAPI.selectSingleNode(threadNode,
								hrefXpath);
						String userUrl = JavaUtil.getNodeValue(urlNode, "href");
						userUrl = "http://www.pt791.com" + userUrl;
						String viewInfo = JavaUtil.getHttpBody(userUrl, null);
						// http://www.pt791.com/jianli/119471/
						String uid = JavaUtil.match(userUrl, "jianli/(\\d+)/")[1];
						Cancer cancer = getCancerInfo(viewInfo, uid);

						if (!returnThreads.contains(cancer))
							returnThreads.add(cancer);
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

	public Cancer getCancerInfo(String viewInfo, String uid) {
		Cancer cancer = new Cancer();
		try {
			Document doc = JavaUtil.getDocument(viewInfo);
			// System.out.println(viewInfo);
			Node mainNode = XPathAPI.selectSingleNode(doc,
					"//DIV[@id='resume-main']");
			String nameXpath = ".//DIV[@class='g-panel panel1']//TR/TD[@class='navy']";
			String name = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, nameXpath)).replaceAll(
					"\"", "'");
			String sexXpath = ".//DIV[@class='g-panel panel1']//TR/TD[4]";
			String sex = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, sexXpath)).replaceAll(
					"\"", "'");

			String url =  viewInfo.substring(
							viewInfo.indexOf("的照片\"") + "的照片\"".length()
									+ " src=\"".length(),
							viewInfo.indexOf("onload") - 2);
			if(!url.startsWith("http")){
				url = "http://www.pt791.com"+url;
			}
			String filename = getFileName(url);

			String jiguanXpath = ".//DIV[@class='g-panel panel1']//TR[2]/TD[@class='navy']";
			String jiguan = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, jiguanXpath))
					.replaceAll("\"", "'");

			String birthdayXpath = ".//DIV[@class='g-panel panel1']//TR[2]/TD[last()]";
			String birthday = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, birthdayXpath))
					.replaceAll("\"", "'");

			String heightXpath = ".//DIV[@class='g-panel panel1']//TR[3]/TD[@class='navy']";
			String height = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, heightXpath))
					.replaceAll("\"", "'");

			String hunyXpath = ".//DIV[@class='g-panel panel1']//TR[3]/TD[last()]";
			String huny = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, hunyXpath)).replaceAll(
					"\"", "'");

			String xueliXpath = ".//DIV[@class='g-panel panel1']//TR[4]/TD[@class='navy']";
			String xueli = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, xueliXpath))
					.replaceAll("\"", "'");

			String gongzuoXpath = ".//DIV[@class='g-panel panel1']//TR[4]/TD[last()]";
			String gongzuo = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, gongzuoXpath))
					.replaceAll("\"", "'");

			String locationXpath = ".//DIV[@class='g-panel panel1']//TR[5]/TD[@class='navy']";
			String location = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, locationXpath))
					.replaceAll("\"", "'");

			String yixiangXpath = ".//DIV[@class='g-panel']";
			String yixiang = JavaUtil.getTagContent(
					XPathAPI.selectSingleNode(mainNode, yixiangXpath))
					.replaceAll("\"", "'");

			String educationXpath = "./DIV[6]";
			String education = JavaUtil.getTagContent(
					XPathAPI.selectSingleNode(mainNode, educationXpath))
					.replaceAll("\"", "'");

			String jingliXpath = "./DIV[8]";
			String jingli = JavaUtil.getTagContent(
					XPathAPI.selectSingleNode(mainNode, jingliXpath))
					.replaceAll("\"", "'");

			String languageXpath = "./DIV[10]";
			String language = JavaUtil.getTagContent(
					XPathAPI.selectSingleNode(mainNode, languageXpath))
					.replaceAll("\"", "'");

			String trainingXpath = "./DIV[12]";
			String training = JavaUtil.getTagContent(
					XPathAPI.selectSingleNode(mainNode, trainingXpath))
					.replaceAll("\"", "'");

			String computerXpath = "./DIV[14]";
			String computer = JavaUtil.getTagContent(
					XPathAPI.selectSingleNode(mainNode, computerXpath))
					.replaceAll("\"", "'");

			String evaluationXpath = "./DIV[16]";
			String evaluation = JavaUtil.getTagContent(
					XPathAPI.selectSingleNode(mainNode, evaluationXpath))
					.replaceAll("\"", "'");
			/**
			 * 联系方式暂时不写
			 */
//			String temp = "http://www.eptrc.com/check.aspx?action=ajax_viewresume&op=contact&id="
//					+ uid;
//			String body = JavaUtil.getHttpBody(temp, task.getUcookies());
			// <li><b>手机号码：</b><em><img
			// src="UploadFiles/contact/person/9d/1c9f88083b45c248685882840fa237.jpg"
			// align=absmiddle /></em></li>
			// <li><b>联系电话：</b><em></em></li>
			// <li><b>Email：</b><em><a href="###"
			// onclick="EmailProxy.NormalEmail(140655);return false;">发送邮件</a></em></li>
			// <li><b>QQ：</b><em></em></li>
			// <li><b>MSN：</b><em></em></li>
			// <li><b>飞信：</b><em></em></li>
//			String tempUrl = "http://www.eptrc.com/";
//			Document contractDoc = JavaUtil.getDocument(body);
//			String telXpath = ".LI[1]//EM/IMG";
//			Node telNode = XPathAPI.selectSingleNode(contractDoc, telXpath);
//			String telphone = "";
//			if (telNode != null) {
//				String telphoneUrl = tempUrl
//						+ JavaUtil.getNodeValue(telNode, "src");
//				telphone = getFileName(telphoneUrl);
//			}
//
//			String phoneXpath = ".LI[2]//EM/IMG";
//			Node phoneNode = XPathAPI.selectSingleNode(contractDoc, phoneXpath);
//			String phone = "";
//			if (phoneNode != null) {
//				String phoneUrl = tempUrl
//						+ JavaUtil.getNodeValue(phoneNode, "src");
//				phone = getFileName(phoneUrl);
//			}
//
//			String qqXpath = ".LI[4]//EM/IMG";
//			Node qqNode = XPathAPI.selectSingleNode(contractDoc, qqXpath);
//			String qq = "";
//			if (qqNode != null) {
//				String qqUrl = tempUrl + JavaUtil.getNodeValue(qqNode, "src");
//				qq = getFileName(qqUrl);
//			}
//
//			String msnXpath = ".LI[5]//EM/IMG";
//			Node msnNode = XPathAPI.selectSingleNode(contractDoc, msnXpath);
//			String msn = "";
//			if (msnNode != null) {
//				String msnUrl = tempUrl + JavaUtil.getNodeValue(msnNode, "src");
//				msn = getFileName(msnUrl);
//			}
//
//			String fxinXpath = ".LI[6]//EM/IMG";
//			Node fxinNode = XPathAPI.selectSingleNode(contractDoc, fxinXpath);
//			String fxin = "";
//			if (fxinNode != null) {
//				String fxinUrl = tempUrl
//						+ JavaUtil.getNodeValue(fxinNode, "src");
//				fxin = getFileName(fxinUrl);
//			}

			cancer.setBirthday(birthday);
			cancer.setComputer(computer);
			cancer.setEducation(education);
			cancer.setEvaluation(evaluation);
			cancer.setFilename(filename);
			cancer.setGongzuo(gongzuo);
			cancer.setHeight(height);
			cancer.setJiguan(jiguan);
			cancer.setJingli(jingli);
			cancer.setLanguage(language);
			cancer.setLocation(location);
			cancer.setName(name);
			cancer.setSex(sex);
			cancer.setTraining(training);
			cancer.setXueli(xueli);
			cancer.setYixiang(yixiang);
			cancer.setHuny(huny);
//			cancer.setTelphone(telphone);
//			cancer.setPhone(phone);
//			cancer.setMsn(msn);
//			cancer.setFxin(fxin);
//			cancer.setQq(qq);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cancer;
	}

	public String getFileName(String imgUrl) {
		URL url;
		String time = null;
		try {
			url = new URL(imgUrl);
			String prx = imgUrl.substring(imgUrl.lastIndexOf(".") + 1);
			propertiesUtils utils = new propertiesUtils();
			String filePath = utils.getString("imgFile");
			File pathFile = new File(filePath);
			if (!pathFile.exists()) {
				pathFile.mkdir();
			}
			time = System.currentTimeMillis() + "." + prx;
			File outFile = new File(filePath + time);
			OutputStream os = new FileOutputStream(outFile);
			InputStream is = url.openStream();
			byte[] buff = new byte[is.available()];
			while (true) {
				int readed = is.read(buff);
				if (readed == -1) {
					break;
				}
				byte[] temp = new byte[readed];
				System.arraycopy(buff, 0, temp, 0, readed);
				os.write(temp);
			}
			is.close();
			os.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;

	}

	public boolean isFirstForumPage() {
		return false;
	}

	// private String filepath =
	// this.getClass().getResource("/").getPath().replaceAll("%20",
	// " ")+"type-mapping.xml";
	public static void main(String[] args) throws DocumentException,
			UnsupportedEncodingException {
		BaseDao baseDao = new BaseDaoImpl();
		String bodyStr = JavaUtil.readFile("d:/xx.html");
		bodyStr = new String(bodyStr.getBytes("GBK"), "gbk");
		PtPersonProcessImpl process = new PtPersonProcessImpl(new Task(),
				"www.eptrc.com/person_list.aspx");
		List<Cancer> list = process.getCancer(bodyStr);
		baseDao.insertList("Add.addCancers", list);
	}

	@Override
	protected String getForumId(String linkUrl) {
		return null;
	}

	@Override
	protected List<Posts> getPosts(String bodyStr) {
		return null;
	}

	@Override
	protected List<Threads> getThreads(String bodyStr) {
		return null;
	}

	@Override
	protected List<Company> getCompanys(String bodyStr) {
		// TODO Auto-generated method stub
		return null;
	}

}
