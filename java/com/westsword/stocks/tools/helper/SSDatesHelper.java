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
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.StockPaths;
import com.westsword.stocks.base.utils.LineLoader;
import com.westsword.stocks.base.time.StockDates;
import com.westsword.stocks.base.ckpt.CheckPoint0;
import com.westsword.stocks.tools.helper.man.*;

public class SSDatesHelper {

    public void make(String[] args) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<1) {
            usage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        double threshold = SSUtils.getThreshold(cmd);

        String tradeDate0 = newArgs[0];

        StockDates stockDates = new StockDates(stockCode);
        String startDate = stockDates.firstDate();                    //always the first of StockDates 
        AmManager am = new AmManager(stockCode);

        String sDir = StockPaths.getSSDatesDir(stockCode, threshold, tradeDate0); 
        Utils.mkDir(sDir);

        SSDatesManager ssdm = new SSDatesManager();
        CheckPoint0 ckpt = new CheckPoint0();
        int length = ckpt.getLength();
        Combinations c = new Combinations(length-1, 2);     //exclude the last ckpt
        //loop hmsList combination(n,2)
        Iterator<int[]> itr = c.iterator();
        while(itr.hasNext()) {
            int[] e = itr.next();
            String hmsList = ckpt.getHMSList(e);                     //

            SSDates ssd = new SSDates(stockCode, startDate, threshold, tradeDate0, hmsList);
            ssdm.run(ssd, am);
        }
    }
    private static void usage() {
        System.err.println("usage: java AnalyzeTools makessdates [-ch] tradeDate");
        System.err.println("       list all similar tradeDates for the (tradeDate, hmsList)");
        System.err.println("       -c stockCode;");
        System.err.println("       -h threshold;");
        System.exit(-1);
    }
    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = new Options();
            options.addOption("c", true,  "a stock's code");
            options.addOption("h", true,  "a threshold value to get ss for tradeDates");
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }





    public void maxMatchSingle(String[] args) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<2) {
            maxmatchsingleUsage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        double threshold = SSUtils.getThreshold(cmd);

        String fTradeDates = newArgs[0];
        String hmsList = newArgs[1];

        LineLoader loader = new LineLoader();
        ArrayList<String> tradeDateList= new ArrayList<String>();
        loader.load(tradeDateList, fTradeDates);
        String[] sTradeDates = tradeDateList.toArray(new String[0]);
        AmManager am = new AmManager(stockCode, tradeDateList);

        CmManager cm = new CmManager();
        double[][] corrM =  cm.getCorrMatrix(sTradeDates, hmsList, am);

        String[] out = new String[2];
        int maxCount = getMaxMatchCount(corrM, threshold, sTradeDates, out);

        System.out.format("%d %s %s %s\n", maxCount, out[0], hmsList, out[1]);
    }
    private static int getMaxMatchCount(double[][] corrM, double threshold, String[] sTradeDates,
            String[] out) {
        String sMaxTradeDate="";
        String sMaxMatchTradeDates = "";
        int maxCount=0;
        for(int i=0; i<corrM.length; i++) {
            int count=0;
            String sMatchTradeDates = "";
            for(int j=0; j<corrM[i].length; j++) {
                if(corrM[i][j]>=threshold) {
                    count++;
                    sMatchTradeDates += sTradeDates[j] + " ";
                }
            }
            if(count>maxCount) {
                maxCount=count;
                sMaxTradeDate = sTradeDates[i];
                sMaxMatchTradeDates = sMatchTradeDates;
            }
        }

        sMaxMatchTradeDates = sMaxMatchTradeDates.trim();
        sMaxMatchTradeDates = sMaxMatchTradeDates.replaceAll(" ", ",");
        out[0] = sMaxTradeDate;
        out[1] = sMaxMatchTradeDates;

        return maxCount;
    }
    private static void maxmatchsingleUsage() {
        System.err.println("usage: java AnalyzeTools maxmatchsingle [-ch] fTradeDates hmsList");
        System.err.println("       find the tradeDate in fTradeDates which has max matches for hmsList");
        System.err.println("       -c stockCode;");
        System.err.println("       -h threshold;");
        System.exit(-1);
    }




    public void maxMatchAll(String[] args) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<1) {
            maxmatchallUsage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        double threshold = SSUtils.getThreshold(cmd);

        String fTradeDates = newArgs[0];

        LineLoader loader = new LineLoader();
        ArrayList<String> tradeDateList= new ArrayList<String>();
        loader.load(tradeDateList, fTradeDates);
        String[] sTradeDates = tradeDateList.toArray(new String[0]);
        AmManager am = new AmManager(stockCode, tradeDateList);

        CmManager cm = new CmManager();
        String[] out = new String[2];
        int maxCount=Integer.MIN_VALUE;
        String sMaxTradeDate="";
        String sMaxMatchTradeDates="";
        String sMaxHMSList="";

        CheckPoint0 ckpt = new CheckPoint0();
        int length = ckpt.getLength();
        Combinations c = new Combinations(length-1, 2);     //exclude the last ckpt
        //loop all hmsList combination(n,2)
        Iterator<int[]> itr = c.iterator();
        while(itr.hasNext()) {
            int[] e = itr.next();
            String hmsList = ckpt.getHMSList(e);                     //

            double[][] corrM =  cm.getCorrMatrix(sTradeDates, hmsList, am);
            int count = getMaxMatchCount(corrM, threshold, sTradeDates, out);
            if(count>maxCount) {
                maxCount = count;
                sMaxTradeDate = out[0];
                sMaxMatchTradeDates = out[1];
                sMaxHMSList = hmsList;
                System.out.format("%d %s %s %s\n", 
                        maxCount, sMaxTradeDate, sMaxHMSList, sMaxMatchTradeDates);
            }
        }

    }
    private static void maxmatchallUsage() {
        System.err.println("usage: java AnalyzeTools maxmatchall [-ch] fTradeDates");
        System.err.println("       find the tradeDate in fTradeDates which has max matches for all hmsList");
        System.err.println("       -c stockCode;");
        System.err.println("       -h threshold;");
        System.exit(-1);
    }
}

