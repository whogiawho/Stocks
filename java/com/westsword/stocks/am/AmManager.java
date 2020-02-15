package com.westsword.stocks.am;

import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.utils.*;
import com.westsword.stocks.utils.StockPaths;
import com.westsword.stocks.base.time.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class AmManager {

    //all tradeDaes
    public AmManager(String stockCode) {
        this(stockCode, TradeDates.getTradeDateList(stockCode)[0]);
    }
    //tradeDates between [startDate, ]
    public AmManager(String stockCode, String startDate) {
        this(stockCode, TradeDates.getTradeDateList(stockCode, startDate));
    }
    //tradeDates[]
    public AmManager(String stockCode, String[] tradeDates) {
        mStockCode = stockCode;
        mSdTime = new SdTime1(stockCode);
        mStockDates = new StockDates(stockCode);
        mAmRecordMap = new TreeMap<Integer, AmRecord>();

        load(mAmRecordMap, tradeDates);
    }


    public void load(TreeMap<Integer, AmRecord> rMap, String[] sTradeDates) {
        long tStart = PerformanceLog.start();
        AmRecordLoader amLoader = new AmRecordLoader();
        for(int i=0; i<sTradeDates.length; i++) {
            String tradeDate = sTradeDates[i];
            String sAmRecordFile = StockPaths.getAnalysisFile(mStockCode, tradeDate);
            amLoader.load(null, rMap, sAmRecordFile);
            /*
            System.out.format("%s: loading %s complete\n", 
                    Utils.getCallerName(getClass()), tradeDate);
            */
        }
        System.err.format("%s: loading complete\n", 
                Utils.getCallerName(getClass()));
        PerformanceLog.end(tStart, "%s: loading analysis.txt = %d\n", 
                Utils.getCallerName(getClass()));
    }


    public NavigableMap<Integer, AmRecord> getItemMap(String startDate, String startHMS,
            String endDate, String endHMS) {
        long startTp = Time.getSpecificTime(startDate, startHMS);
        int startIdx = mSdTime.getAbs(startTp);
        long endTp = Time.getSpecificTime(endDate, endHMS);
        int endIdx = mSdTime.getAbs(endTp);

        NavigableMap<Integer, AmRecord> itemMap = mAmRecordMap.subMap(startIdx, true, endIdx, true);

        return itemMap;
    }
    public double getInPrice(int tradeType, long inTime) {
        AmRecord r = getFloorItem(inTime);
        return r.getInPrice(tradeType);
    }
    public AmRecord getFloorItem(long tp) {
        int idx = mSdTime.getAbs(tp);
        idx = mAmRecordMap.floorKey(idx);
        return mAmRecordMap.get(idx);
    }
    public AmRecord getCeilingItem(long tp) {
        int idx = mSdTime.getAbs(tp);
        idx = mAmRecordMap.ceilingKey(idx);
        return mAmRecordMap.get(idx);
    }

    //startDate0,startHMS0 endDate0,endHMS0
    //startDate1,startHMS1 endDate1,endHMS1
    public double getAmCorrel(String startDate0, String startHMS0, String endDate0, String endHMS0, 
            String startDate1, String startHMS1, String endDate1, String endHMS1) {
        double amCorrel = Double.NaN;


        long startTp0 = Time.getSpecificTime(startDate0, startHMS0);
        long endTp0 = Time.getSpecificTime(endDate0, endHMS0);
        int startIdx0 = mSdTime.getAbs(startTp0);
        int endIdx0 = mSdTime.getAbs(endTp0);

        long startTp1 = Time.getSpecificTime(startDate1, startHMS1);
        long endTp1 = Time.getSpecificTime(endDate1, endHMS1);
        int startIdx1 = mSdTime.getAbs(startTp1);
        int endIdx1 = mSdTime.getAbs(endTp1);
              
        NavigableMap<Integer, AmRecord> map0 = mAmRecordMap.subMap(startIdx0, true, endIdx0, true);
        NavigableMap<Integer, AmRecord> map1 = mAmRecordMap.subMap(startIdx1, true, endIdx1, true);
        int size0 = map0.size(); int expSize0 = endIdx0-startIdx0+1;
        int size1 = map1.size(); int expSize1 = endIdx1-startIdx1+1;
        if(!checkSizes(size0, expSize0, size1, expSize1,
                    startDate0, startHMS0, endDate0, endHMS0,
                    startDate1, startHMS1, endDate1, endHMS1))
            return amCorrel;

        double[] x = new double[size0];
        double[] y = new double[size1];
        int i=0,j=0;
        for(Integer key: map0.keySet()) {
            x[i] = map0.get(key).am;
            i++;
        }
        for(Integer key: map1.keySet()) {
            y[j] = map1.get(key).am;
            j++;
        }
        
        amCorrel = new PearsonsCorrelation().correlation(x, y);

        return amCorrel;
    }
    //support 2 kinds:
    //  startHMS<endHMS - f0&f1 is in one day
    //  startHMS>endHMS - f0&f1 is in 2 continuous days
    public double getAmCorrel(String tradeDate0, String tradeDate1, String startHMS, String endHMS) {
        double amCorrel = Double.NaN;

        if(startHMS.compareTo(endHMS)<0) {
            amCorrel = getAmCorrel(tradeDate0, startHMS, tradeDate0, endHMS,
                    tradeDate1, startHMS, tradeDate1, endHMS);
        } else {
            String nextTradeDate0 = mStockDates.nextDate(tradeDate0);
            String nextTradeDate1 = mStockDates.nextDate(tradeDate1);

            if(nextTradeDate0!=null && nextTradeDate1!=null) {
                amCorrel = getAmCorrel(tradeDate0, startHMS, nextTradeDate0, endHMS,
                        tradeDate1, startHMS, nextTradeDate1, endHMS);
            }
        }

        return amCorrel;
    }


    
    private String mStockCode;
    private SdTime1 mSdTime;
    private StockDates mStockDates;

    private TreeMap<Integer, AmRecord> mAmRecordMap;

    public String getStockCode() {
        return mStockCode;
    }


    private boolean checkSizes(int size0, int expSize0, int size1, int expSize1,
            String startDate0, String startHMS0, String endDate0, String endHMS0,
            String startDate1, String startHMS1, String endDate1, String endHMS1) {
        boolean bCheck = true;

        if(size0!=expSize0) {
            mismatchSize(size0, expSize0, 
                    startDate0, startHMS0, endDate0, endHMS0);
            return false;
        }
        if(size1!=expSize1) {
            mismatchSize(size1, expSize1, 
                    startDate1, startHMS1, endDate1, endHMS1);
            return false;
        }
        if(expSize0!=expSize1) {
            unequalExpSize(expSize0, expSize1, 
                    startDate0, startHMS0, endDate0, endHMS0,
                    startDate1, startHMS1, endDate1, endHMS1);
            return false;
        }

        return bCheck;
    }
    private void mismatchSize(int size, int expSize, 
            String startDate, String startHMS, String endDate, String endHMS) {
        String sSize = String.format("%d", size);
        sSize = AnsiColor.getColorString(sSize, AnsiColor.ANSI_RED);
        String sExpSize = String.format("%d", expSize);
        sExpSize = AnsiColor.getColorString(sExpSize, AnsiColor.ANSI_RED);

        System.out.format("%s: [%s_%s,%s_%s] size(%s)!=expSize(%s)\n", 
                Utils.getCallerName(getClass()), 
                startDate, startHMS, endDate, endHMS, sSize, sExpSize);
    }
    private void unequalExpSize(int size0, int size1, 
            String startDate0, String startHMS0, String endDate0, String endHMS0,
            String startDate1, String startHMS1, String endDate1, String endHMS1) {
        String sSize0 = String.format("%d", size0);
        sSize0 = AnsiColor.getColorString(sSize0, AnsiColor.ANSI_RED);
        String sSize1 = String.format("%d", size1);
        sSize1 = AnsiColor.getColorString(sSize1, AnsiColor.ANSI_RED);

        System.out.format("%s: [%s_%s,%s_%s].expSize(%s) != [%s_%s,%s_%s].expSize(%s)\n", 
                Utils.getCallerName(getClass()), 
                startDate0, startHMS0, endDate0, endHMS0, sSize0, 
                startDate1, startHMS1, endDate1, endHMS1, sSize1);
    }
}
