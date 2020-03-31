package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.cli.*;
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.StockPaths;
import com.westsword.stocks.base.time.StockDates;
import com.westsword.stocks.base.ckpt.CheckPoint0;

public class SSDatesHelper {

    public void make(String[] args) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<1) {
            usage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        double threshold = SSUtils.getThreshold(cmd);

        String tradeDate0 = newArgs[0];

        StockDates stockDates = new StockDates(stockCode);
        String startDate = stockDates.firstDate();                    //always the first of StockDates 
        AmManager am = new AmManager(stockCode);

        String sDir = StockPaths.getSSDatesDir(stockCode, threshold, tradeDate0); 
        Utils.mkDir(sDir);

        SSDatesManager ssdm = new SSDatesManager();
        CheckPoint0 ckpt = new CheckPoint0();
        int length = ckpt.getLength();
        Combinations c = new Combinations(length-1, 2);     //exclude the last ckpt
        //loop hmsList combination(n,2)
        Iterator<int[]> itr = c.iterator();
        while(itr.hasNext()) {
            int[] e = itr.next();
            String hmsList = ckpt.getHMSList(e);                     //

            SSDates ssd = new SSDates(stockCode, startDate, threshold, tradeDate0, hmsList);
            ssdm.run(ssd, am);
        }
    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools makessdates [-ch] tradeDate");
        System.err.println("       list all similar tradeDates for the (tradeDate, hmsList)");
        System.err.println("       -c stockCode;");
        System.err.println("       -h threshold;");
        System.exit(-1);
    }

    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = new Options();
            options.addOption("c", true,  "a stock's code");
            options.addOption("h", true,  "a threshold value to get ss for tradeDates");
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }
}
