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

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;

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

}
