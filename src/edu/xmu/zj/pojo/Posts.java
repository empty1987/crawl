package edu.xmu.zj.pojo;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author Administrator
 * 
 */
@SuppressWarnings("unused")
public class Posts {
	private static Logger logger=Logger.getLogger(Posts.class);

	private String pid;//回复ID	
	private String tid;//主贴ID
	private User user;//发表用户
	private String uip;//内容
	private String iparea;//内容
	private Date dateline;//发布时间
	private Date savetime;//发布时间
	private String message;//内容
	private String messagenotag;//内容
	private String purl;//回复地址
	private String taskid;//归属任务
	private String title;
	private boolean istopic = false;
	private String userid;
	private String username;
	private  String turl;
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getUip() {
		return uip;
	}
	public void setUip(String uip) {
		this.uip = uip;
	}
	public String getIparea() {
		return iparea;
	}
	public void setIparea(String iparea) {
		this.iparea = iparea;
	}
	public Date getDateline() {
		return dateline;
	}
	public void setDateline(Date dateline) {
		this.dateline = dateline;
	}
	public Date getSavetime() {
		return savetime;
	}
	public void setSavetime(Date savetime) {
		this.savetime = savetime;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessagenotag() {
		return messagenotag;
	}
	public void setMessagenotag(String messagenotag) {
		this.messagenotag = messagenotag;
	}
	public String getPurl() {
		return purl;
	}
	public void setPurl(String purl) {
		this.purl = purl;
	}
	public String getTaskid() {
		return taskid;
	}
	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isIstopic() {
		return istopic;
	}
	public void setIstopic(boolean istopic) {
		this.istopic = istopic;
	}
	public String getTurl() {
		return turl;
	}
	public void setTurl(String turl) {
		this.turl = turl;
	}
}
