package com.westsword.stocks.base.time;

import java.io.*;
import java.util.*;

import com.westsword.stocks.Settings;

import org.apache.commons.lang3.ArrayUtils;


//the class to get dates from stockCode's daily dir
public class TradeDates extends Dates {
    private String mStockCode;

    //Two kinds of constructors:
    //1. tradeDates from sotckCode's daily data
    public TradeDates(String stockCode) {
        super(getTradeDateList(stockCode));
        mStockCode = stockCode;
    }
    public TradeDates() {
        this(Settings.getStockCode());
    }
    public TradeDates(String stockCode, String startDate, String endDate) {     //including endDate
        super(getTradeDateList(stockCode, startDate, endDate));
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
    public static String[] getTradeDateList(String stockCode, String startDate, String endDate) {
        String[] list = getTradeDateList(""+stockCode);
        TreeSet<String> trSet = new TreeSet<String>(Arrays.asList(list));
        return trSet.headSet(endDate, true).tailSet(startDate, true).toArray(new String[0]);
    }
    public static String[] getTradeDateList(String stockCode, String startDate) {
        String[] list = getTradeDateList(""+stockCode);
        return getTradeDateList(stockCode, startDate, list[list.length-1]);
    }

    //get all tradeDates
    public static String[] getTradeDateList(String stockCode) {
        String stockData = Settings.dailyDir + stockCode;
        //System.out.format("stockData=%s\n", stockData);

        File fStockData= new File(stockData);
        String[] sTradeDates = fStockData.list();
        //System.out.format("size=%d\n", sTradeDates.length);
        /*
        for(int i=0; i<sTradeDates.length; i++) {
            System.out.format("%s\n", sTradeDates[i]);
        }
        */

        return sTradeDates;
    }




}
