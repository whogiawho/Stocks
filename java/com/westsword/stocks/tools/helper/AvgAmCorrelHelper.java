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
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;

public class AvgAmCorrelHelper {
    public static void getCorrel(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=5) {
            usage();
            return;
        }

        String stockCode = newArgs[0];
        String tradeDate0 = newArgs[1];
        String hms0 = newArgs[2];
        String tradeDate1 = newArgs[3];
        String hms1 = newArgs[4];


        SdTime1 sdt = new SdTime1(stockCode);

        //part0
        double[] avgam0 = AvgAmUtils.getAvgAm(stockCode, tradeDate0, hms0, sdt, cmd);
        //part1
        double[] avgam1 = AvgAmUtils.getAvgAm(stockCode, tradeDate1, hms1, sdt, cmd);

        PearsonsCorrelation pc = new PearsonsCorrelation();
        double correl = pc.correlation(avgam0, avgam1);
        System.out.format("%8.3f\n", correl);
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools [-bmi] avgamcorrel stockCode tradeDate0 hms0 tradeDate1 hms1");
        System.err.println("       -b sdbw       ; at most sdbw shall be looked backward; default 300");
        System.err.println("       -m mindist    ; default 5");
        System.err.println("       -i interval   ; default 1");
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

        return options;
    }
}
