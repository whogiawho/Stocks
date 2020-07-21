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
import com.westsword.stocks.tools.helper.man.*;

public class SSIFilterHelper {

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

        String tradeDate0 = newArgs[0];              //tradeDate0 must >= startDate
        if(!SSUtils.checkDate(startDate, tradeDate0)) {
            usage();
            return;
        }

        //
        boolean bStdout = SSUtils.getSwitchStdout(cmd);
        AmManager am = new AmManager(stockCode, startDate, true);
        StockDates stockDates = new StockDates(stockCode);

        String hmsList = newArgs[1];
        int maxCycle = Integer.valueOf(newArgs[2]);
        System.out.format("%8s %8s %8.2f %8s %15s %4d\n",
                stockCode, startDate, threshold, tradeDate0, hmsList, maxCycle);

        String fTradeDetails = newArgs[3];
        //read fTradeDetails, and 
        //  put those matched tradeDates whose cycle<=maxCycle into set0 
        //  put those matched tradeDates whose cycle>maxCycle into set1 
        //loop all hmsLists before hmsList.endHMS to make similarSet, and intersect with set0
        //  only those with size>=40 are considered
        //then intersect with set1 
        //  only those whose sum of outPrice(maxCycle) are >0 are considered
    }



    public static void commonUsageInfo() {
        System.err.println("       -o          ; does not write message to stdout");
        System.err.println("       -c stockCode;");
        System.err.println("       -d startDate;");
        System.err.println("       -h threshold;");
    }
    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssifilter [-ocdh] tradeDate hmsList maxCycle fTradeDetails");
        commonUsageInfo();

        System.exit(-1);
    }

    public static Options getOptions() {
        Options options = new Options();
        options.addOption("o", false, "does not write message to stdout");
        options.addOption("c", true,  "a stock's code");
        options.addOption("d", true,  "a tradeDate from which a ss search is started");
        options.addOption("h", true,  "a threshold value to get ss for tradeDates");

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
