package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.cli.*;
import com.mathworks.engine.MatlabEngine;
import java.util.concurrent.ExecutionException;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.AnsiColor;
import com.westsword.stocks.base.time.*;

public class SSInstancesHelper {

    public void run(String args[]) {
        CommandLine cmd = getCommandLine(args);
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

        double targetRate = Double.valueOf(newArgs[1]);
        if(!SSUtils.checkTargetRate(newArgs[1])) {
            usage();
            return;
        }
        int maxCycle = Integer.valueOf(newArgs[0]);

        String sTradeSumFile = SSUtils.getTradeSumFile(cmd);
        String hmsList0 = SSUtils.getHMSList(cmd);
        if(sTradeSumFile==null&&hmsList0==null||sTradeSumFile!=null&&hmsList0!=null) {
            usage();
            return;
        }

        //
        boolean bResetLog = SSUtils.getSwitchResetLog(cmd);
        boolean bLog2Files = SSUtils.getSwitchLog2File(cmd);
        boolean bStdout = SSUtils.getSwitchStdout(cmd);
        AmManager am = new AmManager(stockCode, startDate);
        StockDates stockDates = new StockDates(stockCode);

        ArrayList<TradeSum> list = getTradeSumList(stockCode, startDate, sTradeSumFile, hmsList0);
        double[][] corrM = getCorrMatrix(stockCode, startDate, sTradeSumFile, hmsList0, am);

        SSiManager ssim = new SSiManager();
        //loop the tradeSumList 
        for(int i=0; i<list.size(); i++) {
            TradeSum ts = list.get(i);
            String hmsList = ts.hmsList;

            System.out.format("%8s %8s %8.2f %4d %4d %8s %8s %4d %8.3f\n",
                    stockCode, startDate, threshold, sTDistance, tradeType,
                    ts.tradeDate, hmsList, maxCycle, targetRate);
            SSInstance r = new SSInstance(stockCode, startDate, threshold, sTDistance, tradeType, 
                    ts.tradeDate, hmsList, maxCycle, targetRate);
            ssim.run(r, am, stockDates,
                    bLog2Files, bResetLog, bStdout);
        }
    }

    private static double[][] getCorrMatrix(String stockCode, String startDate, 
            String sTradeSumFile, String hmsList, AmManager am) {
        double[][] m = null;

        try {
            if(sTradeSumFile == null) {
                MatlabEngine eng = MatlabEngine.startMatlab();
                String[] sTradeDates = new TradeDates(stockCode, startDate).getAllDates();
                m = am.getCorrMatrix(hmsList, sTradeDates, eng);
                eng.close();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return m;
    }
    private static ArrayList<TradeSum> getTradeSumList(String stockCode, String startDate,
            String sTradeSumFile, String hmsList) {
        ArrayList<TradeSum> list = new ArrayList<TradeSum>();

        if(sTradeSumFile != null) {
            TradeSumLoader l = new TradeSumLoader();
            l.load(sTradeSumFile, list);
        } else {
            String[] sTradeDates = new TradeDates(stockCode, startDate).getAllDates();
            for(int i=0; i<sTradeDates.length; i++) 
                list.add(new TradeSum(sTradeDates[i], hmsList));
        }

        return list;
    }


    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssinstances [-rnocdhtsfm] maxCycle targetRate");
        System.err.println("       targetRate      ; something like [0-9]{1,}.[0-9]{1,3}");
        System.err.println("                       relative(<=1): targetRate"); 
        System.err.println("                       absolute(>1) : targetRate-1");

             String line = "       only one, either -f or -m, can be enabled!";
        line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
        System.err.println(line);
        SSInstanceHelper.commonUsageInfo();

        System.err.println("       -f sTradeSumFile; [tradeDate, hmsList] in the file; exclusive to -m");
        System.err.println("       -m hmsList      ; loop all [tradeDate, hmsList]; exclusive to -f");
        System.exit(-1);
    }

    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = SSInstanceHelper.getOptions();

            options.addOption("f", true,  "a tradeSum file");
            options.addOption("m", true,  "hmsList");
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }

}
