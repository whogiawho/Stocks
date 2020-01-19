package com.westsword.stocks.utils;

import com.westsword.stocks.Settings;

public class StockPaths {
    public static String getStockRootDir() {
        return Settings.rootDir;
    }
    public static String getDailyDir() {
        return Settings.dailyDir;
    }
    public static String getDailyDir(String stockCode) {
        return getDailyDir()+stockCode+"\\";
    }
    public static String getDailyDir(String stockCode, String tradeDate) {
        return getDailyDir()+stockCode+"\\"+tradeDate+"\\";
    }

    //raw tradeDetails.txt
    public static String getRawTradeDetailsFile(String stockCode, String tradeDate) {
        return getStockRootDir() + "data\\rawTradeDetails\\"+stockCode+"\\"+stockCode+"."+tradeDate+".txt";
    }
    //raw pankou.txt
    public static String getPankouTxt(String stockCode, String tradeDate) {
        return getStockRootDir() + "data\\rawPankou\\"+stockCode+"\\"+tradeDate+"\\"+"pankou"+"\\"+"pankou.txt";
    }







    



    public static String getAnalysisFile() {
        String stockCode = Settings.getStockCode();
        String tradeDate = Settings.getTradeDate();

        return getAnalysisFile(stockCode, tradeDate);
    }
    public static String getAnalysisFile(String stockCode, String tradeDate) {
        return getDailyDir(stockCode, tradeDate)+"analysis.txt";
    }


    public static String getSpecialDatesDir() {
        return getStockRootDir() + "specialDates\\";
    }
    public static String getSuspensionDatesFile(String stockCode) {
        return getSpecialDatesDir() + stockCode + ".suspension.txt";
    }
    public static String getMissingDatesFile(String stockCode) {
        return getSpecialDatesDir() + stockCode + ".missing.txt";
    }
    public static String getHolidaysFile() {
        return getSpecialDatesDir() + "holidays.txt";
    }
}
