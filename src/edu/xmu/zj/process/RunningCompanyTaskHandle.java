package edu.xmu.zj.process;

import java.util.Date;

import org.apache.log4j.Logger;

import edu.xmu.zj.dao.BaseDao;
import edu.xmu.zj.dao.impl.BaseDaoImpl;
import edu.xmu.zj.pojo.Task;
import edu.xmu.zj.util.SysObject;

public class RunningCompanyTaskHandle extends Thread
{
  private static Task rtask;
  private long sleeptime = 5000L;
  private BaseDao baseDao = new BaseDaoImpl();

  private static Logger logger = Logger.getLogger(RunningCompanyTaskHandle.class);

  public RunningCompanyTaskHandle(Task task) {
    rtask = task;
  }

  public void run()
  {
    logger.debug("运行任务守护线程开始启动");
    mainPro();
  }

  private void mainPro()
  {
    rtask.setStatus(1);
    while (true)
    {
      rtask = TaskRun.task;

      if (rtask.getStatus()  == -1) {
        logger.info("当前任务：" + rtask.getId() + " 停止，删除当前任务");
        break;
      }

      int timeout = 180;
      if (rtask.getNondtimes() > timeout) {
        rtask.setStatus(-1);
        logger.info("过了" + timeout + "秒，仍然没有下载到任何网页，重启任务:" + rtask.getId());
      }

      rtask.countSpeed();
      try
      {
        Thread.sleep(this.sleeptime);
      } catch (InterruptedException localInterruptedException) {
      }
    }
    logger.info("任务：" + rtask.getId() + " 共提取了 " + SysObject.getUrllistCount( rtask.getId()+"") + " 个URL ");
    logger.info("任务：" + rtask.getId() + " 完成 " + new Date());
    baseDao.update("Update.updateTaskById2", rtask.getId());
    baseDao.removeObject("Delete.deleteRunningTaskById" ,rtask.getId());
    logger.debug("清理运行任务表完成...");
    logger.info("退出进程：" + rtask.getId());
    System.exit(0);
  }
}