package com.westsword.stocks.tools.helper;

import com.westsword.stocks.am.AmUtils;

public class AnalysisHelper {
    public static void makeTxt(String args[]) {
        if(args.length != 3 && args.length != 2 && args.length !=4) {
            usage();
            return;
        }

        String stockCode = args[1];
        String tradeDate1 = null;
        if(args.length >= 3)
            tradeDate1 = args[2];
        String tradeDate2 = null;
        if(args.length >= 4)
            tradeDate2 = args[3];

        AmUtils amUtils = new AmUtils(stockCode);
        if(args.length == 3) {
            long startAm = amUtils.loadPrevLastAm(tradeDate1);
            amUtils.writeAmRecords(startAm, tradeDate1);
        } else if(args.length == 2) {
            amUtils.writeAllAmRecords();
        } else {
            amUtils.writeAmRecords(tradeDate1, tradeDate2);
        }
    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools makeanalysistxt stockCode [tradeDate1] [tradeDate2]");
        System.err.println("  no tradeDate1&tradeDate2 - all tradeDates");
        System.err.println("  tradeDate1               - single tradeDate1");
        System.err.println("  tradeDate1&tradeDate2    - [tradeDate1, tradeDate2)");
        System.exit(-1);
    }
}
