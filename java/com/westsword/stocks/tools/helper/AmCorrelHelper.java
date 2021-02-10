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

public class AmCorrelHelper {
    public static void getAmCorrel(String args[]) {
        if(args.length != 5 && args.length!=6) {
            usage();
            return;
        }

        if(args.length == 5) {
            String stockCode = args[1];
            String tradeDate1 = args[2];
            String tradeDate2 = args[3];
            String hmsList = args[4];
            getAmCorrel(stockCode, tradeDate1, tradeDate2, hmsList);
        } else {
            String stockCode = args[1];
            String s0 = args[2];
            String e0 = args[3];
            String s1 = args[4];
            String e1 = args[5];

            getAmCorrel(stockCode, s0, e0, s1, e1);
        }
    }
    private static void getAmCorrel(String stockCode, String s0, String e0, String s1, String e1) {
        String[] fields = s0.split(",");
        String startDate0 = fields[0];
        String startHMS0 = fields[1];
        fields = e0.split(",");
        String endDate0 = fields[0];
        String endHMS0 = fields[1];

        fields = s1.split(",");
        String startDate1 = fields[0];
        String startHMS1 = fields[1];
        fields = e1.split(",");
        String endDate1 = fields[0];
        String endHMS1 = fields[1];

        String[] tradeDates0 = TradeDates.getTradeDateList(stockCode, startDate0, endDate0);
        String[] tradeDates1 = TradeDates.getTradeDateList(stockCode, startDate1, endDate1);
        ArrayList<String> tradeDates = new ArrayList<String>(Arrays.asList(tradeDates0));
        tradeDates.addAll(Arrays.asList(tradeDates1));
        StockDates stockDates = new StockDates(stockCode);

        AmManager am = new AmManager(stockCode, tradeDates);

        String sAmCorrel = "";
        double amcorrel = am.getAmCorrel(startDate0, startHMS0, endDate0, endHMS0,
                startDate1, startHMS1, endDate1, endHMS1); 
        sAmCorrel += String.format("%8.3f", amcorrel);
        System.out.format("%s\n", sAmCorrel);
    }
    private static void getAmCorrel(String stockCode, String tradeDate1, String tradeDate2, String hmsList) {
        StockDates stockDates = new StockDates(stockCode);
        String[] tradeDates = new String[] {
            tradeDate1,
            stockDates.nextDate(tradeDate1),
            tradeDate2,
            stockDates.nextDate(tradeDate2),
        };

        AmManager am = new AmManager(stockCode, tradeDates, true);

        String sAmCorrel = "";
        String[] fields = hmsList.split("_");
        String startHMS = fields[0];
        for(int i=1; i<fields.length; i++) {
            String endHMS = fields[i];
            double amcorrel = am.getAmCorrel(tradeDate1, tradeDate2, startHMS, endHMS); 
            sAmCorrel += String.format("%8.3f", amcorrel);
        }
        System.out.format("%s\n", sAmCorrel);

    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools getamcorrel stockCode tradeDate1 tradeDate2 hmsList");
        System.err.println("       java AnalyzeTools getamcorrel stockCode sDate0,sHms0 eDate0,eHms0 sDate1,sHms1 eDate1,eHms1");
        System.exit(-1);
    }
}
