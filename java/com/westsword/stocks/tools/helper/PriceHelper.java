package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.AStockSdTime;

public class PriceHelper {
    public static void amplitude(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<1) {
            usage();
            return;
        }

        String startHMS = SSUtils.getStartHMSList(cmd);
        if(startHMS==null)
            startHMS=AStockSdTime.getCallAuctionEndTime();
        String endHMS = SSUtils.getEndHMSList(cmd);
        if(endHMS==null)
            endHMS=AStockSdTime.getCloseQuotationTime();

        String stockCode=newArgs[0];
        String tradeDate=newArgs[1];

        String[] sTradeDates = new String[] {
            tradeDate,
        };
        AmManager am = new AmManager(stockCode, sTradeDates, true);

        double amp = am.getPriceAmplitude(tradeDate, startHMS, endHMS); 

        System.out.format("%.3f\n", amp);
    }



    public static Options getOptions() {
        Options options = new Options();
        options.addOption("a", true,  "startHMS; HHMMSS");
        options.addOption("b", true,  "endHMS; HHMMSS");

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


    private static void usage() {
        System.err.println("usage: java AnalyzeTools priceamp [-ab] stockCode tradeDate");
        System.exit(-1);
    }
}
