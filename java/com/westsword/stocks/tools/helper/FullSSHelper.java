package com.westsword.stocks.tools.helper;

import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.Stock;
import com.westsword.stocks.Utils;
import com.westsword.stocks.utils.*;
import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.Regression;

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

        SSInstanceHelper ssi = new SSInstanceHelper();
        //loop tradeDates [startDate, lastTradeDate]
        //loop hmsList combination(n,2)
        String[] fields0 = sMaxCycleList.split(" +");
        String[] fields1 = sTargetRateList.split(" +");
        for(int i=0; i<fields0.length; i++) {
            int maxCycle = Integer.valueOf(fields0[i]);
            for(int j=0; j<fields1.length; j++) {
                double sTargetRate = Double.valueOf(fields1[j]);
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
