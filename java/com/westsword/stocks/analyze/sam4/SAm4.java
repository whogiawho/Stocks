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


package com.westsword.stocks.analyze.sam4;

import java.util.*;
import org.apache.commons.math3.fitting.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.analyze.sam.*;

/*
 *            /\
 *
 *       /
 *  \___/
 *
 */
/*
 */
public class SAm4 {
    public final static int maxdaSegmentBaseSize = 430;
    public final static int maxdaSegmentSizeDelta = 75;
    public final static double maxdaSegmentLThres = 0.10;
    public final static double maxdaSegmentRThres = 0.30;
    public final static int uaSegmentBaseSize = 50;
    public final static int uaSegmentSizeLoDelta = 45;
    public final static int uaSegmentSizeHiDelta = 45;
    public final static double uaSegmentLThres = 0.25;
    public final static double uaSegmentRThres = 0.75;
    public static int getMaxdaSegmentMinSize() {
        return maxdaSegmentBaseSize - maxdaSegmentSizeDelta;
    }
    public static int getMaxdaSegmentMaxSize() {
        return maxdaSegmentBaseSize + maxdaSegmentSizeDelta;
    }
    public static double getMaxdaSegmentLThres() {
        return maxdaSegmentLThres;
    }
    public static double getMaxdaSegmentRThres() {
        return maxdaSegmentRThres;
    }
    public static int getUaSegmentMinSize() {
        return uaSegmentBaseSize - uaSegmentSizeLoDelta;
    }
    public static int getUaSegmentMaxSize() {
        return uaSegmentBaseSize + uaSegmentSizeHiDelta;
    }
    public static double getUaSegmentLThres() {
        return uaSegmentLThres;
    }
    public static double getUaSegmentRThres() {
        return uaSegmentRThres;
    }

    public static boolean inTriggered(SAm dstSAm, String[] lines) {
        String line0 = "";
        Segment maxS = dstSAm.getSegmentOfMaxLen();
        if(maxS==null||!maxS.isAllLt0())                      //criteria0: max&allLt0
            return false;
        int maxSize = maxS.getLength();
        if(maxSize<getMaxdaSegmentMinSize()|| 
                maxSize>=getMaxdaSegmentMaxSize())            //criteria1: size
            return false;
        int maxSIdx = dstSAm.indexOfSeg(maxS);
        if(maxSIdx != 0)                                      //criteria2: 1st segment
            return false;
        Segment daS = dstSAm.getDownArrowSegment(maxSIdx, 
                getMaxdaSegmentLThres(), getMaxdaSegmentRThres());
        if(daS!=maxS)                                         //criteria3: LRthres
            return false;
        if(maxS.start==0)                                     //criteria4: start>0
            return false;
        double[] v = maxS.getSlopeR2wMin(SAm.END_BASED);
        double slopeWmin = v[0];
        double minE = maxS.minAm();
        double minER = maxS.getLocationOfMinAm();
        line0 = String.format("%5d %5d %6.2f %6.2f %8.4f", 
                maxS.start, maxSize, minE, minER, slopeWmin);
        lines[0] = line0;
        

        String line1 = "";
        Segment nextUaS = dstSAm.getUpArrowSegment(maxSIdx+1, 
                getUaSegmentLThres(), getUaSegmentRThres());
        if(nextUaS==null||!nextUaS.isAllGt0())                //criteria6: LRthres&allGt0 nextUaS 
            return false;
        int uaSize = nextUaS.getLength();
        if(uaSize<getUaSegmentMinSize()||                     //criteria7: size of nextUaS
                uaSize>=getUaSegmentMaxSize())
            return false;
        if(!criteria5(dstSAm, maxS, nextUaS, slopeWmin))      //criteria5:
            return false;

        //next segment of maxS
        nextSofMaxS(dstSAm, nextUaS, maxSIdx);

        double uaMaxE = nextUaS.maxAm();
        double uaMaxER = nextUaS.getLocationOfMaxAm();
        line1 = String.format("|%5d %5d %6.2f %6.2f", 
                nextUaS.start, uaSize, uaMaxE, uaMaxER);
        lines[1] = line1;

        return true;
    }

    private static boolean criteria5(SAm dstSAm, Segment maxS, Segment nextUaS, double slopeWmin) {
        String hms = dstSAm.getHMS();

        if(maxS.start>190 && maxS.start<400 &&                //criteria5: 
                slopeWmin>=0.0030 && slopeWmin<=0.0052)    
            return false;
        if(maxS.start>100 && maxS.start<610) {
            if(hms.compareTo("092500")>=0 && hms.compareTo("100000")<0) {
                if(maxS.start>=330 && maxS.start<370)
                    return false;
            }
            if(hms.compareTo("142000")>=0 && hms.compareTo("150000")<0) {
                if(slopeWmin<=0.0040)
                    return false;
                if(slopeWmin>=0.0080 && slopeWmin<=0.0140)
                    return false;
            }
            if(hms.compareTo("130000")>=0 && hms.compareTo("142000")<0) {
                if(maxS.start>=400 && maxS.start<500)
                    return false;
            }
        }
        if(maxS.start>=760 && maxS.start<850) {
            if(hms.compareTo("102000")>=0 && hms.compareTo("110000")<0)
                return false;
            if(hms.compareTo("144000")>=0 && hms.compareTo("150000")<0)
                return false;
        }
        if(maxS.start>=610 && maxS.start<760) {
            double uaMaxE = nextUaS.maxAm();
            //short trade is expected
            if(uaMaxE>40)
                return false;
            if(uaMaxE<10&&hms.compareTo("100000")<0)
                return false;
            if(hms.compareTo("130000")>=0 && hms.compareTo("140000")<0 && slopeWmin>0.02)
                return false;
            if(hms.compareTo("140000")>=0 && hms.compareTo("150000")<0 && slopeWmin>0.02)
                return false;
        }

        return true;
    }
    private static void nextSofMaxS(SAm dstSAm, Segment nextUaS, int maxSIdx) {
        double nextSAm = Double.NaN;
        double nextSSlope = Double.NaN;
        Segment nextS = dstSAm.nextSegment(maxSIdx);
        if(nextS!=null && nextS!=nextUaS && nextS.isAllLt0()) {
            nextSAm = nextS.getAm();
            nextSSlope = nextS.getSlopeR2wMin(SAm.START_BASED)[0];
        }
    }
}

