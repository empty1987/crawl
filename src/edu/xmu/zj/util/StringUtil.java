package edu.xmu.zj.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** 
 *Copyright(C):	
 *@n
 *@n File:			StringUtils.java
 *@n Function:		字符串处理类
 *@n Author:		xjb整合	 2009-6-7
 */

public class StringUtil { 
	  
	 
    
    /**
     *<br> *****************************************************
     *<br> 功    能：将空值转为0
     *<br> 入口参数：param s1 待加工的字符串
     *<br> 返    回：如果传入参数是null返回空串"",否则原字串去空格后返回
     *<br> ******************************************************
     */
    public static String nullToZero(String s1) {
      if (s1 == null)return "0";
      s1 = s1.trim();
      if(s1.length()==0)return "0";
      return s1;
    }
    
   
  
    
	/**
     *<br> *****************************************************
     *<br> 功    能：获取异常堆栈信息
     *<br> 入口参数：param e 异常对象
     *<br> 返    回：return length 打印长度，0为不限制长度
     *<br> ******************************************************
     */
	public static String getErrorStack(Exception e, int length) {
		  String error = null;
		  if (e != null) {
		   try {
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    PrintStream ps = new PrintStream(baos);
		    e.printStackTrace(ps);
		    error = baos.toString();
		    if (length > 0) {
		     if (length > error.length()) {
		      length = error.length();
		     }
		     error = error.substring(0, length);
		    }
		    baos.close();
		    ps.close();
		   } catch (Exception e1) {
		    error = e.toString();
		   } 
		  }
		  return error;
	} 

	
	 
	
}
