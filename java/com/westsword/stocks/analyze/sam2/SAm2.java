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


package com.westsword.stocks.analyze.sam2;

import java.util.*;

import com.westsword.stocks.analyze.sam.*;

/*
 *            /\
 *
 *    \
 *     \
 *
 */
public class SAm2 {
    public final static int maxSegmentBaseSize = 500;
    public final static int maxSegmentSizeDelta = 100;
    public final static int uaSegmentBaseSize = 90;
    public final static int uaSegmentSizeDelta = 30;
    public final static double uaSegmentLThres = 0.5;
    public final static double uaSegmentRThres = 0.76;

    public static int getMaxSegmentMinSize() {
        return maxSegmentBaseSize - maxSegmentSizeDelta;
    }
    public static int getMaxSegmentMaxSize() {
        return maxSegmentBaseSize + maxSegmentSizeDelta;
    }
    public static int getUaSegmentMinSize() {
        return uaSegmentBaseSize - uaSegmentSizeDelta;
    }
    public static int getUaSegmentMaxSize() {
        return uaSegmentBaseSize + uaSegmentSizeDelta;
    }
    public static double getUaSegmentLThres() {
        return uaSegmentLThres;
    }
    public static double getUaSegmentRThres() {
        return uaSegmentRThres;
    }


    public static boolean inTriggered(SAm dstSAm, String[] lines, int filter) {
        String line0 = "";
        Segment maxS = dstSAm.getSegmentOfMaxLen();
        if(maxS==null)
            return false;
        ArrayList<Double> maxList = maxS.eList;
        int maxSize = maxList.size();
        double[] vals = SAmUtils.getSlopeR2wMin(maxList, SAm.START_BASED);
        double slope = vals[0];
        double r2    = vals[1];
        double maxE = Collections.max(maxList);
        double minE = Collections.min(maxList);
        int maxEIdx = maxList.indexOf(maxE);
        int minEIdx = maxList.indexOf(minE);
        int minmaxDist = minEIdx - maxEIdx;
        if(maxSize<SAm2.getMaxSegmentMinSize()||
                maxSize>SAm2.getMaxSegmentMaxSize())        //criteria 0
            return false;
        if(!SAmUtils.isAllLt0(maxList))                     //criteria 1
            return false;
        if(maxEIdx>10)                                      //criteria 2
            return false;
        line0 = String.format("%5d %8.4f %5d %5d %8.3f", 
                maxS.start, slope, minmaxDist, maxSize, maxE);
        lines[0] = line0;

        String line1 = "";
        int maxSIdx = dstSAm.indexOfSeg(maxS);
        Segment uaS = dstSAm.getUpArrowSegment(maxSIdx+1, 
                SAm2.getUaSegmentLThres(), SAm2.getUaSegmentRThres());
        if(uaS==null)                                       //criteria 3
            return false;
        ArrayList<Double> uaList = uaS.eList;
        if(!SAmUtils.isAllGt0(uaList))                      //criteria 4
            return false;
        int uaSize = uaList.size();
        if(uaSize<SAm2.getUaSegmentMinSize()||
                uaSize>=SAm2.getUaSegmentMaxSize())         //criteria 5
            return false;
        double uaMaxE = Collections.max(uaList);
        int idx = uaList.indexOf(uaMaxE);
        line1 = String.format("|%5d %5d %8.3f", uaS.start, uaSize, uaMaxE);
        lines[1] = line1;

        boolean bFiltered = filterInstances(maxS, uaS,      //criteria 6
                dstSAm, slope, minmaxDist, filter);
        if(bFiltered) 
            return false;

        return true;
    }
    public static boolean filterInstances(Segment maxS, Segment uaS, 
            SAm dstSAm, double slope, int minmaxDist, int filter) {
        boolean bFiltered = false;

        if(filter == 1) {                //correl filter
            Offset p = null;
            if(maxS.start>=100 && maxS.start<270) {
                p = new Offset100();
            } else if(maxS.start>=620) {
                p = new Offset620();
            } else {
                p = new OffsetOthers();
            }
            if(p!=null)
                bFiltered = p.filterByCorrel(maxS, dstSAm, false);
        }

        return bFiltered;
    }


}

