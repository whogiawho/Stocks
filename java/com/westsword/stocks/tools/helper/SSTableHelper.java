package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.am.*;
import com.westsword.stocks.analyze.ssanalyze.*;

public class SSTableHelper {

    public static void checkSSTable(String[] args) {
        if(args.length < 4 ) {
            usage();
            return;
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        String ssTableName = args[3];

        String sSSTable = StockPaths.getSSTableFile(ssTableName);
        if(!Utils.existFile(sSSTable)) {
            String line = String.format("%s does not exist!\n", sSSTable);
            line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
            System.out.format("%s", line);
            System.exit(-1);
        }

        SSTableLoader loader = new SSTableLoader();
        ArrayList<SSTableRecord> sstrList = new ArrayList<SSTableRecord>();
        loader.load(sstrList, sSSTable, ssTableName);

        ArrayList<String> tradeDateList = new ArrayList<String>();
        for(int i=0; i<sstrList.size(); i++) {
            SSTableRecord sstr = sstrList.get(i);
            tradeDateList.addAll(sstr.getTradeDates());
        }
        tradeDateList.add(tradeDate);
        AmManager am = new AmManager(stockCode, tradeDateList);

        for(int i=0; i<sstrList.size(); i++) {
            SSTableRecord sstr = sstrList.get(i);
            double[] ret = new double[sstr.getComponentSize()];
            //System.out.format("size=%d\n", sstr.getComponentSize());

            boolean bEval = sstr.eval(am, tradeDate, ret);
            String sAmCorrels = sstr.getAmCorrels(ret);
            sAmCorrels = AnsiColor.getColorString(sAmCorrels, AnsiColor.ANSI_RED);
            double inPrice = sstr.getInPrice(am, tradeDate);
            if(bEval) {
                String sFormat = "%-30s %s %4d %4d " + 
                                 "%8.3f %8.3f %s\n";
                System.out.format(sFormat, 
                        sstr.sTableName, sstr.sMatchExp, sstr.sTDistance, sstr.tradeType, 
                        inPrice, sstr.threshold, sAmCorrels);
            }
        }
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools checksstable stockCode tradeDate ssTableName");

        System.exit(-1);
    }
}
