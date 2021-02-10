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
import org.apache.commons.math3.stat.regression.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;

public class SAmUtils {
    public static double[] getSlopeR2wMax(ArrayList<Double> listR, int type) {
        double max = Collections.max(listR); 
        int maxIdx = listR.indexOf(max);
        ArrayList<Double> newListR = null;
        if(type == SAm.START_BASED)
            newListR = new ArrayList<Double>(listR.subList(0, maxIdx+1));
        else
            newListR = new ArrayList<Double>(listR.subList(maxIdx, listR.size()));

        return getSlopeR2(newListR);
    }
    public static double[] getSlopeR2wMin(ArrayList<Double> listR, int type) {
        double min = Collections.min(listR); 
        int minIdx = listR.indexOf(min);
        ArrayList<Double> newListR = null;
        if(type == SAm.START_BASED)
            newListR = new ArrayList<Double>(listR.subList(0, minIdx+1));
        else
            newListR = new ArrayList<Double>(listR.subList(minIdx, listR.size()));

        return getSlopeR2(newListR);
    }
    public static double[] getSlopeR2(List<Double> listR) {
        double[] vals = new double[2];

        SimpleRegression sr = new SimpleRegression();
        for(int i=0; i<listR.size(); i++) {
            sr.addData((double)i, listR.get(i));
        }
        vals[0] = sr.getSlope();
        vals[1] = sr.getRSquare();

        return vals;
    }

    public static ArrayList<Double> getAmderList(String stockCode, String tradeDate, String hms) {
        ArrayList<Double> l = new ArrayList<Double>();
        AmDerUtils.loadAmder(l, stockCode, tradeDate, hms);
        return l;
    }

    public static boolean isAllGt0(ArrayList<Double> amderList) {
        boolean bAllGt0 = true;

        for(int i=0; i<amderList.size(); i++) {
            if(amderList.get(i)<0)
                return false;
        }

        return bAllGt0;
    }
    public static boolean isAllLt0(ArrayList<Double> amderList) {
        boolean bAllLt0 = true;

        for(int i=0; i<amderList.size(); i++) {
            if(amderList.get(i)>0)
                return false;
        }

        return bAllLt0;
    }

    public static String getLongInfo(AmManager amm, String tradeDate, String hms, String lastDate) {
        double inPrice = amm.getUpPrice(tradeDate, hms);
        double closePrice = amm.getCloseQuotationPrice(lastDate);
        String cpt = AStockSdTime.getCloseQuotationTime();
        double[] v = amm.getExtremePrice(tradeDate, hms, lastDate, cpt);
        double sMaxProfit = v[0] - inPrice;
        double maxWinRate = sMaxProfit/inPrice*100;
        double closeWinRate = (closePrice - inPrice)/inPrice*100;

        String line = "";
        //line += String.format("%6.3f ", sMaxProfit);
        line += String.format("%6.2f %6.2f", maxWinRate, closeWinRate);
        return line;
    }
    public static String getShortInfo(AmManager amm, String tradeDate, String hms, String lastDate) {
        double inPrice = amm.getDownPrice(tradeDate, hms);
        double closePrice = amm.getCloseQuotationPrice(lastDate);
        String cpt = AStockSdTime.getCloseQuotationTime();
        double[] v = amm.getExtremePrice(tradeDate, hms, lastDate, cpt);
        double sMaxProfit = inPrice - v[1];
        double maxWinRate = sMaxProfit/inPrice*100;
        double closeWinRate = (inPrice - closePrice)/inPrice*100;

        String line = "";
        //line += String.format("%6.3f ", sMaxProfit);
        line += String.format("%6.2f %6.2f", maxWinRate, closeWinRate);
        return line;
    }

    //the segment of max length
    public static Segment getSegmentOfMaxLen(ArrayList<Segment> segList) {
        Segment maxlenS = null;

        int maxlen = Integer.MIN_VALUE;
        for(int i=0; i<segList.size(); i++) {
            Segment s = segList.get(i);
            int currSize = s.eList.size();
            if(currSize>maxlen) {
                maxlenS = s;
                maxlen = currSize;
            }
        }

        return maxlenS;
    }
    //the segment of max amder
    public static Segment getSegmentOfMaxAm(ArrayList<Segment> segList) {
        Segment maxS = null;

        double maxAm = Double.NEGATIVE_INFINITY;
        for(int i=0; i<segList.size(); i++) {
            Segment s = segList.get(i);
            double currMaxAm = Collections.max(s.eList);
            if(currMaxAm>maxAm) {
                maxS = s;
                maxAm = currMaxAm;
            }
        }

        return maxS;
    }
    public static Segment getSegmentOfMinAm(ArrayList<Segment> segList) {
        Segment minS = null;

        double minAm = Double.POSITIVE_INFINITY;
        for(int i=0; i<segList.size(); i++) {
            Segment s = segList.get(i);
            double currMinAm = Collections.min(s.eList);
            if(currMinAm<minAm) {
                minS = s;
                minAm = currMinAm;
            }
        }

        return minS;
    }
    public static Segment getUpArrowSegment(ArrayList<Segment> segList, int startIdx, 
            double lThres, double rThres) {
        Segment seg = null;
        //System.out.format("%f %f\n", lThres, rThres);

        for(int i=startIdx; i<segList.size(); i++) {
            Segment s = segList.get(i);
            ArrayList<Double> l = s.eList;
            int size = l.size();
            double currMaxAm = Collections.max(l);
            int maxIdx = l.indexOf(currMaxAm);
            double loc = (double)maxIdx/size;
            //System.out.format("loc=%f\n", loc);
            if(loc>lThres && loc<rThres) {
                seg = s;
                break;
            }
        }

        return seg;
    }
    public static Segment getDownArrowSegment(ArrayList<Segment> segList, int startIdx, 
            double lThres, double rThres) {
        Segment seg = null;
        //System.out.format("%f %f\n", lThres, rThres);

        for(int i=startIdx; i<segList.size(); i++) {
            Segment s = segList.get(i);
            ArrayList<Double> l = s.eList;
            int size = l.size();
            double currMinAm = Collections.min(l);
            int minIdx = l.indexOf(currMinAm);
            double loc = (double)minIdx/size;
            //System.out.format("loc=%f\n", loc);
            if(loc>lThres && loc<rThres) {
                seg = s;
                break;
            }
        }

        return seg;
    }


}
