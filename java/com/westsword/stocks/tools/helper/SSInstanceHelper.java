package com.westsword.stocks.tools.helper;

import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.Stock;
import com.westsword.stocks.Utils;
import com.westsword.stocks.utils.*;
import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.Regression;

public class SSInstanceHelper {

    public void run(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<4) {
            usage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        String startDate = SSUtils.getStartDate(cmd);
        double threshold = SSUtils.getThreshold(cmd);
        int sTDistance = SSUtils.getNearestOutDist(cmd);
        int tradeType = SSUtils.getTradeType(cmd);

        String tradeDate0 = newArgs[0];              //tradeDate0 must >= startDate
        if(!checkDates(startDate, tradeDate0)) {
            usage();
            return;
        }
        double targetRate = Double.valueOf(newArgs[3]);
        if(!SSUtils.checkTargetRate(newArgs[3])) {
            usage();
            return;
        }

        //
        boolean bResetLog = SSUtils.getSwitchResetLog(cmd);
        boolean bLog2Files = SSUtils.getSwitchLog2File(cmd);
        boolean bPrintTradeDetails = true;
        AmManager am = new AmManager(stockCode, startDate);
        StockDates stockDates = new StockDates(stockCode);

        String hmsList = newArgs[1];
        int maxCycle = Integer.valueOf(newArgs[2]);
        System.out.format("%8s %8s %8.2f %4d %4d %8s %8s %4d %8.3f\n",
                stockCode, startDate, threshold, sTDistance, tradeType,
                tradeDate0, hmsList, maxCycle, targetRate);

        _run(stockCode, startDate, threshold, sTDistance, tradeType,
                tradeDate0, hmsList, maxCycle, targetRate,
                am, stockDates, 
                bLog2Files, bResetLog, bPrintTradeDetails);
    }

    public void _run(String stockCode, String startDate, double threshold, int sTDistance, int tradeType,
            String tradeDate0, String hmsList, int maxCycle, double targetRate,
            AmManager am, StockDates stockDates, 
            boolean bLog2Files, boolean bResetLog, boolean bPrintTradeDetails) {
        String[] sPaths = getPaths(stockCode, startDate,
                threshold, tradeType, sTDistance,
                tradeDate0, hmsList, maxCycle, targetRate);
        // sPaths[0] - sTradeDetailsDir
        // sPaths[1] - sTradeDetailsFile
        // sPaths[2] - sTradeSumFile
        resetPaths(bLog2Files, sPaths[0], sPaths[1]);
        resetLogFile(bResetLog, hmsList, sPaths[1], sPaths[2]);

        BufR br = new BufR();
        __run(stockCode, startDate, threshold, sTDistance, tradeType,
                tradeDate0, hmsList, maxCycle, targetRate,
                am, stockDates, sPaths[1],
                bLog2Files, bPrintTradeDetails, br);

        //delete the tradeDetails log file by the filter criteria
        boolean bFilterTradeDetails = bLog2Files && filterIt(br);
        if(bFilterTradeDetails) {
            Utils.deleteFile(sPaths[1]);
        }

        boolean bLog2TradeSumFile = bLog2Files && !filterIt(br);
        writeTradeSumLog(tradeDate0, sPaths[2],
                hmsList, br, bLog2TradeSumFile);
    }
    public void __run(String stockCode, String startDate, double threshold, int sTDistance, int tradeType,
            String tradeDate0, String hmsList, int maxCycle, double targetRate,
            AmManager am, StockDates stockDates, String sTradeDetailsFile, 
            boolean bLog2Files, boolean bPrintTradeDetails, BufR br) {
        //
        String inHMS = SSUtils.getInHMS(hmsList);

        ArrayList<String> similarTradeDates = SSUtils.getSimilarTradeDates(stockCode, startDate, threshold, 
                tradeDate0, hmsList, am);

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

                writeTradeDetailsLog(stockDates, outTimeList.size()+1,
                        tradeDate1, tradeType, inHMS, br.netRevenue, outParms, 
                        bLog2Files, bPrintTradeDetails, sTradeDetailsFile);
            }

