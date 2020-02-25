package com.westsword.stocks.tools;

import java.io.*;
import java.util.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.ckpt.*;

import com.westsword.stocks.*;
import com.westsword.stocks.am.*;
import com.westsword.stocks.utils.*;
import com.westsword.stocks.tools.helper.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.util.Combinations;

public class GetSettings{
    public static void testSettings() {
        System.out.format("\n testSettings: \n");

        Settings s = new Settings();
        String stockCode = Settings.getStockCode();
        System.out.format("stockCode=%s\n", stockCode);
        String tradeDate = Settings.getTradeDate();
        System.out.format("tradeDate=%s\n", tradeDate);
    }
    public static void testMisc() {
        System.out.format("\n testMisc: \n");

        //test mkdir "a\b\c"
        String sDir = StockPaths.getDailyDir() + "a\\b\\c";
        System.out.format("mkdir %s\n", sDir);
        Utils.mkDir(sDir);
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
        WorkDates workDates = new WorkDates(date0, date1);
        String[] sTradeDates = workDates.getAllDates();
        System.out.format("testWorkDates: length=%d\n", sTradeDates.length);
        System.out.format("testWorkDates: distance of (%s, %s)=%d\n", 
                date0, date1, workDates.getDistance(date0, date1));
        /*
        for(int i=0; i<sTradeDates.length; i++) {
            System.out.format("%s\n", sTradeDates[i]);
        }
        */
    }
    public static void testStockDates(String stockCode, String date0, String date1) {
        System.out.format("\n testStockDates: \n");
        StockDates stockDates = new StockDates(date0, date1, stockCode);
        String[] sTradeDates = stockDates.getAllDates();
        System.out.format("testStockDates: length=%d\n", sTradeDates.length);
        System.out.format("testStockDates: distance of (%s, %s)=%d\n", 
                date0, date1, stockDates.getDistance(date0, date1));
        /*
        for(int i=0; i<sTradeDates.length; i++) {
            System.out.format("%s\n", sTradeDates[i]);
        }
        */
    }
    public static void testTradeDates(String stockCode, String date0, String date1) {
        System.out.format("\n testTradeDates: \n");
        TradeDates tradeDates = new TradeDates(stockCode, date0, date1);
        String date = tradeDates.firstDate();
        while(date!=null) {
            System.out.format("%s\n", date);
            date = tradeDates.nextDate(date);
        }
    }

    public static void testStockDatesDistance(String stockCode, int maxCycle) {
        System.out.format("\n testStockDatesDistance: \n");
        StockDates stockDates = new StockDates(stockCode);
        String date = stockDates.firstDate();
        while(date!=null) {
            String nextNDate = stockDates.nextDate(date, maxCycle);
            int distance = stockDates.getDistance(date, nextNDate);
            System.out.format("%8s %8s %8d %8d\n", date, nextNDate, maxCycle, distance);

            date = stockDates.nextDate(date);
        }
    }
    public static void listStockDates(String stockCode, String date0, String date1) {
        System.out.format("\n listStockDates: \n");
        StockDates stockDates = new StockDates(date0, date1, stockCode);
        String date = stockDates.firstDate();
        while(date!=null) {
            System.out.format("%s\n", date);
            date = stockDates.nextDate(date);
        }
    }
    private static void amcorrelPrint(AmManager m, 
            String tradeDate0, String tradeDate1, String startHMS, String endHMS) {
        double amcorrel = m.getAmCorrel(tradeDate0, tradeDate1, startHMS, endHMS);
        System.out.format("%s %s %s %s amcorrel=%-8.3f\n", 
                tradeDate0, tradeDate1, startHMS, endHMS, amcorrel);
    }
    public static void testAmManager(String stockCode) {
        System.out.format("\n testAmManager: \n");
        AmManager m = new AmManager(stockCode);

        amcorrelPrint(m, "20090115", "20090116", "09:30:00", "14:30:00");
        amcorrelPrint(m, "20090115", "20090119", "09:30:00", "14:30:00");
        amcorrelPrint(m, "20090115", "20090120", "09:30:00", "14:30:00");
        amcorrelPrint(m, "20090115", "20090121", "09:30:00", "14:30:00");
        amcorrelPrint(m, "20090115", "20090122", "09:30:00", "14:30:00");
        amcorrelPrint(m, "20090115", "20090123", "09:30:00", "14:30:00");
    }
    public static void testTreeMap0(String stockCode) {
        TreeMap<Double, Integer> tm = new TreeMap<Double, Integer>();
        
        Double d0 = Double.valueOf("1.0");
        Double d1 = Double.valueOf("1.0000000000000000000000000000000000001");
        tm.put(d0, 1);
        tm.put(d1, 2);

        System.out.format("tm.size=%d\n", tm.size());
    }
    public static void testTreeMap1(String stockCode) {
        TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
        map.put(0, 0);
        map.put(1, 1);
        map.put(6, 6);
        map.put(7, 7);

        NavigableMap<Integer, Integer> subMap = map.subMap(2, true, 5, true);
        System.out.format("subMap.size = %d\n", subMap.size());
    }

