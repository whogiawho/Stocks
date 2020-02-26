package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.Stock;
import com.westsword.stocks.Utils;
import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.StockPaths;
import com.westsword.stocks.analyze.Regression;

public class SSInstance {
    public String stockCode;
    public String startDate;
    public double threshold;
    public int sTDistance;
    public int tradeType;

    public String tradeDate;
    public String hmsList;                       //
    public int maxCycle;
    public double targetRate;

    public SSInstance(String stockCode, String startDate, double threshold, int sTDistance, int tradeType,
            String tradeDate, String hmsList, int maxCycle, double targetRate) {
        this.stockCode = stockCode;
        this.startDate = startDate;
        this.threshold = threshold;
        this.sTDistance = sTDistance;
        this.tradeType = tradeType;
        this.tradeDate = tradeDate;
        this.hmsList = hmsList;
        this.maxCycle = maxCycle;
        this.targetRate = targetRate;
    }

    


    public void run(AmManager am, StockDates stockDates, 
            boolean bLog2Files, boolean bResetLog, boolean bStdout) {
        String[] sPaths = getPaths();
        // sPaths[0] - sTradeDetailsDir
        // sPaths[1] - sTradeDetailsFile
        // sPaths[2] - sTradeSumFile
        resetPaths(bLog2Files, sPaths[0], sPaths[1]);
        resetLogFile(bResetLog, hmsList, sPaths[1], sPaths[2]);

        BufR br = new BufR();
        _run(am, stockDates, sPaths[1], br);

        //log tradeDetails by the filter criteria
        boolean bLogTradeDetails = bLog2Files && !filterIt(br);
        if(bLogTradeDetails) {
            Utils.append2File(sPaths[1], br.sTradeDetails);
        }
        //write tradeDetails to stdout
        if(bStdout)
            System.out.format("%s", br.sTradeDetails);

        //log tradeSum by the filter criteria
        boolean bLog2TradeSumFile = bLog2Files && !filterIt(br);
        TradeSumLogThread.write(tradeDate, sPaths[2],
                hmsList, br, bLog2TradeSumFile, bStdout);

        /*
        TradeSumLogThread t = new TradeSumLogThread(tradeDate, sPaths[2],
                hmsList, br, bLog2TradeSumFile, bStdout);
        t.start();
        */
    }
    public void _run(AmManager am, StockDates stockDates, String sTradeDetailsFile, BufR br) {
        //
        String inHMS = SSUtils.getInHMS(hmsList);

        ArrayList<String> similarTradeDates = SSUtils.getSimilarTradeDates(this, am);

        ArrayList<Long> outTimeList = new ArrayList<Long>();
        for(int i=0; i<similarTradeDates.size(); i++) {
            String tradeDate1 = similarTradeDates.get(i);
            String nextTradeDateN = stockDates.nextDate(tradeDate1, maxCycle);
            int distance = stockDates.getDistance(tradeDate1, nextTradeDateN);

            //search to get tradeDate1 inHMS's inPrice stats for next N day
            String[] outParms = new String[8];
            getTradeParms(tradeDate1, inHMS, nextTradeDateN, 
                    sTDistance, tradeType, targetRate,
                    am, stockDates, outParms);

            if(distance == maxCycle||outParms[0].equals("true")) {
                long inTime = Time.getSpecificTime(tradeDate1, inHMS);
                //remove those that are smaller than inTime from outTimeList, for getting maxHangCount
                removeQuitedItems(outTimeList, inTime);

                br.handleParms(outParms, tradeType, outTimeList.size()+1);
                br.sMatchedTradeDates += tradeDate1 + " ";

                br.sTradeDetails += getTradeDetailsLine(stockDates, outTimeList.size()+1,
                        tradeDate1, tradeType, inHMS, br.netRevenue, outParms);
            }

            //add outTime to outTimeList
            outTimeList.add(Long.valueOf(outParms[4]));
        }
    }


    public String getSSTradeSumLogFile() {
        return StockPaths.getSSTradeSumLogFile(stockCode, startDate, 
                threshold, tradeType, sTDistance,
                tradeDate, maxCycle, targetRate);
    }
    public String getSSTradeDetailsLogFile() {
        return StockPaths.getSSTradeDetailsLogFile(stockCode, startDate,
                threshold, tradeType, sTDistance,
                tradeDate, hmsList, maxCycle, targetRate);
    }
    public String getSSTradeDetailsLogDir() {
        return StockPaths.getSSTradeDetailsLogDir(stockCode, startDate, 
                threshold, tradeType, sTDistance,
                tradeDate, maxCycle, targetRate);
    }

