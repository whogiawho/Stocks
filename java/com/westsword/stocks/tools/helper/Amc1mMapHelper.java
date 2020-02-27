package com.westsword.stocks.tools.helper;


import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.StockPaths;
import com.westsword.stocks.base.time.TradeDates;
import com.westsword.stocks.base.ckpt.CheckPoint0;

public class Amc1mMapHelper {

    public void make(String[] args) {
        if(args.length != 3) {
            usage();
            return;
        }

        String stockCode = args[1];
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode);
        AmManager am = new AmManager(stockCode);

        String tradeDateList = args[2];
        String[] fields = tradeDateList.split(" +");
        for(int i=0; i<fields.length; i++) {
            String tradeDate0 = fields[i];
            _make(stockCode, tradeDate0, sTradeDates, am);
        }
    }

    private void _make(String stockCode, String tradeDate0, 
            String[] sTradeDates, AmManager am) {
        String amcorrelMapDir = StockPaths.getAmCorrelMapDir(stockCode, tradeDate0);
        Utils.mkDir(amcorrelMapDir);

        ConcurrentHashMap<String, Double> amCorrelMap0 = AmcMap.load(stockCode, tradeDate0);

        CheckPoint0 ckpt0 = new CheckPoint0();
        int length = ckpt0.getLength();
        Combinations c = new Combinations(length, 2);

        for(int i=0; i<sTradeDates.length; i++) {
            String tradeDate1 = sTradeDates[i];
            ConcurrentHashMap<String, Double> amCorrelMap1 = AmcMap.load(stockCode, tradeDate1, tradeDate0);
            String sOutFile = StockPaths.getAmCorrelMapFile(stockCode, tradeDate0, tradeDate1);

            Iterator<int[]> itr = c.iterator();
            while(itr.hasNext()) {
                int[] e = itr.next();
                String hmsList = ckpt0.getHMSList(e);                     //
                String[] fields = hmsList.split("_");
                setAmCorrel2File(stockCode, tradeDate0, tradeDate1, fields[0], fields[1], 
                        am, sOutFile, amCorrelMap0, amCorrelMap1);
            }
        }
    }
    private void setAmCorrel2File(String stockCode, String tradeDate0, String tradeDate1,
            String startHMS, String endHMS, AmManager am, String sOutFile,
            ConcurrentHashMap<String, Double> amCorrelMap0, ConcurrentHashMap<String, Double> amCorrelMap1) {
        //<tradeDate0,tradeDate1>'s check
        String key0 = Utils.getAmcKey(tradeDate0, tradeDate1, startHMS, endHMS);
        if(amCorrelMap0.get(key0)!=null)
            return;

        double amCorrel = 0.0; 
        String key1 = Utils.getAmcKey(tradeDate1, tradeDate0, startHMS, endHMS);
        if(amCorrelMap1.get(key1)!=null) {      //<tradeDate1,tradeDate0>'s check
            amCorrel = amCorrelMap1.get(key1);
        } else {
            amCorrel = am.getAmCorrel(tradeDate0, tradeDate1, startHMS, endHMS);
        }

        String sFormat = "%35s %8.3f\n";
        String line = String.format(sFormat, key0, amCorrel);
        Utils.append2File(sOutFile, line);
    }





    private static void usage() {
        System.err.println("usage: java AnalyzeTools make1mamcmap stockCode tradeDateList");
        System.err.println("       for 1min ckpts pair startHMS,endHMS, make amcorrel map for each tradeDate0 of tradeDateList:");
        System.err.println("         <tradeDate0,tradeDate1,startHMS,endHMS>");
        System.err.println("         tradeDate1 - loop all tradeDates");
        System.exit(-1);
    }

}
