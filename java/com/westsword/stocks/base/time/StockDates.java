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

import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.utils.StockPaths;
import com.westsword.stocks.base.utils.LineLoader;


public class StockDates extends WorkDates {
    private boolean bDebug = false;
    private TreeSet<String> mMissingSet = new TreeSet<String>();

    //those dates excluding:
    //  Sat&Mon,
    //  specialDates/holidays.txt
    public StockDates(String start, String end) {       //start&end must be of format YYYYMMDD
        super(start, end);

        if(bDebug) {
            System.out.format("%s, size = %d\n", 
                    Utils.getCallerName(getClass()), size());
        }

        //remove holidays 
        String sHolidaysFile = StockPaths.getHolidaysFile();
        LineLoader loader = new LineLoader();
        ArrayList<String> holidaysList = new ArrayList<String>();
        loader.load(holidaysList, sHolidaysFile);
        removeAll(holidaysList);

        if(bDebug) {
            System.out.format("%s, size = %d\n", 
                    Utils.getCallerName(getClass()), size());
        }
    }

    //those dates excluding:
    //  Sat&Mon,
    //  specialDates/holidays.txt
    //  specialDates/[stockCode].suspension.txt
    public StockDates(String start, String end, String stockCode) {
        this(start, end);

        //remove stockCode's suspension dates 
        String sSuspensionFile = StockPaths.getSuspensionDatesFile(stockCode);
        LineLoader loader = new LineLoader();
        ArrayList<String> suspensionList = new ArrayList<String>();
        loader.load(suspensionList, sSuspensionFile);
        removeAll(suspensionList);
        if(bDebug) {
            System.out.format("%s, size = %d\n", 
                    Utils.getCallerName(getClass()), size());
        }

        //load missing dates
        String sMissingFile = StockPaths.getMissingDatesFile(stockCode);
        ArrayList<String> missingList = new ArrayList<String>();
        loader.load(missingList, sMissingFile);
        mMissingSet.addAll(missingList);

        if(bDebug) {
            System.out.format("%s, mMissingSet.size = %d\n", 
                    Utils.getCallerName(getClass()), mMissingSet.size());
        }
    }
    //settings.stockCode.SdStartDate --- currentDate|nextYearLastDate
    public StockDates(String stockCode, boolean bCurrentDate) {
        this(Settings.getSdStartDate(stockCode),
                bCurrentDate?Time.currentDate():Dates.nextYearLastDate(Time.currentDate()),
                stockCode);
    }
    public StockDates(String stockCode) {
        this(stockCode, true);
    }
}
