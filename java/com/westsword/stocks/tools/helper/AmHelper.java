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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class AmHelper {
    public static void getAm(String args[]) {
        if(args.length != 4) {
            usage();
            return;
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        String hmsList = args[3];

        StockDates stockDates = new StockDates(stockCode);
        String[] tradeDates = new String[] {
            tradeDate,
        };

        AmManager am = new AmManager(stockCode, tradeDates, true);
        long aM = am.getAm(tradeDate, hmsList);

        System.out.format("%d\n", aM);
    }

    private static void usage() {
        System.err.println("usage: java AnalyzeTools getam stockCode tradeDate hmsList");
        System.exit(-1);
    }



    public static void stdprice(String args[]) {
        if(args.length != 4) {
            stdUsage();
            return;
        }

        String stockCode = args[1];
        String startTradeDate = args[2];
        String endTradeDate = args[3];

        SdTime1 sdTime = new SdTime1(stockCode);

        AmManager am = new AmManager(stockCode, startTradeDate, endTradeDate);

        long startTp = sdTime.getCallAuctionEndTime(startTradeDate);
        long endTp = sdTime.getCloseQuotationTime(endTradeDate);

        int startSd = sdTime.getAbs(startTp);
        int endSd = sdTime.getAbs(endTp);
        System.out.format("startSd=%d, endSd=%d\n", startSd, endSd);

        double[] stdAMs = am.stdprice(startSd, endSd);
    }
    private static void stdUsage() {
        System.err.println("usage: java AnalyzeTools stdprice stockCode startTradeDate endTradeDate");
        System.exit(-1);
    }



    public static void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            searchUsage();
            return;
        }

        AmDerLoader l = new AmDerLoader();
        ArrayList<Double> amderList = new ArrayList<Double>();
        PearsonsCorrelation pc = new PearsonsCorrelation();

        //get src file amder series
        String stockCode = newArgs[0];
        String tradeDate = newArgs[1];
        String hms = newArgs[2];
        String sSrcDerivativeDir = StockPaths.getDerivativeDir(stockCode, tradeDate);
        String sSrcFile = sSrcDerivativeDir + hms + ".txt";
        l.load(amderList, sSrcFile);
        Double[] X = amderList.toArray(new Double[0]);
        double[] x = ArrayUtils.toPrimitive(X);

        //loop dst file amder series
        TradeDates tradeDates = new TradeDates(stockCode);
        String startDate = getStartDate(cmd, tradeDates.firstDate());
        String endDate = getEndDate(cmd, tradeDates.lastDate());
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
        for(int i=0; i<sTradeDates.length; i++) {
            String sDstTradeDate = sTradeDates[i];
            String sDstDerivativeDir = StockPaths.getDerivativeDir(stockCode, sDstTradeDate);

            String[] sFiles = Utils.getSubNames(sDstDerivativeDir);
            for(int j=0; j<sFiles.length; j++) {
                String sDstFile = sFiles[j];
                String hms1 = sDstFile.replace(".txt", "");
                sDstFile = sDstDerivativeDir + sDstFile;

                amderList.clear();
                l.load(amderList, sDstFile);
                Double[] Y = amderList.toArray(new Double[0]);
                double[] y = ArrayUtils.toPrimitive(Y);

                double correl = pc.correlation(x, y);
                System.out.format("%s,%s %s,%s %8.3f\n", 
                        tradeDate, hms, sDstTradeDate, hms1, correl);
            }
        }
    }
    private static void searchUsage() {
        System.err.println("usage: java AnalyzeTools searchsam [-hse] stockCode tradeDate hms");
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
}
