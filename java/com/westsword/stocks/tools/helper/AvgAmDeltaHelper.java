 /*
 Copyright (C) 2019-2050 WestSword, Inc.
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.  */
 
 /* Written by whogiawho <whogiawho@gmail.com>. */
 
 
package com.westsword.stocks.tools.helper;

import java.util.*;
import org.apache.commons.cli.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class AvgAmDeltaHelper {
    public static void get(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=2 && newArgs.length!=1) {
            usage();
            return;
        }

        String stockCode = newArgs[0];
        String tsPair = null;
        if(newArgs.length==2) {
            tsPair = newArgs[1];
        }

        if(tsPair!=null) {
            String[] fields = tsPair.split(",");
            String tradeDate = fields[0];
            String hms = fields[1];
            handleSingleHMS(stockCode, tradeDate, hms, cmd);
        } else {
            //get avgam delta for all sds of tradeDate
            handleAllHMS(stockCode, cmd);
        }
    }



    public static void handleAllHMS(String stockCode, CommandLine cmd) {
        int sdbw = AmDerUtils.getBackwardSd(cmd);
        int minSkippedSD = AmDerUtils.getMinimumSkipSd(cmd);

        TradeDates tradeDates = new TradeDates(stockCode);
        String startDate = CmdLineUtils.getString(cmd, "s", tradeDates.nextDate(tradeDates.firstDate()));
        String endDate = CmdLineUtils.getString(cmd, "e", tradeDates.lastDate());

        AmManager amm = new AmManager(stockCode, tradeDates.prevDate(startDate), endDate);
        TreeMap<Integer, AmRecord> amrMap = amm.getAmRecordMap();
        SdTime1 sdt = new SdTime1(stockCode);
        StockDates stockDates = new StockDates(stockCode);

        int startSd = sdt.getAbs(startDate, sdt.getCallAuctionEndTime());
        int endSd = sdt.getAbs(endDate, sdt.getCloseQuotationTime());

        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[] prevAvgAm = AmDerUtils.getAvgAm(startSd-1, sdbw, minSkippedSD, amrMap);
        for(int sd=startSd; sd<=endSd; sd++) {
            long hexTp = sdt.rgetAbs(sd);
            String tradeDate = Time.getTimeYMD(hexTp, false);
            String hms = Time.getTimeHMS(hexTp, false);

            double[] avgam = AmDerUtils.getAvgAm(sd, sdbw, minSkippedSD, amrMap);
            double correl = pc.correlation(prevAvgAm, avgam);
            System.out.format("%s %s %s %8.3f %8.3f\n", 
                    stockCode, tradeDate, hms, correl, amm.getUpPrice(tradeDate, hms));

            prevAvgAm = avgam;
        }
    }
    public static void handleSingleHMS(String stockCode, String tradeDate, String hms, CommandLine cmd) {
        int sdbw = AmDerUtils.getBackwardSd(cmd);
        int minSkippedSD = AmDerUtils.getMinimumSkipSd(cmd);

        SdTime1 sdt = new SdTime1(stockCode);
        long tp = Time.getSpecificTime(tradeDate, hms);
        int sd = sdt.getAbs(tp);
        AmManager amm = AmManager.get(stockCode, tradeDate, hms, sdbw+1, null);

        double[] avgam0 = AmDerUtils.getAvgAm(sd, sdbw, minSkippedSD, amm.getAmRecordMap());
        double[] avgam1 = AmDerUtils.getAvgAm(sd-1, sdbw, minSkippedSD, amm.getAmRecordMap());
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double correl = pc.correlation(avgam0, avgam1);

        System.out.format("%s %s %s %8.3f %8.3f\n", 
                stockCode, tradeDate, hms, correl, amm.getUpPrice(tradeDate, hms));
    } 


    private static void usage() {
        System.err.println("usage: java AnalyzeTools avgamdelta [-sebm] stockCode [tradeDate,hms]");
        System.err.println("       -s startDate  ; ");
        System.err.println("       -e endDate    ; ");
        System.err.println("       -b sdbw       ; at most sdbw shall be looked backward; default 300");
        System.err.println("       -m mindist    ; default 5");
        System.exit(-1);
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
    public static Options getOptions() {
        Options options = new Options();
        options.addOption("s", true,  "startDate");
        options.addOption("e", true,  "endDate");
        options.addOption("b", true,  "at most sdtime shall be looked backward when calculating derivatives");
        options.addOption("m", true,  "minimum skipped sd distance from current time");

        return options;
    }
}
