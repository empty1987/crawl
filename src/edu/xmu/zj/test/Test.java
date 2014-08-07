package edu.xmu.zj.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.heaton.bot.Attribute;
import com.heaton.bot.HTTP;
import com.heaton.bot.HTTPSocket;

import edu.xmu.zj.process.TaskRun;
import edu.xmu.zj.util.JavaUtil;

public class Test {
	public static void main(String[] args) throws Exception {
//		String temp = "http://www.eptrc.com/check.aspx?action=ajax_viewresume&op=contact&id=140655";
//		String body = JavaUtil.getHttpBody(temp, "	AJSTAT_ok_times=4; 35TOOL_ok_times=4; rc_ccid=109298; EPTRC_ok_times=1; ASP.NET_SessionId=hty3nt45kivqueieouena5j1; AJSTAT_ok_pages=5; _35tool_visitor_id17=181637485; _35tool_visitor_time17=2012-6-24%2018%3A16%3A41; 35TOOL_ok_pages=4; hremsarea_userid=164478; hremsarea_userkey=7367430bc9a54871231fd2f84e47f66e");
//		System.out.println(body);
		
		
		byte[] _buff1 = null;
		HTTP _http = new HTTPSocket();
//		_http.setUseCookies(true, true);// 设置会话cookie为真，持久性cookie为真
//		if (null != userCookieList)
//			_http.cookieStore = userCookieList;// 附加已经获取的cookie
		_http.setTimeout(60 * 1000);
		_http.getClientHeaders()
				.add(new Attribute("Accept", "*/*"));
		_http.getClientHeaders().add(
				new Attribute("Accept-Language", "en-us,zh-cn"));
		_http.getClientHeaders().add(
				new Attribute("Connection", "close"));
		_http.setAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 1.1.4322)");
	 
//			_http.proxyStr = "http://14.18.16.67:80" ;
//			_http.initProxy();
		 

		_http.send("http://www.ptfish.com/forum-2-1.html", null);
		_buff1 = (_http.getBodyBytes());// 取得body二进制内容
		 String httpBody = JavaUtil.readBytes(_buff1);
		 System.out.println(httpBody);
	}

}
