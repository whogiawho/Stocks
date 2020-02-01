package com.westsword.stocks.tools.helper;

import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.StockDates;

public class SSInstanceHelper {
    public static void run(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<4) {
            usage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        double threshold = SSUtils.getThreshold(cmd);
        String startDate = SSUtils.getStartDate(cmd);
        int sTDistance = SSUtils.getNearestOutDist(cmd);
        int tradeType = SSUtils.getTradeType(cmd);

        String tradeDate = newArgs[0];
        String hmsList = newArgs[1];
        String inHMS = SSUtils.getInHMS(hmsList);
        int maxCycle = new Integer(newArgs[2]);
        double targetRate = new Double(newArgs[3]);

        System.out.format("%8s %8.3f %8s %4d %4d %8s %8s %4d %8.3f\n",
                stockCode, threshold, startDate, sTDistance, tradeType,
                tradeDate, hmsList, maxCycle, targetRate);

        StockDates stockDates = new StockDates(stockCode);
        AmManager am = new AmManager(stockCode);
        ArrayList<String> similarTradeDates = SSUtils.getSimilarTradeDates(stockCode, hmsList, threshold, startDate, tradeDate, am);
        for(int i=0; i<similarTradeDates.size(); i++) {
            String tradeDate1 = similarTradeDates.get(i);
            String nextTradeDateN = stockDates.nextDate(tradeDate1, maxCycle);
            int distance = stockDates.getDistance(tradeDate1, nextTradeDateN);

            //search to get tradeDate1 inHMS's inPrice stats for next N day
            String[] outParms = new String[8];
            getTradeParms(tradeDate1, inHMS, nextTradeDateN, 
                    sTDistance, tradeType, targetRate,
                    am, stockDates, outParms);
        }
    }


    public static void getTradeParms(String tradeDate1, String inHMS, String nextTradeDateN, 
            int sTDistance, int tradeType, double targetRate, 
            AmManager am, StockDates stockDates, String[] outParms) {
    }

    private static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = new Options();
            options.addOption("c", true,  "a stock's code");
            options.addOption("h", true,  "a threshold value to get ss for tradeDates");
            options.addOption("d", true,  "a tradeDate from which a ss search is started");
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
        System.err.println(sPrefix+"ssinstance [-chdts] tradeDate start,end maxCycle targetRate");
        System.err.println("       -c stockCode");
        System.err.println("       -h threshold");
        System.err.println("       -d startDate");
        System.err.println("       -t 0|1; nearest day to end trade session");
        System.err.println("       -s tradeType");
        System.exit(-1);
    }
}
