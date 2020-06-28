 /*
 Copyright (C) 2019-2050 WestSword, Inc.
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.  */
 
 /* Written by whogiawho <whogiawho@gmail.com>. */
 
 
package com.westsword.stocks.analyze.ssanalyze;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class SSTableRecord extends BaseSSTR {
    public String sTableName;

    public int tradeCount;                 //traded volume

    private Boolean mbEvalResult;
    private TreeSet<AtomExpr> mExprSet;
    private Stack<AtomExpr> mExprStack;

    public SSTableRecord (int tradeCount, 
            String stockCode, String startDate, double threshold, int sTDistance, int tradeType, 
            int maxCycle, double targetRate, String sMatchExp, String sTableName) {
        super(stockCode, startDate, threshold, sTDistance, tradeType,
                maxCycle, targetRate, sMatchExp);

        this.sTableName = sTableName;

        this.tradeCount = tradeCount;

        mbEvalResult = null;
        mExprSet = getAtomExprSet();
        mExprStack = getAtomExprStack(mExprSet);
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
    public void print(Boolean bEval, boolean bOnlyTraded) {
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

        if(bOnlyTraded && bEval || !bOnlyTraded)
            System.out.format("%-8s in %-4s at %s:\n %s\n", sPrefix, sTableName, Time.current(), sOut);
    }

    public Boolean eval(long currentTp, AmManager am, TreeMap<Integer, AmRecord> amrMap, SdTime1 sdTime) {
        if(mbEvalResult==null) {
            if(!mExprStack.empty()) {
                String currentDate = Time.getTimeYMD(currentTp, false);
    
                while(!mExprStack.empty()) {
                    AtomExpr e = mExprStack.peek();
                    String hms = e.sLastHMS;
                    long tp = Time.getSpecificTime(currentDate, hms);
                    if(currentTp < tp) {
                        return null;
                    } else {
                        //pop it and evaluate e 
                        mExprStack.pop();

                        double amcorrel = getAmCorrel(am, amrMap, currentDate, e.sExpr, sdTime);
                        if(amcorrel < threshold) {
                            mbEvalResult = false;
                            return mbEvalResult;
                        }
                    }
                }

                mbEvalResult = true;
            }
        }

        return mbEvalResult;
    }

    //eval this record with tradeDate
    public boolean eval(AmManager am, String tradeDate, SdTime1 sdTime, double[] ret) {
        boolean bResult = false;

        String startHMS = AStockSdTime.CALL_AUCTION_END_TIME;
        String endHMS = AStockSdTime.CLOSE_QUOTATION_TIME;
        NavigableMap<Integer, AmRecord> amrMap = am.getItemMap(tradeDate, startHMS, tradeDate, endHMS);

        return eval(am, amrMap, tradeDate, sdTime, ret); 
    }
    //assuming amrMap is now ready for evaluation: its last HMS is larger than mExprSet.last().sLastHMS
    private boolean eval(AmManager am, NavigableMap<Integer, AmRecord> amrMap, String currentDate, SdTime1 sdTime, double[] ret) {
        boolean bResult = true;

        String[] fields = getComponents();
        for(int i=0; i<fields.length; i++) {
            double amcorrel = getAmCorrel(am, amrMap, currentDate, fields[i], sdTime);

            if(ret != null)
                ret[i] = amcorrel;
            if(amcorrel < threshold) {
                bResult = false;
                break;
            }
        }

        return bResult;
    }
    private double getAmCorrel(AmManager am, NavigableMap<Integer, AmRecord> amrMap, String currentDate, 
            String sExpr, SdTime1 sdTime) {
        String[] subFields = sExpr.split(":");
        String tradeDate = subFields[0];
        String[] hmss = subFields[1].split("_");

        NavigableMap<Integer, AmRecord> map0 = am.getItemMap(tradeDate, hmss[0], tradeDate, hmss[1]);
        NavigableMap<Integer, AmRecord> map1 = AmUtils.getItemMap(amrMap, sdTime, 
                currentDate, hmss[0], currentDate, hmss[1]); 
        double amcorrel = Double.NaN;
        //print warning message if map0.size&map1.size are not equal
        if(map0.size()!=map1.size()) {
            String sWarn = String.format("%s %d!=%d", sMatchExp, map0.size(), map1.size());
            sWarn = AnsiColor.getColorString(sWarn, AnsiColor.ANSI_RED);
            System.out.format("%s: %s\n", Utils.getCallerName(getClass()), sWarn);
            amcorrel = AmCorrel.get(map0, map1);
        } else {
            amcorrel = am.getAmCorrel(map0, map1);
        }

        return amcorrel;
    }






    private boolean bSessionOpened;
    public boolean getSessionOpened() {
        return bSessionOpened;
    }
    public void setSessionOpened(boolean bSessionOpened) {
        this.bSessionOpened = bSessionOpened;
    }

    //not assuming the last HMS of 1st component is the inTime
    public double getInPrice(AmManager am, String tradeDate) {
        double inPrice = Double.NaN;

        AtomExpr e = mExprSet.last();
        String sInTime = e.sLastHMS;

        long tp = Time.getSpecificTime(tradeDate, sInTime);
        inPrice = am.getInPrice(tradeType, tp);

        return inPrice;
    }











    //currentTp==null - an evaluation is forced to be done
    //currentTp!=null - a check is done whether to be evaluated
    public Boolean _eval(long currentTp, AmManager am, TreeMap<Integer, AmRecord> amrMap, SdTime1 sdTime) {
        if(mbEvalResult==null) {
            if(eligible(currentTp)) {
                String currentDate = Time.getTimeYMD(currentTp, false);
                mbEvalResult = eval(am, amrMap, currentDate, sdTime, null);
            }
        }

        return mbEvalResult;
    }
    public boolean eligible(long currentTp) {
        boolean bEligible = true;

        String currentDate = Time.getTimeYMD(currentTp, false);
        AtomExpr e = mExprSet.last();
        String hms = e.sLastHMS;
        long tp = Time.getSpecificTime(currentDate, hms);
        if(currentTp<tp) {
            bEligible = false;
        }
 
        return bEligible;
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
}
