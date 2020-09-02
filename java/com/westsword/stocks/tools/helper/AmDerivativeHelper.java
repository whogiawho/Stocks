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
import org.apache.commons.math3.stat.regression.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.base.Utils;

public class AmDerivativeHelper {
    public static void list(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3 && newArgs.length!=2) {
            usage();
            return;
        }

        String stockCode = newArgs[0];
        String tradeDate = newArgs[1];
        String hms = null;
        if(newArgs.length==3) {
            hms = newArgs[2];
        }

        if(hms!=null) {
            handleSingle(stockCode, tradeDate, hms, cmd);
        } else {
            //mkdir derivative
            Utils.resetDir(StockPaths.getDerivativeDir(stockCode, tradeDate));

            //make am derivatives for all sds of tradeDate
            handleAll(stockCode, tradeDate, cmd);
        }
    }

    private static AmManager getAmManager(String stockCode, String tradeDate, int sdbw, String hms) {
        SdTime1 sdt = new SdTime1(stockCode);
        int sd = sdt.getAbs(tradeDate, hms);
        int sSd = sd - sdbw;
        long sTp = sdt.rgetAbs(sSd);
        String sDate = Time.getTimeYMD(sTp, false);
        String[] sTradates = new TradeDates(stockCode, sDate, tradeDate).getAllDates();
        //System.out.format("sTradates=%s\n", Arrays.toString(sTradates));

        AmManager amm = new AmManager(stockCode, sTradates);

        return amm;
    }
    public static void handleAll(String stockCode, String tradeDate, CommandLine cmd) {
        double r2Threshold = AmDerUtils.getR2Threshold(cmd);
        int sdbw = AmDerUtils.getBackwardSd(cmd);
        int minSkippedSD = AmDerUtils.getMinimumSkipSd(cmd);

        AmManager amm = getAmManager(stockCode, tradeDate, sdbw, AStockSdTime.getCallAuctionEndTime());

        //loop all sds of tradeDate
        SdTime1 sdt = new SdTime1(stockCode);
        int startSd = sdt.getAbs(sdt.getCallAuctionEndTime(tradeDate));
        int endSd = sdt.getAbs(sdt.getCloseQuotationTime(tradeDate));
        for(int sd=startSd; sd<=endSd; sd++) {
            //convert sd to sDerivativeFile
            long tp = sdt.rgetAbs(sd);
            String hms = Time.getTimeHMS(tp, false);
            String sDerivativeFile = StockPaths.getDerivativeFile(stockCode, tradeDate, hms);

            listSingleSd(sd, r2Threshold, sdbw, minSkippedSD,
                    amm, false, sDerivativeFile);
        }
    }
    public static void handleSingle(String stockCode, String tradeDate, String hms, CommandLine cmd) {
        double r2Threshold = AmDerUtils.getR2Threshold(cmd);
        int sdbw = AmDerUtils.getBackwardSd(cmd);
        int minSkippedSD = AmDerUtils.getMinimumSkipSd(cmd);

        AmManager amm = getAmManager(stockCode, tradeDate, sdbw, hms);

        SdTime1 sdt = new SdTime1(stockCode);
        long tp = Time.getSpecificTime(tradeDate, hms);
        int sd = sdt.getAbs(tp);
        System.err.format("%s_%s tp=%x, sd=%d am=%d\n", tradeDate, hms, tp, sd, amm.getAm(sd));

        listSingleSd(sd, r2Threshold, sdbw, minSkippedSD, 
                amm, true, null);
    } 
    public static void listSingleSd(int sd, double r2Threshold, int sdbw, int minSkippedSD, 
            AmManager amm, boolean bStdOut, String sDerivativeFile) {

        AmDerUtils.listSingleSd(sd, r2Threshold, sdbw, minSkippedSD,
                amm.getAmRecordMap(), bStdOut, sDerivativeFile);
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools listamderivatives [-bhm] stockCode tradeDate [hms]");
        System.err.println("       only print but not write to file when hms is specified");
        System.err.println("       -b sdbw       ; at most sdbw shall be looked backward; default 300");
        System.err.println("       -h r2Threshold; default 0.5");
        System.err.println("       -m mindist    ; default 5");
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
        options.addOption("h", true,  "a R2 threshold for effective derivative");
        options.addOption("m", true,  "minimum skipped sd distance from current time");

        return options;
    }




}
