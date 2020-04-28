package com.westsword.stocks.analyze.ssanalyze;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class SSTableRecord {
    public String sTableName;

    public int tradeCount;

    public String stockCode;
    public String startDate;
    public double threshold;
    public int sTDistance;
    public int tradeType;

    public int maxCycle;
    public double targetRate;

    public String sMatchExp;


    private Boolean mbEvalResult;


    public SSTableRecord (int tradeCount, 
            String stockCode, String startDate, double threshold, int sTDistance, int tradeType, 
            int maxCycle, double targetRate, String sMatchExp, String sTableName) {
        this.sTableName = sTableName;

        this.tradeCount = tradeCount;

        this.stockCode = stockCode;
        this.startDate = startDate;
        this.threshold = threshold;
        this.sTDistance = sTDistance;
        this.tradeType = tradeType;

        this.maxCycle = maxCycle;
        this.targetRate = targetRate;

        this.sMatchExp = sMatchExp;

        mbEvalResult = null;
    }
    public SSTableRecord(SSTableRecord r) {
        this(r.tradeCount, 
                r.stockCode, r.startDate, r.threshold, r.sTDistance, r.tradeType,
                r.maxCycle, r.targetRate, r.sMatchExp, r.sTableName);
    }


    public String toString() {
        String sFormat = "%4d %4d %s %8.3f %6d %4d %8s\n";
        String line = String.format(sFormat, 
                sTDistance, tradeType, sMatchExp, targetRate, tradeCount, maxCycle, stockCode);

        return line;
    }
    public void print(Boolean bEval) {
        String sPrefix = "";
        String sOut = toString();
        if(bEval != null) {
            if(bEval == true) {           //print with RED|GREEN color
                sOut = AnsiColor.getColorString(sOut, tradeType);
                sPrefix = "Traded";
            } else {
                sPrefix = "Removed";
            }
        }

        System.out.format("%-8s in %-4s at %s:\n %s\n", sPrefix, sTableName, Time.current(), sOut);
    }
    public ArrayList<String> getListOfLastHMS() {
        ArrayList<String> hmsList = new ArrayList<String>();

        String[] fields = getComponents();
        for(int i=0; i<fields.length; i++) {
            String[] subFields = fields[i].split(":");
            //subFields[0] - tradeDate; subFields[1] - hmsList
            String[] hmss = subFields[1].split("_");
            hmsList.add(hmss[hmss.length-1]);
        }

        return hmsList;
    }
    public String[] getComponents() {
        return sMatchExp.split("\\&\\|");
    }
    public ArrayList<String> getTradeDates() {
        ArrayList<String> sTradeDateList = new ArrayList<String>();

        String[] fields = getComponents();
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
    //currentTp==null - an evaluation is forced to be done
    //currentTp!=null - a check is done whether to be evaluated
    public Boolean eval(long currentTp, AmManager am, TreeMap<Integer, AmRecord> amrMap) {
        if(mbEvalResult==null) {
            if(eligible(currentTp)) {
                String currentDate = Time.getTimeYMD(currentTp, false);
                mbEvalResult = eval(am, amrMap, currentDate);
            }
        }

        return mbEvalResult;
    }
    private boolean eval(AmManager am, TreeMap<Integer, AmRecord> amrMap, String currentDate) {
        boolean bResult = true;

        String[] fields = getComponents();
        for(int i=0; i<fields.length; i++) {
            String[] subFields = fields[i].split(":");
            String tradeDate = subFields[0];
            String[] hmss = subFields[1].split("_");
            NavigableMap<Integer, AmRecord> map0 = am.getItemMap(tradeDate, hmss[0], tradeDate, hmss[1]);
            NavigableMap<Integer, AmRecord> map1 = getItemMap(amrMap, currentDate, hmss[0], hmss[1]);
            //print warning message if map0.size&map1.size are not equal

            double amcorrel = am.getAmCorrel(map0, map1);
            if(amcorrel < threshold) {
                bResult = false;
                break;
            }
        }

        return bResult;
    }
    public boolean eligible(long currentTp) {
        boolean bEligible = true;

        String currentDate = Time.getTimeYMD(currentTp, false);
        ArrayList<String> hmsList = getListOfLastHMS();
        for(int i=0; i<hmsList.size(); i++) {
            String hms = hmsList.get(i);
            long tp = Time.getSpecificTime(currentDate, hms);
            if(currentTp<tp) {
                bEligible = false;
                break;
            }
        }

        return bEligible;
    }
    private NavigableMap<Integer, AmRecord> getItemMap(TreeMap<Integer, AmRecord> amrMap, 
            String currentDate, String startHMS, String endHMS) {
        SdTime1 sdTime = new SdTime1(stockCode);

        return AmUtils.getItemMap(amrMap, sdTime, currentDate, startHMS, currentDate, endHMS);
    }



    public static ArrayList<String> getTradeDates(ArrayList<SSTableRecord> list) {
        ArrayList<String> sTradeDateList = new ArrayList<String>();

        for(int i=0; i<list.size(); i++) {
            SSTableRecord r = list.get(i);
            sTradeDateList.addAll(r.getTradeDates());
        }

        return sTradeDateList;
    }


    private boolean bSessionOpened;
    public boolean getSessionOpened() {
        return bSessionOpened;
    }
    public void setSessionOpened(boolean bSessionOpened) {
        this.bSessionOpened = bSessionOpened;
    }
}
