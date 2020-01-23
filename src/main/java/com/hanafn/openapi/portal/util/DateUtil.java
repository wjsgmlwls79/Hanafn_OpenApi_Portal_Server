/*
 * @(#)DateUtil.java	1.0	2009. 08. 30.
 *
 * Copyright (c) 2009 TA Networks
 * All rights reserved.
 */
package com.hanafn.openapi.portal.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 공통 Utility
 * Class 설명 : 배치업무에서 사용되는 공통 유틸리티 메소드 정의
 * @version 1.0
 * @since   2009. 03. 13
 * @author  DH.KANG
 */
public class DateUtil {

	public static String formatDateTime(String format) {
		return DateFormatUtils.format(System.currentTimeMillis(), format);
	}

	public static String formatDateTime(String date, String format) {
		GregorianCalendar calandar = new GregorianCalendar();
		if(date.length() == 6){
			calandar.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(date.substring(0,2)));
			calandar.set(GregorianCalendar.MINUTE, Integer.parseInt(date.substring(2,4)));
			calandar.set(GregorianCalendar.SECOND, Integer.parseInt(date.substring(4)));
		}
		else if(date.length() == 8){
			calandar.set(Integer.parseInt(date.substring(0,4)), Integer.parseInt(date.substring(4,6))-1, Integer.parseInt(date.substring(6,8)));
		}
		else if(date.length() == 14){
			calandar.set(Integer.parseInt(date.substring(0,4)), Integer.parseInt(date.substring(4,6))-1, Integer.parseInt(date.substring(6,8)), Integer.parseInt(date.substring(8,10)), Integer.parseInt(date.substring(10,12)), Integer.parseInt(date.substring(12,14)));
		}
		else{
			return "";
		}

