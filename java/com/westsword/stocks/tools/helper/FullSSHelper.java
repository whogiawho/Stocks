package com.westsword.stocks.tools.helper;

import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.Stock;
import com.westsword.stocks.utils.*;
import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.Regression;
import com.westsword.stocks.base.ckpt.*;

import org.apache.commons.math3.util.Combinations;

public class FullSSHelper {
    public void run(String args[]) {
        CommandLine cmd = SSInstanceHelper.getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<2) {
            usage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        String startDate = SSUtils.getStartDate(cmd);
        double threshold = SSUtils.getThreshold(cmd);
        int sTDistance = SSUtils.getNearestOutDist(cmd);
        int tradeType = SSUtils.getTradeType(cmd);
        //
        boolean bResetLog = SSUtils.getSwitchResetLog(cmd);
        boolean bLog2Files = SSUtils.getSwitchLog2File(cmd);
        boolean bPrintTradeDetails = true;
        AmManager am = new AmManager(stockCode);
        StockDates stockDates = new StockDates(stockCode);

        String sMaxCycleList = newArgs[0];
        String sTargetRateList = newArgs[1];
        if(!SSUtils.checkTargetRateList(sTargetRateList)) {
            usage();
            return;
        }

        String[] fields0 = sMaxCycleList.split(" +");
        String[] fields1 = sTargetRateList.split(" +");
        CheckPoint0 ckpt = new CheckPoint0();
        int length = ckpt.getLength();
        Combinations c = new Combinations(length, 2);
        SSInstanceHelper ssih = new SSInstanceHelper();
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode);
        TradeDates tradeDates = new TradeDates(stockCode, startDate, sTradeDates[sTradeDates.length-1]);

        //loop tradeDates [startDate, lastTradeDate]
        String tradeDate0 = tradeDates.firstDate();
        while(tradeDate0!=null) {
            tradeDate0 = tradeDates.nextDate(tradeDate0);                //
            //loop hmsList combination(n,2)
            Iterator<int[]> itr = c.iterator();
            while(itr.hasNext()) {
                int[] e = itr.next();
                String hmsList = ckpt.getHMSList(e);                     //
                //loop maxCycleList
                for(int i=0; i<fields0.length; i++) {
                    int maxCycle = Integer.valueOf(fields0[i]);          //
                    //loop targetRateList
                    for(int j=0; j<fields1.length; j++) {
                        double targetRate = Double.valueOf(fields1[j]);  //

                        ssih._run(stockCode, startDate, threshold, sTDistance, tradeType,
                                tradeDate0, hmsList, maxCycle, targetRate,
                                am, stockDates,
                                bLog2Files, bResetLog, bPrintTradeDetails);
                    }
                }
            }
        }
    }



    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"getfullss [-rncdhts] maxCycleList targetRateList");
        System.err.println("       targetRateList; see ssinstance usage for details");
        System.err.println("       -r            ; reset tradeDetails log file and tradeSum item");
        System.err.println("       -n            ; does not log to files");
        System.err.println("       -c stockCode  ;");
        System.err.println("       -d startDate  ;");
        System.err.println("       -h threshold  ;");
        System.err.println("       -t       0|1  ; nearest day to end trade session");
        System.err.println("       -s tradeType  ;");
        System.exit(-1);
    }
}