    // sPaths[0] - sTradeDetailsDir
    // sPaths[1] - sTradeDetailsFile
    // sPaths[2] - sTradeSumFile
    private String[] getPaths() {
        String[] sPaths = new String[3];

        sPaths[0] = getSSTradeDetailsLogDir();
        sPaths[1] = getSSTradeDetailsLogFile();
        sPaths[2] = getSSTradeSumLogFile();

        return sPaths;
    }
    private void resetPaths(boolean bLog2Files, String sTradeDetailsDir, String sTradeDetailsFile) {
        if(bLog2Files) {
            //mkdir tradeDetailsLogDir
            Utils.mkDir(sTradeDetailsDir);
            //reset tradeDetailsLogFile
            Utils.deleteFile(sTradeDetailsFile);
        }
    }
    private void resetLogFile(boolean bResetLog, 
            String hmsList, String sTradeDetailsFile, String sTradeSumFile) {
        if(bResetLog) {
            //reset the tradeDetailsLogFile
            Utils.deleteFile(sTradeDetailsFile);

            //remove the item from tradeSumLogFile 
            TradeSumLoader l = new TradeSumLoader();
            String[] out = new String[1];
            l.load(sTradeSumFile, hmsList, out);
            //write sText back to tradeSum log file
            Utils.append2File(sTradeSumFile, out[0], false);
        }
    }

