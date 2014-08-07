package edu.xmu.zj.process;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.heaton.bot.Attribute;
import com.heaton.bot.AttributeList;
import com.heaton.bot.CookieParse;
import com.heaton.bot.HTTP;
import com.heaton.bot.HTTPSocket;

import edu.xmu.zj.dao.BaseDao;
import edu.xmu.zj.dao.impl.BaseDaoImpl;
import edu.xmu.zj.pojo.Task;
import edu.xmu.zj.util.JavaUtil;
import edu.xmu.zj.util.SysObject;


/**
 * 检查任务类型。通过插件列表中的所有类，将任务URL依次使用分析类
 * 
 * @date 2009-08-18
 * 
 */
public class ValidateTaskType extends Thread {
	private Logger logger=Logger.getLogger(ValidateTaskType.class);
	private String locationUrl="";
	private String httpBody_top=""; 
	private String httpBody_top2=""; 
	private AttributeList httpHead=null;
	private AttributeList userCookieList= new AttributeList();;
	private String urlHost="";
	private String urlEx="";
	private BaseDao baseDao = new BaseDaoImpl();
	public ValidateTaskType(){
	}

	/**
	 * 获取请求URL的网页内容
	 */
	private String getHttpBody(Task task,String url) {		
		String tempUrl=null;
		if(url==null)
			tempUrl=task.getUrl().replaceAll("###.*", "");// discuz中将版块号保存于帖子链接中，请求时去除
		else
			tempUrl=url.replaceAll("###.*", "");
		String cookieStr = task.getUcookies();//用户设置的cookie
		String httpBody=null;
		
		//设置cookie
		if (null != cookieStr && !cookieStr.trim().equals("")) {
			String[] cookies = cookieStr.split(";");
			for (int i = 0; i < cookies.length; i++) {
				try {
					int _i = cookies[i].indexOf("=");
					if (_i != -1) {
						try {
							CookieParse _cookie = new CookieParse();
							_cookie.source = new StringBuffer(cookies[i]);
							_cookie.get();
							_cookie.setName(_cookie.get(0).getName());
							userCookieList.add(_cookie);
						} catch (RuntimeException e) {
						}
					}
				} catch (Exception e) {
					logger.error("验证时设置cookie出错");
				}
			}
		}

		// 验证转向
		int i = 0;
		for (; i < 5; i++) {
			for(int j=0;j<6;){
				byte[] _buff1 = null;
				try {
					HTTP _http = new HTTPSocket();
					 _http.setUseCookies(true,true);//设置会话cookie为真，持久性cookie为真
						if (null != userCookieList)
							_http.cookieStore = userCookieList;// 附加已经获取的cookie
					_http.setTimeout(60 * 1000);
					_http.getClientHeaders().add(new Attribute("Accept", "*/*"));
					_http.getClientHeaders().add(new Attribute("Accept-Language", "en-us,zh-cn"));
//					_http.getClientHeaders().add(new Attribute("Accept-Encoding", "gzip,deflate"));
					_http.getClientHeaders().add(new Attribute("Connection", "close"));
					_http.setAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 1.1.4322)");
					if(task.getProxyurl()!=null&&task.getProxyurl().length()>0){
						_http.proxyStr = "https://"+task.getProxyurl()+":"+task.getProxyport()+"@"+task.getProxyuser()+":"+task.getProxypwd()+"";
						_http.initProxy();
					}

					logger.debug("send begin");
					_http.send(tempUrl, null);
					logger.debug("send end");
					_buff1 = (_http.getBodyBytes());// 取得body二进制内容
					logger.debug(tempUrl+" before read stream");
					httpBody = JavaUtil.readBytes(_buff1);
					logger.debug(tempUrl+" end read stream");
					this.httpHead=_http.getServerHeaders();
//					if(_http.getReferrer()!=null||_http.getReferrer().length()>0){
//						this.httpHead.add(new Attribute("location",_http.getReferrer()));
//					}
//					timeout = false;
					break;
				} catch (Exception e) {
					logger.debug("Exception   ",e);
				}
			}
			// logger.debug("返回头为："+this.httpHead);
			if (httpBody == null || httpBody.length() < 100){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				continue;
			}

			checkLocation(httpBody,tempUrl);
			try {
				checkSetCookie();
			} catch (Exception e) {
			}
			if (this.locationUrl == null || this.locationUrl.length() == 0) {
				break;
			} else {
				logger.debug("链接：" + task.getUrl() + " 存在转向。当前为第 " + i + " 次。转向链接为："
						+ this.locationUrl);
				// 说明获取了转向链接，继续请求
				tempUrl=this.initUrl(this.locationUrl);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		return httpBody;
	}

	/**
	 * 检查是否包含链接转向，3种方法<br>
	 * <ol>
	 * <li>头部包含“location:”或“content-location:”，返回代号302</li>
	 * <li>内容部分包含“meta http-equiv=refresh content="2;URL=..."”</li>
	 * <li>js脚本刷新，正则为："(?s)<script.{0,50}?>\\s*((document)|(window)|(this))\\.location(\\.href)?\\s*="</li>
	 * </ol>
	 */
	private void checkLocation(String body,String url) {
		// 1.		
		for(int i=0;i<this.httpHead.length();i++){
			if(this.httpHead.get(i)==null) continue;
			if("location".equals(this.httpHead.get(i).getName().toLowerCase())
					||"content-location".equals(this.httpHead.get(i).getName().toLowerCase())){
				this.locationUrl=this.httpHead.get(i).getValue();
				return;
			}
		}
		

		// 2.
		String bodyLocationStr = "";
		if (body.length() > 5120) {
			bodyLocationStr = body.substring(0, 5120);// 太长则截取部分内容
		} else {
			bodyLocationStr = body;
		}
		bodyLocationStr = bodyLocationStr.replaceAll("<!--(?s).*?-->", "")
				.replaceAll("['\"]", "");// 去除注释和引号部分

		int metaLocation = -1;
		metaLocation = bodyLocationStr.toLowerCase().indexOf("http-equiv=refresh");
		if (metaLocation != -1) {
			String locationPart = bodyLocationStr.substring(metaLocation,
					bodyLocationStr.indexOf(">", metaLocation));
			metaLocation = locationPart.toLowerCase().indexOf("url");
			if (metaLocation != -1) {
				// 假定url=...是在 > 之前最后的部分
				this.locationUrl = locationPart.substring(metaLocation + 4,
						locationPart.length()).replaceAll("\\s+[^>]*", "");
				return;
			}else{
				this.locationUrl=url;
				return;
			}
		}

		// 3.
		Matcher locationMath = Pattern
				.compile(
						"(?s)<script.{0,50}?>\\s*((document)|(window)|(this))\\.location(\\.href)?\\s*=")
				.matcher(body.toLowerCase());
		if (locationMath.find()) {
			String[] cs = body.substring(locationMath.end()).trim()
					.split("[> ;<]");
			this.locationUrl = this.initUrl(cs[0].replaceAll("\"|'", ""));
			cs = null;
			return;
		}

		// 4.
//		locationMath = Pattern
//				.compile(
//						"(?s)<script.{0,50}?>[^<]*?((document)|(window)|(this))\\.location\\.reload")
//				.matcher(body.toLowerCase());
//		if (locationMath.find()) {
//			this.locationUrl = url;
//			return;
//		}

		// 没有转向
		this.locationUrl = null;
	}

	private void checkSetCookie() {
		for (int i = 0; i < this.httpHead.length(); i++) {
			if(this.httpHead.get(i).getName().equalsIgnoreCase("set-cookie")){
				String cookies[]=this.httpHead.get(i).getValue().split(";");
				for (int j = 0; j < cookies.length; j++) {
					CookieParse _cookie = new CookieParse();
					_cookie.source = new StringBuffer(cookies[j]);
					_cookie.get();
					_cookie.setName(_cookie.get(0).getName());
					userCookieList.add(_cookie);
				}
			}
		}
	}
	/**
	 * 构造完整URL
	 * @param url
	 * @return
	 */
	protected String initUrl(String url) {
		if (!url.startsWith("http")) {
			if(url.startsWith("/")||url.startsWith("../"))
				url=this.urlHost+url.replaceAll("\\.\\.", "");
			else
				url=this.urlEx+url;
		}		
		return url;
	}
	
}
