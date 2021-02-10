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


package com.westsword.stocks.analyze.sam;

import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;

public class SAm implements Comparable<SAm> {
    public static int START_BASED = 0;
    public static int END_BASED = 1;

    private String stockCode; 
    private String tradeDate;
    private String hms;

    private ArrayList<Double> amderList;
    private ArrayList<Segment> segList;

    public SAm(String stockCode, String tradeDate, String hms) {
        this.stockCode = stockCode;
        this.tradeDate = tradeDate;
        this.hms = hms;

        amderList = SAmUtils.getAmderList(stockCode, tradeDate, hms);
        segList = Segment.make(this, amderList);
    }
    public String getStockCode() {
        return stockCode;
    }
    public String getTradeDate() {
        return tradeDate;
    }
    public String getHMS() {
        return hms;
    }
    public String toString() {
        return  String.format("%s %s %s", stockCode, tradeDate, hms);
    }
    public int compareTo(SAm sam) {
        int bCmp = stockCode.compareTo(sam.getStockCode());
        if(bCmp==0) {
            bCmp = tradeDate.compareTo(sam.getTradeDate());
            if(bCmp==0)
                bCmp = hms.compareTo(sam.getHMS());
        } 

        return bCmp;
    }



    public Segment getSegmentOfMinAm() {
        return SAmUtils.getSegmentOfMinAm(segList);
    }
    public Segment getSegmentOfMaxAm() {
        return SAmUtils.getSegmentOfMaxAm(segList);
    }
    public Segment getSegmentOfMaxLen() {
        return SAmUtils.getSegmentOfMaxLen(segList);
    }
    public Segment getUpArrowSegment(int startIdx, double lThres, double rThres) {
        return SAmUtils.getUpArrowSegment(segList, startIdx, lThres, rThres);
    }
    public Segment getDownArrowSegment(int startIdx, double lThres, double rThres) {
        return SAmUtils.getDownArrowSegment(segList, startIdx, lThres, rThres);
    }
    public int indexOfSeg(Segment segment) {
        return segList.indexOf(segment);
    }
    public Segment nextSegment(int idx) {
        Segment next = null;

        if(idx>=0 && idx<segList.size()-1)
            next = segList.get(idx+1);

        return next;
    }
    public Segment prevSegment(int idx) {
        Segment prev = null;

        if(idx>0 && idx<segList.size())
            prev = segList.get(idx-1);

        return prev;
    }

    public int getFollowPositiveLargerSegment(int maxSIdx) {
        int size = -1;

        double rThres = (double)3/3;
        ArrayList<Double> maxList = segList.get(maxSIdx).eList;
        double maxUpAmder = Collections.max(maxList);

        for(int i=maxSIdx+1; i<segList.size(); i++) {
            ArrayList<Double> l = segList.get(i).eList;
            double currMax = Collections.max(l);
            if(currMax>maxUpAmder*rThres) {
                /*
                System.out.format("currMax=%8.3f maxUpAmder=%8.3f\n", 
                        currMax, maxUpAmder);
                */
                size = l.size();
                break;
            }
        }

        return size;
    }
    public int getFollowNegativeLargerSegment(int maxSIdx) {
        int size = -1;

        ArrayList<Double> maxList = segList.get(maxSIdx).eList;
        double maxUpAmder = Collections.max(maxList);

        for(int i=maxSIdx+1; i<segList.size(); i++) {
            ArrayList<Double> l = segList.get(i).eList;
            double minDownAmder = Collections.min(l);
            if(minDownAmder<0 && Math.abs(minDownAmder)>maxUpAmder) {
                /*
                System.out.format("minDownAmder=%8.3f maxUpAmder=%8.3f\n", 
                        minDownAmder, maxUpAmder);
                */
                size = l.size();
                break;
            }
        }

        return size;
    }




    //[start, end)
    public long getAm(int start, int end) {
        SdTime1 sdt = new SdTime1(stockCode);
        int lastIdx = sdt.getAbs(tradeDate, hms);
        int amSize = amderList.size();
        int interval = Settings.getAmDerInterval();
        int endIdx = lastIdx - (amSize-end+1)*interval;
        int startIdx = lastIdx - (amSize-start)*interval;
        long endTp = sdt.rgetAbs(endIdx);
        long startTp = sdt.rgetAbs(startIdx);
        String endTradeDate = Time.getTimeYMD(endTp, false);
        String startTradeDate = Time.getTimeYMD(startTp, false);
        AmManager amm = new AmManager(stockCode, startTradeDate, endTradeDate);

        /*
        System.out.format("%s: start=%x end=%x lastIdx=%d\n", 
                toString(), startTp, endTp, lastIdx);
        */

        long am = amm.getAm(startIdx, endIdx);

        return am;
    }
    public long getAm(Segment seg) {
        return getAm(seg.start, seg.end);
    }
    //[start, end)
    public int getPosCount(int start, int end) {
        int count = 0;
        for(int i=start; i<end; i++)
            if(amderList.get(i)>0)
                count++;

        return count;
    }
    public int getPosCount(int start) {
        return getPosCount(start, amderList.size());
    }
    //[start, end)
    public int getNegCount(int start, int end) {
        int count = 0;
        for(int i=start; i<end; i++)
            if(amderList.get(i)<0)
                count++;

        return count;
    }
    public int getNegCount(int start) {
        return getNegCount(start, amderList.size());
    }
    public int getNumberOfSegments() {
        return segList.size();
    }





    public static double getCorrel(ArrayList<Double> l0, ArrayList<Double> l1, int type, 
            int length) {
        int l0Size = l0.size();
        int l1Size = l1.size();
        List<Double> m0=null, m1=null;
        if(type == END_BASED) {
            m0 = l0.subList(l0Size-length, l0Size);
            m1 = l1.subList(l1Size-length, l1Size);
        } else {
            m0 = l0.subList(0, length);
            m1 = l1.subList(0, length);
        }
        Double[] X = m0.toArray(new Double[0]);
        Double[] Y = m1.toArray(new Double[0]);
        double[] x = ArrayUtils.toPrimitive(X);
        double[] y = ArrayUtils.toPrimitive(Y);
   
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double correl = pc.correlation(x, y);
  
        return correl;
    }
    public static double getCorrel(ArrayList<Double> l0, ArrayList<Double> l1, int type) {
        int l0Size = l0.size();
        int l1Size = l1.size();
        int minSize = l0Size;
        if(minSize > l1Size)
            minSize = l1Size;
 
        return getCorrel(l0, l1, type, minSize);
    }




}

