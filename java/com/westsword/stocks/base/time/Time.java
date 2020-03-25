package com.westsword.stocks.base.time;


import java.util.*;

import com.westsword.stocks.base.Settings;

public class Time {
    private final static String sTz = Settings.getTimeZone();
    private final static String sLc = Settings.getLocale();


    public static String unformalizeYMD(String tradeDate) {
        return tradeDate.replace("-", "");
    }
    public static String formalizeYMD(String tradeDate) {
        if(tradeDate.contains("-"))
            return tradeDate;

        String ymd = "";

        String year = tradeDate.substring(0, 4);
        String month = tradeDate.substring(4, 6);
        String date = tradeDate.substring(6, 8);

        ymd = year + "-" + month + "-" + date;

        return ymd;
    }
    //assumption:
    //hms is an int by YYYY, MM, DD, hh, mm, ss
    //so it is always 4 digit number or 2 digits number
    public static String formalizeNumber(int hms) {
        String sHMS;

        if(hms<=9)
            sHMS=""+0+hms;
        else
            sHMS=""+hms;

        return sHMS;
    }


    public static String getTimeYMDHMS(long time) {
        String sYMD = getTimeYMD(time);
        String sHMS = getTimeHMS(time);

        return sYMD + "_" +sHMS;
    }
    public static String getTimeYMD(long time, boolean bWithMinus) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(time*1000);

        String sYear, sMonth, sDay;
        int year = cal.get(Calendar.YEAR);
        sYear = formalizeNumber(year);
        int month = cal.get(Calendar.MONTH)+1;
        sMonth = formalizeNumber(month);
        int date = cal.get(Calendar.DAY_OF_MONTH);
        sDay = formalizeNumber(date);

        String ymd;
        if(bWithMinus)
            ymd = ""+sYear+"-"+sMonth+"-"+sDay;
        else
            ymd = ""+sYear+sMonth+sDay;
 
        return ymd;
    }
    public static String getTimeYMD(Calendar cal, boolean bWithMinus) {
        return getTimeYMD(cal.getTimeInMillis()/1000, bWithMinus);
    }
    public static String getTimeYMD(long time) {
        return getTimeYMD(time, true);
    }
    public static String getTimeHMS(long time) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(time*1000);

        String sHour, sMinute, sSecond;
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        sHour = formalizeNumber(hour);
        int minute = cal.get(Calendar.MINUTE);
        sMinute = formalizeNumber(minute);
        int second = cal.get(Calendar.SECOND);
        sSecond = formalizeNumber(second);

        return ""+sHour+":"+sMinute+":"+sSecond;
    }



    public static long getSpecificTime(int year, int month, int date, String sTime) {
        sTime = HMS.formalize(sTime);

        String[] fields=sTime.split(":");
        int hour = Integer.valueOf(fields[0]);
        int minute = Integer.valueOf(fields[1]);
        int second = Integer.valueOf(fields[2]);

        Calendar cal = getCalendar();
        cal.set(year, month, date, hour, minute, second);

        long millis = cal.getTimeInMillis()/1000;

        return millis;
    }
    //tradeDate:       must be of format "YYYYMMDD"
    public static long getSpecificTime(String tradeDate, String sTime) {
        sTime = HMS.formalize(sTime);

        //System.out.format("tradeDate=%s\n", tradeDate);
        int year = Integer.valueOf(tradeDate.substring(0, 4));
        int month = Integer.valueOf(tradeDate.substring(4, 6));
        month -= 1;
        int date = Integer.valueOf(tradeDate.substring(6, 8));

        return getSpecificTime(year, month, date, sTime);
    }
    public static long getSpecificTime(long timepoint, String sTime) {
        sTime = HMS.formalize(sTime);

        String ymd = Time.getTimeYMD(timepoint);
        String[] fields = ymd.split("-");
        int year = Integer.valueOf(fields[0]);
        int month = Integer.valueOf(fields[1]);
        month -= 1;
        int date = Integer.valueOf(fields[2]);

        return getSpecificTime(year, month, date, sTime);
    }


    public static String currentDate() {
        long tp = System.currentTimeMillis()/1000;
        return getTimeYMD(tp, false);
    }
    public static String current() {
        long tp = System.currentTimeMillis()/1000;
        return getTimeYMDHMS(tp);
    }


    public static Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone(sTz), new Locale(sLc));
    }
    public static Calendar getCalendar(String tradeDate) {
        tradeDate = unformalizeYMD(tradeDate);
        int year = Integer.valueOf(tradeDate.substring(0, 4));
        int month = Integer.valueOf(tradeDate.substring(4, 6)) - 1;
        int date = Integer.valueOf(tradeDate.substring(6, 8));

        Calendar cal = getCalendar();
        cal.clear();
        cal.set(year, month, date);

        return cal;
    }
}
