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

import com.westsword.stocks.am.AmUtils;

public class AnalysisHelper {
    public static void makeTxt(String args[]) {
        if(args.length != 3 && args.length != 2 && args.length !=4) {
            usage();
        }

        String stockCode = args[1];
        String tradeDate1 = null;
        if(args.length >= 3)
            tradeDate1 = args[2];
        String tradeDate2 = null;
        if(args.length >= 4)
            tradeDate2 = args[3];

        AmUtils amUtils = new AmUtils(stockCode, false);
        if(args.length == 3) {
            long startAm = amUtils.loadPrevLastAm(tradeDate1);
            amUtils.writeAmRecords(startAm, tradeDate1);
        } else if(args.length == 2) {
            amUtils.writeAllAmRecords();
        } else {
            amUtils.writeAmRecords(tradeDate1, tradeDate2);
        }
    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools makeanalysistxt stockCode [tradeDate1] [tradeDate2]");
        System.err.println("  no tradeDate1&tradeDate2 - all tradeDates");
        System.err.println("  tradeDate1               - single tradeDate1");
        System.err.println("  tradeDate1&tradeDate2    - [tradeDate1, tradeDate2)");
        System.exit(-1);
    }
}
