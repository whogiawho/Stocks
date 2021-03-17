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
import com.westsword.stocks.base.utils.AnsiColor;
import com.westsword.stocks.analyze.ss.SSTableRecord;

public class SSTRInstanceHelper {

    public void run(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String sSSTableFile = SSUtils.getSSTableFile(cmd);
        String[] newArgs = cmd.getArgs();
        if(sSSTableFile==null&&newArgs.length<3||sSSTableFile!=null&&newArgs.length!=0) {
            usage();
            return;
        }

        if(sSSTableFile==null) {
            handleWoF(cmd, newArgs);
        } else {
            //assuming sSSTableFile shares one (stockCode,startDate)
            handleWF(sSSTableFile, cmd);
        }
    }
    private void handleWF(String sSSTableFile, CommandLine cmd) {
        boolean bStdout = SSUtils.getSwitchStdout(cmd);

        AmManager am = null;
        StockDates stockDates = null;

        ArrayList<SSTableRecord> list = SSUtils.getSSTableRecordList(sSSTableFile);
        for(int i=0; i<list.size(); i++) {
            SSTableRecord ts = list.get(i);

            String stockCode = ts.stockCode;
            String startDate = ts.startDate;
            double threshold = ts.threshold;
            int sTDistance = ts.sTDistance;
            int tradeType = ts.tradeType;
            int maxCycle = ts.maxCycle;
            double targetRate = ts.targetRate;
            String sMatchExpr = ts.sMatchExp;

            //skip checking targetRate, sMatchExpr's tradeDateList
            if(am == null) {
                am = new AmManager(stockCode, startDate, true);
            }
            if(stockDates == null) {
                stockDates = new StockDates(stockCode);
            }
            System.out.format("%8s %8s %8.2f %4d %4d %4d %8.3f %s\n",
                    stockCode, startDate, threshold, sTDistance, tradeType,
                    maxCycle, targetRate, sMatchExpr);

            SSTRInstance r = new SSTRInstance(stockCode, startDate, threshold, sTDistance, tradeType,
                    maxCycle, targetRate, sMatchExpr);
            r.run(am, stockDates, bStdout);
        }
    }
    private void handleWoF(CommandLine cmd, String[] newArgs) {
        String stockCode = SSUtils.getStockCode(cmd);
        String startDate = SSUtils.getStartDate(cmd);
        double threshold = SSUtils.getThreshold(cmd);
        int sTDistance = SSUtils.getNearestOutDist(cmd);
        int tradeType = SSUtils.getTradeType(cmd);

        double targetRate = Double.valueOf(newArgs[1]);
        if(!SSUtils.checkTargetRate(newArgs[1])) {
            usage();
            return;
        }

        //
        boolean bStdout = SSUtils.getSwitchStdout(cmd);
        AmManager am = new AmManager(stockCode, startDate, true);
        StockDates stockDates = new StockDates(stockCode);

        String sMatchExpr = newArgs[2];              //tradeDates(sMatchExpr) must >= startDate
        ArrayList<String> tradeDateList = SSTableRecord.getTradeDates(sMatchExpr);
        if(!SSUtils.checkDates(startDate, tradeDateList)) {
            usage();
            return;
        }
        int maxCycle = Integer.valueOf(newArgs[0]);
        System.out.format("%8s %8s %8.2f %4d %4d %4d %8.3f %s\n",
                stockCode, startDate, threshold, sTDistance, tradeType,
                maxCycle, targetRate, sMatchExpr);

        SSTRInstance r = new SSTRInstance(stockCode, startDate, threshold, sTDistance, tradeType,
                maxCycle, targetRate, sMatchExpr);
        r.run(am, stockDates, bStdout);
    }



    public static void commonUsageInfo() {
        System.err.println("       -o          ; does not write message to stdout");
        System.err.println("       -c stockCode;");
        System.err.println("       -d startDate;");
        System.err.println("       -h threshold;");
        System.err.println("       -t       0|1; nearest day to end trade session");
        System.err.println("       -s tradeType;");
    }
    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"sstrinstance [-ocdhtsf] maxCycle targetRate sMatchExpr");
        System.err.println("       tradeDates   ; tradeDates(sMatchExpr)>=startDate");
        System.err.println("       targetRate  ; something like [0-9]{1,}.[0-9]{1,3}");
        System.err.println("                       relative(<=MaxGrowthRate): targetRate"); 
        System.err.println("                       absolute(>MaxGrowthRate) : targetRate-MaxGrowthRate");

             String line = "       when enableing -f, [maxCycle,targetRate,sMatchExpr] is skipped!";
        line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
        System.err.println(line);
        commonUsageInfo();

        System.err.println("       -f sSSTableFile; sMatchExpr in the file; override options[cdhts]&parms");

        System.exit(-1);
    }

    public static Options getOptions() {
        Options options = new Options();
        options.addOption("o", false, "does not write message to stdout");
        options.addOption("c", true,  "a stock's code");
        options.addOption("d", true,  "a tradeDate from which a sstr search is started");
        options.addOption("h", true,  "a threshold value to get sstr for tradeDates");
        options.addOption("t", true,  "0|1; nearest tradeDate distance to end trade session");
        options.addOption("s", true,  "1|5; tradeType");

        return options;
    }
    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = getOptions();

            options.addOption("f", true,  "a ssTable file");
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }
}
