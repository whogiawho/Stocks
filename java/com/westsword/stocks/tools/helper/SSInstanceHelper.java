package com.westsword.stocks.tools.helper;

import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.tools.helper.man.*;

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
        if(!SSUtils.checkDates(startDate, tradeDate0)) {
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
        boolean bStdout = SSUtils.getSwitchStdout(cmd);
        AmManager am = new AmManager(stockCode, startDate, true);
        StockDates stockDates = new StockDates(stockCode);

        String hmsList = newArgs[1];
        int maxCycle = Integer.valueOf(newArgs[2]);
        System.out.format("%8s %8s %8.2f %4d %4d %8s %8s %4d %8.3f\n",
                stockCode, startDate, threshold, sTDistance, tradeType,
                tradeDate0, hmsList, maxCycle, targetRate);

        SSInstance r = new SSInstance(stockCode, startDate, threshold, sTDistance, tradeType,
                tradeDate0, hmsList, maxCycle, targetRate);
        r.run(am, stockDates, null,
                bLog2Files, bResetLog, bStdout);
    }



    public static void commonUsageInfo() {
        System.err.println("       -r          ; reset tradeDetails log file and tradeSum item");
        System.err.println("       -n          ; does not log to files");
        System.err.println("       -o          ; does not write message to stdout");
        System.err.println("       -c stockCode;");
        System.err.println("       -d startDate;");
        System.err.println("       -h threshold;");
        System.err.println("       -t       0|1; nearest day to end trade session");
        System.err.println("       -s tradeType;");
    }
    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssinstance [-rnocdhts] tradeDate hmsList maxCycle targetRate");
        System.err.println("       tradeDate   ; tradeDate>=startDate");
        System.err.println("       targetRate  ; something like [0-9]{1,}.[0-9]{1,3}");
        System.err.println("                       relative(<=1): targetRate"); 
        System.err.println("                       absolute(>1) : targetRate-1");
        commonUsageInfo();


        System.exit(-1);
    }

    public static Options getOptions() {
        Options options = new Options();
        options.addOption("r", false, "reset tradeDetails log file and tradeSum item");
        options.addOption("n", false, "does not log to files");
        options.addOption("o", false, "does not write message to stdout");
        options.addOption("c", true,  "a stock's code");
        options.addOption("d", true,  "a tradeDate from which a ss search is started");
        options.addOption("h", true,  "a threshold value to get ss for tradeDates");
        options.addOption("t", true,  "0|1; nearest tradeDate distance to end trade session");
        options.addOption("s", true,  "1|5; tradeType");

        return options;
    }
    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = getOptions();

            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }

}
