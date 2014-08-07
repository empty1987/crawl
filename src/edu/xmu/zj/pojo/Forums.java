package edu.xmu.zj.pojo;

import java.util.Date;

/**
 * 版块实体类，任务和版块编号相同视为同一版块
 * @author Administrator
 */
public class Forums {
	private String fid;
	private String taskid;
	private String fname;
	private String furl;
	private Date lastdate;	
	private Date nowdate;	
	
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public String getTaskid() {
		return taskid;
	}
	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}
	public String getFname() {
		return fname;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}
	public Date getLastdate() {
		return lastdate;
	}
	public void setLastdate(Date lastdate) {
		this.lastdate = lastdate;
	}
	public Date getNowdate() {
		return nowdate;
	}
	public void setNowdate(Date nowdate) {
		this.nowdate = nowdate;
	}
	
	@Override
	public boolean equals(Object arg0) {		
		return (this.fid.equals(((Forums)arg0).fid)&&(this.taskid.equals(((Forums)arg0).taskid)));
	}
	public String getFurl() {
		return furl;
	}
	public void setFurl(String furl) {
		this.furl = furl;
	}
	
	
}
