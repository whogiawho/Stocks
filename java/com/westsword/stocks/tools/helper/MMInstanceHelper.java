 /*
 Copyright (C) 1989-2020 Free Software Foundation, Inc.
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

public class MMInstanceHelper {

    public void run(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<3) {
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

        //
        boolean bResetLog = SSUtils.getSwitchResetLog(cmd);
        boolean bLog2Files = SSUtils.getSwitchLog2File(cmd);
        boolean bStdout = SSUtils.getSwitchStdout(cmd);
        AmManager am = new AmManager(stockCode, startDate, true);
        StockDates stockDates = new StockDates(stockCode);

        String hmsList = newArgs[1];
        int maxCycle = Integer.valueOf(newArgs[2]);
        System.out.format("%8s %8s %8.2f %4d %4d %8s %8s %4d\n",
                stockCode, startDate, threshold, sTDistance, tradeType,
                tradeDate0, hmsList, maxCycle);

        MMInstance r = new MMInstance(stockCode, startDate, threshold, sTDistance, tradeType,
                tradeDate0, hmsList, maxCycle);
        r.run(am, stockDates, null,
                bLog2Files, bResetLog, bStdout);
    }



    public static void commonUsageInfo() {
        System.err.println("       -c stockCode;");
        System.err.println("       -d startDate;");
        System.err.println("       -h threshold;");
        System.err.println("       -t       0|1; nearest day to end trade session");
        System.err.println("       -s tradeType;");
    }
    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"mminstance [-cdhts] tradeDate hmsList maxCycle");
        System.err.println("       tradeDate   ; tradeDate>=startDate");
        commonUsageInfo();

        System.exit(-1);
    }

    public static Options getOptions() {
        Options options = new Options();
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
