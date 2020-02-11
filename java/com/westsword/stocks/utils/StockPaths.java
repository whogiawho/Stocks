package com.westsword.stocks.utils;

import com.westsword.stocks.Stock;
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







    
    public static String getSSRootDir() {
        return getStockRootDir() + "\\data\\similarStack\\";
    }
    public static String getSimilarStackDir(String stockCode) {
        return getSSRootDir() + stockCode + "\\";
    }
    public static String getSSTradeDetailsLogDir0(String stockCode, String startDate, 
            double threshold, int tradeType, int sTDistance) {
        String sDir = "";
        sDir += getSimilarStackDir(stockCode) + startDate + "_" + String.format("%.2f", threshold);
        sDir += "_" + "T" + sTDistance + getTradeTypeSymbol(tradeType) + "\\";

        return sDir;
    }
    public static String getSSTradeDetailsLogDir(String stockCode, String startDate, 
            double threshold, int tradeType, int sTDistance,
            String tradeDate, int maxCycle, double targetRate) {
        String sDir = getSSTradeDetailsLogDir0(stockCode, startDate,
                threshold, tradeType, sTDistance);

        sDir += tradeDate + "_" + String.format("%03d_%.3f", maxCycle, targetRate);
        sDir += "\\";

        return sDir;
    }
    public static String getSSTradeDetailsLogFile(String stockCode, String startDate, 
            double threshold, int tradeType, int sTDistance,
            String tradeDate, String hmsList, int maxCycle, double targetRate) {
        String sDir = getSSTradeDetailsLogDir(stockCode, startDate,
                threshold, tradeType, sTDistance,
                tradeDate, maxCycle, targetRate);

        return sDir + hmsList + ".txt";
    }
    public static String getSSTradeSumLogFile(String stockCode, String startDate, 
            double threshold, int tradeType, int sTDistance,
            String tradeDate, int maxCycle, double targetRate) {
        String sDir = getSSTradeDetailsLogDir0(stockCode, startDate,
                threshold, tradeType, sTDistance);

        sDir += tradeDate + "_" + String.format("%03d_%.3f", maxCycle, targetRate);

        return sDir+".txt";
    }
    private static String getTradeTypeSymbol(int tradeType) {
        String sTradeType = "L";
        if(tradeType == Stock.TRADE_TYPE_SHORT)
            sTradeType = "S";

        return sTradeType;
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


    public static String getLogDir() {
        return getStockRootDir() + "data\\log\\";
    }
    public static String getPerformanceLogFile() {
        return getLogDir() + "performance.log";
    }
}
