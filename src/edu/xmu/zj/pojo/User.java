package edu.xmu.zj.pojo;

import java.util.Date;

/**
 * 用户实体类
 * 
 * @author Administrator
 * 
 */
public class User {

	private String uid;//用户ID
	private String uname;//用户名称
	private String groupName;//用户组
	private Date registdate;//注册时间
	private Date lastdate;//最后访问时间
	private Date lastActivity;//最后活动时间
	private Date lastpost;//最后发表时间
	private String comeFrom;//什么系统的用户
	private Date currdate;//当前记录时间
	private String url;
	private String sex;
	private String birthday;
	private String hometown;
	private String location;

	
	 

	public Date getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(Date lastActivity) {
		this.lastActivity = lastActivity;
	}

	public Date getLastpost() {
		return lastpost;
	}

	public void setLastpost(Date lastpost) {
		this.lastpost = lastpost;
	}

	public String getComeFrom() {
		return comeFrom;
	}

	public void setComeFrom(String comeFrom) {
		this.comeFrom = comeFrom;
	}

	
	public User(){
	}

	public User(String uid,String uname){
		this.uid=uid;
		this.uname=uname;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		if(uid!=null)
			uid=uid.trim();
		this.uid = uid;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		if(uname!=null)
			uname=uname.trim();
		this.uname = uname;
	}
	public Date getCurrdate() {
		return currdate;
	}
	public void setCurrdate(Date currdate) {
		this.currdate = currdate;
	}
	public Date getLastdate() {
		return lastdate;
	}
	public void setLastdate(Date lastdate) {
		this.lastdate = lastdate;
	}
	public Date getRegistdate() {
		return registdate;
	}
	public void setRegistdate(Date registdate) {
		this.registdate = registdate;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getHometown() {
		return hometown;
	}

	public void setHometown(String hometown) {
		this.hometown = hometown;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	
}
