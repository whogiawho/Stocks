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


package com.westsword.stocks.analyze.sam0;

import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;

/*
 *     /\
 *       /\
 *
 */
public class SAm0 {
    public final static double UA0_VS_UA1 = 0;

    public final static int uaSegment0BaseSize = 40;
    public final static int uaSegment0SizeDelta = 10;
    public final static double uaSegment0LThres = 0.45;
    public final static double uaSegment0RThres = 0.55;
    public final static int uaSegment1BaseSize = 40;
    public final static int uaSegment1SizeDelta = 15;
    public final static double uaSegment1LThres = 0.15;
    public final static double uaSegment1RThres = 0.85;

    public static int getUaSegment0MinSize() {
        return uaSegment0BaseSize - uaSegment0SizeDelta;
    }
    public static int getUaSegment0MaxSize() {
        return uaSegment0BaseSize + uaSegment0SizeDelta;
    }
    public static double getUaSegment0LThres() {
        return uaSegment0LThres;
    }
    public static double getUaSegment0RThres() {
        return uaSegment0RThres;
    }

    public static int getUaSegment1MinSize() {
        return uaSegment1BaseSize - uaSegment1SizeDelta;
    }
    public static int getUaSegment1MaxSize() {
        return uaSegment1BaseSize + uaSegment1SizeDelta;
    }
    public static double getUaSegment1LThres() {
        return uaSegment1LThres;
    }
    public static double getUaSegment1RThres() {
        return uaSegment1RThres;
    }

    public boolean inTriggered(SAm dstSAm, String[] lines, AmManager amm, ReGroup grp) {
        String line0 = "";
        Segment uaS0 = dstSAm.getUpArrowSegment(0, 
                SAm0.getUaSegment0LThres(), SAm0.getUaSegment0RThres());
        if(uaS0==null||!uaS0.isAllGt0())                            //criteria0: LRthres&allGt0 uaS0
            return false;
        int ua0Size = uaS0.getLength();
        if(ua0Size<getUaSegment0MinSize()||
                ua0Size>=getUaSegment0MaxSize())                    //criteria1: size of uaS0 
            return false;
        double ua0MaxE = uaS0.maxAm();
        double ua0MaxER = uaS0.getLocationOfMaxAm();
        line0 = String.format("%5d %5d %6.2f %6.2f", 
                uaS0.start, ua0Size, ua0MaxE, ua0MaxER);
        lines[0] = line0;
        
        int uaS0Idx = dstSAm.indexOfSeg(uaS0);
        Segment nextS = dstSAm.nextSegment(uaS0Idx);
        String line1 = "";
        Segment uaS1 = dstSAm.getUpArrowSegment(uaS0Idx+1, 
                getUaSegment1LThres(), getUaSegment1RThres());
        if(uaS1==null||uaS1!=nextS||!uaS1.isAllGt0())               //criteria3: LRthres&allGt0 uaS1
            return false;
        int ua1Size = uaS1.getLength();
        if(ua1Size<getUaSegment1MinSize()||
                ua1Size>=getUaSegment1MaxSize())                    //criteria4: size of uaS1
            return false;
        double ua1MaxE = uaS1.maxAm();
        double ua1MaxER = uaS1.getLocationOfMaxAm();
        double ua0Vua1= ua0MaxE/ua1MaxE;
        line1 = String.format("|%5d %5d %6.2f %6.2f", 
                uaS1.start, ua1Size, ua1MaxE, ua1MaxER);
        lines[1] = line1;

        int dist = uaS1.start-uaS0.end;
        String line2 = "";
        line2 = String.format("|%6.2f %6d", ua0Vua1, dist);
        //double[] v = maxS.getSlopeR2wMax(SAm.START_BASED);
        //line2 += String.format(" %6.3f", v[0]);
        //v = maxS.getSlopeR2wMax(SAm.END_BASED);
        //line2 += String.format(" %6.3f", v[0]);

        //v = nextUaS.getSlopeR2wMax(SAm.START_BASED);
        //line2 += String.format(" %6.3f", v[0]);
        //v = nextUaS.getSlopeR2wMax(SAm.END_BASED);
        //line2 += String.format(" %6.3f", v[0]);
        lines[2] = line2;

        if(ua0Vua1<UA0_VS_UA1)                               //criteria5:
            return false;
        if(uaS0.start<700)
            return false;
        if(dist<10)
            return false;

        String hms = dstSAm.getHMS();
        if(ua0Vua1>0.95) {
            lines[3] = "L";
        } else if(hms.compareTo("112000")<0) {
            lines[3] = getTradeTypeBefore112000(dstSAm, uaS0Idx);
        } else {
            if(!scenarioAfter112000(dstSAm, uaS0Idx, ua1MaxE, lines))
                return false;
        }

        String tradeDate = dstSAm.getTradeDate();
        if(grp.indexOf(tradeDate)!=-1)
            loopRemainingHMS(dstSAm, amm, grp);

        return true;
    }
    private static boolean scenarioAfter112000(SAm dstSAm, int uaS0Idx, double ua1MaxE, String[] lines) {
        boolean bFound = true;

        Segment s0 = dstSAm.prevSegment(uaS0Idx);
        if(ua1MaxE>100||s0==null) 
            lines[3] = "L";
        else {
            Segment s1 = prevMaxNegSegment(dstSAm, uaS0Idx);
            if(s1!=null) {
                double minER = s1.getLocationOfMinAm();
                double maxER = s1.getLocationOfMaxAm();
                /*
                System.out.format("%s: s1.size=%d minER=%-8.3f maxER=%-8.3f\n", 
                        dstSAm.toString(), s1.getLength(), minER, maxER);
                */
                if(minER>0.2&&minER<0.8)
                    lines[3] = "L";
                else
                    lines[3] = "S";
            } else {
                bFound = false;
            }
        }

        return bFound;
    }
    private static String getTradeTypeBefore112000(SAm dstSAm, int uaS0Idx) {
        String sTradeType = "";

        Segment prevS = dstSAm.prevSegment(uaS0Idx);
        if(prevS.getLength()>600 && prevS.isAllLt0() && prevS.start==0) {
            double minER = prevS.getLocationOfMinAm();
            if(minER>0.75 && minER<0.95) {
                sTradeType = "L";
                //System.out.format("%s: %8.3f\n", dstSAm.toString(), minER); 
            } else 
                sTradeType = "S";
        } else
            sTradeType = "S";

        return sTradeType;
    }
    private static Segment prevMaxNegSegment(SAm dstSAm, int idx) {
        Segment s = null;
        int maxL = Integer.MIN_VALUE;

        while(idx>0) {
            Segment prev = dstSAm.prevSegment(idx--);
            int currL = prev.getLength();
            if(prev.isAllLt0()&&currL>maxL) {
                maxL = currL;
                s = prev;
            }
        }

        return s;
    }

