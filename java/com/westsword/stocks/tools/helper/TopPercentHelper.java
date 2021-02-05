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

public class TopPercentHelper {

    public static void get(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=2) {
            usage();
        }
        if(!cmd.hasOption("h")&&!cmd.hasOption("p")) {
            usage();
        }
        if(cmd.hasOption("h")&&cmd.hasOption("p")) {
            usage();
        }

        int daysBackward = CmdLineUtils.getInteger(cmd, "b", 0);
        String endHMS = CmdLineUtils.getString(cmd, "e", "150000");
        int tradeType = CmdLineUtils.getInteger(cmd, "s", Stock.TRADE_TYPE_LONG);
        String stockCode = newArgs[0];
        String tradeDate = newArgs[1];

        //get price
        double price = Double.NaN;
        if(cmd.hasOption("h")) {
            String priceHMS = cmd.getOptionValue("h");
            //priceHMS<=endHMS
            if(priceHMS.compareTo(endHMS)>0)
                usage();
            //get price from priceHMS;
            AmManager amm = new AmManager(stockCode, tradeDate, tradeDate);
            long inTp = Time.getSpecificTime(tradeDate, priceHMS);
            price = amm.getInPrice(tradeType, inTp);
        } else {
            price = Double.valueOf(cmd.getOptionValue("p"));
        }

        //load pvTable
        PVTable pvTable = new PVTable(stockCode, tradeDate, endHMS, daysBackward);
        double topPercent = pvTable.getTopPercent(tradeType, price);
        System.out.format("%-8.3f %8.3f\n", topPercent, price);
    }

    private static void usage() {
        System.err.println("usage: java AnalyzeTools gettoppercent [-beshp] stockCode tradeDate");
        System.err.println("       -b daysBackward; default 0");
        System.err.println("       -e hms0        ; the HMS before which pvtable is got; default 150000");
        System.err.println("       -s tradeType   ; 1|5=Short|Long; default 5");
        System.err.println("       -h hms1        ; the HMS where the price is got; excluding to -p");
        System.err.println("       -p price       ; the price; exlucding to -h");

        String line = "  either -p or -h must be set; but only one of them\n"; 
        line += "  when -h is specified, hm1 must be <= hms0"; 
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
        options.addOption("b", true,  "at most days backward should be considered; default 0");
        options.addOption("e", true,  "the HMS before which is considered; default 150000");
        options.addOption("s", true,  "1|5; tradeType");
        options.addOption("h", true,  "the HMS where price is got");
        options.addOption("p", true,  "the price; -p&-h are excluding");

        return options;
    }
}