    private static boolean filterIt(BufR br) {
        int matchedCnt = br.getMatchedCount();
        double avgNetRevenue = br.netRevenue/matchedCnt;
        double avgMaxRevenue = br.maxRevenue/matchedCnt;

        boolean bCond0 = avgNetRevenue <= SSUtils.MINIMUM_AVG_NET_REVENUE;
        boolean bCond1 = matchedCnt<40;

        return  bCond0||bCond1;
    }
    //outParms[0] - bOK
    //outParms[1] - inPrice
    //outParms[2] - outPrice
    //outParms[3] - profit
    //outParms[4] - outTime
    //outParms[5] - maxPosPrice
    //outParms[6] - riskDelta
    //outParms[7] - maxDeltaPriceBias 
    //search to get tradeDate1 inHMS's stats(sTDistance, tradeType, targetRate) for next N day
    public static void getTradeParms(String tradeDate1, String inHMS, String nextTradeDateN, 
            int sTDistance, int tradeType, double targetRate, 
            AmManager am, StockDates stockDates, String[] outParms) {
        boolean bOK = true;

        long inTime = Time.getSpecificTime(tradeDate1, inHMS);
        double inPrice = am.getInPrice(tradeType, inTime);
        //t0 or t1
        NavigableMap<Integer, AmRecord> itemMap = getItemMap(am, stockDates, 
                tradeDate1, inHMS, nextTradeDateN, sTDistance);

        double[] out = new double[3];
        AmRecord outItem = Regression.getTradeResult(itemMap, 
                tradeType, inPrice, targetRate, out);
        if(outItem == null) {
            long outTime = Time.getSpecificTime(nextTradeDateN, AStockSdTime.getCloseQuotationTime());
            outItem = am.getFloorItem(outTime);
            bOK = false;
        }

        outParms[0] = "" + bOK;
        outParms[1] = "" + inPrice;
        outParms[2] = "" + outItem.getOutPrice(tradeType);
        outParms[3] = "" + outItem.getProfit(tradeType, inPrice);
        outParms[4] = "" + outItem.hexTimePoint;
        outParms[5] = "" + out[1];
        outParms[6] = "" + out[2];
        outParms[7] = "" + out[0];
    }
    private static String getTradeDetailsLine(StockDates stockDates, int currentHangCount,
            String tradeDate1, int tradeType, String inHMS, double netRevenue, String[] outParms) {
        String sFormat = "%s %s %s %s " + 
            "%8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %4d %4d\n";

        double inPrice = Double.valueOf(outParms[1]);
        double outPrice = Double.valueOf(outParms[2]);
        double profit = Double.valueOf(outParms[3]);
                    long outTime = Long.valueOf(outParms[4]);
                    String outDate = Time.getTimeYMD(outTime, false);
                    String outHMS = Time.getTimeHMS(outTime);
        double risk0 = Double.valueOf(outParms[6]);
        double risk1 = Double.valueOf(outParms[7]);
        double maxPosDelta = getMaxPosDelta(tradeType, inPrice, outParms);
        String line = String.format(sFormat,
                tradeDate1, inHMS, outDate, outHMS,  
                inPrice, outPrice, profit, netRevenue, maxPosDelta, risk0, risk1, 
                stockDates.getDistance(tradeDate1, outDate), currentHangCount);

        return line;
    }
    private static void removeQuitedItems(ArrayList<Long> outTimeList, long inTime) {
        ArrayList<Integer> removedIdxList = new ArrayList<Integer>();

        //System.out.format("outTimeList.size()=%d\n", outTimeList.size());
        for(int i=0; i<outTimeList.size(); i++) {
            Long outTime = outTimeList.get(i);
            if(inTime>outTime) {
                removedIdxList.add(i);
                //System.out.format("inTime=%x, outTime=%x\n", inTime, outTime);
            }
        }

        for(int i=removedIdxList.size()-1; i>=0; i--) {
            int idx = removedIdxList.get(i);
            outTimeList.remove(idx);
        } 
    }
    private static double getMaxRevenue(String[] outParms, int tradeType) {
        double buyPrice, sellPrice;
        if(tradeType == Stock.TRADE_TYPE_LONG) {
            buyPrice = Double.valueOf(outParms[1]);
            sellPrice = Double.valueOf(outParms[5]);
        }else {
            buyPrice = Double.valueOf(outParms[5]);
            sellPrice = Double.valueOf(outParms[1]);
        }
        double maxRevenue = Utils.getNetProfit(buyPrice, sellPrice);

        return maxRevenue;
    }
    private static double getMaxPosDelta(int tradeType, double inPrice, String[] outParms) {
        double maxPosDelta;

        double maxPosPrice = Double.valueOf(outParms[5]);
        if(tradeType == Stock.TRADE_TYPE_LONG)
            maxPosDelta =  maxPosPrice - inPrice;
        else
            maxPosDelta =  inPrice - maxPosPrice;

        return maxPosDelta;
    }
    private static NavigableMap<Integer, AmRecord> getItemMap(AmManager am, StockDates stockDates, 
            String tradeDate, String inHMS, String nextTradeDateN, int sTDistance) {
        NavigableMap<Integer, AmRecord> itemMap = new TreeMap<Integer, AmRecord>();

        if(sTDistance==0) {                                                        //t+0
            itemMap = am.getItemMap(tradeDate, inHMS, 
                    nextTradeDateN, AStockSdTime.getCloseQuotationTime());
        } else if(sTDistance==1) {                                                 //t+1
            String nextTradeDate1 = stockDates.nextDate(tradeDate);
            if(nextTradeDate1 != null) 
                itemMap = am.getItemMap(nextTradeDate1, AStockSdTime.getCallAuctionEndTime(), 
                        nextTradeDateN, AStockSdTime.getCloseQuotationTime());
        } else {
            String msg = String.format("%d is not a valid N for T+N!\n", sTDistance); 
            throw new RuntimeException(msg);
        }

        return itemMap;
    }


    public static class BufR {
        public double netRevenue=0.0;
        public double maxRevenue=0.0;
        public double risk0=0.0;
        public double risk1=0.0;
        public int okCount=0;
        public int failCount=0;
        public int maxHangCount=0;
        public String sMatchedTradeDates="";

        public String sTradeDetails = "";

        public void handleParms(String[] outParms, int tradeType, int currentHangCount) {
            if(outParms[0].equals("true")) {
                okCount++;
                risk1 += Double.valueOf(outParms[7]);//only consider those OK
            } else {
                failCount++;
                risk0 += Double.valueOf(outParms[6]);//only consider those Fail
            }
            netRevenue += Double.valueOf(outParms[3]);
            maxRevenue += getMaxRevenue(outParms, tradeType);
            if(currentHangCount>maxHangCount)
                maxHangCount=currentHangCount;
        }

        public int getMatchedCount() {
            return okCount+failCount;
        }
    }


}
