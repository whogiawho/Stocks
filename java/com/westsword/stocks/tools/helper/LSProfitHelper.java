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
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class LSProfitHelper {
    public static void get(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            usage();
        }
        //both set is not allowed
        if(cmd.hasOption("s")&&cmd.hasOption("m")) {
            usage();
        }
        //none set is now allowed
        if(!cmd.hasOption("s")&&!cmd.hasOption("m")) {
            usage();
        }

        String stockCode = newArgs[0];
        String tradeDate = newArgs[1];
        String hms = newArgs[2];
        TradeDates tradeDates = new TradeDates(stockCode);

        String endDate = "";
        String endHMS = "";
        if(cmd.hasOption("m")) {
            int maxCycle = getMaxCycle(cmd, 1);
            endDate = tradeDates.nextDate(tradeDate, maxCycle);
            endHMS = AStockSdTime.getCloseQuotationTime();
        } else {
            int maxSdt = getMaxSdt(cmd, 60);
            SdTime1 sdt = new SdTime1(stockCode);
            long tp = Time.getSpecificTime(tradeDate, hms);
            int sd = sdt.getAbs(tp);
            sd += maxSdt;
            tp = sdt.rgetAbs(sd);
            endDate = Time.getTimeYMD(tp, false);
            endHMS = Time.getTimeHMS(tp, false);
        }
        //System.out.format("start=%s end=%s\n", tradeDate, endDate);

        AmManager amm = new AmManager(stockCode, tradeDate, endDate);
        double[] v = amm.getExtremePrice(tradeDate, hms, endDate, endHMS);
        double lInPrice = amm.getUpPrice(tradeDate, hms);
        double sInPrice = amm.getDownPrice(tradeDate, hms);

        System.out.format("%8.3f %8.3f\n", v[0]-lInPrice, sInPrice-v[1]);
    }

    private static void usage() {
        System.err.println("usage: java AnalyzeTools getlsprofit [-ms] stockCode tradeDate hms");
        System.err.println("  for<stockCode,tradeDate,hms> within maxCycle get its L&S maxProfit");
        System.err.println("       -m maxCycle        ; default 1 day");
        System.err.println("       -s sdtime          ; default 60");

             String line = "  -m&-s are exclusive! at least one must be set";
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

        return options;
    }
    public static int getMaxCycle(CommandLine cmd, int defaultMaxCycle) {
        return CmdLineUtils.getInteger(cmd, "m", defaultMaxCycle);
    }
    public static int getMaxSdt(CommandLine cmd, int defaultSdt) {
        return CmdLineUtils.getInteger(cmd, "s", defaultSdt);
    }
}
