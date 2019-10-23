package com.westsword.stocks;

import java.io.*;
import java.nio.charset.*;
import java.util.regex.*;

import com.westsword.stocks.utils.FileLoader;

import org.apache.commons.io.FileUtils;

public class Settings extends FileLoader {
    public final static String rootDir="f:\\Stocks\\";
    public final static String dailyDir="f:\\Stocks\\data\\daily\\";
    public final static String settingFile=rootDir+"settings.txt";

    public final static String sHexinServerIni = "D:\\HexinSoftware\\Hexin\\renjiqi\\hqserver.ini";
    public final static String sC4TradeDetailsExe = "F:\\Stocks\\bin\\c4tradedetails.exe";



    private void appendValue(String file, String key, String value) {
        String line = String.format("%s=%s\r\n", key, value);
        Utils.append2File(file, line);
    }
    public void setValue(String file, String key, String value) {
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
            mValue = fields[1];
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






    public static String getTradeDate() {
        return getString("currentDate");
    }
    public static String getStockCode() {
        return getString("stockCode");
    }


    //return MIN_VALUE if key does not exist
    public static long getLong(String sFile, String key, int radix) {
        long value = Long.MIN_VALUE;

        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);
        if(sValue != null)
            value = Long.valueOf(sValue, radix);

        return value;
    }
    public static long getLong(String key, int radix) {
        return getLong(Settings.settingFile, key, radix);
    }
    //return MIN_VALUE if key does not exist
    public static int getInteger(String sFile, String key) {
        int value = Integer.MIN_VALUE;

        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);
        if(sValue != null)
            value = Integer.valueOf(sValue);

        return value;
    }
    public static int getInteger(String key) {
        return getInteger(Settings.settingFile, key);
    }
    //return Double.NaN if key does not exist
    public static double getDouble(String sFile, String key) {
        double value = Double.NaN;

        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);
        if(sValue != null)
            value = Double.valueOf(sValue);

        return value;
    }
    public static double getDouble(String key) {
        return getDouble(Settings.settingFile, key);
    }
    //return null if key does not exist
    public static String getString(String sFile, String key) {
        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);

        return sValue;
    }
    public static String getString(String key) {
        return getString(Settings.settingFile, key);
    }
    //return false if key does not exist
    public static boolean getBoolean(String sFile, String key) {
        boolean value=false;

        Settings t = new Settings();
        String sValue = t.getValue(sFile, key);
        if(sValue != null)
            value = Boolean.valueOf(sValue);

        return value;
    }
    public static boolean getBoolean(String key) {
        return getBoolean(Settings.settingFile, key);
    }
}
