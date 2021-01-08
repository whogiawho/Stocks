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

public class Segment {
    public int start;
    public int end;
    public ArrayList<Double> eList;

    public Segment() {
        eList = new ArrayList<Double>();
    }

    //the segment of max length, and at least >minLen
    public static Segment getMaxLenSegment(ArrayList<Segment> segList, int minLen) {
        Segment maxlenS = null;

        int maxlen = minLen;
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
    //the segment with length>=minLen, and with max amder
    public static Segment getMaxSegment(ArrayList<Segment> segList, int minLen) {
        Segment maxS = null;

        double maxAmder = Double.NEGATIVE_INFINITY;
        for(int i=0; i<segList.size(); i++) {
            Segment s = segList.get(i);
            double currMaxAmder = Collections.max(s.eList);
            if(s.eList.size()>minLen && currMaxAmder>maxAmder) {
                maxS = s;
                maxAmder = currMaxAmder;
            }
        }

        return maxS;
    }
    public static Segment getUpArrowSegment(ArrayList<Segment> segList, int startIdx, 
            double lThres, double rThres) {
        Segment seg = null;
        //System.out.format("%f %f\n", lThres, rThres);

        for(int i=startIdx; i<segList.size(); i++) {
            Segment s = segList.get(i);
            ArrayList<Double> l = s.eList;
            int size = l.size();
            double currMaxAmder = Collections.max(l);
            int maxIdx = l.indexOf(currMaxAmder);
            double loc = (double)maxIdx/size;
            //System.out.format("loc=%f\n", loc);
            if(loc>lThres && loc<rThres) {
                seg = s;
                break;
            }
        }

        return seg;
    }

    public static ArrayList<Segment> make(ArrayList<Double> amderList) {
        ArrayList<Segment> segList = new ArrayList<Segment>();

        int size = amderList.size();
        int i=0;
        while(true) {
            while(i<size && Double.isNaN(amderList.get(i))) {
                i++;
            }
            if(i>=size)
                break;

            Segment s = new Segment();
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


