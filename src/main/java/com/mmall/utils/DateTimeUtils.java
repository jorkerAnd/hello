package com.mmall.utils;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateTimeUtils {

//Date-》str
private static final String STANDARD_FORMAT="yyyy-MM-dd HH:mm:ss";

    //str->Date
    public static Date strToDate(String dateTimeStr, String formatStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }


    //Date->str
    public static String dateToStr(Date date, String formatStr) {
        if (date == null)
            return StringUtils.EMPTY;
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);

    }

    public static Date strToDate(String dateTimeStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }


    //Date->str
    public static String dateToStr(Date date) {
        if (date == null)
            return StringUtils.EMPTY;
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);

    }


//    public static void main(String[] args) {
//        System.out.println(DateTimeUtils.dateToStr(new Date(),"yyyy-MM-dd HH:mm:ss"));
//        System.out.println();
//    }
}
