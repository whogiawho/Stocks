package com.westsword.stocks.am;

import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.utils.StockPaths;
import com.westsword.stocks.base.time.Time;
import com.westsword.stocks.base.time.SdTime1;
import com.westsword.stocks.base.time.TradeDates;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class AmManager {

    public AmManager(String stockCode) {
        mStockCode = stockCode;
        mSdTime = new SdTime1(stockCode);
        mTradeDates = new TradeDates(stockCode);

        mAmRecordMap = new TreeMap<Integer, AmRecord>();
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode);
        load(mAmRecordMap, sTradeDates);
    }

    public void load(TreeMap<Integer, AmRecord> rMap, String[] sTradeDates) {
        AmRecordLoader amLoader = new AmRecordLoader();
        for(int i=0; i<sTradeDates.length; i++) {
            String tradeDate = sTradeDates[i];
            String sAmRecordFile = StockPaths.getAnalysisFile(mStockCode, tradeDate);
            amLoader.load(null, rMap, sAmRecordFile);
            System.out.format("%s: loading %s complete\n", 
                    Utils.getCallerName(getClass()), tradeDate);
        }
    }

    //startDate0,startHMS0 endData0,endHMS0
    //startDate1,startHMS1 endDate1,endHMS1
    public double getAmCorrel(String startDate0, String startHMS0, String endData0, String endHMS0, 
            String startDate1, String startHMS1, String endDate1, String endHMS1) {
        double amCorrel = Double.NaN;

        long startTp0 = Time.getSpecificTime(startDate0, startHMS0);
        long endTp0 = Time.getSpecificTime(endData0, endHMS0);
        int startIdx0 = mSdTime.getAbs(startTp0);
        int endIdx0 = mSdTime.getAbs(endTp0);

        long startTp1 = Time.getSpecificTime(startDate1, startHMS1);
        long endTp1 = Time.getSpecificTime(endDate1, endHMS1);
        int startIdx1 = mSdTime.getAbs(startTp1);
        int endIdx1 = mSdTime.getAbs(endTp1);
              
        NavigableMap<Integer, AmRecord> map0 = mAmRecordMap.subMap(startIdx0, true, endIdx0, true);
        NavigableMap<Integer, AmRecord> map1 = mAmRecordMap.subMap(startIdx1, true, endIdx1, true);
        int size0 = map0.size();
        int size1 = map1.size();
        if(size0!=size1) {
            System.out.format("%s: map0.size(%d) != map1.size(%d)\n", 
                    Utils.getCallerName(getClass()), size0, size1);
            System.exit(1);
        }

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
            String nextTradeDate0 = mTradeDates.nextDate(tradeDate0);
            String nextTradeDate1 = mTradeDates.nextDate(tradeDate1);
            if(nextTradeDate0!=null && nextTradeDate1!=null) {
                amCorrel = getAmCorrel(tradeDate0, startHMS, nextTradeDate0, endHMS,
                        tradeDate1, startHMS, nextTradeDate1, endHMS);
            }
        }

        return amCorrel;
    }


    
    private String mStockCode;
    private SdTime1 mSdTime;
    private TradeDates mTradeDates;

    private TreeMap<Integer, AmRecord> mAmRecordMap;

    public String getStockCode() {
        return mStockCode;
    }
}
