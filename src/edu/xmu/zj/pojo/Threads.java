package edu.xmu.zj.pojo;

import java.util.Date;

/**
 * 帖子主题实体类
 * 
 * @author Administrator
 */
public class Threads {
	private String tid;//主题编号
	private String fid;//版块编号
	private String title;//标题
	private Date dateline;//发布时间
	private Date lastpost;//最后回复时间
	private int views;//点击
	private int replies;//回复
	private String turl;//帖子地址
	private boolean isupdate;//已更新
	private String taskid;// 所属任务	
	private User user;// 发帖人
	private String userid;
	private String username;
	private String url;
	private String type;
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
	private String message;// 主题的发帖内容，用于TRS入库
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title =title;
	}
	public Date getDateline() {
		return dateline;
	}
	public void setDateline(Date dateline) {
		this.dateline = dateline;
	}
	public Date getLastpost() {
		return lastpost;
	}
	public void setLastpost(Date lastpost) {
		this.lastpost = lastpost;
	}
	public int getViews() {
		return views;
	}
	public void setViews(int views) {
		this.views = views;
	}
	public int getReplies() {
		return replies;
	}
	public void setReplies(int replies) {
		this.replies = replies;
	}
	public String getTurl() {
		return turl;
	}
	public void setTurl(String turl) {
		this.turl = turl;
	}
	public boolean isIsupdate() {
		return isupdate;
	}
	public void setIsupdate(boolean isupdate) {
		this.isupdate = isupdate;
	}
	public String getTaskid() {
		return taskid;
	}
	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	@Override
	public boolean equals(Object obj) {
		return this.tid.equals(((Threads)obj).tid);
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
