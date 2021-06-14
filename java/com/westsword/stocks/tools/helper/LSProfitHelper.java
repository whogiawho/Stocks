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
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class LSProfitHelper {
    public static void get(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        checkOptions(newArgs, cmd);

        String stockCode = newArgs[0];
        String tradeDate = newArgs[1];
        String hms = newArgs[2];

        if(cmd.hasOption("m")||cmd.hasOption("s")) {
            getExtremePrice(stockCode, tradeDate, hms, cmd);
        } else {
            getExitTime(stockCode, tradeDate, hms, cmd);
        }
    }
    private static void checkOptions(String[] newArgs, CommandLine cmd) {
        if(newArgs.length!=3) {
            usage();
        }

        int cnt = 0;

        boolean sSet = cmd.hasOption("s");
        boolean mSet = cmd.hasOption("m");
        boolean pSet = cmd.hasOption("p");

        cnt = sSet? cnt+1: cnt;
        cnt = mSet? cnt+1: cnt;
        cnt = pSet? cnt+1: cnt;

        if(cnt!=1)
            usage();
    }

    private static void getExitTime(String stockCode, String tradeDate, String hms, CommandLine cmd) {
        TradeDates tradeDates = new TradeDates(stockCode);
        AmManager amm = new AmManager(stockCode, tradeDate);

        //long
        long inTp = Time.getSpecificTime(tradeDate, hms);
        String nextTradeDateN = tradeDates.lastDate();
        double targetRate = getTargetRate(cmd);
        int sTDistance = 0;
        StockDates stockDates = new StockDates(stockCode);
        int tradeType = Stock.TRADE_TYPE_LONG;
        AmRecord outItemL = amm.getTradeResult(inTp, nextTradeDateN, targetRate, 
                sTDistance, tradeType, stockDates, null);
        long outTpL = outItemL!=null? outItemL.hexTimePoint: -1;
        

        //short
        tradeType = Stock.TRADE_TYPE_SHORT;
        AmRecord outItemS = amm.getTradeResult(inTp, nextTradeDateN, targetRate, 
                sTDistance, tradeType, stockDates, null);
        long outTpS = outItemS!=null? outItemS.hexTimePoint: -1;

        //System.out.format("targetRate=%-8.3f nextTradeDateN=%s\n", targetRate, nextTradeDateN);
        System.out.format("%8x %8x\n", outTpL, outTpS);
    }
    private static void getExtremePrice(String stockCode, String tradeDate, String hms, CommandLine cmd) {
        TradeDates tradeDates = new TradeDates(stockCode);
        SdTime1 sdt = new SdTime1(stockCode);
        String[] sRet = getEndDateHMS(tradeDate, hms, cmd, tradeDates, sdt);
        String endDate = sRet[0];
        String endHMS = sRet[1];
        //System.out.format("start=%s,%s end=%s,%s\n", tradeDate, hms, endDate, endHMS);

        AmManager amm = new AmManager(stockCode, tradeDate, endDate);

        boolean bPrintOutTime = getOutTimeSwitch(cmd);
        if(!bPrintOutTime) {
            onlyProfit(tradeDate, hms, endDate, endHMS, amm);
        } else {
            profitWithOutTime(tradeDate, hms, endDate, endHMS, amm);
        }
    }
    private static void onlyProfit(String tradeDate, String hms, String endDate, String endHMS, AmManager amm) {
        double[] v = amm.getExtremePrice(tradeDate, hms, endDate, endHMS);
        double lInPrice = amm.getUpPrice(tradeDate, hms);
        double sInPrice = amm.getDownPrice(tradeDate, hms);

        System.out.format("%8.3f %8.3f\n", v[0]-lInPrice, sInPrice-v[1]);
    }
    private static void profitWithOutTime(String tradeDate, String hms, String endDate, String endHMS, AmManager amm) {
        long[] lOutTime = new long[2];
        double[] v = amm.getExtremePrice(tradeDate, hms, endDate, endHMS, lOutTime);
        double lInPrice = amm.getUpPrice(tradeDate, hms);
        double sInPrice = amm.getDownPrice(tradeDate, hms);

        System.out.format("%8.3f %8.3f %x %x\n", 
                v[0]-lInPrice, sInPrice-v[1], lOutTime[0], lOutTime[1]);
    }

    public static String[] getEndDateHMS(String tradeDate, String hms, CommandLine cmd, 
            TradeDates tradeDates, SdTime1 sdt) {
        String endDate = "";
        String endHMS = "";
        if(cmd.hasOption("m")) {
            int maxCycle = getMaxCycle(cmd, 1);
            endDate = tradeDates.nextDate(tradeDate, maxCycle);
            endHMS = AStockSdTime.getCloseQuotationTime();
        } else {
            int maxSdt = getMaxSdt(cmd, 60);
            long tp = Time.getSpecificTime(tradeDate, hms);
            int sd = sdt.getAbs(tp);
            sd += maxSdt;
            tp = sdt.rgetAbs(sd);
            endDate = Time.getTimeYMD(tp, false);
            endHMS = Time.getTimeHMS(tp, false);
        }

        String[] sRet = new String[2];
        sRet[0] = endDate;
        sRet[1] = endHMS;

        return sRet;
    }
    private static void usage() {
        System.err.println("usage: java AnalyzeTools getlsprofit [-mstp] stockCode tradeDate hms");
        System.err.println("  for<stockCode,tradeDate,hms> within maxCycle get its L&S maxProfit, or");
        System.err.println("    to get targetRate the least sdtime must be waited for or the exit time");
        System.err.println("       -m maxCycle        ; default 1 day");
        System.err.println("       -s sdtime          ; default 60");
        System.err.println("       -t                 ; print outTime; default off");
        System.err.println("       -p targetRate      ; the targetRate to achieve");

             String line = "  -m&-s&-p are mutually exclusive! at least one must be set";
        line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
        System.err.println(line);

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
        options.addOption("m", true,  "maxCycle to get L&S maxProfit; default 1");
        options.addOption("s", true,  "sdtime to get L&S maxProfit; default 60");
        options.addOption("t", false, "print outTime");
        options.addOption("p", true,  "delta profit");

        return options;
    }
    public static int getMaxCycle(CommandLine cmd, int defaultMaxCycle) {
        return CmdLineUtils.getInteger(cmd, "m", defaultMaxCycle);
    }
    public static int getMaxSdt(CommandLine cmd, int defaultSdt) {
        return CmdLineUtils.getInteger(cmd, "s", defaultSdt);
    }
    public static boolean getOutTimeSwitch(CommandLine cmd) {
        return CmdLineUtils.getBoolean(cmd, "t", false);
    }
    public static double getTargetRate(CommandLine cmd) {
        return CmdLineUtils.getDouble(cmd, "p", 0);
    }
}
