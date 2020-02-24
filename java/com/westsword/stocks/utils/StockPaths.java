package com.westsword.stocks.utils;

import com.westsword.stocks.Stock;
import com.westsword.stocks.Utils;
import com.westsword.stocks.Settings;

public class StockPaths {
    private final static String sSep = Utils.getSeperator();

    private static String getValueWOS(String sWin, String sLinux) {
        //System.out.format("StockPaths.getValueWOS: %s %s\n", sWin, sLinux);
        String sValue = sWin;
        if(Utils.isLinux()) {
            sValue = sLinux;
        }
        return sValue;
    }


    public static String getStockRootDir() {
        return getValueWOS("d:\\Stocks\\", "/root/Stocks/");
    }
    public static String getDailyDir() {
        return getStockRootDir() + getValueWOS("data\\daily\\", "data/daily/");
    }
    public static String getC4TradeDetailsExe() {
        return getStockRootDir() + getValueWOS("bin\\c4tradedetails.exe", "bin/c4tradedetails.exe");
    }
    public static String getSettingFile() {
        return StockPaths.getStockRootDir() + "settings.txt";
    }

    public static String getDailyDir(String stockCode) {
        return getDailyDir()+stockCode+sSep;
    }
    public static String getDailyDir(String stockCode, String tradeDate) {
        return getDailyDir()+stockCode+sSep+tradeDate+sSep;
    }

    //raw tradeDetails.txt
    public static String getRawTradeDetailsFile(String stockCode, String tradeDate) {
        String sPath = getStockRootDir() + "data"+sSep+"rawTradeDetails";
        sPath += sSep+stockCode+sSep+stockCode+"."+tradeDate+".txt";
        return sPath;
    }
    //raw pankou.txt
    public static String getPankouTxt(String stockCode, String tradeDate) {
        String sPath = getStockRootDir() + "data"+sSep+"rawPankou";
        sPath += sSep+stockCode+sSep+tradeDate+sSep+"pankou"+sSep+"pankou.txt";
        return sPath;
    }







    
    public static String getSSRootDir() {
        return getStockRootDir() + sSep + "data" + sSep + "similarStack" + sSep;
    }
    public static String getSimilarStackDir(String stockCode) {
        return getSSRootDir() + stockCode + sSep;
    }
    public static String getSSTradeDetailsLogDir0(String stockCode, String startDate, 
            double threshold, int tradeType, int sTDistance) {
        String sDir = "";
        sDir += getSimilarStackDir(stockCode) + startDate + "_" + String.format("%.2f", threshold);
        sDir += "_" + "T" + sTDistance + getTradeTypeSymbol(tradeType) + sSep;

        return sDir;
    }
    public static String getSSTradeDetailsLogDir(String stockCode, String startDate, 
            double threshold, int tradeType, int sTDistance,
            String tradeDate, int maxCycle, double targetRate) {
        String sDir = getSSTradeDetailsLogDir0(stockCode, startDate,
                threshold, tradeType, sTDistance);

        sDir += tradeDate + "_" + String.format("%03d_%.3f", maxCycle, targetRate);
        sDir += sSep;

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
        return getStockRootDir() + "specialDates" + sSep;
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
        return getStockRootDir() + "data" + sSep + "log" + sSep;
    }
    public static String getPerformanceLogFile() {
        return getLogDir() + "performance.log";
    }
}
