package com.westsword.stocks.tools.helper;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.StockDates;

public class AmHelper {
    public static void getAm(String args[]) {
        if(args.length != 4) {
            usage();
            return;
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        String hmsList = args[3];

        StockDates stockDates = new StockDates(stockCode);
        String[] tradeDates = new String[] {
            tradeDate,
        };

        AmManager am = new AmManager(stockCode, tradeDates, true);
        long aM = am.getAm(tradeDate, hmsList);

        System.out.format("%d\n", aM);
    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools getam stockCode tradeDate hmsList");
        System.exit(-1);
    }
}
