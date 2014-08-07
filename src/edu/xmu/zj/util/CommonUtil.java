package edu.xmu.zj.util;

import java.util.List;
import java.util.Map;
/** 
 *Copyright(C):	
 *@n
 *@n File:		  CommonUtil.java
 *@n Function:		通用操作类
 *@n Author:		xjb整合	 2009-7-12
 */
public class CommonUtil {
	
	/**
     *<br> *****************************************************
     *<br> 功    能：判断集合是否为空 
     *<br> 入口参数：param list 集合对象 
     *<br> 返    回：return boolean 为空返回true 反之返回false
     *<br> ******************************************************
     */ 
	@SuppressWarnings("unchecked")
	public static boolean isNull(List list){
		
		if(list == null)return true;
		if(list.size() == 0)return true;
		return false;
		
	}
	
	/**
     *<br> *****************************************************
     *<br> 功    能：判断Map是否为空 
     *<br> 入口参数：param map map对象 
     *<br> 返    回：return boolean 为空返回true 反之返回false
     *<br> ******************************************************
     */
	@SuppressWarnings("unchecked")
	public static boolean isNull(Map map){
		if(map == null)return true;
		if(map.size() == 0)return true;
		return false;
	}
}
