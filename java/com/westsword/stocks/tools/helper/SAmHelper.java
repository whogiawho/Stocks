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

import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.analyze.sam.*;

public class SAmHelper {


    public static void searchUsage(String cmdName, String sInstanceName) {
        System.err.println("usage: java AnalyzeTools " + cmdName + " [-mhse] stockCode tradeDate hms");
        System.err.println("  specific search those amderivatives like " + 
                "<" + sInstanceName + ">");
        System.err.println("       -m maxCycle        ; default 1");
        System.err.println("       -h threshold       ; default 0.8");
        System.err.println("       -s startDate       ; start date to begin search");
        System.err.println("       -e endDate         ; last date to end search");
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
        options.addOption("m", true,  "maxCycle to get maxProfit; default 1");
        options.addOption("h", true,  "a threshold to list similar amder hms or not");
        options.addOption("s", true,  "starDate to begin; default SdStartDate");
        options.addOption("e", true,  "endDate to end search; default lastTradeDate of stockCode");

        return options;
    }

    public static String getStartDate(CommandLine cmd, String defaultDate) {
        return CmdLineUtils.getString(cmd, "s", defaultDate);
    }
    public static String getEndDate(CommandLine cmd, String defaultDate) {
        return CmdLineUtils.getString(cmd, "e", defaultDate);
    }
    public static double getThreshold(CommandLine cmd, double defaultThreshold) {
        return CmdLineUtils.getDouble(cmd, "h", defaultThreshold);
    }
    public static int getMaxCycle(CommandLine cmd, int defaultMaxCycle) {
        return CmdLineUtils.getInteger(cmd, "m", defaultMaxCycle);
    }

}
