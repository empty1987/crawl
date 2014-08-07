package edu.xmu.zj.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;

/**
 * 系统级别变量保存：每个任务当前启动的线程数及操作
 * 
 * @author administrator
 * 
 */

public class SysObject {	
	@SuppressWarnings("unused")
	private static Logger logger=Logger.getLogger(SysObject.class);
	
	private static Queue<String> urlQueue=new LinkedList<String>();
	public synchronized static void addUrl(String url){
		urlQueue.offer(url);
	}
	public synchronized static String getUrl(){
		return urlQueue.poll();
	}
	
	//任务线程数集合 域名，线程数量
	private static int TaskThreads=0;
	public static Map<Integer,Double> clickCoefficient=new HashMap<Integer,Double>();

	static{
		// 初始化整点点击系数
		clickCoefficient.put(0, 3.6);
		clickCoefficient.put(1, 2.2);
		clickCoefficient.put(2, 1.5);
		clickCoefficient.put(3, 1.1);
		clickCoefficient.put(4, 0.8);
		clickCoefficient.put(5, 0.7);
		clickCoefficient.put(6, 0.8);
		clickCoefficient.put(7, 1.3);
		clickCoefficient.put(8, 3.4);
		clickCoefficient.put(9, 5.6);
		clickCoefficient.put(10, 6.0);
		clickCoefficient.put(11, 6.0);
		clickCoefficient.put(12, 5.1);
		clickCoefficient.put(13, 5.5);
		clickCoefficient.put(14, 5.9);
		clickCoefficient.put(15, 6.2);
		clickCoefficient.put(16, 6.4);
		clickCoefficient.put(17, 5.9);
		clickCoefficient.put(18, 4.9);
		clickCoefficient.put(19, 4.9);
		clickCoefficient.put(20, 5.3);
		clickCoefficient.put(21, 5.8);
		clickCoefficient.put(22, 5.9);
		clickCoefficient.put(23, 5.2);
	}
	/**
	 * 改变当前线程数量
	 * @param num
	 */
	public synchronized static void changeCurrTaskThread(int num){
		int currTaskThread = TaskThreads + num;
//		logger.debug("任务: "+host+" 线程数量为:"+currTaskThread);
		TaskThreads=currTaskThread;
	}
	
	/**
	 * 返回当前任务线程数量
	 * @return 线程数量
	 */
	public static Integer getCurrTaskThread(String host){
			return TaskThreads;
	}
	
	/**
	 * 每个任务对应的URL的CRC32列表用来过滤重复URL。
	 */
	private static Set<Long> urlLists=new HashSet<Long>();
	
	/**
	 * 检查传入url是否存在，返回标识能否写入URL列表文件
	 * @param taskid 任务URL，KEY
	 * @param url 请求写入URL
	 * @return 是否可以写入URL列表文件
	 */
	public synchronized static boolean existsUrl(String taskid,String url){
		boolean result=false;
		CRC32 c=new CRC32();
		c.update(url.getBytes());
		if(!urlLists.contains(c.getValue())){
			urlLists.add(c.getValue());
			result=true;
		}
			
		return result;
	}
	
	/**
	 * 是否包含给出URL
	 * @param taskid
	 * @param url
	 * @return
	 */
	public synchronized static boolean isExistsUrl(String taskid,String url){
		boolean result=false;
		CRC32 c=new CRC32();
		c.update(url.getBytes());
		if(urlLists.contains(c.getValue())){
			result=true;
		}
		return result;
	}
	
	/**
	 * 任务是否运行过，若列表为空，认为没有运行过或正在等待运行
	 * @param taskid
	 * @return
	 */
	public synchronized static boolean taskRuned(String taskid){
		if(urlLists.size()==0){
			return false;
		}
		return true;
	}
	
	/**
	 * 清空任务列表
	 * @param taskid
	 */
	public synchronized static void cleanTask(String taskid){
		urlLists.clear();
		urlQueue.clear();
	}
	
	/**
	 * 获取现在任务数量
	 * @return
	 */
	public static int getUrllistCount(String taskid){
		return urlLists.size();
	}
}
