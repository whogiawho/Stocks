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
import org.apache.commons.math3.fitting.*;

public class Segment {
    private SAm mSam;

    public int start;
    public int end;
    public ArrayList<Double> eList;

    public Segment(SAm sam) {
        mSam = sam;
        eList = new ArrayList<Double>();
    }
    public Segment() {
        this(null);
    }
    public double maxAm() {
        return Collections.max(eList);
    }
    public double minAm() {
        return Collections.min(eList);
    }
    public int indexOf(double e) {
        return eList.indexOf(e);
    }
    public int getLength() {
        return eList.size();
    }
    public boolean isAllGt0() {
        return SAmUtils.isAllGt0(eList);
    }
    public boolean isAllLt0() {
        return SAmUtils.isAllLt0(eList);
    }
    public double getLocationOfMaxAm() {
        double maxAm = maxAm(); 
        int maxAmIdx = indexOf(maxAm);
        int size = getLength();

        return (double)maxAmIdx/size;
    }
    public double getLocationOfMinAm() {
        double minAm = minAm(); 
        int minAmIdx = indexOf(minAm);
        int size = getLength();

        return (double)minAmIdx/size;
    }
    public int getPosCount() {
        int count = 0;
        for(int i=0; i<eList.size(); i++) {
            if(eList.get(i)>0)
                count++;
        }

        return count;
    }
    public int getNegCount() {
        int count = 0;
        for(int i=0; i<eList.size(); i++) {
            if(eList.get(i)<0)
                count++;
        }

        return count;
    }
    //[start, end)
    public double[] getSlopeR2(int start, int end) {
        return SAmUtils.getSlopeR2(eList.subList(start, end));
    }
    public double[] getSlopeR2() {
        return SAmUtils.getSlopeR2(eList);
    }
    public double[] getSlopeR2wMin(int type) {
        return SAmUtils.getSlopeR2wMin(eList, type);
    }
    public double[] getSlopeR2wMax(int type) {
        return SAmUtils.getSlopeR2wMax(eList, type);
    }
    public List<WeightedObservedPoint> getObl() {
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        for(int i=0; i<eList.size(); i++)
            obs.add(i, eList.get(i));
        return obs.toList();
    }
    public List<Double> left(int idx) {
        return eList.subList(0, idx);
    }
    public List<Double> right(int idx) {
        return eList.subList(idx, eList.size());
    }
    public long getAm() {
        return mSam.getAm(start, end);
    }
    public double get(int idx) {
        return eList.get(idx);
    }


    //the idx of first local max element starting from start
    public int getIdxOfLocalMax(int start, int range) {
        int idx = -1;

        for(int i=start+1; i<eList.size(); i++) {
            double currE = eList.get(i);
            //i gt all left elements
            int j=i-1, cnt=0;
            while(j>=0 && cnt<range && currE>=eList.get(j)) {
                if(currE>eList.get(j))
                    cnt++; 
                j--;
            }
            if(!(j<0||cnt>=range))
                continue;
            //i gt all rigth elements
            j=i+1; cnt=0;
            while(j<eList.size() && cnt<range && currE>=eList.get(j)) {
                if(currE>eList.get(j))
                    cnt++; 
                j++;
            }
            if(!(j>=eList.size()||cnt>=range))
                continue;
            else {
                idx = i;
                //System.out.format("%f\n", eList.get(idx));
                break;
            }
        }

        return idx;
    }
    //the idx of first local min element starting from start
    public int getIdxOfLocalMin(int start, int range) {
        int idx = -1;

        for(int i=start+1; i<eList.size(); i++) {
            double currE = eList.get(i);
            //i lt all left elements
            int j=i-1, cnt=0;
            while(j>=0 && cnt<range && currE<=eList.get(j)) {
                if(currE<eList.get(j))
                    cnt++; 
                j--;
            }
            if(!(j<0||cnt>=range))
                continue;
            //i lt all rigth elements
            j=i+1; cnt=0;
            while(j<eList.size() && cnt<range && currE<=eList.get(j)) {
                if(currE<eList.get(j))
                    cnt++; 
                j++;
            }
            if(!(j>=eList.size()||cnt>=range))
                continue;
            else {
                idx = i;
                //System.out.format("%f\n", eList.get(idx));
                break;
            }
        }

        return idx;
    }



    public static ArrayList<Segment> make(SAm sam, ArrayList<Double> amderList) {
        ArrayList<Segment> segList = new ArrayList<Segment>();

        int size = amderList.size();
        int i=0;
        while(true) {
            while(i<size && Double.isNaN(amderList.get(i))) {
                i++;
            }
            if(i>=size)
                break;

            Segment s = new Segment(sam);
            s.start = i;
            while(i<size && !Double.isNaN(amderList.get(i))) {
                s.eList.add(amderList.get(i));
                i++;
            }
            s.end = i-1;
            segList.add(s);
            if(i>=size)
                break;
        }

        return segList;
    }
}


