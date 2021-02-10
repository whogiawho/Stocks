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


package com.westsword.stocks.analyze.sam5;

import java.util.*;
import org.apache.commons.math3.fitting.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;

/*
 *        /\
 *       /
 */
public class SAm5 {
    public final static int LOCAL_EXTREME_ERR_OFFSET = 5;

    public static boolean inTriggered(SAm dstSAm, String[] lines, String lastDate) {
        //maxS
        String line0 = "";
        Segment maxS = dstSAm.getSegmentOfMaxLen();
        if(maxS==null||!maxS.isAllGt0())                      //criteria0: max&allGt0
            return false;
        int maxSize = maxS.getLength();
        double maxE = maxS.maxAm();
        double maxER = maxS.getLocationOfMaxAm();
        line0 = String.format("%5d %5d %8.3f %6.2f", 
                maxS.start, maxSize, maxE, maxER);
        lines[0] = line0;
        
        int cnt = dstSAm.getNumberOfSegments();               //criteria1
        if(cnt!=1)
            return false;

        String tradeDate = dstSAm.getTradeDate();             //criteria2
            if(tradeDate.equals("20101105"))
                return false;

        String hms = dstSAm.getHMS();                         //criteria2
        String[] negativeHMSList = {
        };
        for(int i=0; i<negativeHMSList.length; i++) {
            if(hms.equals(negativeHMSList[i]))
                return false;
        }

        String line1 = "";
        try {
            int idxOfFLMax= maxS.getIdxOfLocalMax(0, LOCAL_EXTREME_ERR_OFFSET);
            if(idxOfFLMax==-1)
                return false;
            double[] ret = maxS.getSlopeR2(0, idxOfFLMax);
            line1 = String.format("%4d %6.3f %6.3f", idxOfFLMax, ret[0], ret[1]);
            //System.out.format("%s: %s\n", dstSAm.toString(), line1);
            
            int idxOfFLMin= maxS.getIdxOfLocalMin(idxOfFLMax, SAm5.LOCAL_EXTREME_ERR_OFFSET);
            if(idxOfFLMin==-1)
                return false;
            ret = maxS.getSlopeR2(idxOfFLMax, idxOfFLMin);
            line1 = String.format("%s %4d %8.3f %6.3f", line1, idxOfFLMin, ret[0], ret[1]);
            //System.out.format("%s: %s\n", dstSAm.toString(), line1);

            ret = maxS.getSlopeR2(idxOfFLMin, maxS.getLength());
            line1 = String.format("%s %8.3f %6.3f", line1, ret[0], ret[1]);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.format("%s: %s | %s\n", dstSAm.toString(), line0, line1);
        }
        lines[1] = line1;


        return true;
    }

    private static void trackUaS(SAm dstSAm, Segment uaS, String lastDate, String[] sRet) {
        String stockCode = dstSAm.getStockCode();
        String tradeDate = dstSAm.getTradeDate();
        String hms = dstSAm.getHMS();

        int rawCnt = uaS.getPosCount();

        SdTime1 sdt = new SdTime1(stockCode);
        int start = sdt.getAbs(tradeDate, hms);
        int end = sdt.getAbs(tradeDate, sdt.getCloseQuotationTime());
        int step = Settings.getAmDerInterval();
        for(int i=start; i<end; i+=step) {
            long hexTp = sdt.rgetAbs(i);
            String sTradeDate = Time.getTimeYMD(hexTp, false);
            String sHMS = Time.getTimeHMS(hexTp, false);
            //System.out.format("%s %s\n", sTradeDate, sHMS);

            int offset = (i-start)/step;
            SAm sam = new SAm(stockCode, sTradeDate, sHMS);

            int uaSStart = uaS.start - offset;
            int uaSEnd = uaS.end - offset;
            int posCount = sam.getPosCount(uaSStart, uaSEnd);
            if(posCount*2 <= rawCnt) {
                sRet[0] = sTradeDate;
                sRet[1] = sHMS;
                break;
            }
        }
    }
   

}

