/** 
  *ThreadUtil.java 2009-11-4 xujb
  */
package edu.xmu.zj.util;

import org.apache.log4j.Logger;

/**
 * @function  线程帮助类
 * @author xujb
 */
public class ThreadUtil {
	
	private static Logger logger = Logger.getLogger(ThreadUtil.class);

	/** 
	 * @function 封装线程sleep函数
	 * @param millis 毫秒数
	 * @return void 
	 * @Exception  
	 */
	 public static void sleep(long millis){ 
		 
		 try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			 logger.error("线程睡眠被中断",e);
		}
	 }

}
