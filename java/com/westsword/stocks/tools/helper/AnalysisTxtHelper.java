package com.westsword.stocks.tools.helper;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.Time;

public class AnalysisTxtHelper {
    public static void getPrice(String args[]) {
        if(args.length != 4) {
            if(args[0].equals("getupprice"))
                usage2();
            else
                usage3();
            return;
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        String[] tradeDates = new String[] {
            tradeDate,
        };
        String hms = args[3];
        AmManager am = new AmManager(stockCode, tradeDates, true);
        long tp = Time.getSpecificTime(tradeDate, hms);
        double price=0.0;
        if(args[0].equals("getupprice"))
            price = am.getUpPrice(tp); 
        else
            price = am.getDownPrice(tp); 

        System.out.format("%8.3f\n", price);
    }

    private static void usage2() {
        System.err.println("usage: java AnalyzeTools getupprice stockCode tradeDate hms");
        System.exit(-1);
    }
    private static void usage3() {
        System.err.println("usage: java AnalyzeTools getdownprice stockCode tradeDate hms");
        System.exit(-1);
    }
}
