 /*
 Copyright (C) 2019-2050 WestSword, Inc.
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
 
 
package com.westsword.stocks.base.utils;


import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;

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
    public static String getDataDir() {
        return getStockRootDir() + "data" + sSep;
    }
    public static String getDailyDir() {
        return getDataDir() + "daily" + sSep;
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
        String sPath = getDataDir() + "rawTradeDetails";
        sPath += sSep+stockCode+sSep+stockCode + "." + tradeDate + ".txt";
        return sPath;
    }
    //raw pankou.txt
    public static String getPankouTxt(String stockCode, String tradeDate) {
        String sPath = getDataDir() + "rawPankou";
        sPath += sSep+stockCode + sSep + tradeDate + sSep + "pankou" + sSep + "pankou.txt";
        return sPath;
    }







    
    public static String getSSDatesRootDir() {
        return getDataDir() + sSep + "ssdates" + sSep;
    }
    public static String getSSDatesDir(String stockCode, double threshold, String tradeDate) {
        String sDir = "";
        sDir += getSSDatesRootDir() + stockCode + "_" + String.format("%.2f", threshold) + sSep;
        sDir += tradeDate + sSep;

        return sDir;
    }
    public static String getSSDatesFile(String stockCode, double threshold, String tradeDate, 
            String hmsList) {

        return getSSDatesDir(stockCode, threshold, tradeDate) + hmsList + ".txt";
    }



    public static String getSSRootDir() {
        return getDataDir() + sSep + "similarStack" + sSep;
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

        return sDir + ".txt";
    }
    private static String getTradeTypeSymbol(int tradeType) {
        String sTradeType = "L";
        if(tradeType == Stock.TRADE_TYPE_SHORT)
            sTradeType = "S";

        return sTradeType;
    }



    public static String getSSTableDir() {
        return getDataDir() + "ssTable" + sSep;
    }
    public static String getSSTableFile(String sName) {
        return getSSTableDir() + sName + ".txt";
    }
    public static String getAnalysisFile() {
        String stockCode = Settings.getStockCode();
        String tradeDate = Settings.getTradeDate();

        return getAnalysisFile(stockCode, tradeDate);
    }
    public static String getAnalysisFile(String stockCode, String tradeDate) {
        return getDailyDir(stockCode, tradeDate)+"analysis.txt";
    }
    public static String getPVTableDir(String stockCode, String tradeDate) {
        return getDailyDir(stockCode, tradeDate) + "pvtable" + sSep;
    }
    public static String getPVTableFile(String stockCode, String tradeDate) {
        return getPVTableDir(stockCode, tradeDate)+"full.txt";
    }
    public static String getPVTableFile(String stockCode, String tradeDate, String endHMS) {
        return getPVTableDir(stockCode, tradeDate)+endHMS+".txt";
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
        return getDataDir() + "log" + sSep;
    }
    public static String getPerformanceLogFile() {
        return getLogDir() + "performance.log";
    }


    public static String getAmCorrelMapDir(String stockCode, String tradeDate) {
        return getDailyDir(stockCode, tradeDate) + "amcorrelmap" + sSep;
    }
    public static String getAmCorrelMapFile(String stockCode, String tradeDate0, String tradeDate1) {
        return getAmCorrelMapDir(stockCode, tradeDate0) + tradeDate1 + ".txt";
    }


    public static String getTradeSessionDir() {
        return getDataDir() + "sessions" + sSep;
    }
    public static String getTradeSessionDir(boolean bOpen) {
        return getTradeSessionDir() + (bOpen?"open":"close") + sSep;
    }
    public static String getTradeSessionFile(long inHexTimePoint, boolean bOpen) {
        String sSessionFile = "";

        sSessionFile += getTradeSessionDir(bOpen);
        String sHexTimePoint = String.format("%x", inHexTimePoint);
        sSessionFile += sHexTimePoint + ".txt";

        return sSessionFile;
    }

    public static String getDerivativePngDir() {
        return getDerivativePngDir(Settings.getStockCode(), Settings.getTradeDate());
    }
    public static String getDerivativePngDir(String stockCode, String tradeDate) {
        return getDailyDir(stockCode, tradeDate) + sSep + "derivativePng" + sSep; 
    }
    public static String getDerivativePngFile(String stockCode, String tradeDate, String hms) {
        return getDerivativePngDir(stockCode, tradeDate) + hms + ".png"; 
    }
    public static String getDerivativeDir(String stockCode, String tradeDate) {
        return getDailyDir(stockCode, tradeDate) + sSep + "derivative" + sSep; 
    }
    public static String getDerivativeFile(String stockCode, String tradeDate, String hms) {
        return getDerivativeDir(stockCode, tradeDate) + hms + ".txt";
    }


    public static String getAmDerivativeFile(String stockCode, String tradeDate) {
        return getDailyDir(stockCode, tradeDate) + sSep + "amderivative.txt"; 
    }
    public static String getAmRatePngFile(String stockCode, String tradeDate) {
        return getDailyDir(stockCode, tradeDate) + sSep + "amrate.png"; 
    }
    public static String getAmRatePngFile() {
        return getAmRatePngFile(Settings.getStockCode(), Settings.getTradeDate());
    }



    public static String getMakeAmDerPngVbs() {
        return getStockRootDir() + getValueWOS("vbs\\makeAmDerivativePng.vbs", "vbs/makeAmDerivativePng.vbs");
    }
    public static String getC4TradeDetailsExe() {
        return getStockRootDir() + getValueWOS("bin\\c4tradedetails.exe", "bin/c4tradedetails.exe");
    }

    public static String pythonCommand() {
        return "python -u";
    }
    public static String getPythonRootDir() {
        return getStockRootDir() + getValueWOS("python\\", "python/");
    }
    public static String pythonBuyPath() {
        return getPythonRootDir() + "buy.py";
    }
    public static String pythonSellPath() {
        return getPythonRootDir() + "sell.py";
    }
    public static String pythonGetEntrustPath() {
        return getPythonRootDir() + "today_entrusts.py";
    }
    public static String pythonGetTradePath() {
        return getPythonRootDir() + "today_trades.py";
    }
    public static String pythonGetBalancePath() {
        return getPythonRootDir() + "balance.py";
    }
    public static String pythonGetPositionPath() {
        return getPythonRootDir() + "position.py";
    }
    public static String pythonRefreshPath() {
        return getPythonRootDir() + "refresh.py";
    }


}
