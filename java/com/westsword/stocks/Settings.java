package com.westsword.stocks;

import java.io.*;
import java.nio.charset.*;
import java.util.regex.Pattern;

import com.westsword.stocks.utils.FileLoader;

import org.apache.commons.io.FileUtils;

public class Settings extends FileLoader {
    public final static String rootDir="f:\\Stocks\\";
    public final static String dailyDir="f:\\Stocks\\data\\daily\\";
    public final static String settingFile=rootDir+"settings.txt";

    public final static String sHexinServerIni = "D:\\HexinSoftware\\Hexin\\renjiqi\\hqserver.ini";
    public final static String sC4TradeDetailsExe = "F:\\Stocks\\bin\\c4tradedetails.exe";


    public void setValue(String file, String key, String value) {
        final File f= new File(file);    

        //System.out.format("key=%s, value=%s\n", key, value);
        try {
            String contents = FileUtils.readFileToString(f, StandardCharsets.UTF_8.name());
            contents = Pattern.compile(key+"="+".*\r\n").matcher(contents).replaceAll(key+"="+value+"\r\n");
            FileUtils.write(f, contents);
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


    public static int getInteger(String key) {
        int value;

        Settings t = new Settings();
        String sValue = t.getValue(Settings.settingFile, key);

        value = Integer.valueOf(sValue);

        return value;
    }
    public static double getDouble(String key) {
        double value;

        Settings t = new Settings();
        String sValue = t.getValue(Settings.settingFile, key);

        value = Double.valueOf(sValue);

        return value;
    }
    public static String getString(String key) {
        Settings t = new Settings();
        String sValue = t.getValue(Settings.settingFile, key);

        return sValue;
    }
    public static boolean getBoolean(String key) {
        boolean value;

        Settings t = new Settings();
        String sValue = t.getValue(Settings.settingFile, key);

        value = Boolean.valueOf(sValue);

        return value;
    }
}
