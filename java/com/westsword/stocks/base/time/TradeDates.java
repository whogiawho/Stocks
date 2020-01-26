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
    public TradeDates(String stockCode, String endDate) {     //including endDate
        super(getTradeDateList(stockCode, endDate));
        mStockCode = stockCode;
    }
    public TradeDates() {
        this(Settings.getStockCode());
    }
    //2. tradeDates from a defined array
    public TradeDates(String stockCode, String[] sTradeDates) {
        super(sTradeDates);
        mStockCode = stockCode;
    }


    public String getStockCode() {
        return mStockCode;
    }




    //exclude dates which are later than tradeDate
    public static String[] getTradeDateList(String stockCode, String tradeDate) {
        String[] list = getTradeDateList(""+stockCode);

        ArrayList<String> removedList = new ArrayList<String>();
        for(int i=0; i<list.length; i++) {
            if(tradeDate.compareTo(list[i]) < 0)
                removedList.add(list[i]);
        }
        
        for(int i=0; i<removedList.size(); i++)
            list = ArrayUtils.removeElement(list, removedList.get(i));

        return list;
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
