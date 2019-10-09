package com.westsword.stocks.tools;

import java.io.*;
import java.util.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;

import com.westsword.stocks.*;
import com.westsword.stocks.utils.*;

import org.apache.commons.io.FilenameUtils;

public class GetSettings{
    public static void testSettings() {
        System.out.format("\n testSettings: \n");

        Settings s = new Settings();
        String stockCode = Settings.getStockCode();
        System.out.format("stockCode=%s\n", stockCode);
        String tradeDate = Settings.getTradeDate();
        System.out.format("tradeDate=%s\n", tradeDate);
    }




    public static void testWorkDates() {
        System.out.format("\n testWorkDates: \n");
        WorkDates dates = new WorkDates("20090101", "20191231");
        String[] sTradeDates = dates.getAllDates();
        /*
        for(int i=0; i<sTradeDates.length; i++) {
            System.out.format("%s\n", sTradeDates[i]);
        }
        */
    }
    public static void testStockDates() {
        System.out.format("\n testStockDates: \n");
        String stockCode="600030";
        StockDates dates = new StockDates("20090101", "20191231", stockCode);
        String[] sTradeDates = dates.getAllDates();
        /*
        for(int i=0; i<sTradeDates.length; i++) {
            System.out.format("%s\n", sTradeDates[i]);
        }
        */
    }

    public static void main(String args[]) throws Exception {
        //testWorkDates();
        testStockDates();

        testSettings();       
    }











}
