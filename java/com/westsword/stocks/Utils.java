package com.westsword.stocks;

import java.io.*;
import java.math.*;
import java.util.*;
import java.text.*;
import java.nio.charset.*;

import com.westsword.stocks.base.time.Time;
import com.westsword.stocks.base.time.NatureDates;

import org.apache.commons.io.FileUtils;

public class Utils {
    private final static boolean bSwitchOfRawData = Settings.getSwitch(Settings.SWITCH_OF_RAW_DATA);
    private final static String sTz = Settings.getTimeZone();
    private final static String sLc = Settings.getLocale();


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

    public final static double MINIMUM_HANDLING_CHARGE = 5;
    public static double getMinBuyFee(double inPrice, int amount) {
        double cargoFee = inPrice*(double)amount;
        double normalBHC = cargoFee*Parms.BuyStockServiceRate;
        double bhc = normalBHC>MINIMUM_HANDLING_CHARGE ? normalBHC:MINIMUM_HANDLING_CHARGE;
        return cargoFee + bhc;
    }
    //with considering amount
    public static double getTradeCost(double buyPrice, double sellPrice, int amount) {
        double cost = 0.0;

        double normalBHC = buyPrice*amount*Parms.BuyStockServiceRate;
        double normalSHC = sellPrice*amount*Parms.SellStockServiceRate;
        double bhc = normalBHC>MINIMUM_HANDLING_CHARGE ? normalBHC:MINIMUM_HANDLING_CHARGE;
        double shc = normalSHC>MINIMUM_HANDLING_CHARGE ? normalSHC:MINIMUM_HANDLING_CHARGE;
        bhc = bhc/amount;
        shc = shc/amount;

        cost += bhc + shc;
        cost += sellPrice*Parms.SellStockTaxRate;

        return cost;
    }
    public static double getNetProfit(double buyPrice, double sellPrice, int amount) {
        return sellPrice - buyPrice - getTradeCost(buyPrice, sellPrice, amount);
    }

    //without considering amount
    public static double getTradeCost(double buyPrice, double sellPrice) {
        double cost = 0.0;

        cost += buyPrice*Parms.BuyStockServiceRate + sellPrice*Parms.SellStockServiceRate;
        cost += sellPrice*Parms.SellStockTaxRate;

        return cost;
    }
    public static double getNetProfit(double buyPrice, double sellPrice) {
        return sellPrice - buyPrice - getTradeCost(buyPrice, sellPrice);
    }

    public static double getMaxBuyPrice(double sellPrice, int amount, double targetYearRate,
            String inDate, String outDate) {
        NatureDates dates = new NatureDates(inDate, outDate);
        int dist = dates.getDistance(inDate, outDate);
        double expProfit = targetYearRate*dist*sellPrice/360;

        double normalSHC = sellPrice*amount*Parms.SellStockServiceRate;
        double shc = normalSHC>MINIMUM_HANDLING_CHARGE ? normalSHC:MINIMUM_HANDLING_CHARGE;
        shc = shc/amount;
        double sum = sellPrice*(1 - Parms.SellStockTaxRate) - expProfit - shc;

        double price0 = sum/(1 + Parms.BuyStockServiceRate);
        double price1 = sum - MINIMUM_HANDLING_CHARGE/amount;

        double tPrice = MINIMUM_HANDLING_CHARGE/(amount*Parms.BuyStockServiceRate);
        //System.out.format("%8.3f %8.3f %8.3f\n", price0, price1, tPrice);

        if(price0 > tPrice)
            return price0;
        else
            return Math.min(price1, tPrice);
    }
    public static double getMinSellPrice(double buyPrice, int amount, double targetYearRate, 
            String inDate, String outDate) {
        NatureDates dates = new NatureDates(inDate, outDate);
        int dist = dates.getDistance(inDate, outDate);
        double expProfit = targetYearRate*dist*buyPrice/360;

        double normalBHC = buyPrice*amount*Parms.BuyStockServiceRate;
        double bhc = normalBHC>MINIMUM_HANDLING_CHARGE ? normalBHC:MINIMUM_HANDLING_CHARGE;
        bhc = bhc/amount;
        double sum = expProfit + buyPrice + bhc;

        double price0 = sum/(1-Parms.SellStockServiceRate-Parms.SellStockTaxRate);
        double price1 = (sum+MINIMUM_HANDLING_CHARGE/amount)/(1-Parms.SellStockTaxRate);

        double tPrice = MINIMUM_HANDLING_CHARGE/(amount*Parms.SellStockServiceRate);
        //System.out.format("%8.3f %8.3f %8.3f\n", price0, price1, tPrice);

        if(price1 < tPrice)
            return price1;
        else
            return Math.max(price0, tPrice);
    }


