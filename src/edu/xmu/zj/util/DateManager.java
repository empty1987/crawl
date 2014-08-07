package edu.xmu.zj.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 
 * <p>
 * Description:根据参数提供的字符串，获取对应的Date对象
 * </p>
 * <p>
 * Copyright (c) 2008
 * </p>
 * 
 * @author lin
 * 
 * 2008-8-15
 * 
 */
public class DateManager {
	public static String getDefaultSearchTime() {
		Calendar calendar = Calendar.getInstance();
//		int beforeDay = 7;
//		calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - beforeDay);
		
		int beforeYear = 1;
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - beforeYear);
		SimpleDateFormat fordate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String calendarStr = fordate.format(calendar.getTime());
		return calendarStr;
	}
	public static Date preMonthFirstDate(Date date) {
		int month = DateManager.getMonth(date);
		int year = DateManager.getYear(date);
		month--;
		if (month == 0) {
			month = 12;
			year--;
		}
		String strDate = year + "-" + month + "-" + "1";
		return getDate(strDate);
	}

	public static Date preMonthLastDate(Date date) {
		int month = DateManager.getMonth(date);
		int year = DateManager.getYear(date);
		month--;
		if (month == 0) {
			month = 12;
			year--;
		}
		int daysInMonth[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		String strDate = year + "-" + month + "-" + daysInMonth[month-1];
		return getDate(strDate);
	}

	public static Date getDate(String value, String patten) {
		Date date = null;
		if (value == null) {
			return new Date();
		}
		SimpleDateFormat fordate = new SimpleDateFormat(patten);
		try {
			date = fordate.parse(value);
		} catch (ParseException e) {

			e.printStackTrace();
		}
		return date;

	}

	public static Date getTime(String value) {
		Date date = null;
		if (value == null) {
			return new Date();
		}
		SimpleDateFormat fordate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			date = fordate.parse(value);
		} catch (ParseException e) {

			e.printStackTrace();
		}
		return date;
	}

	public static Date getDate(String value) {
		Date date = null;
		if (value == null) {
			return new Date();
		}
		SimpleDateFormat fordate = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date = fordate.parse(value);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return date;
	}

	public static String getNowTime() {
		Date date = new Date();
		SimpleDateFormat fordate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return fordate.format(date);
	}

	public static Date preDate(Date date) {
		long time = date.getTime();
		time = time - 24 * 60 * 60 * 1000;
		Date ytd = new Date(time);
		return ytd;
	}

	public static Date yesterday() {
		Date now = new Date();
		long time = now.getTime();
		time = time - 24 * 60 * 60 * 1000;
		Date ytd = new Date(time);
		return ytd;

	}

	public static Date nextDate(Date date) {
		 
		long time = date.getTime();
		time = time + 24 * 60 * 60 * 1000;
		Date tomorrow = new Date(time);
		return tomorrow;

	}

	public static Date tomorrow() {
		Date now = new Date();
		long time = now.getTime();
		time = time + 24 * 60 * 60 * 1000;
		Date tomorrow = new Date(time);
		return tomorrow;

	}

	public static String formatDate(Date date) {
		return formatDate(date, "yyyy-MM-dd");
	}

	public static String formatDate(Date date, String formater) {
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(formater);
		if (date == null) {
			return null;
		} else {
			String datestring = bartDateFormat.format(date);
			return datestring;
		}
	}

	public static String getYearMonth() {
		String dateStr = formatDate(new Date(), "yyyy.MM");
		return dateStr;
	}

	public static String getFormatDate(String format) {
		String dateStr = formatDate(new Date(), format);
		return dateStr;
	}

	/**
	 * 
	 */
	public static Date getDateFromMonth(String value) {
		Date date = null;
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,
				java.util.Locale.CHINA);

		Date d = null;
		if (value == null) {
			return new Date();
		}
		try {
			d = df.parse(value);
		} catch (java.text.ParseException e) {
			try {
				d = df.parse("1900-1-1");
			} catch (java.text.ParseException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(d);
		date = new java.sql.Date(calendar.getTimeInMillis());
		return date;
	}

	public static String getYearStr(Date date) {
		String a = formatDate(date, "yyyy");
		return a;
	}

	public static int getYear(Date date) {
		String a = formatDate(date, "yyyy");
		int b = Integer.parseInt(a);
		return b;
	}

	public static String getDayStr(Date date) {
		String a = formatDate(date, "dd");
		return a;
	}

	public static int getDay(Date date) {
		String a = formatDate(date, "dd");
		int b = Integer.parseInt(a);
		return b;
	}

	public static int getHour(Date date) {
		String a = formatDate(date, "HH");
		int b = Integer.parseInt(a);
		return b;
	}

	public static String getMonthStr(Date date) {
		String a = formatDate(date, "MM");
		return a;
	}

	public static int getMonth(Date date) {
		String a = formatDate(date, "MM");
		int b = Integer.parseInt(a);
		return b;
	}

	public static Date getDateAfter(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
		return now.getTime();
	}

	/**
	 * 取得当前时间
	 * 
	 * @return: yyyyMMdd HHmmss
	 */
	public static String getDateTimeForStr(Date date) {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		return df.format(date);
	}
	
	/**
	 * 取得当前时间
	 * 
	 * @return: yyyyMMdd HHmmss
	 */
	public static String getDateForStr(Date date) {
		if(date==null){
			return "";
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(date);
	}

	/**
	 * 取得当前时间
	 * 
	 * @return: yyyyMMdd
	 */
	public static String getDateTime(Date date) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		return df.format(date);
	}

	public static Integer between(Date start, Date end) {
		if (start.getTime() >= end.getTime()) {
			return 0;
		}
		int i = 1;
		start = DateManager.getDate(DateManager.formatDate(start));
		end = DateManager.getDate(DateManager.formatDate(end));
		while (start.getTime() < end.getTime()) {
			start.setTime(start.getTime() + 24 * 60 * 60 * 1000);
			i++;
		}
		return i;
	}
	
	public static Date getLastWeekFirst(Date date){
		
		return null;
	}

	 
}