		return DateFormatUtils.format(calandar.getTime(), format);
	}

	public static String formatDate6(String date, String format) {
		GregorianCalendar calandar = new GregorianCalendar();
		calandar.set(Integer.parseInt(date.substring(0,2)), Integer.parseInt(date.substring(2,4))-1, Integer.parseInt(date.substring(4,6)));

		return DateFormatUtils.format(calandar.getTime(), format);
	}

	/**
	 * 현재 일자와 시간을 반환한다. (17자리 : yyyyMMddHHmmssSSS)
	 * @return String
	 */
	public static String getCurrentDateTime17() {
		return DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMddHHmmssSSS");
	}

	/**
	 * 현재 일자와 시간을 반환한다. (16자리 : yyyyMMddHHmmssSS)
	 * @return String
	 */
	public static String getCurrentDateTime16() {
		String dateTime = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMddHHmmssSSS");
		return dateTime.substring(0, 16);
	}

	/**
	 * 현재 일자와 시간을 반환한다. (14자리 : yyyyMMddHHmmss)
	 * @return String
	 */
	public static String getCurrentDateTime() {
		return DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMddHHmmss");
	}

	/**
	 * 현재 일자와 시간을 반환한다. (12자리 : yyMMddHHmmss)
	 * @return String
	 */
	public static String getCurrentDateTime12() {
		return DateFormatUtils.format(System.currentTimeMillis(), "yyMMddHHmmss");
	}

	/**
	 * 현재일자를 반환한다. (8자리 : yyyyMMdd)
	 * @return String
	 */
	public static String getCurrentDate() {
		return DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd");
	}

	/**
	 * 현재일자를 반환한다. (6자리 : yyMMdd)
	 * @return String
	 */
	public static String getCurrentDate6() {
		return DateFormatUtils.format(System.currentTimeMillis(), "yyMMdd");
	}

	/**
	 * 현재시간을 반환한다. (6자리 : HHmmss)
	 * @return String
	 */
	public static String getCurrentTime() {
		return DateFormatUtils.format(System.currentTimeMillis(), "HHmmss");
	}

    /**
     * 월,일에 현제 년도를 추가 반환한다. (8자리 : yyyyMMdd)
     * @param mmdd 월일
     * @return String
     */
    public static String getCalYear( String mmdd ) {

    	String systemDate = getCurrentDate();
    	return systemDate.substring(0, 4) + mmdd;
    }

	/**
	 * 오늘을 기준으로 파라메터로 입력한 값 이후의 일자를 반환한다. (8자리 : yyyyMMdd)
	 * @param day 합산할 일
	 * @return String
	 */
	public static String getDate(int day) {
		GregorianCalendar calandar = new GregorianCalendar();
		calandar.add(Calendar.DAY_OF_YEAR, day);
		return DateFormatUtils.format(calandar.getTime(), "yyyyMMdd");
	}

	/**
	 * 오늘을 기준으로 파라메터로 입력한 값 이후의 분를 반환한다. (6자리 : HHmmss)
	 * @param minute 합산할 분
	 * @return String
	 */
	public static String getMinute(int minute) {
		GregorianCalendar calandar = new GregorianCalendar();
		calandar.add(Calendar.MINUTE, minute);
		return DateFormatUtils.format(calandar.getTimeInMillis(), "HHmmss");
	}

	/**
	 * 오늘을 기준으로 파라메터로 입력한 값 이후의 일자를 반환한다. (8자리 : yyyyMMdd)
	 * @param time 합산할 일
	 * @return String
	 */
	public static String addTimeByHour(int time) {
		GregorianCalendar calandar = new GregorianCalendar();
		calandar.add(Calendar.HOUR, time);
		return DateFormatUtils.format(calandar.getTime(), "HHmmss");
	}

	/**
	 * 오늘을 기준으로 파라메터로 입력한 값 이후의 일자를 반환한다. (8자리 : yyyyMMdd)
	 * @param day 합산할 일
	 * @return String
	 */
	public static String addDateByDay(int day) {
		GregorianCalendar calandar = new GregorianCalendar();
		calandar.add(Calendar.DAY_OF_YEAR, day);
		return DateFormatUtils.format(calandar.getTime(), "yyyyMMdd");
	}

	/**
	 * 오늘을 기준으로 파라메터로 입력한 값 이후의 일자를 반환한다. (8자리 : yyyyMMdd)
	 * @param month 합산할 월
	 * @return String
	 */
	public static String addDateByMonth(int month) {
		GregorianCalendar calandar = new GregorianCalendar();
		calandar.add(Calendar.MONTH, month);
		return DateFormatUtils.format(calandar.getTime(), "yyyyMMdd");
	}

	/**
	 * 오늘을 기준으로 파라메터로 입력한 값 이후의 일자를 반환한다. (8자리 : yyyyMMdd)
	 * @param year 합산할 년
	 * @return String
	 */
	public static String addDateByYear(int year) {
		GregorianCalendar calandar = new GregorianCalendar();
		calandar.add(Calendar.YEAR, year);
		return DateFormatUtils.format(calandar.getTime(), "yyyyMMdd");
	}
	/**
	 * 입력한 일자를 기준으로 파라메터로 입력한 값 이후의 일자를 반환한다. (8자리 : yyyyMMdd)
	 * @param date 일자, day 합산할 일
	 * @return String
	 */
	public static String addDay(String date, int day) {
		GregorianCalendar calandar = new GregorianCalendar();
		calandar.set(Integer.parseInt(date.substring(0,4)), Integer.parseInt(date.toString().substring(4,6))-1, Integer.parseInt(date.toString().substring(6,8)));

		calandar.add(Calendar.DAY_OF_YEAR, day);
		return DateFormatUtils.format(calandar.getTime(), "yyyyMMdd");
	}

	/**
	 * 입력기준일로 부터 월을 계산하여 계산된 일자를 반환한다. (8자리 : yyyyMMdd)
	 * @param date 합산할 일
	 * @param month 합산할 월
	 * @return String
	 */
	public static String getCalDateMonth(String date, int month) {
		GregorianCalendar calandar = new GregorianCalendar();

		calandar.set(Integer.parseInt(date.substring(0,4)), Integer.parseInt(date.toString().substring(4,6))-1, Integer.parseInt(date.toString().substring(6,8)));
		calandar.add(Calendar.MONTH, month);
		return DateFormatUtils.format(calandar.getTime(), "yyyyMMdd");
	}

	/**
	 * 입력기준일로 부터 년을 계산하여 계산된 일자를 반환한다. (8자리 : yyyyMMdd)
	 * @param date 합산할 일
	 * @param year 합산할 년
	 * @return String
	 */
	public static String getCalDateYear(String date, int year) {
		GregorianCalendar calandar = new GregorianCalendar();

		calandar.set(Integer.parseInt(date.substring(0,4)), Integer.parseInt(date.toString().substring(4,6))-1, Integer.parseInt(date.toString().substring(6,8)));
		calandar.add(Calendar.YEAR, year);
		return DateFormatUtils.format(calandar.getTime(), "yyyyMMdd");
	}

	/**
	 * 입력한 두 날짜의 차이를 반환한다. (8자리 : yyyyMMdd)
	 * @param stDate 날짜 차이일
	 * @param enDate 날짜 차이일
	 * @return String
	 */
	public static int getDayDiff(String stDate, String enDate) {
		if (stDate.length() > 8) {
			stDate = stDate.substring(0,10).replaceAll("-","");
		}

		if (enDate.length() > 8) {
			enDate = enDate.substring(0,10).replaceAll("-","");
		}

		GregorianCalendar stCalandar = new GregorianCalendar();
		GregorianCalendar enCalandar = new GregorianCalendar();
		stCalandar.set(Integer.parseInt(stDate.substring(0,4)), Integer.parseInt(stDate.toString().substring(4,6))-1, Integer.parseInt(stDate.toString().substring(6,8)));
		enCalandar.set(Integer.parseInt(enDate.substring(0,4)), Integer.parseInt(enDate.toString().substring(4,6))-1, Integer.parseInt(enDate.toString().substring(6,8)));

		long diffSec = (enCalandar.getTimeInMillis() - stCalandar.getTimeInMillis())/1000;
		int difDay = (int)diffSec/(60*60*24);

		return difDay;
	}

	/**
	 * 입력한 두 날짜의 차이를 반환한다. (14자리 : yyyyMMdd hhmmss)
	 * @param stDate 날짜 차이일
	 * @param enDate 날짜 차이일
	 * @return String
	 */
	public static long getDayDiffMinute(String stDate, String enDate) {
		if (stDate.length() > 14) {
			stDate = stDate.substring(0,19).replaceAll("-","").replaceAll(":", "").trim();
		}

		if (enDate.length() > 14) {
			enDate = enDate.substring(0,19).replaceAll("-","").replaceAll(":", "").replace(" ","");
		}

		long diffSec = (Long.parseLong(enDate) - Long.parseLong(stDate));

		return diffSec;
	}

	/**
	 * Date 형식 유효성 검사
	 * @param params
	 * @param format
	 * @return
	 */
	public static boolean checkDate(String params, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        Date chkDate = new Date();

        dateFormat.applyPattern(format);
        dateFormat.setLenient(false);      // 엄밀하게 검사한다는 옵션 (반드시 있어야 한다)

        try {
            chkDate = dateFormat.parse(params);
        } catch (ParseException e) {
            return false;
        }
		return true;
	}

	/**
	 * 지난달 반환한다. (8자리 : yyyyMM)
	 * @return String
	 */
	public static String getbeforeMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(cal.MONTH, -1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
		return dateFormat.format(cal.getTime());
	}

	/**
     * 현제 년월에 일자를 추가 반환한다. (8자리 : yyyyMMdd)
     * @param dd
     * @return String
     */
    public static String getCalday( String dd ) {

    	String systemDate = getCurrentDate();
    	return systemDate.substring(0, 6) + dd;
    }

    /**
	 * 전일 반환한다. (6자리 : yyMMdd)
	 * @return String
	 */
	public static String getPlus1Day(String date) {
		GregorianCalendar calandar = new GregorianCalendar();

		calandar.set(Integer.parseInt(date.substring(0,2)), Integer.parseInt(date.toString().substring(2,4))-1, Integer.parseInt(date.toString().substring(4,6)));
		calandar.add(calandar.DAY_OF_MONTH, +1);

		return DateFormatUtils.format(calandar.getTime(), "yyMMdd");
	}

	/**
	 * 전일 반환한다. (6자리 : yyyyMMdd)
	 * @return String
	 */
	public static String getMinu1Day(String date) {
		GregorianCalendar calandar = new GregorianCalendar();

		calandar.set(Integer.parseInt(date.substring(0,2)), Integer.parseInt(date.toString().substring(2,4))-1, Integer.parseInt(date.toString().substring(4,6)));
		calandar.add(calandar.DAY_OF_MONTH, -1);

		return DateFormatUtils.format(calandar.getTime(), "yyMMdd");
	}
}
