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
 
 
package com.westsword.stocks.am;


import java.io.*;
import java.util.*;

import com.westsword.stocks.base.time.*;

//the class to get dates from stockCode's daily dir
public class SimilarTradeDates {
    private String mStockCode;
    private String mStartDate;

    public SimilarTradeDates(String stockCode, String startDate) {
        this.mStockCode = stockCode;
        this.mStartDate = startDate;
    }

    public ArrayList<String> get(double threshold, 
            String tradeDate, String hmsList, AmManager am, double[][] corrM) {
        ArrayList<String> tradeDateList = new ArrayList<String>();

        if(corrM!=null) {
            get(threshold, tradeDate, corrM, tradeDateList);
        } else {
            String[] hms = hmsList.split("_");
            TradeDates tradeDates = new TradeDates(mStockCode);
            String tradeDate0 = mStartDate;
            while(tradeDate0 != null) {
                String[] out = new String[1]; 
                boolean bHMSMatched = am.isHMSMatched(tradeDate0, tradeDate, hms, threshold, out);
                if(bHMSMatched)
                    tradeDateList.add(tradeDate0);
                /*
                System.out.format("%s %s %s %s\n", 
                        tradeDate0, tradeDate, hmsList, out[0]);
                */

                tradeDate0 = tradeDates.nextDate(tradeDate0);
            }
        }

        return tradeDateList;
    }

    public void get(double threshold, String tradeDate, double[][] corrM, ArrayList<String> tradeDateList) {
        TradeDates tradeDates = new TradeDates(mStockCode, mStartDate);
        int idx = tradeDates.getIndex(tradeDate);
        int h = corrM.length;
        int w = corrM[0].length;
        for(int i=0; i<w; i++) {
            if(corrM[idx][i] >= threshold) {
                String sMatchedDate = tradeDates.getDate(i);
                if(tradeDateList!=null)
                    tradeDateList.add(sMatchedDate);
            }
        }
    }
}


