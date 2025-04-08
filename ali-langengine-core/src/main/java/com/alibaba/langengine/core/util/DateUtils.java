/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.core.util;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * 操作日期的工具类,
 *
 * 日期相关的工具函数请加入到本类中
 *
 * @author xiaoxuan.lp
 *
 */
public class DateUtils {
	private static final String DEFAULT_DATE_FORMAT = "yyyy.MM.dd";
	public static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	public DateUtils() {
	}

	private static final ThreadLocal<SimpleDateFormat> dayFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private static final ThreadLocal<SimpleDateFormat> minudeFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
    	@Override
    	protected SimpleDateFormat initialValue() {
    		return new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	}
    };

	/**
	 * 将日期格式化为字符串形式yyyy.MM.dd
	 *
	 * @param date
	 * @return 如果date == null，返回""
	 */
	public static String formatDate(final Date date) {
		return formatDate(date, DEFAULT_DATE_FORMAT);
	}

	/**
	 * 将格式化的日期字符串转换为时间戳
	 *
	 * @param dateStr
	 * @return
	 */
	public static Long getTimeStampFromStr(String dateStr) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		try {
			ts = Timestamp.valueOf(dateStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ts.getTime();
	}

	// 计算两个时间差（秒）
	public static long calTimediff(Date startDate, Date endDate) {
		long a = startDate.getTime();
		long b = endDate.getTime();
		long c = (b - a) / 1000;
		return c;
	}

	/**
	 * 以指定的日期格式来格式化日期
	 *
	 * @param date
	 * @param format
	 *            不允许为null
	 * @return 如果date == null，返回""
	 */
	public static String formatDate(final Date date, final String format) {
		if (date == null) {
			return "";
		}
		return new SimpleDateFormat(format).format(date);
	}

	/**
	 * 获取n天后的日期
	 *
	 * @param date
	 * @param days
	 */
	public static Date getDateAfter(final Date date, int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		return calendar.getTime();
	}

	public static Long getCurrentDaySeconds() throws Exception {
    	Date current = parse("" + getCurrentDay(),"yyyyMMdd") ;
        return current.getTime() / 1000 ;
    }

	/**
	 *
	 * @param beginDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @return 1 beginDate>endDate -1 beginDate<endDate 0 beginDate=endDate
	 * @throws Exception
	 */

	public static int compareDate(String beginDate, String endDate) throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		try {
			Date dt1 = df.parse(beginDate);
			Date dt2 = df.parse(endDate);
			if (dt1.getTime() > dt2.getTime()) {
				// begingDate在endDate前
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				// begingDate在endDate后
				return -1;
			} else {
				return 0;
			}
		} catch (ParseException e) {
			throw new Exception("日期格式化异常");
		}

	}

	/**
	 *
	 * @param begingDate
	 * @param endDate
	 * @return 1 beginDate>endDate -1 beginDate<endDate 0 beginDate=endDate
	 */
	public static int compareDate(Date begingDate, Date endDate) {
		if (begingDate.getTime() > endDate.getTime()) {
			// begingDate在endDate前
			return 1;
		} else if (begingDate.getTime() < endDate.getTime()) {
			// begingDate在endDate后
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * 获得指定日期的第一秒(yyyy-MM-dd 00:00:00:000)
	 *
	 * @param fullDate
	 *            日期
	 * @return 日期
	 */
	public static Date getFirstSecondOfTheDay(Date fullDate) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 获得指定日期后一天的第一秒(yyyy-MM-dd 00:00:00:000)
	 *
	 * @param fullDate
	 *            日期
	 * @return 日期
	 */
	public static Date getFirstSecondOfNextDay(Date fullDate) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	@SuppressWarnings({"deprecation" })
	public static Long getCurrentDay() throws Exception {
		Date date = new Date();
		return new Long((date.getYear()+1900)* 10000 + (1+date.getMonth()) *100 + date.getDate());
	}

	/**
	 * 获得指定日期的最后一秒 (yyyy-MM-dd 23:59:59:000)
	 *
	 * @param fullDate
	 *            日期
	 * @return 日期
	 */
	public static Date getLastSecondOfTheDay(Date fullDate) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 获得指定日期前一天的最后一秒 (yyyy-MM-dd 23:59:59:000)
	 *
	 * @param fullDate
	 *            日期
	 * @return
	 */
	public static Date getLastSecondOfLastDay(Date fullDate) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 获得指定日期N个月后最后一天的前一天的最后一秒 (yyyy-MM-dd 23:59:59:000)
	 *
	 * @param fullDate
	 *            日期
	 * @param months
	 *            月份数
	 * @return
	 */
	public static Date getLastSecondOfLastDayAfterNMonths(Date fullDate, int months) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.add(Calendar.MONTH, months);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 获得指定日期N-1天的最后一秒 (yyyy-MM-dd 23:59:59:000)
	 *
	 * @param fullDate
	 *            日期
	 * @param days
	 *            天数
	 * @return
	 */
	public static Date getLastSecondOfLastDayAfterNDays(Date fullDate, int days) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.add(Calendar.DAY_OF_MONTH, days - 1);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 增加年数
	 *
	 * @param fullDate
	 *            日期
	 * @param years
	 *            年数
	 * @return 增加月份数的日期
	 */
	public static Date addYear(Date fullDate, int years) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fullDate);
		calendar.add(Calendar.YEAR, years);
		return calendar.getTime();
	}

	/**
	 * 增加月份数
	 *
	 * @param fullDate
	 *            日期
	 * @param months
	 *            月份数
	 * @return 增加月份数的日期
	 */
	public static Date addMonth(Date fullDate, int months) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fullDate);
		calendar.add(Calendar.MONTH, months);
		return calendar.getTime();
	}

	/**
	 * 增加天数
	 *
	 * @param fullDate
	 *            日期
	 * @param days
	 *            天数
	 * @return 增加天数后的日期
	 */
	public static Date addDay(Date fullDate, int days) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.add(Calendar.DAY_OF_MONTH, days);
		return cal.getTime();
	}

    private static final ThreadLocal<SimpleDateFormat> hourFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH");
        }
    };

	private static final ThreadLocal<SimpleDateFormat> minuteFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss");
        }
    };

    public static String getHour(Date date) throws ParseException {
    	return hourFormatThreadLocal.get().format(date) + ":00";
    }

    public static String getMinute(Date date) throws ParseException {
    	return minuteFormatThreadLocal.get().format(date);
    }
    
    /**
     * 功能描述：返回小时
     *
     * @param date
     *            日期
     * @return 返回小时
     */
    public static int getRealHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 功能描述：返回分
     *
     * @param date
     *            日期
     * @return 返回分钟
     */
    public static int getRealMinute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }


	/**
	 * 增加小时数［应用场景来源于数据中心，实时详情中，选择不同的区间，展示不同时间段的数据］
	 *
	 * @author chaohui.zch 2016年7月22日下午3:00:32
	 */
	public static Date addHour(Date fullDate, int hours) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.add(Calendar.HOUR_OF_DAY, hours);
		return cal.getTime();
	}

    @SuppressWarnings("deprecation")
	public static Long convertDateToNum(Date date){
        return new Long((date.getYear()+1900)* 10000 + (1+date.getMonth()) *100 + date.getDate());
    }

	public static Date formatDate(String date) throws ParseException {
	    return dayFormatThreadLocal.get().parse(date);
	}

	public static Date formatMinuteDate(String date) throws ParseException {
		return minudeFormatThreadLocal.get().parse(date);
	}

	/**
	 * 增加分钟数［应用场景来源于数据中心，进行API排名时，计算增长最快的API时，需要与前一分钟进行比较］
	 *
	 * @author chaohui.zch 2016年9月7日下午4:16:06
	 */
	public static Date addMinute(Date fullDate, int minutes) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.add(Calendar.MINUTE, minutes);
		return cal.getTime();
	}

	public static Date addSecond(Date fullDate, int second) {
		if (null == fullDate) {
			return (Date) null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(fullDate);
		cal.add(Calendar.SECOND, second);
		return cal.getTime();
	}

	/**
	 * 获取指定日期的当前时间点，例如当前时间是 2016-07-22 03:46:45,传入日期为2016-07-21 则返回2016-07-21
	 * 03:46:45
	 */
	@SuppressWarnings("deprecation")
	public static Date getCurrentTimeOfTheDay(Date fullDate) {
		if (null == fullDate) {
			return (Date) null;
		}
		fullDate.setHours((new Date()).getHours());
		fullDate.setMinutes((new Date()).getMinutes());
		fullDate.setSeconds((new Date()).getSeconds());
		return fullDate;
	}

	/**
	 * linux下转换日期
	 *
	 * @param str
	 * @return
	 */
	public static Date toDate(String str) throws ParseException {
		Date dt = new Date();
		String[] parts = str.split("-");

		if (parts.length == 3) {
			int years = Integer.parseInt(parts[0]);
			int months = Integer.parseInt(parts[1]);
			int days = Integer.parseInt(parts[2]);
			int hours = 0;
			int minutes = 0;
			int seconds = 0;

			GregorianCalendar gc = new GregorianCalendar(years, months, days, hours, minutes, seconds);

			dt = gc.getTime();
		} else {
			throw new ParseException("转换日期出错", 0);
		}
		return dt;
	}

	/**
	 * 计算时间差，转换为【xx天xx时xx分】的格式
	 *
	 * @param endDate
	 * @param beginDate
	 * @return
	 */
	public static String calcShortenedDate(Date endDate, Date beginDate) {
		if (endDate == null || beginDate == null) {
			return "";
		}
		StringBuffer shortenedDate = new StringBuffer();
		long shortenedSeconds = (endDate.getTime() - beginDate.getTime()) / 1000;
		if (shortenedSeconds < 0) {
			return "";
		}
		long day = shortenedSeconds / (24 * 3600);
		long hour = shortenedSeconds % (24 * 3600) / 3600;
		long minute = shortenedSeconds % 3600 / 60;
		shortenedDate.append(day);
		shortenedDate.append("天");
		shortenedDate.append(hour);
		shortenedDate.append("时");
		shortenedDate.append(minute);
		shortenedDate.append("分");
		return shortenedDate.toString();
	}

	/**********************************************
	 * For AC
	 **********************************************
	 */
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

	/**
	 * 格式化日期.
	 *
	 * @param date
	 *            为null时返回""
	 *            期望的日期格式，为空时使用默认的日期格式DateUtil.YYYY_MM_DD_HH_MM_SS
	 * @return
	 */
	public static String format(Date date) {
		return format(date, null);
	}

	public static String formatDate(Long date) {
		DateFormat format = new SimpleDateFormat(LONG_DATE_FORMAT);
		return format.format(new Date(date));
	}

	/**
	 * 格式化日期.
	 *
	 * @param date
	 *            为null时返回""
	 * @param pattern
	 *            期望的日期格式，为空时使用默认的日期格式DateUtil.YYYY_MM_DD_HH_MM_SS
	 * @return
	 */
	public static String format(Date date, String pattern) {
		if (date == null) {
			return "";
		}
		if (StringUtils.isBlank(pattern)) {
			pattern = YYYY_MM_DD_HH_MM_SS;
		}
		SimpleDateFormat formater = new SimpleDateFormat(pattern);
		return formater.format(date);
	}

	public static String formatMinute(Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat formater = new SimpleDateFormat(YYYY_MM_DD_HH_MM);
		return formater.format(date);
	}

	/**
	 * 将日期字符串解析成指定格式的Date对象
	 *
	 * @param dateTime
	 *            日期字符串
	 * @param format
	 *            指定格式
	 * @return （正确格式）日期对象
	 * @throws ParseException
	 */
	public static Date parse(String dateTime, String format) throws ParseException {
		if (dateTime == null || dateTime.length() <= 0)
			return null;
		String sDateTime = ((dateTime.indexOf('.') > 0)) ? dateTime.substring(0, dateTime.indexOf('.')) : dateTime;

		// 适配 长度兼容 取短
		if(format.length() > sDateTime.length()){
			format = format.substring(0, sDateTime.length());
		}
		if(format.length() < sDateTime.length()){
			sDateTime = sDateTime.substring(0, format.length());
		}

		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.parse(sDateTime);
	}


	/**
	 * 将日期字符串解析成"yyyy-MM-dd HH:mm:ss"格式的Date对象
	 *
	 * @param dateTime
	 *            日期字符串
	 * @return （正确格式）日期对象
	 * @throws ParseException
	 */
	public static Date parseDateTime(String dateTime) throws ParseException {
		return parse(dateTime, YYYY_MM_DD_HH_MM_SS);
		// return parseDate(dateTime);
	}

	public static Date parseDateTimeMinute(String dateTime) throws ParseException {
		return parse(dateTime, YYYY_MM_DD_HH_MM);
	}

	/**
	 * 将日期字符串解析成"yyyy-MM-dd"格式的Date对象
	 *
	 * @param dateTime
	 *            日期字符串
	 * @return （正确格式）日期对象
	 * @throws ParseException
	 */
	public static Date parseDate(String dateTime) throws ParseException {
		return parse(dateTime, YYYY_MM_DD);
	}

	public static Date getYesterday(Date date) {
		Date yesterday = dateAddOrSubtract(date, Calendar.DAY_OF_MONTH, -1);
		return yesterday;
	}

	public static Date dateAddOrSubtract(Date date, int field, int count) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(field, count);
		return c.getTime();
	}

	public static int getDaysBetween(String date1, String date2) {
		if (StringUtils.isBlank(date1) || StringUtils.isBlank(date2)){
			return -1;
		}
		try {
			Date _date1 = formatDate(date1);
			Date _date2 = formatDate(date2);
			return getDaysBetween(_date1,_date2);
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static int getDaysBetween(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return 0;
		}
		Calendar d1 = new GregorianCalendar();
		d1.setTime(date1);
		Calendar d2 = new GregorianCalendar();
		d2.setTime(date2);
		if (d1.after(d2)) { // swap dates so that d1 is start and d2 is end
			Calendar swap = d1;
			d1 = d2;
			d2 = swap;
		}
		int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
		int y2 = d2.get(Calendar.YEAR);
		if (d1.get(Calendar.YEAR) != y2) {
			d1 = (Calendar) d1.clone();
			do {
				days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);
				d1.add(Calendar.YEAR, 1);
			} while (d1.get(Calendar.YEAR) != y2);
		}
		return days;
	}

	/**
	 * 取得两个日期之间相差的月份 如果日期相差1个月以内，则显示一个月
	 *
	 * @param startdate
	 * @param enddate
	 * @return
	 */
	public static int getMonth(Date startdate, Date enddate) {

		if (startdate == null || enddate == null)
			return 0;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startdate);
		int startyear = calendar.get(Calendar.YEAR);
		int startmonth = calendar.get(Calendar.MONTH);
		calendar.setTime(enddate);
		int endmonth = calendar.get(Calendar.MONTH);
		int endyear = calendar.get(Calendar.YEAR);

		int addMonth = (endyear - startyear) * 12;
		int result = (endmonth - startmonth) + addMonth;
		if (result <= 0)
			result = 1;
		return result;
	}

	/**
	 * 增加月份，录入startDate为2015-02-11，如果month为3，即endTime为2015-06-01
	 *
	 * @param startDate
	 * @param month
	 * @return
	 */
	public static Date addMonthsToFirstDay(Date startDate, int month) {
		Date endDate = addMonth(startDate, month + 1);
		String endDateStr = format(endDate, "yyyy-MM");
		endDateStr += "-01";
		try {
			endDate = parseDate(endDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return endDate;
	}

	/**
	 * 根据指定日期获取月份中最后一天
	 *
	 * @param date
	 * @return
	 */
	public static Date getMonthEndDate(Date date) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		ca.set(Calendar.HOUR_OF_DAY, 23);
		ca.set(Calendar.MINUTE, 59);
		ca.set(Calendar.SECOND, 59);
		ca.set(Calendar.DAY_OF_MONTH, 1);
		ca.add(Calendar.MONTH, 1);
		ca.add(Calendar.DAY_OF_MONTH, -1);
		Date lastDate = new Date(ca.getTime().getTime());
		return lastDate;
	}

	/**
	 * 从时间字符串截取到时间 HH:mm
	 *
	 * @return String HH:mm
	 */
	public static String getHourAndMinute(String date) {
		// Date temp = null;
		// try {
		// temp = parse(date,YYYY_MM_DD_HH_MM);
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
		return date.toString().substring(11);
	}

	/**
	 * 指定日期是周几
	 * @param pTime
	 * @return
	 * @throws Exception
	 */
	public static int getDayForWeek(String pTime) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(format.parse(pTime));
		int dayForWeek = 0;
		if (c.get(Calendar.DAY_OF_WEEK) == 1) {
			dayForWeek = 7;
		} else {
			dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
		}
		return dayForWeek;
	}

	public static int getMinutesBetween(Date startDate,Date endDate){
		Long diff = Math.abs( startDate.getTime() - endDate.getTime()) / (1000 * 60);
		return diff.intValue();
	}

	/**
	 * 判断是否最后一分钟(专门针对来自于xray报表取出的timeStamp)
	 * @param xrayTimeStamp
	 * @return
	 */
	public static Boolean isLastMinuteOfTheDay(String xrayTimeStamp) throws Exception {
		if (StringUtils.isBlank(xrayTimeStamp)){
			throw new RuntimeException("timeStamp in isLastMinuteOfTheDay is empty");
		}
		return xrayTimeStamp.endsWith("23:59");
	}

	public static Boolean isLastMinuteOfTheDay(Date xrayTimeStamp) throws Exception {
		if (null == xrayTimeStamp){
			throw new RuntimeException("timeStamp in isLastMinuteOfTheDay is empty");
		}
		String format = formatDate(xrayTimeStamp,YYYY_MM_DD_HH_MM_SS);
		return format.endsWith("23:59");
	}

	/**
	 * 转换为日期(专门针对来自于xray报表取出的timeStamp)
	 * @param xrayTimeStamp
	 * @return
	 */
	public static Date parseDateFromXrayTimeStamp(String xrayTimeStamp) throws Exception {
		if (StringUtils.isBlank(xrayTimeStamp)){
			throw new RuntimeException("timeStamp in parseDateFromXrayTimeStamp is empty");
		}
		try {
			return parse(xrayTimeStamp + ":00",YYYY_MM_DD_HH_MM_SS);
		} catch (ParseException e) {
			throw new RuntimeException("parse error in parseDateFromXrayTimeStamp");
		}
	}

	public static Date getDateFormatByMinute() throws Exception {

		Date date = new Date();

		return parseDateTimeMinute(format(date, YYYY_MM_DD_HH_MM));
	}

	public static Date getStartTimeOfThisDay(Date date){
		if(date==null){
			date=new Date();
		}

		Calendar cStart = Calendar.getInstance();
		cStart.setTime(date);
		cStart.set(Calendar.HOUR_OF_DAY, 0);
		cStart.set(Calendar.MINUTE, 0);
		cStart.set(Calendar.SECOND, 0);
		cStart.set(Calendar.MILLISECOND, 0);
		return cStart.getTime();
	}

	public static Date getEndTimeOfThisDay(Date date){
		if(date==null){
			date=new Date();
		}

		Calendar cStart = Calendar.getInstance();
		cStart.setTime(date);
		cStart.set(Calendar.HOUR_OF_DAY, 23);
		cStart.set(Calendar.MINUTE, 59);
		cStart.set(Calendar.SECOND, 59);
		cStart.set(Calendar.MILLISECOND, 0);
		return cStart.getTime();
	}

	public static Date getTimeBeforeMinutes(Date date, int minutes) {
		if (date == null) {
			date = new Date();
		}

		Calendar cStart = Calendar.getInstance();
		cStart.setTime(date);
		cStart.add(Calendar.MINUTE, -minutes);
		cStart.set(Calendar.SECOND, 0);
		cStart.set(Calendar.MILLISECOND, 0);
		return cStart.getTime();
	}

}
