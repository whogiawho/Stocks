package com.westsword.stocks;

public class Stock {
    public final static int TRADE_TYPE_INVALID = 0;

    public final static int TRADE_TYPE_UP = 5;
    public final static int TRADE_TYPE_DOWN = 1;
    public final static int TRADE_TYPE_EXTERNAL = 5;
    public final static int TRADE_TYPE_INTERNAL = 1;
    public final static int TRADE_TYPE_BUY = 5;
    public final static int TRADE_TYPE_SELL = 1;
    public final static int TRADE_TYPE_LONG = 5;
    public final static int TRADE_TYPE_SHORT = 1;

    public final static int LONG_POSITION = 5;
    public final static int SHORT_POSITION = 1;

    public final static int ENTER_TRADE = 0;
    public final static int QUIT_TRADE = 1;

    public final static int SHOU_UNIT = 100;

    public static String getSymbol(int tradeType) {
        String sSymbol = "+";

        if(tradeType == TRADE_TYPE_SHORT)
            sSymbol = "-";

        return sSymbol;
    }
}
