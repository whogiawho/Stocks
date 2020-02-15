package com.westsword.stocks.tools.helper;

import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.Stock;
import com.westsword.stocks.Utils;
import com.westsword.stocks.utils.*;
import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;

public class SSInstancesHelper {

    public void run(String args[]) {
        CommandLine cmd = SSInstanceHelper.getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<4) {
            usage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        String startDate = SSUtils.getStartDate(cmd);
        double threshold = SSUtils.getThreshold(cmd);
        int sTDistance = SSUtils.getNearestOutDist(cmd);
        int tradeType = SSUtils.getTradeType(cmd);

        String tradeDate0 = newArgs[0];              //tradeDate0 must >= startDate
        if(!SSInstanceHelper.checkDates(startDate, tradeDate0)) {
            usage();
            return;
        }
        double targetRate = Double.valueOf(newArgs[2]);
        if(!SSUtils.checkTargetRate(newArgs[2])) {
            usage();
            return;
        }

        //
        boolean bResetLog = SSUtils.getSwitchResetLog(cmd);
        boolean bLog2Files = SSUtils.getSwitchLog2File(cmd);
        boolean bPrintTradeDetails = true;
        AmManager am = new AmManager(stockCode, startDate);
        StockDates stockDates = new StockDates(stockCode);

        int maxCycle = Integer.valueOf(newArgs[1]);
        String sTradeSumFile = newArgs[3];

        TradeSumLoader l = new TradeSumLoader();
        ArrayList<TradeSum> list = new ArrayList<TradeSum>();
        l.load(sTradeSumFile, list);
        SSInstanceHelper ssi = new SSInstanceHelper();

        for(int i=0; i<list.size(); i++) {
            TradeSum r = list.get(i);
            String hmsList = r.hmsList;

            if(tradeDate0.equals(r.tradeDate)) {
                System.out.format("%8s %8s %8.2f %4d %4d %8s %8s %4d %8.3f\n",
                        stockCode, startDate, threshold, sTDistance, tradeType,
                        tradeDate0, hmsList, maxCycle, targetRate);
                ssi._run(stockCode, startDate, threshold, sTDistance, tradeType,
                        tradeDate0, hmsList, maxCycle, targetRate,
                        am, stockDates, 
                        bLog2Files, bResetLog, bPrintTradeDetails);
            }
        }
    }



    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssinstances [-rncdhts] tradeDate maxCycle targetRate sTradeSumFile");
        System.err.println("       tradeDate   ; tradeDate>=startDate");
        System.err.println("       targetRate  ; something like [0-9]{1,}.[0-9]{1,3}");
        System.err.println("                       relative(<=1): targetRate"); 
        System.err.println("                       absolute(>1) : targetRate-1");
        System.err.println("       -r          ; reset tradeDetails log file and tradeSum item");
        System.err.println("       -n          ; does not log to files");
        System.err.println("       -c stockCode;");
        System.err.println("       -d startDate;");
        System.err.println("       -h threshold;");
        System.err.println("       -t       0|1; nearest day to end trade session");
        System.err.println("       -s tradeType;");
        System.exit(-1);
    }

}
