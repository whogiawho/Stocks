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
 
 
package com.westsword.stocks.base;


import java.io.*;
import java.nio.charset.*;
import java.util.regex.*;
import org.apache.commons.io.FileUtils;

import com.westsword.stocks.base.utils.FileLoader;
import com.westsword.stocks.base.utils.StockPaths;

public class Settings extends FileLoader {


    private void appendValue(String file, String key, String value) {
        String line = String.format("%s=%s\r\n", key, value);
        Utils.append2File(file, line);
    }
    public void setValue(String key, String value) {
        setValue(StockPaths.getSettingFile(), key, value);
    }
    public void setValue(String file, String key, String value) {
        if(!Utils.isFile(file)) {
            appendValue(file, key, value);
            return;
        }

        final File f= new File(file);    

        //System.out.format("key=%s, value=%s\n", key, value);
        try {
            String contents = FileUtils.readFileToString(f, StandardCharsets.UTF_8.name());
            Matcher m = Pattern.compile(key+"="+".*\r\n").matcher(contents);
            boolean bMatch = m.find();
            if(bMatch) {
                contents = m.replaceAll(key+"="+value+"\r\n");
                FileUtils.write(f, contents);
            } else {
                appendValue(file, key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onLineRead(String line, int count) {
        //remove space of start and end
        line=line.trim();
        //skip if starting with #
        if(line.startsWith("#"))
            return true;
        
        String[] fields=line.split("=");
        //remove space of start and end for both fields[0] and fields[1]
        for(int i=0;i<fields.length;i++) {
            if(fields[i] != null)
                fields[i] = fields[i].trim();
        }

        if(mKey!=null&&fields[0].equals(mKey)) {
            if(fields.length >= 2)
                mValue = fields[1];
            else
                mValue = null;
            return false;
        }

        return true;
    }
    private String mKey = null;
    private String mValue = null;
    public String getValue(String file, String key) {
        mValue = null;

        mKey = key;
        load(file);

        return mValue;
    }





    public static String getAmRateViewer() {
        return getString("AmRateViewer");
    }
    public static Integer getQsIdx() {
        return getInteger("QSIDX");
    }
    public static Integer getWay2SearchAmRecord() {
        return getInteger("Way2SearchAmRecord");
    }
    public static String getLocale() {
        return getString("Locale");
    }
    public static String getTimeZone() {
        return getString("TimeZone");
    }
    public static String getSdStartTime() {
        return getString("SdStartTime");
    }
    public static Integer getSdInterval() {   //in seconds
        return getInteger("SdInterval");
    }
    public static Integer getCkptInterval() { //in seconds
        return getInteger("CkptInterval");
    }

    public static double getPriceStep() {
        return getPriceStep(getStockCode());
    }
    public static double getPriceStep(String stockCode) {
        return getDouble(stockCode+".priceStep");
    }
    public static String getSdStartDate() {
        return getSdStartDate(getStockCode());
    }
    public static String getSdStartDate(String stockCode) {
        return getString(stockCode+".SdStartDate");
    }
    public static String getPriceDecimalFormat() {
        return getPriceDecimalFormat(getStockCode());
    }
    public static String getPriceDecimalFormat(String stockCode) {
        return getString(stockCode+".priceDecimalFormat");
    }

    public static Integer getMaxTasks() {
        return getInteger("MaxTasks");
    }
    public static Double getMaxGrowthRate() {
        return getDouble("MaxGrowthRate");
    }


    public static String getTradeDate() {
        return getString("currentDate");
    }
    public static String getStockCode() {
        return getString("stockCode");
    }


    //return null if key does not exist
    public static Long getLong(String sFile, String key, int radix) {
        Long value = null;

        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);
        if(sValue != null)
            value = Long.valueOf(sValue, radix);

        return value;
    }
    public static Long getLong(String key, int radix) {
        return getLong(StockPaths.getSettingFile(), key, radix);
    }
    //return null if key does not exist
    public static Integer getInteger(String sFile, String key) {
        Integer value = null;

        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);
        if(sValue != null)
            value = Integer.valueOf(sValue);

        return value;
    }
    public static Integer getInteger(String key) {
        return getInteger(StockPaths.getSettingFile(), key);
    }
    //return null if key does not exist
    public static Double getDouble(String sFile, String key) {
        Double value = null;

        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);
        if(sValue != null)
            value = Double.valueOf(sValue);

        return value;
    }
    public static Double getDouble(String key) {
        return getDouble(StockPaths.getSettingFile(), key);
    }
    //return null if key does not exist
    public static String getString(String sFile, String key) {
        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);
        sValue = sValue==null?"":sValue;

        return sValue;
    }
    public static String getString(String key) {
        return getString(StockPaths.getSettingFile(), key);
    }
    //return null if key does not exist
    public static Boolean getBoolean(String sFile, String key) {
        Boolean value=null;

        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);
        if(sValue != null)
            value = Boolean.valueOf(sValue);

        return value;
    }
    public static Boolean getBoolean(String key) {
        return getBoolean(StockPaths.getSettingFile(), key);
    }


    public final static int NO_PERFORMANCE_LOG = 0;
    public final static int SWITCH_OF_RAW_DATA = 1;
    public final static int SWITCH_OF_MAIN_LOOP = 2;
    public final static int BUFFERING_TRADERESULT = 3;
    public final static int ONLY_LONG_TRADESESSION = 4;
    public final static int MAX_OUT_PRICE = 5;
    public final static int AM_DERIVATIVE = 6;
    public final static int CHECK_2_SUBMIT_SESSION = 7;
    public final static int AVGAM = 8;
    public final static String[] booleanKeys = {
        "noPerformanceLog",
        "switchOfRawData",
        "switchOfMainLoop",
        "BufferingTradeResult",
        "OnlyLongTradeSession",
        "MaxOutPrice",
        "AmDerivative",
        "Check2SumitSession",
        "AvgAm",
    };
    public final static Boolean[] booleanValues = {
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    };

    public static boolean getSwitch(int idx) {
        if(booleanValues[idx] == null) {
            String key = booleanKeys[idx];
            booleanValues[idx] = getBoolean(key);
        }

        return booleanValues[idx];
    }

    public static double getAmDerR2Threshold() {
        return getDouble("AmDer.R2Threshold");
    }
    public static int getAmDerBackwardSd() {
        return getInteger("AmDer.BackwardSd");
    }
    public static int getAmDerMinimumSkipSd() {
        return getInteger("AmDer.MinimumSkipSd");
    }
    public static int getAmDerInterval() {
        return getInteger("AmDer.Interval");
    }
    public static int getAvgAmBackwardSd() {
        return getInteger("AvgAm.BackwardSd");
    }
    public static int getAvgAmMinimumSkipSd() {
        return getInteger("AvgAm.MinimumSkipSd");
    }
    public static int getAvgAmInterval() {
        return getInteger("AvgAm.Interval");
    }


}
