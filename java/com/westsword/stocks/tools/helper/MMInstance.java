package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.time.*;

public class MMInstance {
    public String stockCode;
    public String startDate;
    public double threshold;
    public int sTDistance;
    public int tradeType;

    public String tradeDate;
    public String hmsList;                       //
    public int maxCycle;

    public MMInstance(String stockCode, String startDate, double threshold, int sTDistance, int tradeType,
            String tradeDate, String hmsList, int maxCycle) {
        this.stockCode = stockCode;
        this.startDate = startDate;
        this.threshold = threshold;
        this.sTDistance = sTDistance;
        this.tradeType = tradeType;
        this.tradeDate = tradeDate;
        this.hmsList = hmsList;
        this.maxCycle = maxCycle;
    }
    public MMInstance(MMInstance r) {
        this(r.stockCode, r.startDate, r.threshold, r.sTDistance, r.tradeType,
                r.tradeDate, r.hmsList, r.maxCycle);
    }



    public void run(AmManager am, StockDates stockDates, double[][] corrM, 
            boolean bLog2Files, boolean bResetLog, boolean bStdout) {
        _run(am, stockDates, corrM);
    }

    public void _run(AmManager am, StockDates stockDates, double[][] corrM) {
        String inHMS = SSUtils.getInHMS(hmsList);

        double[] out = {
            Double.NaN,
        };
        double minmaxDelta = Double.POSITIVE_INFINITY;
        ArrayList<Double> maxDeltaList = new ArrayList<Double>();
        ArrayList<Double> maxNetRList = new ArrayList<Double>();

        ArrayList<String> similarTradeDates = SSUtils.getSimilarTradeDates(this, am, corrM);
        int matchedCnt = similarTradeDates.size();
        for(int i=0; i<matchedCnt; i++) {
            String tradeDate1 = similarTradeDates.get(i);
            String nextTradeDateN = stockDates.nextDate(tradeDate1, maxCycle);

            long inTime = Time.getSpecificTime(tradeDate1, inHMS);
            double cMax = am.maxDelta(inTime, nextTradeDateN, sTDistance, tradeType, 
                    stockDates, out);
            maxDeltaList.add(cMax);
            maxNetRList.add(out[0]);
            if(cMax<minmaxDelta) {
                minmaxDelta = cMax;
            }
        }
        double totalNetR = getTotalNetR(maxDeltaList, maxNetRList, minmaxDelta);
        String sFormat = "%s: %s %s %d %8.3f %8.3f\n";
        System.out.format(sFormat, Utils.getCallerName(getClass()), 
                tradeDate, hmsList, matchedCnt, minmaxDelta, totalNetR);
    }

    private double getTotalNetR(ArrayList<Double> maxDeltaList, ArrayList<Double> maxNetRList, 
            double minmaxDelta) {
        double totalNetR = 0.0;
        return totalNetR;
    }
}
