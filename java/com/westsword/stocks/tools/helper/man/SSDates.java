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
 
 
package com.westsword.stocks.tools.helper.man;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.StockPaths;
import com.westsword.stocks.tools.helper.SSUtils;

public class SSDates {
    public String stockCode;
    public String startDate;
    public double threshold;
    public String tradeDate;
    public String hmsList;                       //


    public SSDates(String stockCode, String startDate, double threshold, 
            String tradeDate, String hmsList) {
        this.stockCode = stockCode;
        this.startDate = startDate;
        this.threshold = threshold;
        this.tradeDate = tradeDate;
        this.hmsList = hmsList;
    }



    public void run(AmManager am) {
        String sOutFile = StockPaths.getSSDatesFile(stockCode, threshold, tradeDate, hmsList);
        ArrayList<String> ssDates = SSUtils.getSimilarTradeDates(stockCode, startDate, threshold, 
                tradeDate, hmsList, am);
        //write ssDates to sOutFile
        String line = String.format("%s\n", ssDates.toString());
        line = line.replaceAll("[\\[\\] ]", "");
        //System.out.format("%s", line);
        Utils.append2File(sOutFile, line, false);
    }
}
