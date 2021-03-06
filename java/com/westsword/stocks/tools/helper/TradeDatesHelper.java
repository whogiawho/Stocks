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

import com.westsword.stocks.base.time.*;

public class TradeDatesHelper {
    public static void nextTradeDate(String args[]) {
        if(args.length !=3 && args.length != 4) {
            usageNext();
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        int nDays = 1;
        if(args.length == 4)
            nDays = Integer.valueOf(args[3]);
        StockDates stockDates = new StockDates(stockCode);

        String sNext = stockDates.nextDate(tradeDate, nDays, true);
        if(sNext==null)
            sNext = "";
        System.out.format("%s\n", sNext);
    }
    private static void usageNext() {
        System.err.println("usage: java AnalyzeTools nexttradedate stockCode tradeDate [nDays]");
        System.exit(-1);
    }




    public static void prevTradeDate(String args[]) {
        if(args.length !=3 && args.length != 4) {
            usagePrev();
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        int nDays = 1;
        if(args.length == 4)
            nDays = Integer.valueOf(args[3]);
        StockDates stockDates = new StockDates(stockCode);

        String sPrev = stockDates.prevDate(tradeDate, nDays, true);
        if(sPrev==null)
            sPrev= "";
        System.out.format("%s\n", sPrev);
    }
    private static void usagePrev() {
        System.err.println("usage: java AnalyzeTools prevtradedate stockCode tradeDate [nDays]");
        System.exit(-1);
    }
}
