package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Combinations;
import org.apache.commons.math3.stat.descriptive.moment.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.utils.AnsiColor;
import com.westsword.stocks.utils.LineLoader;
import com.westsword.stocks.base.ckpt.CheckPoint0;

public class SSGroupHelper {

    public void listChar(String args[]) {
        if(args.length!=4) {
            usage1();
            return;
        }

        String stockCode = args[1];
        String hmsList = args[2];
        if(!SSUtils.checkHMSList(hmsList, 2, 2)) {
            usage1();
            return;
        }
        String[] hms = hmsList.split("_");


        LineLoader loader = new LineLoader();
        ArrayList<String> tradeDateList= new ArrayList<String>();
        loader.load(tradeDateList, args[3]);
        AmManager am = new AmManager(stockCode, tradeDateList);

        MinStdDevR r = new MinStdDevR(stockCode, hmsList);
        r.get(tradeDateList, hms, am);
        r.print();
    }


    public void listChars(String args[]) {
        if(args.length!=3) {
            usage2();
            return;
        }

        String stockCode = args[1];


        LineLoader loader = new LineLoader();
        ArrayList<String> tradeDateList= new ArrayList<String>();
        loader.load(tradeDateList, args[2]);
        AmManager am = new AmManager(stockCode, tradeDateList);


        CheckPoint0 ckpt = new CheckPoint0();
        int length = ckpt.getLength();
        Combinations c = new Combinations(length, 2);
        Iterator<int[]> itr = c.iterator();
        while(itr.hasNext()) {
            int[] e = itr.next();
            String hmsList = ckpt.getHMSList(e);                     //
            String[] hms = hmsList.split("_");

            MinStdDevR r = new MinStdDevR(stockCode, hmsList);
            r.get(tradeDateList, hms, am);
            r.print();
        }
    }


    private static void usage1() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssgroupchar stockCode hmsList sTradeDateFile");
        System.err.println("       sTradeDateFile ; one tradeDate each line");

             String line = "       hmsList        ; ";
                                    String line1 = "make sure only 2 hms exists, like hhmmss_hhmmss";
        line1 = AnsiColor.getColorString(line1, AnsiColor.ANSI_RED);
        line += line1;

        System.err.println(line);
        System.exit(-1);
    }
    private static void usage2() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssgroupchars stockCode sTradeDateFile");
        System.err.println("       sTradeDateFile ; one tradeDate each line");
        System.exit(-1);
    }


    public static class MinStdDevR {
        public String stockCode;
        public String hmsList;

        public String minTradeDate;
        public double minAvgAmCorrel;
        public double minStdDev;

        public MinStdDevR(String stockCode, String hmsList) {
            this.stockCode = stockCode;
            this.hmsList = hmsList;

            minTradeDate = "";
            minAvgAmCorrel = Double.NaN;
            minStdDev = Double.POSITIVE_INFINITY;
        }

        public void print() {
            System.out.format("%s %s %s %8.3f %8.3f\n", 
                    stockCode, hmsList, minTradeDate, minAvgAmCorrel, minStdDev);
        }

        public void get(ArrayList<String> tradeDateList, String[] hms, AmManager am) {
            StandardDeviation sd = new StandardDeviation();
            ArrayList<Double> amcorrelList = new ArrayList<Double>();
            for(int i=0; i<tradeDateList.size(); i++) {
                String tradeDate0 = tradeDateList.get(i);
                amcorrelList.clear();

                double avgAmCorrel = getAmCorrels(tradeDate0, tradeDateList, hms, am, amcorrelList);

                Double[] sds = amcorrelList.toArray(new Double[0]);
                double stddev = sd.evaluate(ArrayUtils.toPrimitive(sds));
                if(stddev<this.minStdDev) {
                    this.minStdDev = stddev;
                    this.minAvgAmCorrel = avgAmCorrel;
                    this.minTradeDate = tradeDate0;
                }
            }
        }

        //for each tradeDate1 in tradeDateList
        //  get a series of amcorrel for [tradeDate0, tradeDate1], and save to amcorrelList
        //return an avg of amcorrelList
        //amcorrelList: [out]
        private double getAmCorrels(String tradeDate0, ArrayList<String> tradeDateList, String[] hms, 
                AmManager am, ArrayList<Double> amcorrelList) {
            double avgAmCorrel = 0;
            int count = 0;
            for(int j=0; j<tradeDateList.size(); j++) {
                String tradeDate1 = tradeDateList.get(j);

                Double amcorrel = getAmCorrel(tradeDate0, tradeDate1, hms[0], hms[1], am);
                if(amcorrel != Double.NaN) {
                    avgAmCorrel += amcorrel;
                    count++;
                    amcorrelList.add(amcorrel);
                }
            }
            avgAmCorrel = avgAmCorrel/count;

            return avgAmCorrel;
        }
    }

    public static double getAmCorrel(String tradeDate0, String tradeDate1, String startHMS, String endHMS, 
            AmManager am) {
        return am.getAmCorrel(tradeDate0, tradeDate1, startHMS, endHMS);
    }
}
