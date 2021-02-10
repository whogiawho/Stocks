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


package com.westsword.stocks.analyze.sam1;

import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;

/*
 *       /--\
 *      /    \
 *     /
 */
public class SAm1 {
    //add restriction to maxSegment end position(<1188 & >700)
    //getMaxCycle(..., 300)
    //if(listL.size()>=500 && listR.size()>=30) {
    public boolean inTriggered(SAm dstSAm, String[] lines, AmManager amm, 
            ArrayList<Double> modelListL, ArrayList<Double> modelListR) {
        String line0 = "";

        //get maxLength segment S
        Segment maxS = dstSAm.getSegmentOfMaxAm();
        if(maxS==null||!maxS.isAllGt0())                      //criteria0: 
            return false;
        //no larger positve segment is allowed
        int maxSIdx = dstSAm.indexOfSeg(maxS);
        int posSize = dstSAm.getFollowPositiveLargerSegment(maxSIdx);
        if(posSize!=-1)
            return false;
        //700<maxS.end<1188
        if(maxS.end<700||maxS.end>=1188)
            return false;

        //started from its maxAm, split S into 2 parts: L&R
        double max = maxS.maxAm();
        int idx = maxS.indexOf(max);
        ArrayList<Double> listL = new ArrayList<Double>(maxS.left(idx));
        ArrayList<Double> listR = new ArrayList<Double>(maxS.right(idx));
        //restrictions of listL&listR's size&slope
        double lSlope=SAmUtils.getSlopeR2(listL)[0], rSlope=SAmUtils.getSlopeR2(listR)[0];

        double threshold=0.9;
        //calculate 2 correls <listL, modelListL> & <listR, modelListR> 
        if(listL.size()>=500 && listR.size()>=30) {
            double lCorrel = SAm.getCorrel(listL, modelListL, SAm.END_BASED);
            double rCorrel = SAm.getCorrel(listR, modelListR, SAm.START_BASED);
            if(lCorrel>=threshold&&rCorrel>=threshold) {
                int negSize = dstSAm.getFollowNegativeLargerSegment(maxSIdx);
                //is there a following segment, its negative max amder is greater than maxSIdx's
                if(negSize!=-1) {
                    String sFormat = "%5d %5d %8.3f %8.3f " + 
                        "%8.3f %5d %8.3f %8.3f";
                    line0 = String.format(sFormat, 
                            listL.size(), listR.size(), lCorrel, rCorrel, 
                            max, negSize, lSlope, rSlope);
                    lines[0] = line0;
                    return true;
                }
            }
        }

        return false;
    }
}

