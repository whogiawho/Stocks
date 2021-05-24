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

public class SaadStatsHelper {
    public static void get(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=0 && newArgs.length!=1) {
            usage();
        }

        //both set(m&s) is not allowed
        if(cmd.hasOption("s")&&cmd.hasOption("m")) {
            usage();
        }
        //none set(m&s) is now allowed
        if(!cmd.hasOption("s")&&!cmd.hasOption("m")) {
            usage();
        }

        String tsTriple = null;
        if(newArgs.length==1) {
            tsTriple = newArgs[0];
        }
        String sAvgAmDeltaFile = CmdLineUtils.getString(cmd, "f", null);
        if(tsTriple==null&&sAvgAmDeltaFile==null||tsTriple!=null&&sAvgAmDeltaFile!=null)
            usage();

        String sDir = CmdLineUtils.getString(cmd, "d", null);
        if(sDir==null)
            usage();
        //System.out.format("sAvgAmDeltaFile=%s sDir=%s\n", sAvgAmDeltaFile, sDir);

        if(tsTriple!=null) {
            String[] fields = tsTriple.split(",");
            String stockCode = fields[0];
            String tradeDate = fields[1];
            String hms = fields[2];
            handleSingle(stockCode, tradeDate, hms, cmd, false);
        } else {
            //get avgam delta for all sds of tradeDate
            handleAll(cmd);
        }
    }

    private static void handleSingle(String stockCode, String tradeDate, String hms, 
            CommandLine cmd, boolean bStats) {
        String sDir = CmdLineUtils.getString(cmd, "d", null);
        double threshold = CmdLineUtils.getDouble(cmd, "h", 0.9);

        String sAvgAm0 = sDir+"\\"+tradeDate+"."+hms+".correl";
        TradeDates tradeDates = new TradeDates(stockCode);
        SdTime1 sdt = new SdTime1(stockCode);

        double minmaxLProfit = Double.POSITIVE_INFINITY;
        double minmaxSProfit = Double.POSITIVE_INFINITY;
        int count=0, lCnt=0, sCnt=0;
        String sFormat = "%s %s %s " + 
            "%8.3f %8.3f %8.3f %8.3f " +  
            "%8.3f %8.3f\n"; 
        ArrayList<DeltaSimRecord> aarList = DeltaSimRecord.getList(sAvgAm0);
        for(int i=0; i<aarList.size(); i++) {
            DeltaSimRecord r = aarList.get(i);
            String sMatchedTradeDate = r.tradeDate;
            String sMatchedHMS = r.hms;
            String[] sRet = LSProfitHelper.getEndDateHMS(sMatchedTradeDate, sMatchedHMS, cmd, tradeDates, sdt);
            String endDate = sRet[0];
            String endHMS = sRet[1];

            if(r.correl1>=threshold) {
                //System.out.format("start=%s,%s end=%s,%s\n", sMatchedTradeDate, sMatchedHMS, endDate, endHMS);
                AmManager amm = new AmManager(stockCode, sMatchedTradeDate, endDate);
                double[] v = amm.getExtremePrice(sMatchedTradeDate, sMatchedHMS, endDate, endHMS);
                double lInPrice = amm.getUpPrice(sMatchedTradeDate, sMatchedHMS);
                double sInPrice = amm.getDownPrice(sMatchedTradeDate, sMatchedHMS);
                double lProfit = v[0]-lInPrice;
                double sProfit = sInPrice-v[1];
                lCnt = lProfit>0? lCnt+1 : lCnt;
                sCnt = sProfit>0? sCnt+1 : sCnt;
                minmaxLProfit = lProfit<minmaxLProfit? lProfit : minmaxLProfit;
                minmaxSProfit = sProfit<minmaxSProfit? sProfit : minmaxSProfit;
                count++;
                if(!bStats)
                    System.out.format(sFormat, 
                            r.stockCode, sMatchedTradeDate, sMatchedHMS, 
                            r.correl0, r.upPrice, r.downPrice, r.correl1, 
                            lProfit, sProfit);
            }
        }
        if(bStats) {
            double lWinRate = (double)lCnt/count;
            double sWinRate = (double)sCnt/count;
            System.out.format("%s %s %s %4d %8.3f %8.3f %8.3f %8.3f\n", 
                    stockCode, tradeDate, hms, count, lWinRate, sWinRate, minmaxLProfit, minmaxSProfit);
        }
    }
    private static void handleAll(CommandLine cmd) {
        String sAvgAmDeltaFile = CmdLineUtils.getString(cmd, "f", null);
        ArrayList<DeltaSimRecord> aarList = DeltaSimRecord.getList(sAvgAmDeltaFile);
        for(int i=0; i<aarList.size(); i++) {
            DeltaSimRecord r = aarList.get(i);
            String stockCode = r.stockCode;
            String tradeDate = r.tradeDate;
            String hms = r.hms;

            handleSingle(stockCode, tradeDate, hms, cmd, true);
        }
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools saadstats [-fdmsh] [stockCode,tradeDate,hms]");
        System.err.println("       loop avgamdeltaFile to get all stats or");
        System.err.println("         with a specified [stockCode,tradeDate,hms]");
        System.err.println("       -f avgamdeltaFile   ; the file generated from avgamdelta;");
        System.err.println("       -d dir              ; the dir where the avgamdelta res files are");
        System.err.println("       -m maxCycle         ; default 1 day");
        System.err.println("       -s sdtime           ; default 60");
        System.err.println("       -h threshold        ; default 0.9");

             String line = "       -d must be enabled! -f&[stockCode,tradeDate,hms] are exclusive";
        line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
        System.err.println(line);

                    line = "       -m&-s are exclusive! at least one must be set";
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
        options.addOption("f", true,  "the file generated from avgamdelta");
        options.addOption("d", true,  "the dir where the avgamdelta res files are");
        options.addOption("m", true,  "maxCycle to get L&S maxProfit; default 1");
        options.addOption("s", true,  "sdtime to get L&S maxProfit; default 60");
        options.addOption("h", true,  "default 0.9");

        return options;
    }
}
