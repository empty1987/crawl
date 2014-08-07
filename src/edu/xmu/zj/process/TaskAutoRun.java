package edu.xmu.zj.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.xmu.zj.dao.BaseDao;
import edu.xmu.zj.dao.impl.BaseDaoImpl;
import edu.xmu.zj.pojo.RunningTask;
import edu.xmu.zj.pojo.Task;

/**
 * 任务起点。获取任务，封装参数，启动任务运行线程。单轮任务完成后，控制任务下一轮状态
 * 
 * @author chenc@inetcop.com.cn
 * @date 2009-04-22
 */
public class TaskAutoRun extends Thread {
	private static Logger logger = Logger.getLogger(TaskAutoRun.class);
	private int flushTime = 15000;// 更新时间 ms
	private BaseDao baseDao = new BaseDaoImpl();
	private String MAX_MEM = "-Xmx512m";
	private String Pre;
	public static String classPath = "\\lib\\";

	static {
		String t_classPath = classPath;
		String getClassRootPath = TaskAutoRun.class.getResource("/.").getPath();
		classPath = getClassRootPath + classPath;
		File dirOfJar = new File(classPath);
		if (dirOfJar.isDirectory()) {
			String[] filenames = dirOfJar.list();
			classPath = ".;";
			for (String filename : filenames) {
				if (filename.endsWith(".jar"))
					classPath += "." + t_classPath + filename + ";";
			}
			logger.info("classPath=" + classPath);
		}
	}

	/**
	 * 线程入口，留空，可能修改时需要在主体方法前后增加操作
	 */
	@Override
	public void run() {
		this.mainPro();
	}

	/**
	 * 构造单个任务线程，控制任务执行状态。使用队列，每隔3分钟检查任务状态。 获取新的任务列表，检查任务线程状态。移除完成线程，根据新列表产生任务。
	 * 已经存在的任务线程不新启动。
	 */
	@SuppressWarnings("unchecked")
	private void mainPro() {
		logger.info("Crawl开始运行");
		cleanTemps();

		logger.info("开始运行...");
		cleanTemps();

		Runtime taskRuntime = Runtime.getRuntime();
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
			this.Pre = (" -classpath " + classPath + " " + this.MAX_MEM);
		else {
			this.Pre = ("javalog " + "xxx" + " -classpath " + classPath + " " + this.MAX_MEM);
		}
		logger.info("启动进程前缀 " + this.Pre);
		while (true) {
			List tasks = baseDao.getList("Select.getTask");
			if (tasks != null) {
				logger.info("本轮任务数：" + tasks.size());
				for (Iterator it = tasks.iterator(); it.hasNext();) {
					int maxpro = 5;
					int nowpro = (Integer) baseDao.getObject("Select.getNowTask");
					if (nowpro < maxpro) {
						Task nowTask = (Task) it.next();
						try {
							RunningTask rtask = new RunningTask();
							rtask.setTaskid(nowTask.getId());
							baseDao.save("Add.addRunningTask", rtask);
							String r = "company_crawl " + this.Pre + " edu.xmu.zj.process.TaskRun " + nowTask.getId();
							logger.info(r);
							taskRuntime.exec(r);
							logger.info(" 启动任务 " + nowTask.getId() + " 进程成功 ");
						} catch (IOException e) {
							logger.error("启动任务 " + nowTask.getId() + " 进程失败 ",
									e);
						} catch (Exception e) {
							logger.error("启动任务 " + nowTask.getId() + " 进程失败 ",
									e);
						}
						try {
							Thread.sleep(1000L);
						} catch (Exception e) {
							logger.info("结束运行,", e);
						}
					}
				}
			}
			try {
				Thread.sleep(this.flushTime);
			} catch (InterruptedException localInterruptedException) {
			}
			logger.info("结束运行xxxxxxxx");
		}
	}

	/**
	 * 清理上次结束时留下的无用文件
	 */
	private void cleanTemps() {
		baseDao.removeObject("Delete.deleteRunningTask", "");
		logger.info("清理运行任务表结束");
	}

	public static void main(String[] args) {
		Properties props = new Properties();
		try {
			InputStream istream = TaskRun.class
					.getResourceAsStream("/log4j.properties");
			props.load(istream);
			istream.close();
			props.setProperty("log4j.appender.logfile.File", "log/bbsics.log");

			// 重新配置后，日志会打到新的文件去。
			PropertyConfigurator.configure(props);// 装入log4j配置信息
		} catch (Exception e) {
			logger.error("装入属性文件异常 Exception ", e);
		}
		TaskAutoRun t = new TaskAutoRun();
		t.start();
	}
}