    public static void testTreeMap(String stockCode) {
        System.out.format("\n testTreeMap: \n");

        testTreeMap0(stockCode);
        testTreeMap1(stockCode);
    }
    public static void testCkpt(String stockCode) {
        System.out.format("\n testCkpt: \n");

        CheckPoint0 ckpt = new CheckPoint0();
        ckpt.print();
    }
    public static void testCombination(String stockCode) {
        System.out.format("\n testCombination: \n");

        CheckPoint0 ckpt = new CheckPoint0();
        Combinations c = new Combinations(5, 2);
        Iterator<int[]> itr = c.iterator();
        while(itr.hasNext()) {
            int[] e = itr.next();
            String hmsList = ckpt.getHMSList(e);
            System.out.format("%s %s\n", Arrays.toString(e), hmsList);
        }
    }
    public static void testTradeSumLoader(String stockCode) {
        System.out.format("\n testTradeSumLoader: \n");
        
        String sTradeSumFile = "d:\\stocks\\data\\similarStack\\600030\\20160108_0.90_T1L\\20160108_001_1.100.txt";
        TradeSumLoader l = new TradeSumLoader();
        ArrayList<TradeSum> list = new ArrayList<TradeSum>();
        l.load(sTradeSumFile, list);
    }
    public static void testTaskManager(String stockCode) {
        System.out.format("\n testTaskManager: \n");

        TaskManager m = new TaskManager();
        while(true) {
            m.run();
        }
    }
    public static void testCheck(String stockCode, String hmsList) {
        System.out.format("\n testCheck: \n");

        String sOut = SSUtils.checkHMSList(hmsList, 2)? "Pass": "Fail";
        System.out.format("%s %s\n", hmsList, sOut);
    }
    public static void testCalendar(String stockCode) {
        System.out.format("\n testCalendar: \n");
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        Locale lc = Locale.getDefault();
        System.out.format("TimeZone=%s Locale=%s\n", tz.toString(), lc.toString());

        cal = Utils.getCalendar();
        tz = cal.getTimeZone();
        lc = Locale.getDefault();
        System.out.format("TimeZone=%s Locale=%s\n", tz.toString(), lc.toString());
    }
    public static void testSystem(String stockCode) {
        System.out.format("\n testSystem: \n");

        String sOS = System.getProperty("os.name");
        System.out.format("sOS=%s\n", sOS);
    }

    public static void main(String args[]) throws Exception {
        String stockCode="600030";
        /*
        testWorkDates("20090101", "20191231");
        testStockDates(stockCode, "20090101", "20191231");

        testWorkDates("20200104", "20200108");
        testStockDates(stockCode, "20200104", "20200108");

        testWorkDates("20200101", "20200108");
        testStockDates(stockCode, "20200101", "20200108");
        */
        //testTradeDates(stockCode, "20090105", "20191231");
        //testTradeDates(stockCode, "20160108", "20191231");

        //testSettings();       
        testMisc();       

        /*
        testSdTime(1);       
        testSdTime(2);       
        testSdTime(3);       
        testAStockSdTime();       
        */
        //testSdTime1();

        //testCheck(stockCode, "111000_123000_143000_145000l");
        //testCheck(stockCode, "111000_123000_143000_145000f");
        //testCheck(stockCode, "111000_123000_143000_145000");
        //testCheck(stockCode, "111000_123000");
        //testCheck(stockCode, "111000");
        //testCalendar(stockCode);
        //testTreeMap(stockCode);
        //testSystem(stockCode);

        //testAmManager(stockCode);
        //testTreeMap(stockCode);
        //testCkpt(stockCode);
        //testCombination(stockCode);
        //testTradeSumLoader(stockCode);
        testTaskManager(stockCode);
        
        //listStockDates(stockCode, "20090101", "20200112");
        //testStockDatesDistance(stockCode, 10);
    }

}
