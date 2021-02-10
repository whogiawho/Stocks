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


package com.westsword.stocks.analyze.sam3;

import java.util.*;
import org.apache.commons.math3.fitting.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.analyze.sam.*;

/*
 *            /\
 *
 * \     /
 *  \___/
 *
 */
public class SAm3 {
    public final static int maxdaSegmentBaseSize = 498;
    public final static int maxdaSegmentSizeDelta = 75;
    public final static double maxdaSegmentLThres = 0.45;
    public final static double maxdaSegmentRThres = 0.55;
    public final static int uaSegmentBaseSize = 40;
    public final static int uaSegmentSizeLoDelta = 20;
    public final static int uaSegmentSizeHiDelta = 25;
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
        double minE = maxS.minAm();
        if(minE>-2.9||maxS.start==0&&minE>-4.00)              //criteria4:
            return false;
        //get sCoeff
        List<WeightedObservedPoint> obl = maxS.getObl();
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
        final double[] coeff = fitter.fit(obl);
        double r2 = Utils.getPolynomialR2(obl, coeff); 
        String sCoeff0 = Utils.toPN0String(coeff);
        String sCoeff1 = Utils.toSneString(coeff);
        if(sCoeff0.equals("--+-") && r2<0.85)                 //criteria5:
            return false;
        //
        double minER = maxS.getLocationOfMinAm();
        line0 = String.format("%5d %5d %6.2f %6.2f", 
                maxS.start, maxSize, minE, minER);
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
        double uaMaxE = nextUaS.maxAm();
        if(uaMaxE<=4)                                         //criteria8.0: uaMaxE>4
            return false;
        if(maxS.start==0&&uaSize<=21&&uaMaxE<7.0)             //criteria8.1:
            return false;
        Segment nnextUaS = dstSAm.nextSegment(dstSAm.indexOfSeg(nextUaS));  
        if(nnextUaS!=null&&nnextUaS.isAllGt0()) {             //criteria9: not adjacent segments
            int adjDist = nnextUaS.start-nextUaS.end;
            if(nnextUaS.getLength()>uaSize && adjDist<50) {
                //System.out.format("%s: %d\n", dstSAm.toString(), adjDist);
                return false;
            }
        }
        if(nnextUaS!=null&&nnextUaS.isAllLt0()) {             //criteria10: no mass AS
            int nnextUaSize = nnextUaS.getLength();
            double nnextUaSminE = nnextUaS.minAm();
            if(nnextUaSize>uaSize&&nnextUaSminE<-30) {
                //System.out.format("%s: %d %6.2f\n", 
                //        dstSAm.toString(), nnextUaSize, nnextUaSminE);
                return false;
            }
        }

        double uaMaxER = nextUaS.getLocationOfMaxAm();
        line1 = String.format("|%5d %5d %6.2f %6.2f", 
                nextUaS.start, uaSize, uaMaxE, uaMaxER);
        lines[1] = line1;

        String line2 = "";
        line2 += String.format("|%6.3f", r2);
        line2 += String.format("%8s ", sCoeff0);
        //line2 += String.format("%s", sCoeff1);
        lines[2] = line2;

        return true;
    }
}

