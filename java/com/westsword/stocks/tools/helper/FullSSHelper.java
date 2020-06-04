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
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.base.ckpt.*;
import com.westsword.stocks.tools.helper.man.*;
import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.StockDates;
import com.westsword.stocks.base.time.TradeDates;

public class FullSSHelper {
    public void run(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<2) {
            usage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        String startDate = SSUtils.getStartDate(cmd);
        double threshold = SSUtils.getThreshold(cmd);
        int sTDistance = SSUtils.getNearestOutDist(cmd);
        int tradeType = SSUtils.getTradeType(cmd);

        String sTradeDateList = SSUtils.getTradeDateList(cmd);
        String sTargetRateList = newArgs[1];
        if(!SSUtils.checkTargetRateList(sTargetRateList)) {
            usage();
            return;
        }

        //
        boolean bResetLog = SSUtils.getSwitchResetLog(cmd);
        boolean bLog2Files = SSUtils.getSwitchLog2File(cmd);
        boolean bStdout = SSUtils.getSwitchStdout(cmd);
        AmManager am = new AmManager(stockCode, startDate, true);
        StockDates stockDates = new StockDates(stockCode);

        String sMaxCycleList = newArgs[0];

        String[] fields0 = sMaxCycleList.split(" +");
        String[] fields1 = sTargetRateList.split(" +");
        CheckPoint0 ckpt = new CheckPoint0();
        int length = ckpt.getLength();
        Combinations c = new Combinations(length-1, 2);    //exclude the last ckpt

        SSiManager ssim = new SSiManager();
        TradeDates tradeDates = getTradeDates(stockCode, startDate, sTradeDateList);
        //loop tradeDates [startDate, lastTradeDate]
        String tradeDate0 = tradeDates.firstDate();
        while(tradeDate0!=null) {
            //loop hmsList combination(n,2)
            Iterator<int[]> itr = c.iterator();
            while(itr.hasNext()) {
                int[] e = itr.next();
                String hmsList = ckpt.getHMSList(e);                     //
                //loop maxCycleList
                for(int i=0; i<fields0.length; i++) {
                    int maxCycle = Integer.valueOf(fields0[i]);          //
                    //loop targetRateList
                    for(int j=0; j<fields1.length; j++) {
                        double targetRate = Double.valueOf(fields1[j]);  //

                       SSInstance r = new SSInstance(stockCode, startDate, threshold, sTDistance, tradeType,
                               tradeDate0, hmsList, maxCycle, targetRate);
                       ssim.run(r, am, stockDates,
                                bLog2Files, bResetLog, bStdout);
                    }
                }
            }
            tradeDate0 = tradeDates.nextDate(tradeDate0);                //
        }
    }


    //2 possibilities:
    //  only sTradeDateList
    //  [startDate, lastTradeDate]
    private TradeDates getTradeDates(String stockCode, String startDate, String sTradeDateList) {
        TradeDates tradeDates;
        if(sTradeDateList == null) {
            tradeDates = new TradeDates(stockCode, startDate);
        } else {
            String[] sTradeDates = sTradeDateList.split(" +");
            tradeDates = new TradeDates(stockCode, sTradeDates);
        }

        return tradeDates;
    }


    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"getfullss [-rnocdhtsl] maxCycleList targetRateList");
        System.err.println("       targetRateList  ; see ssinstance usage for details");
        SSInstanceHelper.commonUsageInfo();


        System.err.println("       -l tradeDateList;");
        System.exit(-1);
    }

    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = SSInstanceHelper.getOptions();

            options.addOption("l", true,  "tradeDate list");
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }
}
