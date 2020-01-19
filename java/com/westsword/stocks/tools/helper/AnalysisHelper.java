package com.westsword.stocks.tools.helper;

import com.westsword.stocks.am.AmUtils;

public class AnalysisHelper {
    public static void makeTxt(String args[]) {
        if(args.length != 3) {
            usage();
            return;
        }

        String stockCode = args[1];
        String tradeDate = args[2];


        AmUtils amUtils = new AmUtils(stockCode);
        long startAm = amUtils.loadPrevLastAm(tradeDate);
        amUtils.writeAmRecords(startAm, tradeDate);
    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools makeanalysistxt stockCode tradeDate");
        System.exit(-1);
    }
}
