package com.westsword.stocks.tools;

import java.io.*;
import java.util.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;

import com.westsword.stocks.*;
import com.westsword.stocks.utils.*;

import org.apache.commons.io.FilenameUtils;

public class GetSettings{
    public static void testSettings() {
        System.out.format("\n testSettings: \n");

        Settings s = new Settings();
        String stockCode = Settings.getStockCode();
        System.out.format("stockCode=%s\n", stockCode);
        String tradeDate = Settings.getTradeDate();
        System.out.format("tradeDate=%s\n", tradeDate);
    }



    private static void sdPrint(SdTime sdTime, String hms) {
        int sd = sdTime.get(hms);

        String rHMS = sdTime.rget(sd);
        System.out.format("%s: %s %10d %15s\n", "sdPrint", hms, sd, rHMS);
    }
    private static void sdTimePrint(SdTime sdTime) {
        sdPrint(sdTime, "09:24:00");
        sdPrint(sdTime, "09:25:00");
        sdPrint(sdTime, "09:25:01");
        sdPrint(sdTime, "09:25:02");
        sdPrint(sdTime, "09:25:03");
        sdPrint(sdTime, "09:27:00");
        sdPrint(sdTime, "09:28:00");
        sdPrint(sdTime, "09:29:00");
        sdPrint(sdTime, "09:30:00");
        sdPrint(sdTime, "09:30:01");
        sdPrint(sdTime, "09:30:02");
        sdPrint(sdTime, "09:30:03");
        sdPrint(sdTime, "09:30:06");
        sdPrint(sdTime, "11:30:00");
        sdPrint(sdTime, "11:30:01");
        sdPrint(sdTime, "11:30:02");
        sdPrint(sdTime, "11:30:03");
        sdPrint(sdTime, "11:31:01");
        sdPrint(sdTime, "12:30:00");
        sdPrint(sdTime, "12:59:59");
        sdPrint(sdTime, "13:00:00");
        sdPrint(sdTime, "13:00:01");
        sdPrint(sdTime, "13:00:02");
        sdPrint(sdTime, "13:00:03");
        sdPrint(sdTime, "15:00:00");
        sdPrint(sdTime, "15:00:01");
        sdPrint(sdTime, "15:00:02");
        sdPrint(sdTime, "15:00:03");
        sdPrint(sdTime, "15:10:03");

        int length = sdTime.getLength();
        System.out.format("length = %d\n", length);
    }
    public static void testSdTime(int interval) {
        System.out.format("\n testSdTime inerval=%d: \n", interval);

        SdTime sdTime = new SdTime(interval);
        sdTime.addRange("09:25:00", "09:25:00");
        sdTime.addRange("09:30:00", "11:30:00");
        sdTime.addRange("13:00:00", "15:00:00");

        sdTimePrint(sdTime);
    }
    public static void testAStockSdTime() {
        System.out.format("\n testAStockSdTime: \n");
        SdTime sdTime = new AStockSdTime();

        sdTimePrint(sdTime);
    }
    public static void testSdTime1() {
        System.out.format("\n testSdTime1: \n");
        SdTime1 sdTime = new SdTime1();

        sdTimePrint(sdTime);

        sdTimePrintEx(sdTime);
    }
    public static void sdTimePrintEx(SdTime1 sdTime) {
        sdPrintEx(sdTime, "20090105", "10:30:00");
        sdPrintEx(sdTime, "20090105", "15:00:00");
        sdPrintEx(sdTime, "20090105", "15:00:03");
        sdPrintEx(sdTime, "20090106", "09:25:00");
        sdPrintEx(sdTime, "20090106", "10:30:00");
        sdPrintEx(sdTime, "20090106", "15:00:00");
        sdPrintEx(sdTime, "20200110", "15:00:00");

        //invalid tradeDate
        //sdPrintEx(sdTime, "20200111", "15:00:00");
    }
    public static void sdPrintEx(SdTime1 sdTime, String tradeDate, String tradeTime) {
        int sd = sdTime.getAbs(tradeDate, tradeTime);

        long tp = sdTime.rgetAbs(sd);
        String rTradeDate = Time.getTimeYMD(tp, false);
        String rTradeTime = Time.getTimeHMS(tp);

        System.out.format("%s: %10s %10s %10d %8x %10s %10s\n", "sdPrintEx", 
                tradeDate, tradeTime, sd, tp, rTradeDate, rTradeTime);
    }

    public static void testWorkDates(String date0, String date1) {
        System.out.format("\n testWorkDates: \n");
        WorkDates dates = new WorkDates(date0, date1);
        String[] sTradeDates = dates.getAllDates();
        System.out.format("testWorkDates: length=%d\n", sTradeDates.length);
        System.out.format("testWorkDates: distance of (%s, %s)=%d\n", 
                date0, date1, dates.getDistance(date0, date1));
        /*
        for(int i=0; i<sTradeDates.length; i++) {
            System.out.format("%s\n", sTradeDates[i]);
        }
        */
    }
    public static void testStockDates(String date0, String date1) {
        System.out.format("\n testStockDates: \n");
        String stockCode="600030";
        StockDates dates = new StockDates(date0, date1, stockCode);
        String[] sTradeDates = dates.getAllDates();
        System.out.format("testStockDates: length=%d\n", sTradeDates.length);
        System.out.format("testStockDates: distance of (%s, %s)=%d\n", 
                date0, date1, dates.getDistance(date0, date1));
        /*
        for(int i=0; i<sTradeDates.length; i++) {
            System.out.format("%s\n", sTradeDates[i]);
        }
        */
    }

    public static void listStockDates(String stockCode, String date0, String date1) {
        System.out.format("\n listStockDates: \n");
        StockDates dates = new StockDates(date0, date1, stockCode);
        String date = dates.firstDate();
        while(date!=null) {
            System.out.format("%s\n", date);
            date = dates.nextDate(date);
        }
    }
    public static void main(String args[]) throws Exception {
        /*
        testWorkDates("20090101", "20191231");
        testStockDates("20090101", "20191231");

        testWorkDates("20200104", "20200108");
        testStockDates("20200104", "20200108");

        testWorkDates("20200101", "20200108");
        testStockDates("20200101", "20200108");
        */

        //testSettings();       

        /*
        testSdTime(1);       
        testSdTime(2);       
        testSdTime(3);       
        testAStockSdTime();       
        */
        testSdTime1();
        
        //listStockDates("600030", "20090101", "20200112");
    }



}
