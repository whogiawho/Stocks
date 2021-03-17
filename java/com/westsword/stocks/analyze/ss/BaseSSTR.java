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
 
 
package com.westsword.stocks.analyze.ss;


import java.util.*;


public class BaseSSTR {
    public String stockCode;
    public String startDate;
    public double threshold;
    public int sTDistance;
    public int tradeType;

    public int maxCycle;
    public double targetRate;

    public String sMatchExp;


    public BaseSSTR(String stockCode, String startDate, double threshold, int sTDistance, int tradeType, 
            int maxCycle, double targetRate, String sMatchExp) {
        this.stockCode = stockCode;
        this.startDate = startDate;
        this.threshold = threshold;
        this.sTDistance = sTDistance;
        this.tradeType = tradeType;

        this.maxCycle = maxCycle;
        this.targetRate = targetRate;

        this.sMatchExp = sMatchExp;
    }
    public BaseSSTR(BaseSSTR r) {
        this(r.stockCode, r.startDate, r.threshold, r.sTDistance, r.tradeType, 
                r.maxCycle, r.targetRate, r.sMatchExp);
    }


    public String toString() {
        String sFormat = "%4d %4d %8.3f %4d %8s %s\n";
        String line = String.format(sFormat, 
                sTDistance, tradeType, targetRate, maxCycle, stockCode, sMatchExp);

        return line;
    }

    public String[] getComponents() {
        return getComponents(sMatchExp);
    }
    public int getComponentSize() {
        return getComponents().length;
    }
    public ArrayList<String> getTradeDates() {
        return getTradeDates(sMatchExp);
    }


    public TreeSet<AtomExpr> getAtomExprSet() {
        TreeSet<AtomExpr> atomSet = new TreeSet<AtomExpr>();

        String[] fields = getComponents();
        for(int i=0; i<fields.length; i++) {
            String[] subFields = fields[i].split(":");
            //subFields[0] - tradeDate; subFields[1] - hmsList
            String[] hmss = subFields[1].split("_");
            AtomExpr expr = new AtomExpr(hmss[hmss.length-1], fields[i]);
            /*
            System.out.format("%s: adding %s %s\n", 
                    Utils.getCallerName(getClass()), hmss[hmss.length-1], fields[i]);
            */
            atomSet.add(expr);
        }

        return atomSet;
    }
    public Stack<AtomExpr> getAtomExprStack(TreeSet<AtomExpr> exprSet) {
        Stack<AtomExpr> atomStack = new Stack<AtomExpr>();

        for(AtomExpr e: exprSet.descendingSet()) {
            atomStack.push(e);
        }

        return atomStack;
    }




    public static ArrayList<String> getTradeDates(String sMatchExp) {
        ArrayList<String> sTradeDateList = new ArrayList<String>();

        String[] fields = getComponents(sMatchExp);
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
        TreeSet<String> sTradeDateSet = new TreeSet<String>();
        for(int i=0; i<list.size(); i++) {
            SSTableRecord r = list.get(i);
            sTradeDateSet.addAll(r.getTradeDates());
        }

        return new ArrayList<String>(sTradeDateSet);
    }
    public static String getAmCorrels(double[] ret) {
        String sAmCorrels = "";
        for(int i=0; i<ret.length; i++) {
            sAmCorrels += String.format("%8.3f", ret[i]);
        }
        return sAmCorrels;
    }
    public static String[] getComponents(String sMatchExp) {
        return sMatchExp.split("\\&|\\|");
    }



    public static class AtomExpr implements Comparable<AtomExpr> {
        public String sLastHMS;
        public String sExpr;

        public AtomExpr(String sLastHMS, String sExpr) {
            this.sLastHMS = sLastHMS;
            this.sExpr = sExpr;
        }

        public int compareTo(AtomExpr e) {
            return sLastHMS.compareTo(e.sLastHMS);
        }
    }


}
