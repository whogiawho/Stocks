package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.am.AmcMap;
import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.am.ThreadRemoveAmc;

public class MinStdDevR {
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

    public void get(ArrayList<String> tradeDateList, String hmsList, AmManager am) {
        String[] sTradeDates = tradeDateList.toArray(new String[0]);
        StandardDeviation sd = new StandardDeviation();
        ArrayList<Double> amcorrelList = new ArrayList<Double>();
        CmManager cm = new CmManager();
        double[][] m = cm.getCorrMatrix(sTradeDates, hmsList, am);

        for(int i=0; i<tradeDateList.size(); i++) {
            String tradeDate0 = tradeDateList.get(i);
            amcorrelList.clear();

            double avgAmCorrel = getAmCorrels(tradeDate0, tradeDateList, m, amcorrelList);

            Double[] sds = amcorrelList.toArray(new Double[0]);
            double stddev = sd.evaluate(ArrayUtils.toPrimitive(sds));
            //System.out.format("%s %8.3f %8.3f\n", tradeDate0, avgAmCorrel, stddev);
            if(stddev<this.minStdDev) {
                this.minStdDev = stddev;
                this.minAvgAmCorrel = avgAmCorrel;
                this.minTradeDate = tradeDate0;
            }
        }
    }
    public void get(ArrayList<String> tradeDateList, String[] hms, AmManager am) {
        StandardDeviation sd = new StandardDeviation();
        ArrayList<Double> amcorrelList = new ArrayList<Double>();
        for(int i=0; i<tradeDateList.size(); i++) {
            String tradeDate0 = tradeDateList.get(i);
            amcorrelList.clear();

            double avgAmCorrel = getAmCorrels(tradeDate0, tradeDateList, hms, 
                    am, stockCode, amcorrelList);

            Double[] sds = amcorrelList.toArray(new Double[0]);
            double stddev = sd.evaluate(ArrayUtils.toPrimitive(sds));
            //System.out.format("%s %8.3f %8.3f\n", tradeDate0, avgAmCorrel, stddev);
            if(stddev<this.minStdDev) {
                this.minStdDev = stddev;
                this.minAvgAmCorrel = avgAmCorrel;
                this.minTradeDate = tradeDate0;
            }
        }

        //new a thread to remove those items of key <tradeDate0,tradeDate1,hms[0],hms[1]>
        Thread t = new ThreadRemoveAmc(tradeDateList, hms);
        t.start();
    }


    private double getAmCorrels(String tradeDate0, ArrayList<String> tradeDateList, double[][] m, 
            ArrayList<Double> amcorrelList) {
        double avgAmCorrel = 0;
        int idx = Utils.getIdx(tradeDateList.toArray(new String[0]), tradeDate0);

        int count = 0;
        for(int j=0; j<m.length; j++) {
            Double amcorrel = m[idx][j];
            if(amcorrel != Double.NaN) {
                avgAmCorrel += amcorrel;
                count++;
                amcorrelList.add(amcorrel);
            }
        }
        avgAmCorrel = avgAmCorrel/count;

        return avgAmCorrel;
    }
    //for each tradeDate1 in tradeDateList
    //  get a series of amcorrel for [tradeDate0, tradeDate1], and save to amcorrelList
    //return an avg of amcorrelList
    //amcorrelList: [out]
    private double getAmCorrels(String tradeDate0, ArrayList<String> tradeDateList, String[] hms, 
            AmManager am, String stockCode, ArrayList<Double> amcorrelList) {
        double avgAmCorrel = 0;
        int count = 0;
        for(int j=0; j<tradeDateList.size(); j++) {
            String tradeDate1 = tradeDateList.get(j);

            Double amcorrel = AmcMap.getAmCorrel(tradeDate0, tradeDate1, hms[0], hms[1], 
                    am, stockCode);
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
