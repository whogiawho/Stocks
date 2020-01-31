package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.*;

import org.apache.commons.cli.*;

public class SSUtils {
    public final static String Default_StockCode = "600030";
    public final static double Default_Threshold = 0.9;
    public final static String Default_StartDate = "20160108";
    public final static int Default_Nearest_Day_To_End_TradeSession = 1;
    public final static int Default_TradeType = 5;


    public static String getString(CommandLine cmd, String sSwitch, String sDefault) {
        String string = sDefault;
        if(cmd.hasOption(sSwitch))
            string = cmd.getOptionValue(sSwitch);

        return string;
    }
    public static int getInteger(CommandLine cmd, String sSwitch, int sDefault) {
        int i = sDefault;
        if(cmd.hasOption(sSwitch))
            i = new Integer(cmd.getOptionValue(sSwitch));

        return i;
    }
    public static double getDouble(CommandLine cmd, String sSwitch, double sDefault) {
        double d = sDefault;
        if(cmd.hasOption(sSwitch))
            d = new Double(cmd.getOptionValue(sSwitch));

        return d;
    }


    public static String getStockCode(CommandLine cmd) {
        return getString(cmd, "c", Default_StockCode);
    }
    public static double getThreshold(CommandLine cmd) {
        return getDouble(cmd, "h", Default_Threshold);
    }
    public static String getStartDate(CommandLine cmd) {
        return getString(cmd, "d", Default_StartDate);
    }
    public static int getNearestDist(CommandLine cmd) {
        return getInteger(cmd, "t", Default_Nearest_Day_To_End_TradeSession);
    }
    public static int getTradeType(CommandLine cmd) {
        return getInteger(cmd, "s", Default_TradeType);
    }

    public static ArrayList<String> getSimilarTradeDates(String stockCode, String sPair, double threshold, String startDate, String tradeDate, AmManager am) {
        ArrayList<String> tradeDateList = new ArrayList<String>();

        String[] hms = sPair.split("_");
        hms[0] = HMS.formalize(hms[0]);
        hms[1] = HMS.formalize(hms[1]);
        TradeDates tradeDates = new TradeDates(stockCode);
        String tradeDate0 = startDate;
        while(tradeDate0 != null) {
            double amcorrel = am.getAmCorrel(tradeDate0, tradeDate, hms[0], hms[1]);
            if(amcorrel >= threshold)
                tradeDateList.add(tradeDate0);
            System.out.format("%s %s %s %s %8.3f\n", 
                    tradeDate0, tradeDate, hms[0], hms[1], amcorrel);

            tradeDate0 = tradeDates.nextDate(tradeDate0);
        }

        return tradeDateList;
    }
}
