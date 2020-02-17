package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.Utils;

public class TradeSumLogThread extends Thread {
    private String tradeDate;
    private String sTradeSumFile;
    private String hmsList;
    private SSInstance.BufR br;
    private boolean bLog2TradeSumFile;
    private boolean bStdout;

    public TradeSumLogThread(String tradeDate, String sTradeSumFile,
            String hmsList, SSInstance.BufR br, boolean bLog2TradeSumFile, boolean bStdout) {
        this.tradeDate = tradeDate;
        this.sTradeSumFile = sTradeSumFile;
        this.hmsList = hmsList;
        this.br = br;
        this.bLog2TradeSumFile = bLog2TradeSumFile;
        this.bStdout = bStdout;
    }

    //notes:
    //1. starting a new thread to write does not speed up
    //2. FileOutputStream of windows version seems to support writing the same file from multithreads
    public void run() {
        write(tradeDate, sTradeSumFile,
                hmsList, br, bLog2TradeSumFile, bStdout);
    }



    public static void write(String tradeDate, String sTradeSumFile,
            String hmsList, SSInstance.BufR br, boolean bLog2TradeSumFile, boolean bStdout) {
        double winRate, avgNetRevenue, avgMaxRevenue, expRisk0, expRisk1;
        int matchedCnt = br.getMatchedCount();
        if(matchedCnt!=0) {
            String sMatchedTradeDates = br.sMatchedTradeDates.trim();
            sMatchedTradeDates = sMatchedTradeDates.replaceAll(" ", ",");
            winRate = ((double)br.okCount)/matchedCnt;
            avgNetRevenue = br.netRevenue/matchedCnt;
            avgMaxRevenue = br.maxRevenue/matchedCnt;
            expRisk0 = br.failCount==0?Double.NaN:br.risk0/br.failCount;
            expRisk1 = br.okCount==0?Double.NaN:br.risk1/br.okCount;
            hmsList = hmsList.replaceAll(":", "");
            hmsList = hmsList.replaceAll(" ", "_");
            String sFormat = "%s %4d %8.1f%% %8.3f %8.3f %8.3f %8.3f %s %s %8.3f\n";
            String line = String.format(sFormat, 
                    tradeDate, matchedCnt, winRate*100, avgNetRevenue, avgMaxRevenue, expRisk0, expRisk1, 
                    sMatchedTradeDates, hmsList, br.netRevenue/br.maxHangCount);

            if(bLog2TradeSumFile)
                Utils.append2File(sTradeSumFile, line);
            if(bStdout)
                System.out.format("%s", line);
        }
    }
}
