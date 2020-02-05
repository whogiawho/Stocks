package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.*;

import org.apache.commons.cli.*;

public class SSUtils {
    public final static String Default_StockCode = "600030";
    public final static double Default_Threshold = 0.9;
    public final static String Default_StartDate = "20160108";
    public final static int Default_Nearest_Day_To_End_TradeSession = 1;          //t0|t1
    public final static int Default_TradeType = 5;                                //Long(5)|Short(1)


    public static String getString(CommandLine cmd, String sSwitch, String sDefault) {
        String string = sDefault;
        if(cmd.hasOption(sSwitch))
            string = cmd.getOptionValue(sSwitch);

        return string;
    }
    public static int getInteger(CommandLine cmd, String sSwitch, int sDefault) {
        int i = sDefault;
        if(cmd.hasOption(sSwitch))
            i = Integer.valueOf(cmd.getOptionValue(sSwitch));

        return i;
    }
    public static double getDouble(CommandLine cmd, String sSwitch, double sDefault) {
        double d = sDefault;
        if(cmd.hasOption(sSwitch))
            d = Double.valueOf(cmd.getOptionValue(sSwitch));

        return d;
    }
    public static boolean getBoolean(CommandLine cmd, String sSwitch, boolean sDefault) {
        boolean bSwitch = sDefault;
        if(cmd.hasOption(sSwitch))
            bSwitch = !sDefault;

        return bSwitch;
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
    public static int getNearestOutDist(CommandLine cmd) {
        return getInteger(cmd, "t", Default_Nearest_Day_To_End_TradeSession);
    }
    public static int getTradeType(CommandLine cmd) {
        return getInteger(cmd, "s", Default_TradeType);
    }
    public static boolean getSwitchLog2File(CommandLine cmd) {
        return getBoolean(cmd, "n", true);
    }
    public static boolean getSwitchResetLog(CommandLine cmd) {
        return getBoolean(cmd, "r", false);
    }

    public static ArrayList<String> getSimilarTradeDates(String stockCode, String startDate, double threshold, 
            String tradeDate, String hmsList, AmManager am) {
        ArrayList<String> tradeDateList = new ArrayList<String>();

        String[] hms = hmsList.split("_");
        hms[0] = HMS.formalize(hms[0]);
        TradeDates tradeDates = new TradeDates(stockCode);
        String tradeDate0 = startDate;
        while(tradeDate0 != null) {
            String sAmCorrels = "";
            boolean bHMSMatched = true;
            for(int i=1; i<hms.length; i++) {
                hms[i] = HMS.formalize(hms[i]);
                double amcorrel = am.getAmCorrel(tradeDate0, tradeDate, hms[0], hms[i]);
                sAmCorrels += String.format("%8.3f ", amcorrel);
                if(amcorrel < threshold) {
                    bHMSMatched = false;
                    break;
                }
            }
            if(bHMSMatched)
                tradeDateList.add(tradeDate0);
            System.out.format("%s %s %s %s\n", 
                    tradeDate0, tradeDate, hmsList, sAmCorrels);

            tradeDate0 = tradeDates.nextDate(tradeDate0);
        }

        return tradeDateList;
    }

    public static String getInHMS(String hmsList) {
        String[] fields = HMS.getHMSArray(hmsList);
        String inHMS = fields[fields.length-1];

        return inHMS;
    }
}
