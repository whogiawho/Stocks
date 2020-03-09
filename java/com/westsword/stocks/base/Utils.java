package com.westsword.stocks.base;


import java.io.*;
import java.math.*;
import java.text.*;
import java.nio.charset.*;
import org.apache.commons.io.FileUtils;

import com.westsword.stocks.base.Settings;

public class Utils {


    public static boolean existFile(String path) {
        File f = new File(path);
        return f .exists();
    }
    //mkdir path if not exist
    public static void mkDir(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
    }
    public static void deleteFile(String path) {
        boolean bSwitchOfRawData = Settings.getSwitch(Settings.SWITCH_OF_RAW_DATA);

        try{
            File file = new File(path);
            
            if(file.delete()){
                if(bSwitchOfRawData)
                    System.out.format("%s\n", file.getName() + " is deleted!");
            }else{
                if(bSwitchOfRawData)
                    System.out.format("Delete(%s) operation is failed.\n", path);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void append2File(String fileName, String line, boolean bAppend) {
        try {
            FileUtils.writeStringToFile(new File(fileName), line, StandardCharsets.UTF_8.name(), bAppend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void append2File(String fileName, String line) {
        append2File(fileName, line, true);
    }
    public static void resetDir(String sDir) {
        try {
            FileUtils.deleteDirectory(new File(sDir));
            //force creating dir
            FileUtils.forceMkdir(new File(sDir));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }






    public static String getCallerName(Class c) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String methodName = e.getMethodName();

        return c.getSimpleName() + "." + methodName;
    }













    public static double roundUp(double inD) {
        String sFormat = Settings.getPriceDecimalFormat();
        DecimalFormat df = new DecimalFormat(sFormat);
        df.setRoundingMode(RoundingMode.CEILING);

        //adjust inD to specified Decimal format
        inD = Double.valueOf(df.format(inD));

        return inD;
    }


    public static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }
    public static boolean isLinux()
    {
        return  System.getProperty("os.name").startsWith("Linux");
    }
    public static String getSeperator() {
        return Utils.isWindows()? "\\":"/";
    }


    public static String getAmcKey(String tradeDate0, String tradeDate1, String startHMS, String endHMS) {
        String sHMSPair = startHMS + "," + endHMS;
        String key = tradeDate0 + ",";
        key += tradeDate1 + ",";
        key += sHMSPair;

        return key;
    }


    public static double getOutPrice(double inPrice, double targetProfit, int tradeType) {
        double outPrice;
        if(tradeType == Stock.TRADE_TYPE_LONG)
            outPrice = inPrice + targetProfit;
        else
            outPrice = inPrice - targetProfit;

        return outPrice;
    }
}
