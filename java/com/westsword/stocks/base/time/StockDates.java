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
    //settings.stockCode.SdStartDate --- currentDate
    public StockDates(String stockCode) {
        this(Settings.getSdStartDate(stockCode),
                Time.currentDate(),
                stockCode);
    }

    public boolean isMissingDate(String date) {
        return mMissingSet.contains(date); 
    }
}
