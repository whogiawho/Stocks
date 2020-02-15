package com.westsword.stocks.tools.helper;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.StockDates;

public class AmCorrelHelper {
    public static void getAmCorrel(String args[]) {
        if(args.length != 5) {
            usage();
            return;
        }

        String stockCode = args[1];
        String tradeDate1 = args[2];
        String tradeDate2 = args[3];
        String hmsList = args[4];

        StockDates stockDates = new StockDates(stockCode);
        String[] tradeDates = new String[] {
            tradeDate1,
            stockDates.nextDate(tradeDate1),
            tradeDate2,
            stockDates.nextDate(tradeDate2),
        };

        AmManager am = new AmManager(stockCode, tradeDates);

        String sAmCorrel = "";
        String[] fields = hmsList.split("_");
        String startHMS = fields[0];
        for(int i=1; i<fields.length; i++) {
            String endHMS = fields[i];
            double amcorrel = am.getAmCorrel(tradeDate1, tradeDate2, startHMS, endHMS); 
            sAmCorrel += String.format("%8.3f", amcorrel);
        }
        System.out.format("%s\n", sAmCorrel);
    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools getamcorrel stockCode tradeDate1 tradeDate2 hmsList");
        System.exit(-1);
    }
}
