package com.westsword.stocks.am;


import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.qr.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class AmManager {
    public final static boolean Buffering_TradeResult = Settings.getSwitch(Settings.BUFFERING_TRADERESULT);


    //all tradeDaes
    public AmManager(String stockCode) {
        this(stockCode, true);
    }
    public AmManager(String stockCode, boolean bParallelLoad) {
        this(stockCode, new TradeDates(stockCode).getAllDates(), bParallelLoad);
    }
    //tradeDates between [startDate, ]
    public AmManager(String stockCode, String startDate) {
        this(stockCode, startDate, true);
    }
    public AmManager(String stockCode, String startDate, boolean bParallelLoad) {
        this(stockCode, new TradeDates(stockCode, startDate).getAllDates(), bParallelLoad);
    }
    //tradeDates between [startDate, endDate]
    public AmManager(String stockCode, String startDate, String endDate) {
        this(stockCode, startDate, endDate, true);
    }
    public AmManager(String stockCode, String startDate, String endDate, boolean bParallelLoad) {
        this(stockCode, new TradeDates(stockCode, startDate, endDate).getAllDates(), bParallelLoad);
    }
    //tradeDates[]
    public AmManager(String stockCode, String[] tradeDates, boolean bParallelLoad) {
        mStockCode = stockCode;
        mSdTime = new SdTime1(stockCode);
        mTradeDates = new TradeDates(stockCode);
        mAmRecordMap = new TreeMap<Integer, AmRecord>();
        mAmrTable = new AmrHashtable();

        //for LoopWay, mAmrTable is useless
        if(Settings.getWay2SearchAmRecord()==0)
            mAmrTable = null;
        if(bParallelLoad)
            parallelLoad(mAmRecordMap, mAmrTable, tradeDates);
        else
            load(mAmRecordMap, mAmrTable, tradeDates);
    }
    public AmManager(String stockCode, String[] tradeDates) {
        this(stockCode, tradeDates, true);
    }
    public AmManager(String stockCode, ArrayList<String> tradeDateList) {
        this(stockCode, tradeDateList.toArray(new String[0]));
    }


    public NavigableMap<Integer, AmRecord> getItemMap(String startDate, String startHMS,
            String endDate, String endHMS) {
        return AmUtils.getItemMap(mAmRecordMap, mSdTime, startDate, startHMS, endDate, endHMS);
    }
    public double getInPrice(int tradeType, long inTime) {
        AmRecord r = getFloorItem(inTime);
        return r.getInPrice(tradeType);
    }
    public double getUpPrice(long inTime) {
        return getInPrice(Stock.TRADE_TYPE_UP, inTime);
    }
    public double getDownPrice(long inTime) {
        return getInPrice(Stock.TRADE_TYPE_DOWN, inTime);
    }
    public double getOutPrice(int tradeType, long outTime) {
        AmRecord r = getFloorItem(outTime);
        return r.getOutPrice(tradeType);
    }
    public double getOutPrice(int tradeType, int sdt) {
        AmRecord r = getFloorItem(sdt);
        return r.getOutPrice(tradeType);
    }
    public AmRecord getFloorItem(int sdt) {
        sdt = mAmRecordMap.floorKey(sdt);
        return mAmRecordMap.get(sdt);
    }
    public AmRecord getFloorItem(long tp) {
        int idx = mSdTime.getAbs(tp);
        return getFloorItem(idx);
    }
    public AmRecord getCeilingItem(long tp) {
        int idx = mSdTime.getAbs(tp);
        idx = mAmRecordMap.ceilingKey(idx);
        return mAmRecordMap.get(idx);
    }
    public AmRecord getTargetAmRecord(int startSdt, int tradeType, int sdLength, double targetProfit) {
        AmRecord r = null;

        AmRecord inR = mAmRecordMap.get(startSdt);
        if(inR == null)
            return r;

        double inPrice = inR.getInPrice(tradeType);
        for(int i=startSdt; i<startSdt+sdLength; i++) {
            AmRecord ar = mAmRecordMap.get(i);
            if(ar == null)
                break;

            if(tradeType == Stock.TRADE_TYPE_LONG && ar.getOutPrice(tradeType)-inPrice>=targetProfit || 
                    tradeType == Stock.TRADE_TYPE_SHORT && inPrice-ar.getOutPrice(tradeType)>=targetProfit) {
                r = ar;
                break;
            }
        }

        return r;
    }
    public AmRecord last() {
        return mAmRecordMap.get(mAmRecordMap.lastKey());
    }


    //
    public double[][] getAmMatrix(String hmsList, String[] sTradeDates) {
        int w = sTradeDates.length;

        String[] fields = hmsList.split("_");
        String startHMS = fields[0];
        String endHMS = fields[1];
        int start = mSdTime.get(startHMS);
        int end = mSdTime.get(endHMS);
        int h = end-start+1;

        double[][] m = new double[h][w];
        //set values to m
        for(int i=0; i<w; i++) {
            String tradeDate = sTradeDates[i];
            start = mSdTime.getAbs(tradeDate, startHMS);
            for(int j=0; j<h; j++) {
                AmRecord r = mAmRecordMap.get(start+j);
                m[j][i] = (double)r.am;
            }
        }

        return m;
    }
    //
    public double[][] getAmMatrix(ArrayList<QualRange> qrList) {
        double[][] m = null;

        if(qrList!=null) {
            int w = qrList.size();
            int h = qrList.get(0).getSdLength();
            m = new double[h][w];

            for(int i=0; i<w; i++) {
                QualRange qr = qrList.get(i);
                int start = qr.getStartSdt(mSdTime);
                for(int j=0; j<h; j++) {
                    AmRecord r = mAmRecordMap.get(start+j);
                    m[j][i] = (double)r.am;
                }
            }
        }

        return m;
    }


    //out[0] - maxNetRevenue
    public double maxDelta(long inTime, String nextTradeDateN, int sTDistance, int tradeType, 
            StockDates stockDates, double[] out) {
        double maxDelta = 0.0;

        double maxPosPrice = mAmrTable.getMaxPosPrice(inTime, tradeType,
                    nextTradeDateN, sTDistance, stockDates);
        double inPrice = getInPrice(tradeType, inTime);

        if(tradeType == Stock.TRADE_TYPE_LONG) {
            maxDelta = maxPosPrice - inPrice;
            out[0] = Trade.getNetProfit(inPrice, maxPosPrice);
        } else {
            maxDelta = inPrice - maxPosPrice;
            out[0] = Trade.getNetProfit(maxPosPrice, inPrice);
        }

        return maxDelta;
    }
    //replace Regression.getTradeResult; 
    //Two ways to return out[]: 
    //  always NaN
    //  just as Regression.getTradeResult
    //out[0] - maxDeltaPriceBias
    //out[1] - maxPosPrice, most optimistic
    //out[2] - riskDelta, risk if not achieving targetRate
    public AmRecord getTradeResult(long inTime, String nextTradeDateN, double targetRate, 
            int sTDistance, int tradeType, StockDates stockDates, double[] out) {
        if(Buffering_TradeResult) {
            TRBufR r = mTrBufMap.get(inTime);
            if(r!=null) {
                //System.out.format("%s: %x already in mTrBufMap size=%d!\n", 
                //        Utils.getCallerName(getClass()), inTime, mTrBufMap.size());
                out[0] = r.maxDeltaPriceBias;
                out[1] = r.maxPosPrice;
                out[2] = r.riskDelta;
                return r.outItem;
            }
        }

        AmRecord outItem = _getTradeResult(inTime, nextTradeDateN, targetRate,
                sTDistance, tradeType, stockDates, out);

        if(Buffering_TradeResult)
            mTrBufMap.put(inTime, new TRBufR(outItem, out[0], out[1], out[2]));

        return outItem;
    }
    public AmRecord _getTradeResult(long inTime, String nextTradeDateN, double targetRate, 
            int sTDistance, int tradeType, StockDates stockDates, double[] out) {
        double inPrice = getInPrice(tradeType, inTime);
        double targetProfit = Trade.getTargetProfit(targetRate, inPrice);
        double outPrice = Utils.getOutPrice(inPrice, targetProfit, tradeType);

        AmRecord outItem = mAmrTable.getOutItem(inTime, nextTradeDateN, outPrice, 
                sTDistance, tradeType, stockDates);
        if(out!=null) {
            out[0] = mAmrTable.getMaxDeltaPriceBias(inTime, tradeType, 
                    nextTradeDateN, stockDates, outItem, this);
            out[1] = mAmrTable.getMaxPosPrice(inTime, tradeType,
                    nextTradeDateN, sTDistance, stockDates);
            out[2] = getRiskDelta(inTime, tradeType, nextTradeDateN);
        }

        return outItem;
    }
    public double getRiskDelta(long inTime, int tradeType, String nextTradeDateN) {
        double riskDelta = Double.NaN;

        double inPrice = getInPrice(tradeType, inTime);
        String hms = Time.getTimeHMS(inTime);
        long lastTp = Time.getSpecificTime(nextTradeDateN, hms);
        double oPrice = getOutPrice(tradeType, lastTp);
        if(tradeType == Stock.TRADE_TYPE_LONG) {
            riskDelta =  oPrice - inPrice;
        } else {
            riskDelta = inPrice - oPrice;
        }

        return riskDelta;
    }


    public long getAm(String tradeDate, String hmsList) {
        long aM = 0;

        String[] fields = hmsList.split("_");
        String startHMS = fields[0];
        String endHMS = fields[1];

        NavigableMap<Integer, AmRecord> map = getItemMap(tradeDate, startHMS, tradeDate, endHMS);
        AmRecord first = map.firstEntry().getValue();
        AmRecord last = map.lastEntry().getValue();
        aM = last.am - first.am;

        return aM;
    }
    public double getAmCorrel(int startIdx0, int endIdx0, int startIdx1, int endIdx1) {
        double amCorrel = Double.NaN;

        NavigableMap<Integer, AmRecord> map0 = mAmRecordMap.subMap(startIdx0, true, endIdx0, true);
        NavigableMap<Integer, AmRecord> map1 = mAmRecordMap.subMap(startIdx1, true, endIdx1, true);

        int size0 = map0.size(); int expSize0 = endIdx0-startIdx0+1;
        int size1 = map1.size(); int expSize1 = endIdx1-startIdx1+1;
        if(!checkSizes(size0, expSize0, size1, expSize1, 
                    "", "", "", "",
                    "", "", "", ""))
            return amCorrel;

        return getAmCorrel(map0, map1);
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
        NavigableMap<Integer, AmRecord> map0 = mAmRecordMap.subMap(startIdx0, true, endIdx0, true);

        long startTp1 = Time.getSpecificTime(startDate1, startHMS1);
        long endTp1 = Time.getSpecificTime(endDate1, endHMS1);
        int startIdx1 = mSdTime.getAbs(startTp1);
        int endIdx1 = mSdTime.getAbs(endTp1);
        NavigableMap<Integer, AmRecord> map1 = mAmRecordMap.subMap(startIdx1, true, endIdx1, true);
              
        int size0 = map0.size(); int expSize0 = endIdx0-startIdx0+1;
        int size1 = map1.size(); int expSize1 = endIdx1-startIdx1+1;
        if(!checkSizes(size0, expSize0, size1, expSize1,
                    startDate0, startHMS0, endDate0, endHMS0,
                    startDate1, startHMS1, endDate1, endHMS1))
            return amCorrel;

        return getAmCorrel(map0, map1);
    }
    public double getAmCorrel(NavigableMap<Integer, AmRecord> map0, NavigableMap<Integer, AmRecord> map1) {
        int size0 = map0.size(); 
        int size1 = map1.size(); 
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
        //System.out.format("%d %d %s %s\n", size0, size1, Arrays.toString(x), Arrays.toString(y));
        
        return new PearsonsCorrelation().correlation(x, y);
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

    //2 key data structures to track AmRecords for a list of tradeDates
    private TreeMap<Integer, AmRecord> mAmRecordMap;
    private AmrHashtable mAmrTable;

    public String getStockCode() {
        return mStockCode;
    }
    public double getPriceAmplitude(String tradeDate, String startHMS, String endHMS) {
        /*
        long startTp = Time.getSpecificTime(tradeDate, startHMS);
        long endTp = Time.getSpecificTime(tradeDate, endHMS);
        int start = mSdTime.getAbs(startTp);
        int end = mSdTime.getAbs(endTp);
        NavigableMap<Integer, AmRecord> map = mAmRecordMap.subMap(start, true, end, true);
        */
        NavigableMap<Integer, AmRecord> map = getItemMap(tradeDate, startHMS, tradeDate, endHMS);

        double min=Double.POSITIVE_INFINITY, max=Double.NEGATIVE_INFINITY;
        for(AmRecord r: map.values()) {
            if(r.downPrice<min)
                min = r.downPrice;
            if(r.upPrice>max)
                max = r.upPrice;
        }
        

        return max-min;
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




    public static class AmrLoadThread extends Thread {
        private TreeMap<Integer, AmRecord> m0;
        private AmrHashtable t0;
        private String sAmRecordFile;
        public AmrLoadThread(TreeMap<Integer, AmRecord> m0, AmrHashtable t0, String sAmRecordFile) {
            this.m0 = m0;
            this.t0 = t0;
            this.sAmRecordFile = sAmRecordFile;
        }

        public void run() {
            AmRecordLoader amLoader = new AmRecordLoader();
            amLoader.load(null, m0, t0, sAmRecordFile);
        }
    }

    //parallel load
    //  rMap is TreeMap; AmrHashtable is composed of 3 TreeMap
    //  TreeMap is not thread-safe; so for each tradeDate's load, seperate TreeMap should be used
    //  And then they should be merged into one, that is <rMap, hTable>
    private void parallelLoad(TreeMap<Integer, AmRecord> rMap, AmrHashtable hTable, String[] sTradeDates) {
        int MaxLoadThread=5;
        int lN=0;
        AmrLoadThread[] tGroup = new AmrLoadThread[MaxLoadThread]; //loading threads
        AmrHashtable[] t0Group = new AmrHashtable[MaxLoadThread];  
        ArrayList<TreeMap<Integer, AmRecord>> m0Group = new ArrayList<TreeMap<Integer, AmRecord>>();
        for(int i=0; i<sTradeDates.length; i++) {
            String tradeDate = sTradeDates[i];
            String sAmRecordFile = StockPaths.getAnalysisFile(mStockCode, tradeDate);
            TreeMap<Integer, AmRecord> m0 = new TreeMap<Integer, AmRecord>();
            AmrHashtable t0 = new AmrHashtable();

            m0Group.add(m0);
            t0Group[lN]=t0;
            AmrLoadThread t = new AmrLoadThread(m0, t0, sAmRecordFile);
            tGroup[lN] = t;
            lN++;
            t.start();

            if(lN>=MaxLoadThread) {
                //wait until tGroup[] completes
                join(tGroup, lN);
                //merge m0Group&t0Group into rMap&hTable
                merge(m0Group, t0Group, rMap, hTable, lN);
                //reset lN, tGroup&m0Group&t0Group
                reset(tGroup, t0Group, m0Group, lN);
                lN=0;
            }
        }
        //wait&merge the last
        join(tGroup, lN);
        merge(m0Group, t0Group, rMap, hTable, lN);

        System.err.format("%s: loading complete\n", 
                Utils.getCallerName(getClass()));
    }
    private void reset(AmrLoadThread[] tGroup, AmrHashtable[] t0Group, 
            ArrayList<TreeMap<Integer, AmRecord>> m0Group, int lN) {
        for(int j=0; j<lN; j++) {
            tGroup[j]=null;
            t0Group[j]=null;
        }
        m0Group.clear();
    }
    private void merge(ArrayList<TreeMap<Integer, AmRecord>> m0Group, AmrHashtable[] t0Group, 
            TreeMap<Integer, AmRecord>rMap, AmrHashtable hTable, int lN) {
        for(int i=0; i<lN; i++) {
            TreeMap<Integer, AmRecord> m0 = m0Group.get(i);
            rMap.putAll(m0);
        }
        for(int i=0; i<lN; i++) {
            AmrHashtable t0 = t0Group[i];
            hTable.merge(t0);
        }
    }
    private void join(AmrLoadThread[] tGroup, int last) {
        //wait until tGroup[] completes
        for(int j=0; j<last; j++) {
            try {
                tGroup[j].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void load(TreeMap<Integer, AmRecord> rMap, AmrHashtable hTable, String[] sTradeDates) {
        long tStart = PerformanceLog.start();

        AmRecordLoader amLoader = new AmRecordLoader();
        for(int i=0; i<sTradeDates.length; i++) {
            String tradeDate = sTradeDates[i];
            String sAmRecordFile = StockPaths.getAnalysisFile(mStockCode, tradeDate);
            amLoader.load(null, rMap, hTable, sAmRecordFile);
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





    //about mTrBufMap's key:
    //  now the key consists of only inTime
    //  it may consists of inTime(_nextTradeDateN_targetRate_sTDistance_tradeType) in the long run
    private ConcurrentHashMap<Long, TRBufR> mTrBufMap = new ConcurrentHashMap<Long, TRBufR>();

    //TRBufR = TradeResult Buf Record
    public static class TRBufR {
        public AmRecord outItem;
        double maxDeltaPriceBias;
        double maxPosPrice;
        double riskDelta;

        public TRBufR(AmRecord outItem, double maxDeltaPriceBias, double maxPosPrice, double riskDelta) {
            this.outItem = outItem;
            this.maxDeltaPriceBias = maxDeltaPriceBias;
            this.maxPosPrice = maxPosPrice;
            this.riskDelta = riskDelta;
        }
    }
}
