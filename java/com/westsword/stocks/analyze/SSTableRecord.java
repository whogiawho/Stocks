package com.westsword.stocks.analyze;


import java.util.*;

import com.westsword.stocks.base.Utils;

public class SSTableRecord {
    public int tradeCount;

    public String stockCode;
    public String startDate;
    public double threshold;
    public int sTDistance;
    public int tradeType;

    public int maxCycle;
    public double targetRate;

    public String sMatchExp;

    public SSTableRecord (int tradeCount, 
            String stockCode, String startDate, double threshold, int sTDistance, int tradeType, 
            int maxCycle, double targetRate, String sMatchExp) {
        this.tradeCount = tradeCount;

        this.stockCode = stockCode;
        this.startDate = startDate;
        this.threshold = threshold;
        this.sTDistance = sTDistance;
        this.tradeType = tradeType;

        this.maxCycle = maxCycle;
        this.targetRate = targetRate;

        this.sMatchExp = sMatchExp;
    }
    public SSTableRecord(SSTableRecord r) {
        this(r.tradeCount, 
                r.stockCode, r.startDate, r.threshold, r.sTDistance, r.tradeType,
                r.maxCycle, r.targetRate, r.sMatchExp);
    }

    public ArrayList<String> getTradeDates() {
        ArrayList<String> sTradeDateList = new ArrayList<String>();

        String[] fields = sMatchExp.split("\\&\\|");
        //System.out.format("%s: fields.length=%d\n", Utils.getCallerName(getClass()), fields.length);
        for(int i=0; i<fields.length; i++) {
            //System.out.format("%s: %s\n", Utils.getCallerName(getClass()), fields[i]);
            String[] subFields = fields[i].split(":");
            //System.out.format("%s: subFields.length=%d\n", Utils.getCallerName(getClass()), subFields.length);
            //subFields[0] - tradeDate; subFields[1] - hmsList
            sTradeDateList.add(subFields[0]);
        }

        return sTradeDateList;
    }

    public static ArrayList<String> getTradeDates(ArrayList<SSTableRecord> list) {
        ArrayList<String> sTradeDateList = new ArrayList<String>();

        for(int i=0; i<list.size(); i++) {
            SSTableRecord r = list.get(i);
            sTradeDateList.addAll(r.getTradeDates());
        }

        return sTradeDateList;
    }
}