    private static void loopRemainingHMS(SAm dstSAm, AmManager amm, ReGroup grp) {
        String stockCode = dstSAm.getStockCode();
        String tradeDate = dstSAm.getTradeDate();
        String hms = dstSAm.getHMS();

        //init amSum
        int reTradeType = grp.getTradeType();
        long amSum = Long.MAX_VALUE;
        if(reTradeType==Stock.TRADE_TYPE_SHORT)
            amSum = Long.MIN_VALUE;

        SdTime1 sdt = new SdTime1(stockCode);
        int start = sdt.getAbs(tradeDate, hms);
        int end = sdt.getAbs(tradeDate, sdt.getCloseQuotationTime());
        int step = Settings.getAmDerInterval();
        String extremeHMS = "";
        for(int i=start; i<end; i+=step) {
            long hexTp = sdt.rgetAbs(i);
            String sHMS = Time.getTimeHMS(hexTp, false);
            //System.out.format("%s %s\n", tradeDate, sHMS);

            SAm sam = new SAm(stockCode, tradeDate, sHMS);
            Segment s0 = sam.getSegmentOfMaxAm();
            double currMaxAm = s0.maxAm();
            Segment s1 = sam.getSegmentOfMinAm();
            double currMinAm = s1.minAm();
            //System.out.format("%s %8.3f %8.3f %d\n", sHMS, currMaxAm, currMinAm, currMinAm);

            long currAmSum = amm.getAm(start, i);
            if(reTradeType==Stock.TRADE_TYPE_SHORT && currAmSum>amSum) {
                amSum = currAmSum;
                extremeHMS = sHMS;
            } else if(reTradeType==Stock.TRADE_TYPE_LONG && currAmSum<amSum) {
                amSum = currAmSum;
                extremeHMS = sHMS;
            }
        }
        grp.setExtreme(amSum, tradeDate, hms, extremeHMS);
        //System.out.format("SAm0.loopRemainingHMS: %s %s %d\n", hms, extremeHMS, amSum);
    }
}

