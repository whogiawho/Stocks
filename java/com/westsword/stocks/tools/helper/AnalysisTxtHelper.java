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

public class AnalysisTxtHelper {
    public static void getRange(String args[]) {
        if(args.length != 4) {
            usage();
        }

        String stockCode = args[1];
        String startS = args[2];
        String endS = args[3];

        String[] fields = startS.split("_");
        String startDate = fields[0];
        String startHMS = fields[1];

        fields = endS.split("_");
        String endDate = fields[0];
        String endHMS = fields[1];

        SdTime1 sdTime = new SdTime1(stockCode);
        StockDates stockDates = new StockDates(stockCode);
        ArrayList<String> dates = new ArrayList<String>();
        String next = startDate;
        while(next!=null && next.compareTo(endDate)<=0) {
            //System.out.format("%s\n", next);
            dates.add(next);
            next = stockDates.nextDate(next);
        }

        AmManager am = new AmManager(stockCode, dates);
        NavigableMap<Integer, AmRecord> itemMap = am.getItemMap(startDate, startHMS, endDate, endHMS);
        for(Integer k: itemMap.keySet()) {
            AmRecord r = itemMap.get(k);
            r.print();
        }
    }
    private static void usage() {
        System.err.println("usage: java AnalyzeTools getanalysis stockCode sDate_sHMS eDate_eHMS ");
        System.exit(-1);
    }




    public static void getPrice(String args[]) {
        if(args.length != 4) {
            if(args[0].equals("getupprice"))
                usage2();
            else
                usage3();
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        String[] tradeDates = new String[] {
            tradeDate,
        };
        String hms = args[3];
        AmManager am = new AmManager(stockCode, tradeDates, true);
        long tp = Time.getSpecificTime(tradeDate, hms);
        double price=0.0;
        if(args[0].equals("getupprice"))
            price = am.getUpPrice(tp); 
        else
            price = am.getDownPrice(tp); 

        System.out.format("%8.3f\n", price);
    }

    private static void usage2() {
        System.err.println("usage: java AnalyzeTools getupprice stockCode tradeDate hms");
        System.exit(-1);
    }
    private static void usage3() {
        System.err.println("usage: java AnalyzeTools getdownprice stockCode tradeDate hms");
        System.exit(-1);
    }
}
