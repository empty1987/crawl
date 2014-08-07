package edu.xmu.zj.process;

import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.xmu.zj.dao.BaseDao;
import edu.xmu.zj.dao.impl.BaseDaoImpl;
import edu.xmu.zj.pojo.Task;
import edu.xmu.zj.util.SysObject;

/**
 * 单个任务运行。检查任务状态，限制，建立URL文件，启动实际任务解析类
 * 
 * 
 * @date 2009-04-23
 * 
 */
public class TaskRun extends Thread {

	private static Logger logger = Logger.getLogger(TaskRun.class);
	private BaseDao baseDao = new BaseDaoImpl();
	// 任务和版块加载与内存中e
	public static Task task;

	/**
	 * 构造函数，必须指定任务
	 * 
	 * @param task
	 */
	public TaskRun(String taskid) {
		TaskRun.task = (Task) baseDao.getObject("Select.getTaskByTaskID",taskid);
	}

	/**
	 * 线程入口，启动运行任务维护
	 */
	public void run() {
		logger.info("任务：" + TaskRun.task.getUrl() + " 开始运行");
		baseDao.update("Update.updateTaskById", TaskRun.task.getId());
	    RunningCompanyTaskHandle rthandle = new RunningCompanyTaskHandle(task);
	    rthandle.setDaemon(true);
	    rthandle.start();
	    try {
	      Thread.sleep(1000L);
	    } catch (InterruptedException e) {
	      e.printStackTrace();
	    }
	    logger.debug("基本信息读取完毕，开始执行任务");
		// 获取类型完成后，清除所有的全局静态信息
		SysObject.cleanTask(TaskRun.task.getTaskid());

		if (TaskRun.task.getType() == null || TaskRun.task.getType().length() == 0) {
			logger.info("任务：" + TaskRun.task.getUrl() + " 没有指定正确的解析类型");
			SysObject.cleanTask(TaskRun.task.getTaskid());
			baseDao.update("Update.updateNextStart", task.getTaskid());
			System.exit(0);
		}
		mainPro();// 主方法
		baseDao.update("Update.updateNextStart", task.getTaskid());// 更新下次开始时间
		logger.info("任务：" + TaskRun.task.getUrl() + " 退出运行");
		System.exit(0);
	}

	/**
	 * 使用分析工厂产生实际分析类，建立URL文件QUEUE.循环从URL文件中提取链接。
	 */
	public void mainPro() {
		// 构建分析类工厂
		AnalyseFactory factory = new AnalyseFactory(task);
		SysObject.addUrl(TaskRun.task.getUrl());
		// 循环读取URL
		end: while (true) {
			String url = null;
			try {
				url = SysObject.getUrl();
			} catch (Exception e1) {
				logger.error("读取URL错误，继续读取下一个");
				continue;
			}

			int endtimes = 3;
			int endcount = 0;
			for (int i = 0; i < endtimes; i++) {
				// 若获取URL为空，且当前没有运行中的线程则跳出
				if ((url == null || url.trim().length() == 0) && (SysObject.getCurrTaskThread(TaskRun.task.getTaskid()) == 0 && SysObject.taskRuned(TaskRun.task.getTaskid()))) {
					// 有可能存在短暂空白，若连续3次均符合条件则退出
					endcount++;
				}
				if (endcount - 1 < i)
					break;
				if (endcount == endtimes) {
					logger.info("获取URL为空，且当前没有运行中的线程，跳出");
					break end;
				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}

			if (url == null || url.trim().length() == 0)
				continue;

			// 线程最大数量超出，休眠
			while (SysObject.getCurrTaskThread(TaskRun.task.getTaskid()) >= TaskRun.task.getMaxthread()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}

			try {
				TaskAnalyseAbs analyser = factory.getAnalyser(url, true);
				analyser.start();
			} catch (Exception e) {
				logger.error("下载单个URL失败 ", e);
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}

		logger.info("任务：" + TaskRun.task.getUrl() + " 共提取了 " + SysObject.getUrllistCount(TaskRun.task.getTaskid()) 	+ " 个URL ");
		try {
			logger.debug("清除数据完毕");
			baseDao.update("Update.updateTask", task);
			logger.debug("保存task数据完毕");
		} catch (Exception e) {
			logger.debug("任务完成后删除错误：" + e.getMessage());
		}
		clearTask();
		logger.info("任务：" + TaskRun.task.getUrl() + " 完成 " + new Date());
	}

	/**
	 * 清除所有的全局静态信息
	 */
	private void clearTask() {
		SysObject.cleanTask(TaskRun.task.getTaskid());// 清除URL排重保存信息
	}

	/**
	 * 任务进程启动入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Properties props = new Properties();
		try {
			TaskRun trun = new TaskRun(3+"");
			trun.start();
		} catch (Exception e) {
			logger.error("主线程运行异常", e);
			System.exit(0);
		}
	}
}
