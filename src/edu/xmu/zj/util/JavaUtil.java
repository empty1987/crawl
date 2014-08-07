package edu.xmu.zj.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.heaton.bot.Attribute;
import com.heaton.bot.AttributeList;
import com.heaton.bot.CookieParse;
import com.heaton.bot.HTTP;
import com.heaton.bot.HTTPSocket;

/**
 * 基本工具类
 * 
 * @author chenc@inetcop.com.cn
 * @date 2009-08-06
 */
public class JavaUtil {
	private static Logger logger = Logger.getLogger(JavaUtil.class);

	private static String filePath = "/doc/";

	/**
	 * 数据流直接写入文件
	 * 
	 * @param is
	 * @param fileName
	 */
	public static void writeFile(InputStream is, String fileName) {
		try {
			URL path = JavaUtil.class.getResource("/");
			String dir = URLDecoder.decode(path.getPath(), "utf-8");
			FileOutputStream fs = new FileOutputStream(dir + filePath
					+ fileName);
			byte[] buf = new byte[1024];
			int len = is.read(buf);
			while (len != -1) {
				fs.write(buf, 0, len);
				len = is.read(buf);
			}
			fs.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

	/**
	 * 文字写入文件
	 * 
	 * @param src
	 * @param fileName
	 */
	public static void writeFile(String src, String fileName) {
		try {
			  
			FileOutputStream fs = new FileOutputStream(fileName);
			fs.write(src.getBytes("utf-8"));// 如果不指定编码，在中英文平台上运行时可能会出现意想不到的结果
			fs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 文件写入文件并制定编码方式
	 * 
	 * @param src
	 * @param charset
	 * @param fileName
	 */
	public static void writeFile(String src, String charset, String fileName) {
		try {
			URL path = JavaUtil.class.getResource("/");
			String dir = URLDecoder.decode(path.getPath(), "utf-8");
			FileOutputStream fs = new FileOutputStream(dir + filePath
					+ fileName);
			fs.write(src.getBytes(charset));
			fs.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

	/**
	 * 读取文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static String readFile(String fileName) {
		FileInputStream fs;
		String content = "";
		try {
			fs = new FileInputStream(fileName);
			byte data[] = new byte[1024];
			int len = fs.read(data);
			while (len != -1) {
				content = content + new String(data, 0, len, "GBK");
				len = fs.read(data);
			}
			fs.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
		return content;
	}

	/**
	 * 以指定编码方式读取文件
	 * 
	 * @param fileName
	 * @param charset
	 * @return
	 */
	public static String readFile(String fileName, String charset) {
		URL path = JavaUtil.class.getResource("/");
		FileInputStream fs;
		String content = "";
		try {
			String dir = URLDecoder.decode(path.getPath(), "utf-8");
			fs = new FileInputStream(dir + filePath + fileName);
			byte data[] = new byte[1024];
			int len = fs.read(data);
			while (len != -1) {
				content = content + new String(data, 0, len, charset);
				len = fs.read(data);
			}
			fs.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
		return content;
	}

	/**
	 * 读取流内容
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("static-access")
	public static String readStream(InputStream is) throws IOException {
		String cs = null;
		try {
			ByteArrayOutputStream buffer = null;
			BufferedInputStream in = new BufferedInputStream(is);
			buffer = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int len = -1;
			// 从socket连接中获取输出流，主要为请求的响应报头和HTML编码

			while ((len = in.read(buff)) != -1) {
				buffer.write(buff, 0, len);
			}// 由于使用BufferOutputStream会出现一个连接被分割在两行的情况，因此只能利用字节流将所有源代码取得，而后换成String

			logger.debug("get stream over");
			HTMLDecoder htmd = new HTMLDecoder();
			SinoDetect sd = new SinoDetect();

			if (buffer != null) {

				try {
					int i = sd.detectEncoding(buffer.toByteArray());
					cs = buffer.toString(Encoding.htmlname[i]);
				} catch (RuntimeException e) {
					cs = buffer.toString("GBK");
				}
				// try{
				// cs=buffer.toString(Encoding.htmlname[i]);
				// }catch(Exception e){
				// cs=buffer.toString("GBK");
				// }
				cs = cs.replace("&nbsp;", "");
				cs = htmd.ASCIIToGB(cs);
			}
			logger.debug("analyse stream over");

			is.close();
		} catch (IOException e) {
			throw e;
		}
		return cs;
	}
	
	
	

	/**
	 * 读取字节内容
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("static-access")
	public static String readBytes(byte[] buff) throws IOException {
		String cs = null;
		try {
			logger.debug("get stream over");
			HTMLDecoder htmd = new HTMLDecoder();
			SinoDetect sd = new SinoDetect();
			int decodeNO = sd.detectEncoding(buff);
			if (buff != null) {
				if (decodeNO == 1) {
					cs = new String(buff, "GBK");
					cs = cs.replace("&nbsp;", " ");
					cs = htmd.ASCIIToGB(cs);
				} else if (decodeNO == 2) {
					cs = new String(buff, "HZ");
					cs = cs.replace("&nbsp;", " ");
					cs = htmd.ASCIIToGB(cs);
				} else if (decodeNO == 3) {
					cs = new String(buff, "BIG5");
					cs = cs.replace("&nbsp;", " ");
					cs = htmd.ASCIIToGB(cs);
				} else if (decodeNO == 4) {
					cs = new String(buff, "EUC_TW");
					cs = cs.replace("&nbsp;", " ");
					cs = htmd.ASCIIToGB(cs);
				} else if (decodeNO == 5) {
					cs = new String(buff, "ISO_2022_CN");
					cs = cs.replace("&nbsp;", " ");
					cs = htmd.ASCIIToGB(cs);
				} else if (decodeNO == 6) {
					cs = new String(buff, "UTF-8");
					cs = cs.replace("&nbsp;", " ");
					cs = htmd.ASCIIToGB(cs);
				} else if (decodeNO == 7) {
					cs = new String(buff, "Unicode");
					cs = cs.replace("&nbsp;", " ");
					cs = htmd.ASCIIToGB(cs);
				} else
					cs = new String(buff,"GBK");
				 
			}
			logger.debug("analyse stream over");

		} catch (IOException e) {
			throw e;
		}
		return cs;
	}

	/**
	 * 读取流内容，指定编码方式
	 * 
	 * @param is
	 * @param charset
	 * @return
	 */
	public static String readStream(InputStream is, String charset) {
		StringBuilder sb = new StringBuilder();
		// String content="";
		try {
			byte data[] = new byte[1024];
			for (int n; (n = is.read(data)) != -1;) {
				sb.append(new String(data, 0, n, charset));
			}

			is.close();
		} catch (IOException e) {

		}
		return sb.toString();
		// return content;
	}

	/**
	 * 正则匹配
	 * 
	 * @param s
	 * @param pattern
	 * @return
	 */
	public static String[] match(String s, String pattern) {
		Matcher m = Pattern.compile(pattern).matcher(s);

		while (m.find()) {
			int n = m.groupCount();
			String[] ss = new String[n + 1];
			for (int i = 0; i <= n; i++) {
				ss[i] = m.group(i);
			}
			return ss;
		}
		return null;
	}

	/**
	 * 正则匹配
	 * 
	 * @param s
	 * @param pattern
	 * @return
	 */
	public static List<String[]> matchAll(String s, String pattern) {
		Matcher m = Pattern.compile(pattern).matcher(s);
		List<String[]> result = new ArrayList<String[]>();

		while (m.find()) {
			int n = m.groupCount();
			String[] ss = new String[n + 1];
			for (int i = 0; i <= n; i++) {
				ss[i] = m.group(i);
			}
			result.add(ss);
		}
		return result;
	}

	/**
	 * 正则匹配，指定开始位置
	 * 
	 * @param s
	 * @param pattern
	 * @return
	 */
	public static String[] firstMatch(String s, String pattern, int startIndex) {
		Matcher m = Pattern.compile(pattern).matcher(s);

		if (m.find(startIndex)) {
			int n = m.groupCount();
			String[] ss = new String[n + 1];
			for (int i = 0; i <= n; i++) {
				ss[i] = m.group(i);
			}
			return ss;
		}
		return null;
	}

	/**
	 * 正则匹配
	 * 
	 * @param s
	 * @param pattern
	 * @return
	 */
	public static boolean isMatch(String s, String pattern) {
		Matcher m = Pattern.compile(pattern).matcher(s);

		if (m.find()) {
			return true;
		}
		return false;
	}

	/**
	 * 正则匹配，忽略大小写
	 * 
	 * @param s
	 * @param pattern
	 * @return
	 */
	public static String[] matchWeak(String s, String pattern) {
		Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(
				s);

		while (m.find()) {
			int n = m.groupCount();
			String[] ss = new String[n + 1];
			for (int i = 0; i <= n; i++) {
				ss[i] = m.group(i);
			}
			return ss;
		}
		return null;
	}

	public static String unescape(String s) {
		s = s.replaceAll("<pre>[\\s]*", "");
		s = s.replaceAll("</pre>.*$", "\n");
		// cdmackie: sometimes we get only "</"
		s = s.replaceAll("</$", "\n");

		// Clean up the end of line, and replace HTML tags
		s = s.replaceAll("&#13;&#10; &#13;&#10;", "\n");
		s = s.replaceAll("&#9;", "\t");
		s = s.replaceAll("&#09;", "\t");
		s = s.replaceAll("&#10;", "\n");
		s = s.replaceAll("&#13;", "");
		// s = s.replaceAll("&#27;", "\27");
		s = s.replaceAll("&#32;", " ");
		s = s.replaceAll("&#33;", "!");
		s = s.replaceAll("&#35;", "#");
		s = s.replaceAll("&#36;", "\\$");
		// cdmackie: this should be escaped
		s = s.replaceAll("&#37;", "\\%");
		s = s.replaceAll("&#38;", "&");
		s = s.replaceAll("&#39;", "'");
		s = s.replaceAll("&#40;", "(");
		s = s.replaceAll("&#41;", ")");
		s = s.replaceAll("&#42;", "*");
		s = s.replaceAll("&#43;", "+");
		s = s.replaceAll("&#44;", ",");
		s = s.replaceAll("&#45;", "-");
		s = s.replaceAll("&#46;", ".");
		s = s.replaceAll("&#47;", "/");
		s = s.replaceAll("&#58;", ":");
		s = s.replaceAll("&#59;", ";");
		s = s.replaceAll("&#60;", "<");

		s = s.replaceAll("&#61;", "=");
		s = s.replaceAll("&#62;", ">");
		s = s.replaceAll("&#63;", "?");
		s = s.replaceAll("&#64;", "@");
		s = s.replaceAll("&#91;", "[");
		s = s.replaceAll("&#92;", "\\\\");// ////////
		s = s.replaceAll("&#93;", "]");
		s = s.replaceAll("&#94;", "^");
		s = s.replaceAll("&#95;", "_");
		s = s.replaceAll("&#96;", "`");
		s = s.replaceAll("&#123;", "{");
		s = s.replaceAll("&#124;", "|");
		s = s.replaceAll("&#125;", "}");
		s = s.replaceAll("&#126;", "~");
		// s = s.replaceAll("&#199;", "?");

		s = s.replaceAll("\r", "");
		s = s.replaceAll("\n", "\r\n");
		s = s.replaceAll("&#34;", "\"");

		// body = string.gsub(body, "<!%-%-%$%$imageserver%-%->",
		// internalState.strImgServer)

		s = s.replaceAll("\r\n\\.", "\r\n\\.\\.");
		// s = s.replaceAll("&#(\\d*);","");
		Pattern p = null;
		Matcher m = null;
		p = Pattern.compile("&#(\\d*);");
		m = p.matcher(s);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, numToString(m.group(1)));
		}
		m.appendTail(sb);
		String x = sb.toString().replaceAll("&#[^;]*;", "");
		return x;
	}

	public static String numToString(String num) {
		String result = "";
		int n = Integer.parseInt(num);
		if (n > 255 && n < 19968)
			result = new String("&#" + n + ";");
		else {
			result = new String("" + (char) n);
		}
		return result;
	}

	/**
	 * 检查给定参数是否全部不为空，并关系
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNullOrEmpty(String... strs) {
		for (int i = 0; i < strs.length; i++) {
			if (strs[i] == null || strs[i].trim().length() == 0)
				return true;
		}
		return false;
	}

	public static String getThreadsPartitonID(String taskid) {
		return String.valueOf(Integer.valueOf(taskid) + 1);
	}

	public static String getPostsPartitonID(String taskid, String tid) {
		String partitionid = tid.replaceAll("[^\\d]*", "").replaceAll("(\\d)*",
				"$1");
		// 分区位置计算方法 3*taskid+tid获取最后一位数字%3-1
		Integer partition = 3 * Integer.valueOf(taskid)
				+ Integer.valueOf(partitionid) % 3 - 1;
		return String.valueOf(partition);
	}

	/**
	 * 获取请求URL的网页内容
	 */
	public static String getHttpBody(String url,String cookieStr) {
		  
		String httpBody = null;
		AttributeList userCookieList = null;//需要转换这个类 
		//设置cookie
		if (null != cookieStr && !cookieStr.trim().equals("")) {
			String[] cookies = cookieStr.split(";");
			userCookieList = new AttributeList();
			for (int i = 0; i < cookies.length; i++) {
				int _i = cookies[i].indexOf("=");
				if (_i != -1) {
					CookieParse _cookie = new CookieParse();
					_cookie.source = new StringBuffer(cookies[i]);
					_cookie.get();
					_cookie.setName(_cookie.get(0).getName());
					userCookieList.add(_cookie);
				}
			}
		}

		 for(int i=0;i<5;i++){
			for(int j=0;j<6;j++){
				byte[] _buff1 = null;
				try {
					HTTP _http = new HTTPSocket();
					 _http.setUseCookies(true,true);//设置会话cookie为真，持久性cookie为真
						if (null != userCookieList)
							_http.cookieStore = userCookieList;// 附加已经获取的cookie
					_http.setTimeout(60 * 1000);
					_http.getClientHeaders().add(new Attribute("Accept-Encoding", "gzip,deflate"));
					_http.getClientHeaders().add(new Attribute("Connection", "close"));
					_http.setAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322)");
					
					logger.debug("send begin");
					_http.send(url, null);
					logger.debug("send end");
					_buff1 = (_http.getBodyBytes());// 取得body二进制内容
					logger.debug(url+" before read stream");
					httpBody = JavaUtil.readBytes(_buff1); 
					//this.httpBody = HTMLDecoder.ASCIIToGB(new String(_buff1));
					//logger.debug("请求内容为："+this.httpBody);
					logger.debug(url+" end read stream"); 
					break;
				} catch (Exception e) {
					if(j == 5) logger.error("发送http请求错误,url为："+url,e);
				} 
			}
			// logger.debug("返回头为："+this.httpHead);
			if (httpBody == null || httpBody.length() < 100){
				ThreadUtil.sleep(1000);
				continue;
			}
			break;
		}
		 return httpBody;
	}
	/**
	 * 将String 解析成Documnet
	 * @param tempbodyStr
	 * @return
	 * @throws Exception
	 */
	public static Document getDocument(String tempbodyStr) throws Exception{
		String bodyStr = tempbodyStr.replace("　", " ").replaceAll("&nbsp;", " ");
		// 生成html parser
		DOMParser parser = new DOMParser();
		// 设置网页的默认编码
		parser.setFeature("http://xml.org/sax/features/namespaces", false);
		parser.setProperty(
				"http://cyberneko.org/html/properties/default-encoding",
				"UTF-8");
		java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(
				bodyStr.getBytes("UTF-8"));
		InputStreamReader fileIn = new InputStreamReader(bis, "UTF-8");
		BufferedReader in = new BufferedReader(fileIn);
		parser.parse(new InputSource(in));
		Document doc = parser.getDocument();
		return doc;
	}
	
	public static String XmlToString(Node node) {
		try {
			//org.apache.xml.serializer.TreeWalker;
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString().replaceAll("<\\?.*\\?>", "");
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取结点的Text值
	 * @param node
	 * @return
	 */
	public static String getTextContent(Node node){
		if(node == null) return null;
		String textContent = node.getTextContent();
		if(textContent == null) return textContent;
		return textContent.trim();
	}
	
	/**
	 * 获取结点的属性值
	 * @param node
	 * @param attrName
	 * @return
	 */
	public  static String getNodeValue(Node node, String attrName){
		if(node == null) return null;
		Node attrNode = node.getAttributes().getNamedItem(attrName);
		if(attrNode == null || attrNode.getNodeValue() == null) return null;
		return attrNode.getNodeValue().trim();
	}

	/**
	 * 获取结点的Tag (XML)
	 * @param node
	 * @return
	 */
	public static String getTagContent(Node node){
		return JavaUtil.XmlToString(node);
	}
}