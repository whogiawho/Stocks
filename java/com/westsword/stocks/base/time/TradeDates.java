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
 
 
package com.westsword.stocks.base.time;


import java.io.*;
import java.util.*;

import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.utils.StockPaths;

//the class to get dates from stockCode's daily dir
public class TradeDates extends Dates {
    private String mStockCode;

    //Two kinds of constructors:
    //1. tradeDates from sotckCode's daily data
    public TradeDates() {
        this(Settings.getStockCode());
    }
    public TradeDates(String stockCode) {
        super(getTradeDateList(stockCode));
        mStockCode = stockCode;
    }
    public TradeDates(String stockCode, String startDate, String endDate) {     //including endDate
        super(getTradeDateList(stockCode, startDate, endDate));
        mStockCode = stockCode;
    }
    public TradeDates(String stockCode, String startDate) {     //[startDate, )
        super(getTradeDateList(stockCode, startDate));
        mStockCode = stockCode;
    }
    //2. tradeDates from a defined array
    public TradeDates(String stockCode, String[] sTradeDates) {
        super(sTradeDates);
        mStockCode = stockCode;
    }


    public String getStockCode() {
        return mStockCode;
    }



    //[startDate, endDate]
    private static String[] getTradeDateList(String stockCode, String startDate, String endDate) {
        String[] list = getTradeDateList(""+stockCode);
        TreeSet<String> trSet = new TreeSet<String>(Arrays.asList(list));
	    //System.out.format("trSet.size = %d, startDate=%s, endDate=%s\n", trSet.size(), startDate, endDate);
	    TreeSet<String> sub = new TreeSet<String>(trSet.headSet(endDate, true));
	    sub = new TreeSet<String>(sub.tailSet(startDate, true));
	    return sub.toArray(new String[0]);
        //return trSet.headSet(endDate, true).tailSet(startDate, true).toArray(new String[0]);
    }
    private static String[] getTradeDateList(String stockCode, String startDate) {
        String[] list = getTradeDateList(""+stockCode);
        return getTradeDateList(stockCode, startDate, list[list.length-1]);
    }

    //get all tradeDates
    private static String[] getTradeDateList(String stockCode) {
        String stockData = StockPaths.getDailyDir(stockCode);
        //System.out.format("stockData=%s\n", stockData);

        File fStockData= new File(stockData);
        String[] sTradeDates = fStockData.list();
        //System.out.format("size=%d\n", sTradeDates.length);
	    TreeSet<String> trSet = new TreeSet<String>(Arrays.asList(sTradeDates));
	    sTradeDates = trSet.toArray(new String[0]);

	    /*
        for(int i=0; i<sTradeDates.length; i++) {
            System.out.format("%s\n", sTradeDates[i]);
        }
	    */

        return sTradeDates;
    }




}
