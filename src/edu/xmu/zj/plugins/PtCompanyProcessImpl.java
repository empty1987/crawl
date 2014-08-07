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
public class PtCompanyProcessImpl extends edu.xmu.zj.process.TaskAnalyseAbs {
	private Logger logger = Logger.getLogger(PtfishProcessImpl.class);
	BaseDao baseDao = new BaseDaoImpl();
	private String forumMoudle = "http://www.eptrc.com/person_list.aspx"; // 列表页
	private String threadMoudle = "view_resume.aspx";// 具体页

	private String tid;
	private String fid;
	private String pid;
	private int pageIndex = 1;

	public PtCompanyProcessImpl(Task task, String url) {
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

			String lastCancerXpath = "//DIV[@class='company-list']//LI";
			NodeList cancers = XPathAPI.selectNodeList(doc, lastCancerXpath);

			if (cancers == null || cancers.getLength() == 0) {
				logger.debug("版面分析出错 ， 可能需要登录！");
				return;
			}
			int[] pageInfo = getPageInfo(doc);
			int iCurPage = pageInfo[0];
			int iTolPage = pageInfo[1];
			if (iCurPage < iTolPage) {
				String nextPage = null;
				nextPage = buildPage(iCurPage + 1);
				logger.debug("the next page URL is " + nextPage);
				this.addLinks(nextPage);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String buildPage(int pageIndex) {
		// http://www.eptrc.com/person_list.aspx?page=2
		String newUrl = null;
		if (url.indexOf("page=") == -1) {
			newUrl = url + "&page=" + pageIndex;
			return newUrl;
		}
		if (url.indexOf("page=") != -1)
			newUrl = url.replaceFirst("page=(\\d)+", "page=" + pageIndex);

		logger.debug("new URL:" + newUrl);
		return newUrl;
	}

	public int[] getPageInfo(Document doc) {
		try {
			String curXPath = "//DIV[@class='PageBar']//B[2]";

			Node curNode = XPathAPI.selectSingleNode(doc, curXPath);
			if (curNode == null) {
				return new int[] { 1, 1 };
			}
			int curPage = Integer.parseInt(JavaUtil.getTextContent(curNode));
			String totalXPath = "//DIV[@class='PageBar']//B[last()]";
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

	public List<Company> getCompanys(String bodyStr) {
		List<Company> returnThreads = new ArrayList<Company>();
		try {
			Document doc = JavaUtil.getDocument(bodyStr);

			String threadXpath = "//DIV[@class='company-list']/UL//LI";
			NodeList threads = XPathAPI.selectNodeList(doc, threadXpath);

			if (threads == null) {
				logger.debug("没有获取公司列表");
				return null;
			}
			for (int i = 0; i < threads.getLength(); i++) {
				try {
					Node threadNode = threads.item(i);

					// 获取具体页的地址
					String hrefXpath = ".//DIV//A";
					Node urlNode = XPathAPI.selectSingleNode(threadNode,
							hrefXpath);
					String userUrl = JavaUtil.getNodeValue(urlNode, "href");
					userUrl = "http://www.eptrc.com/" + userUrl;
					String viewInfo = JavaUtil.getHttpBody(userUrl, null);
					Company company = getCompanyInfo(viewInfo);

					if (!returnThreads.contains(company))
						returnThreads.add(company);
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

	public Company getCompanyInfo(String viewInfo) {
		Company company = new Company();
		try {
			Document doc = JavaUtil.getDocument(viewInfo);
			// System.out.println(viewInfo);
			Node mainNode = XPathAPI.selectSingleNode(doc,
					"//DIV[@class='view-r']");
			String nameXpath = ".//DIV[@id='company-basic']/UL/LI";
			String name = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, nameXpath)).replaceAll(
					"\"", "'").replaceAll(":", " ").replaceAll(",", " ");
			String natureXpath = ".//DIV[@id='company-basic']/UL/LI[2]";
			String nature = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, natureXpath))
					.replaceAll("\"", "'").replaceAll(":", " ").replaceAll(",", " ");
			String locationXpath = ".//DIV[@id='company-basic']/UL/LI[3]";
			String location = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, locationXpath))
					.replaceAll("\"", "'").replaceAll(":", " ").replaceAll(",", " ");

			String typeXpath = ".//DIV[@id='company-basic']/UL/LI[4]";
			String type = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, typeXpath)).replaceAll(
					"\"", "'").replaceAll(":", " ").replaceAll(",", " ");

			String memoXpath = ".//DIV[@id='company-desc']";
			String memo = JavaUtil.getTextContent(
					XPathAPI.selectSingleNode(mainNode, memoXpath)).replaceAll(
					"\"", "'").replaceAll(":", " ").replaceAll(",", " ");
			String cxpath = ".//DIV[@id='company_contact']/UL";
			Node concatNode = XPathAPI.selectSingleNode(mainNode, cxpath);

			Node telphoneNode = XPathAPI.selectSingleNode(concatNode,
					"./LI[1]/EM/IMG");
			String tfilename = "";
			String tempUrl = "http://www.eptrc.com/";
			if (telphoneNode != null) {
				String url = tempUrl
						+ JavaUtil.getNodeValue(telphoneNode, "src");
				tfilename = getFileName(url);
			}

			Node contactNode = XPathAPI.selectSingleNode(concatNode,
					"./LI[2]/EM");
			String contact = JavaUtil.getTextContent(contactNode).replaceAll(":", " ").replaceAll(",", " ");

			Node phoneNode = XPathAPI.selectSingleNode(concatNode,
					"./LI[3]/EM/IMG");
			String phonefilename = "";
			if (phoneNode != null) {
				String url = tempUrl + JavaUtil.getNodeValue(phoneNode, "src");
				phonefilename = getFileName(url);
			}

			Node faxNode = XPathAPI.selectSingleNode(concatNode,
					"./LI[4]/EM/IMG");
			String faxfilename = "";
			if (faxNode != null) {
				String url = tempUrl + JavaUtil.getNodeValue(phoneNode, "src");
				faxfilename = getFileName(url);
			}

			Node addressNode = XPathAPI.selectSingleNode(concatNode,
					"./LI[5]/EM");
			String address = JavaUtil.getTextContent(addressNode).replaceAll(":", " ").replaceAll(",", " ");

			Node websiteNode = XPathAPI.selectSingleNode(concatNode,
					"./LI[6]/EM");
			String website = JavaUtil.getTextContent(websiteNode).replaceAll(":", " ").replaceAll(",", " ");

			Node qqNode = XPathAPI.selectSingleNode(concatNode,
					"./LI[8]/EM/IMG");
			String qqfilename = "";
			if (qqNode != null) {
				String url = tempUrl + JavaUtil.getNodeValue(qqNode, "src");
				qqfilename = getFileName(url);
			}

			Node msnNode = XPathAPI.selectSingleNode(concatNode,
					"./LI[9]/EM/IMG");
			String msnfilename = "";
			if (msnNode != null) {
				String url = tempUrl + JavaUtil.getNodeValue(msnNode, "src");
				msnfilename = getFileName(url);
			}

			Node fxNode = XPathAPI.selectSingleNode(concatNode,
					"./LI[10]/EM/IMG");
			String fxfilename = "";
			if (fxNode != null) {
				String url = tempUrl + JavaUtil.getNodeValue(fxNode, "src");
				fxfilename = getFileName(url);
			}
			company.setAddress(address);
			company.setContact(contact);
			company.setFax(faxfilename);
			company.setFeixin(fxfilename);
			company.setLocation(location);
			company.setMemo(memo);
			company.setMsn(msnfilename);
			company.setName(name);
			company.setNature(nature);
			company.setPhone(phonefilename);
			company.setQq(qqfilename);
			company.setTelphone(tfilename);
			company.setType(type);
			company.setWebsite(website);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return company;
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
		PtCompanyProcessImpl process = new PtCompanyProcessImpl(new Task(),
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
	protected List<Cancer> getCancer(String bodyStr) {
		// TODO Auto-generated method stub
		return null;
	}

}