    //append pankouYear to timeMissingYear, thus forming a time of format
    //    year-month-day hour:min:second
    //timeMissingYear[in]
    //    month-day hour:min:second
    public static long timeWOyear2Long(String timeMissingYear, String pankouYear) {
        String[] list0 = timeMissingYear.split("-"); 
        String sMonth = list0[0]; 
        int month = Integer.valueOf(sMonth);
        String[] list1 = list0[1].split(" "); 
        String sDay = list1[0];
        int day = Integer.valueOf(sDay);
        String[] list2 = list1[1].split(":"); 
        String sHour = list2[0];
        int hour = Integer.valueOf(sHour);
        String sMinute = list2[1];
        int minute = Integer.valueOf(sMinute);
        String sSecond = list2[2];
        int second = Integer.valueOf(sSecond);

        //Year, Month, Day, Hour, Minute, Second
        Calendar cal = getCalendar();
        int year;
        if(pankouYear == null) {
            cal.setTimeInMillis(System.currentTimeMillis());
            year = cal.get(Calendar.YEAR);                             //get current year
        } else {
            year = Integer.valueOf(pankouYear);
        }
        cal.set(year, month-1, day, hour, minute, second);
               
        long millis = cal.getTimeInMillis()/1000;

        return millis;
    }





    public static String getCallerName(Class c) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String methodName = e.getMethodName();

        return c.getSimpleName() + "." + methodName;
    }












    public static Calendar getCalendar() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(sTz), new Locale(sLc));
        return cal;
    }
    public static Calendar getCalendar(String tradeDate) {
        tradeDate = Time.unformalizeYMD(tradeDate);
        int year = Integer.valueOf(tradeDate.substring(0, 4));
        int month = Integer.valueOf(tradeDate.substring(4, 6)) - 1;
        int date = Integer.valueOf(tradeDate.substring(6, 8));

        Calendar cal = getCalendar();
        cal.clear();
        cal.set(year, month, date);

        return cal;
    }

    public static double roundUp(double inD) {
        String sFormat = Settings.getPriceDecimalFormat();
        DecimalFormat df = new DecimalFormat(sFormat);
        df.setRoundingMode(RoundingMode.CEILING);

        //adjust inD to specified Decimal format
        inD = Double.valueOf(df.format(inD));

        return inD;
    }

    //2 kinds of scenratios:
    //  targetRate<=1          [0, 1], percent >100% is not allowed
    //  targetRate>1           (0, ...)
    //Parms:
    //  dist - distance by days
    public static double getTargetProfit(double targetRate, double inPrice, int dist) {
        double targetProfit = inPrice*targetRate*dist/360;

        if(targetRate>1) {
            targetProfit = targetRate - 1;
        }

        return targetProfit;
    }
    //in one year
    public static double getTargetProfit(double targetRate, double inPrice) {
        return getTargetProfit(targetRate, inPrice, 360);
    }

    public static String[][] getPairs(String[] array) {
        String[][] pairs = new String[array.length*(array.length-1)/2][2];

        int k=0;
        for(int i=0; i<array.length; i++) {
            for(int j=i+1; j<array.length; j++) {
                pairs[k][0] = array[i];
                pairs[k][1] = array[j];

                k++;
            }
        }

        return pairs;
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
}