            //add outTime to outTimeList
            outTimeList.add(Long.valueOf(outParms[4]));
        }
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
    private static void writeTradeDetailsLog(StockDates stockDates, int currentHangCount,
            String tradeDate1, int tradeType, String inHMS, double netRevenue, String[] outParms, 
            boolean bLog2Files, boolean bPrintTradeDetails, String sTradeDetailsFile) {
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

        if(bPrintTradeDetails)
            System.out.format("%s", line);
        if(bLog2Files)
            Utils.append2File(sTradeDetailsFile, line);
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
    private static void writeTradeSumLog(String tradeDate, String sTradeSumFile,
            String hmsList, BufR br, boolean bLog2TradeSumFile) {
        double winRate, avgNetRevenue, avgMaxRevenue, expRisk0, expRisk1;
        int matchedCnt = br.getMatchedCount();
        if(matchedCnt!=0) {
            String sMatchedTradeDates = br.sMatchedTradeDates.trim();
            sMatchedTradeDates = sMatchedTradeDates.replaceAll(" ", ",");
            winRate = ((double)br.okCount)/matchedCnt;
            avgNetRevenue = br.netRevenue/matchedCnt;
            avgMaxRevenue = br.maxRevenue/matchedCnt;
            expRisk0 = br.failCount==0?Double.NaN:br.risk0/br.failCount;
            expRisk1 = br.okCount==0?Double.NaN:br.risk1/br.okCount;
            hmsList = hmsList.replaceAll(":", "");
            hmsList = hmsList.replaceAll(" ", "_");
            String sFormat = "%s %4d %8.0f%% %8.3f %8.3f %8.3f %8.3f %s %s %8.3f\n";
            String line = String.format(sFormat, 
                    tradeDate, matchedCnt, winRate*100, avgNetRevenue, avgMaxRevenue, expRisk0, expRisk1, 
                    sMatchedTradeDates, hmsList, br.netRevenue/br.maxHangCount);

            if(bLog2TradeSumFile)
                Utils.append2File(sTradeSumFile, line);
            System.out.format("%s", line);
        }
    }



    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = new Options();
            options.addOption("r", false, "reset tradeDetails log file and tradeSum item");
            options.addOption("n", false, "does not log to files");
            options.addOption("c", true,  "a stock's code");
            options.addOption("d", true,  "a tradeDate from which a ss search is started");
            options.addOption("h", true,  "a threshold value to get ss for tradeDates");
            options.addOption("t", true,  "0|1; nearest tradeDate distance to end trade session");
            options.addOption("s", true,  "1|5; tradeType");
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }

    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssinstance [-rncdhts] tradeDate hmsList maxCycle targetRate");
        System.err.println("       tradeDate   ; tradeDate>=startDate");
        System.err.println("       targetRate  ; something like [0-9]{1,}.[0-9]{1,3}");
        System.err.println("                       relative(<=1): targetRate"); 
        System.err.println("                       absolute(>1) : targetRate-1");
        System.err.println("       -r          ; reset tradeDetails log file and tradeSum item");
        System.err.println("       -n          ; does not log to files");
        System.err.println("       -c stockCode;");
        System.err.println("       -d startDate;");
        System.err.println("       -h threshold;");
        System.err.println("       -t       0|1; nearest day to end trade session");
        System.err.println("       -s tradeType;");
        System.exit(-1);
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


    // sPaths[0] - sTradeDetailsDir
    // sPaths[1] - sTradeDetailsFile
    // sPaths[2] - sTradeSumFile
    private static String[] getPaths(String stockCode, String startDate,
            double threshold, int tradeType, int sTDistance,
            String tradeDate, String hmsList, int maxCycle, double targetRate) {
        String[] sPaths = new String[3];

        sPaths[0] = StockPaths.getSSTradeDetailsLogDir(stockCode, startDate, 
                threshold, tradeType, sTDistance,
                tradeDate, maxCycle, targetRate);
        sPaths[1] = StockPaths.getSSTradeDetailsLogFile(stockCode, startDate,
                threshold, tradeType, sTDistance,
                tradeDate, hmsList, maxCycle, targetRate);
        sPaths[2] = StockPaths.getSSTradeSumLogFile(stockCode, startDate,
                threshold, tradeType, sTDistance,
                tradeDate, maxCycle, targetRate);

        return sPaths;
    }


    private static boolean filterIt(BufR br) {
        int matchedCnt = br.getMatchedCount();
        double avgNetRevenue = br.netRevenue/matchedCnt;
        double avgMaxRevenue = br.maxRevenue/matchedCnt;

        boolean bCond0 = avgNetRevenue <= SSUtils.MINIMUM_AVG_NET_REVENUE;
        boolean bCond1 = matchedCnt<40;

        return  bCond0||bCond1;
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
    private boolean checkDates(String startDate, String tradeDate) {
        boolean bCheck = true;
        if(tradeDate.compareTo(startDate)<0) {
            String line = String.format("tradeDate=%s < startDate=%s", tradeDate, startDate);
            line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
            System.out.format("%s\n", line);
            bCheck = false;
        }

        return bCheck;
    }
}
