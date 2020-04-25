package com.westsword.stocks.base.utils;


import com.westsword.stocks.base.Stock;

public class AnsiColor {
    //something like \\uxxxx is a Unicode char with hex value xxxx
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String specialStringUp=String.format("%s%s%s", ANSI_RED, "↑", ANSI_RESET);
    public static final String specialStringDown=String.format("%s%s%s", ANSI_GREEN, "↓", ANSI_RESET);
    public static final String specialStringUnknown=String.format("%s%s%s", ANSI_BLUE, "？", ANSI_RESET);


    //return yellow if tradeType==TRADE_TYPE_INVALID
    public static String getColorString(String src, int tradeType) {
        String sColor = ANSI_YELLOW;
        if(tradeType == Stock.TRADE_TYPE_LONG)
            sColor = ANSI_RED;
        else if(tradeType == Stock.TRADE_TYPE_SHORT)
            sColor = ANSI_GREEN;
            
        return getColorString(src, sColor);
    }
    public static String getColorString(String src, String sColor) {
        return String.format("%s%s%s", sColor, src, ANSI_RESET);
    }
    public static String getColorDirection(int a) {
        if(a>0)
            return specialStringUp;
        else if(a<0)
            return specialStringDown;
        else
            return specialStringUnknown;
    }
    public static String getColorDirection(int a, int b) {
        return getColorDirection(a-b);
    }
    public static String getColorDirection(double a) {
        if(a>0.0)
            return specialStringUp;
        else if(a<0.0)
            return specialStringDown;
        else
            return specialStringUnknown;
    }
    public static String getColorDirection(double a, double b) {
        return getColorDirection(a-b);
    }
}
