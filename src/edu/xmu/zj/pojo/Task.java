package edu.xmu.zj.pojo;

import java.util.Date;

/**
 * 任务实体类
 * 
 * 
 */

public class Task {
	public static final int RUNNING = 0;// 运行中
	public static final int PAUSE = 9;// 暂停
	public static final int STOP = 2;// 停止
	public static final int RESTART = 4;// 重启

	private long id;

	private String taskid;// 任务ID
	private String url;// URL地址
	private String tname;// 任务名称
	private String type;// 解析类型
	private Integer maxthread =2;// 一个任务最多使用线程数
	private String deword;// 任务排除的字词，用逗号隔开
	private Date lastdate;// 上次更新时间
	private Date nowdate;// 上次更新时间
	private Date nextStart;
	private String url2;
	public int status;
	private int pages; // 页码
	private int lastdpages;//最近爬取的页码

	private Integer nondtimes = 0;
	private Integer closetime = 0;

	private long webtype;

	public long getWebtype() {
		return webtype;
	}

	public void setWebtype(long webtype) {
		this.webtype = webtype;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public void countSpeed()
	  {
	    if ((pages - lastdpages == 0) && (status  == 1))
	      this.nondtimes = this.nondtimes + 5;
	    else
	      this.nondtimes = 0;
	    this.lastdpages = this.pages;
	  }

	  public void addCloseTime()
	  {
	    this.closetime =this.closetime.intValue() + 5;
	  }

	public Date getNextStart() {
		return nextStart;
	}

	public void setNextStart(Date nextStart) {
		this.nextStart = nextStart;
	}

	public void setMaxthread(Integer maxthread) {
		this.maxthread = maxthread;
	}

	private String proxyurl;// 请求时代理地址
	private String proxyport;// 请求时代理端口
	private String proxyuser;// 请求时代理用户名
	private String proxypwd;// 请求时代理密码
	private String ucookies;// cookies
	private Integer sleeptime = 1;// 任务完成一轮后休息时间：s
	private int encode = -1;// 保存记录编码
	private int aligntype = 0;// 系统编码

	public int getAligntype() {
		return aligntype;
	}

	public void setAligntype(int aligntype) {
		this.aligntype = aligntype;
	}

	public boolean isNormalAlign() {
		return this.aligntype == 0;
	}

	public int getEncode() {
		return encode;
	}

	public void setEncode(int encode) {
		this.encode = encode;
	}

	public Integer getSleeptime() {
		return sleeptime;
	}

	public void setSleeptime(Integer sleeptime) {
		this.sleeptime = sleeptime;
	}

	public String getUcookies() {
		return ucookies;
	}

	public void setUcookies(String ucookies) {
		this.ucookies = ucookies;
	}

	public String getProxyurl() {
		return proxyurl;
	}

	public void setProxyurl(String proxyurl) {
		this.proxyurl = proxyurl;
	}

	public String getProxyport() {
		return proxyport;
	}

	public void setProxyport(String proxyport) {
		this.proxyport = proxyport;
	}

	public String getProxyuser() {
		return proxyuser;
	}

	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}

	public String getProxypwd() {
		return proxypwd;
	}

	public void setProxypwd(String proxypwd) {
		this.proxypwd = proxypwd;
	}

	public String getDeword() {
		return deword;
	}

	public void setDeword(String deword) {
		this.deword = deword;
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

	public int getMaxthread() {
		return maxthread;
	}

	public void setMaxthread(int maxthread) {
		this.maxthread = maxthread;
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

	/**
	 * ID相同则认为任务相同
	 */
	@Override
	public boolean equals(Object task) {
		boolean result = false;

		if (this.taskid.equals(((Task) task).getTaskid())) {
			result = true;
		}

		return result;
	}

	public String getTaskid() {
		return taskid;
	}

	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}

	public String getTname() {
		return tname;
	}

	public void setTname(String tname) {
		this.tname = tname;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Integer getNondtimes() {
		return nondtimes;
	}

	public void setNondtimes(Integer nondtimes) {
		this.nondtimes = nondtimes;
	}

	public Integer getClosetime() {
		return closetime;
	}

	public void setClosetime(Integer closetime) {
		this.closetime = closetime;
	}

	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public int getLastdpages() {
		return lastdpages;
	}

	public void setLastdpages(int lastdpages) {
		this.lastdpages = lastdpages;
	}

}
