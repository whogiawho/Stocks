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
        int distance = SSUtils.getNearestDist(cmd);
        int tradeType = SSUtils.getTradeType(cmd);

        String tradeDate = newArgs[0];
        String sPair = newArgs[1];
        int maxCycle = new Integer(newArgs[2]);
        double targetRate = new Double(newArgs[3]);

        System.out.format("%8s %8.3f %8s %4d %4d %8s %8s %4d %8.3f\n",
                stockCode, threshold, startDate, distance, tradeType,
                tradeDate, sPair, maxCycle, targetRate);

        StockDates stockDates = new StockDates(stockCode);
        AmManager am = new AmManager(stockCode);
        SSUtils.getSimilarTradeDates(stockCode, sPair, threshold, startDate, tradeDate, am);
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
