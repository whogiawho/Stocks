package com.westsword.stocks;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import com.westsword.stocks.base.time.Time;

import org.apache.commons.io.FileUtils;

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
        try{
            File file = new File(path);
            
            if(file.delete()){
                System.out.format("%s\n", file.getName() + " is deleted!");
            }else{
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
        System.out.format("%8.3f %8.3f %8.3f\n", price0, price1, tPrice);

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


    public static String getCallerName(Class c) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String methodName = e.getMethodName();

        return c.getSimpleName() + "." + methodName;
    }















    public static Calendar getCalendar(String tradeDate) {
        tradeDate = Time.unformalizeYMD(tradeDate);
        int year = new Integer(tradeDate.substring(0, 4));
        int month = new Integer(tradeDate.substring(4, 6)) - 1;
        int date = new Integer(tradeDate.substring(6, 8));

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, date);

        return cal;
    }

}
