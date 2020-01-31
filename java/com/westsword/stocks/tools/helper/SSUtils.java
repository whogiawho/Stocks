package com.westsword.stocks.tools.helper;

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
}
