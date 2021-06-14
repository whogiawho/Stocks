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

import com.westsword.stocks.am.*;
import com.westsword.stocks.am.average.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.base.Utils;

public class AvgAmHelper {
    public static void list(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=1 && newArgs.length!=3 && newArgs.length!=2) {
            usage();
        }

        if(newArgs.length == 1) {
            String stockCode = newArgs[0];
            String sAvgAmDeltaFile = null;
            String sAvgAmOutDir = null;
            sAvgAmDeltaFile = AvgAmUtils.getAvgAmDeltaFile(cmd);
            sAvgAmOutDir = AvgAmUtils.getAvgAmOutDir(cmd);
            Utils.resetDir(sAvgAmOutDir);

            handleSingleFile(stockCode, sAvgAmDeltaFile, sAvgAmOutDir, cmd);
        } else {
            String stockCode = newArgs[0];
            String tradeDate = newArgs[1];
            String hms = null;
            if(newArgs.length==3) {
                hms = newArgs[2];
            }
            if(hms!=null) {
                handleSingleHMS(stockCode, tradeDate, hms, cmd);
            } else {
                //mkdir avgam 
                Utils.resetDir(StockPaths.getAvgAmDir(stockCode, tradeDate));
                Utils.resetDir(StockPaths.getAvgAmPngDir(stockCode, tradeDate));

                //make avgam for all sds of tradeDate
                handleSingleTradeDate(stockCode, tradeDate, cmd);
            }
        }
    }


    public static void handleSingleFile(String stockCode, String sAvgAmDeltaFile, String sAvgAmOutDir, 
            CommandLine cmd) {
        AmManager amm = new AmManager(stockCode);

        ArrayList<DeltaSimRecord> avrrList = DeltaSimRecord.getList(sAvgAmDeltaFile);
        for(int i=0; i<avrrList.size(); i++) {
            DeltaSimRecord dsr = avrrList.get(i);
            if(!stockCode.equals(dsr.stockCode)) {
                System.err.format("invalid item: %s %s %s\n", dsr.stockCode, dsr.tradeDate, dsr.hms);
                continue;
            }

            stockCode = dsr.stockCode;
            String tradeDate = dsr.tradeDate;
            String hms = dsr.hms;
            String sAvgAmFile = sAvgAmOutDir + "\\" + tradeDate + "." + hms + ".txt";

            handleSingleHMS(stockCode, tradeDate, hms, cmd, amm, sAvgAmFile);
        }
    }
    public static void handleSingleHMS(String stockCode, String tradeDate, String hms, CommandLine cmd, 
            AmManager amm, String sAvgAmFile) {
        int sdbw = AvgAmUtils.getBackwardSd(cmd);
        int minSkippedSD = AvgAmUtils.getMinimumSkipSd(cmd);
        int interval = AvgAmUtils.getInterval(cmd);

        if(amm == null)
            amm = AmManager.get(stockCode, tradeDate, hms, sdbw, null);

        SdTime1 sdt = new SdTime1(stockCode);
        long tp = Time.getSpecificTime(tradeDate, hms);
        int sd = sdt.getAbs(tp);
        System.err.format("%s_%s tp=%x, sd=%d am=%d\n", tradeDate, hms, tp, sd, amm.getAm(sd));

        AvgAmUtils.listAvgAm(sd, sdbw, minSkippedSD, interval,
                amm.getAmRecordMap(), false, sAvgAmFile);
    }


    public static void handleSingleTradeDate(String stockCode, String tradeDate, CommandLine cmd) {
        int sdbw = AvgAmUtils.getBackwardSd(cmd);
        int minSkippedSD = AvgAmUtils.getMinimumSkipSd(cmd);
        int interval = AvgAmUtils.getInterval(cmd);
        int step = AvgAmUtils.getStep(cmd);

        AmManager amm = AmManager.get(stockCode, tradeDate, AStockSdTime.getCallAuctionEndTime(), sdbw, null);
        TreeMap<Integer, AmRecord> amrMap = amm.getAmRecordMap();
        SdTime1 sdt = new SdTime1(stockCode);

        //loop all sds of tradeDate
        int startSd = sdt.getAbs(sdt.getCallAuctionEndTime(tradeDate));
        int endSd = sdt.getAbs(sdt.getCloseQuotationTime(tradeDate));
        for(int sd=startSd; sd<=endSd; sd+=step) {
            //convert sd to sAvgAmFile
            long tp = sdt.rgetAbs(sd);
            String hms = Time.getTimeHMS(tp, false);
            String sAvgAmFile = StockPaths.getAvgAmFile(stockCode, tradeDate, hms);

            AvgAmUtils.listAvgAm(sd, sdbw, minSkippedSD, interval,
                    amm.getAmRecordMap(), false, sAvgAmFile);
        }
    }
    public static void handleSingleHMS(String stockCode, String tradeDate, String hms, CommandLine cmd) {
        int sdbw = AvgAmUtils.getBackwardSd(cmd);
        int minSkippedSD = AvgAmUtils.getMinimumSkipSd(cmd);
        int interval = AvgAmUtils.getInterval(cmd);

        AmManager amm = AmManager.get(stockCode, tradeDate, hms, sdbw, null);

        SdTime1 sdt = new SdTime1(stockCode);
        long tp = Time.getSpecificTime(tradeDate, hms);
        int sd = sdt.getAbs(tp);
        System.err.format("%s_%s tp=%x, sd=%d am=%d\n", tradeDate, hms, tp, sd, amm.getAm(sd));

        AvgAmUtils.listAvgAm(sd, sdbw, minSkippedSD, interval,
                amm.getAmRecordMap(), true, null);
    } 


    private static void usage() {
        System.err.println("usage: java AnalyzeTools listavgams [-bmie] [stockCode] [tradeDate] [hms]");
        System.err.println("       3 kinds are considered:");
        System.err.println("         stockCode tradeDate hms");
        System.err.println("         stockCode tradeDate");
        System.err.println("         stockCode -f fSTH -d sOutDir");
        System.err.println("       only print but not write to file when hms is specified");
        System.err.println("       -b sdbw       ; at most sdbw shall be looked backward; default 1170");
        System.err.println("       -m mindist    ; default 60");
        System.err.println("       -i interval   ; default 1");
        System.err.println("       -e step       ; list avgam every step sd, and only effective when no hms;");
        System.err.println("       -f fSTH       ; a avgamdelta file listing all [stockCode,tradeDate,hms]");
        System.err.println("       -d sOutDir    ; a dir storing all avgam for all items of fSTH");
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
        options.addOption("b", true,  "at most sdtime shall be looked backward when calculating derivatives");
        options.addOption("m", true,  "minimum skipped sd distance from current time");
        options.addOption("i", true,  "the step to get am derivative");
        options.addOption("e", true,  "list avgam every step sd");
        options.addOption("f", true,  "the avgamdelta file");
        options.addOption("d", true,  "a dir");

        return options;
    }
}
