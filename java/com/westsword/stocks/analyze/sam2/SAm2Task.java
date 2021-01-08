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

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;

public class SAm2Task extends Task {
    private String sDstDerivativeDir;
    private String stockCode;
    private String sDstTradeDate;
    private double threshold;
    private TradeDates tradeDates;
    private int maxCycle;

    public SAm2Task(SAm2Manager man, 
            String sDstDerivativeDir, String stockCode, String sDstTradeDate, double threshold, 
            TradeDates tradeDates, int maxCycle) {
        super(man);

        this.sDstDerivativeDir = sDstDerivativeDir;
        this.stockCode = stockCode;
        this.sDstTradeDate = sDstTradeDate;
        this.threshold = threshold;
        this.tradeDates = tradeDates;
        this.maxCycle = maxCycle;
    }
    @Override
    public void runTask() {
        //run instance
        String[] sFiles = Utils.getSubNames(sDstDerivativeDir);
        for(int j=0; j<sFiles.length; j++) {
            String sDstFile = sFiles[j];
            String hms1 = sDstFile.replace(".txt", "");
            sDstFile = sDstDerivativeDir + sDstFile;
            String sDstPrefix = String.format("%s %s %s", stockCode, sDstTradeDate, hms1);

            boolean bFound = printMatched(sDstFile, sDstPrefix, threshold,  
                    tradeDates, maxCycle);
            if(bFound) {
                break;
            }
        }
    }
    private boolean printMatched(String sDstFile, String sDstPrefix, double threshold, 
            TradeDates tradeDates, int maxCycle) {
        AmDerLoader l = new AmDerLoader(0);
        ArrayList<Double> amderList = new ArrayList<Double>();
        l.load(amderList, sDstFile);
 
        String line0 = "";
        ArrayList<Segment> segList = Segment.make(amderList);
        Segment maxS = Segment.getMaxLenSegment(segList, SAm2.MAX_SEGMENT_MIN_LENGTH);
        if(maxS==null)
            return false;
        ArrayList<Double> maxList = maxS.eList;
        int maxSize = maxList.size();
        double[] vals = SAm.getSlopeR2wMin(maxList);
        double slope = vals[0];
        double r2    = vals[1];
        double maxE = Collections.max(maxList);
        double minE = Collections.min(maxList);
        int maxEIdx = maxList.indexOf(maxE);
        int minEIdx = maxList.indexOf(minE);
        int minmaxDist = minEIdx - maxEIdx;
        if(maxSize<400||maxSize>600)                        //criteria 0
            return false;
        if(!SAm.isAllLt0(maxList))                              //criteria 1
            return false;
        if(maxEIdx>10)                                      //criteria 2
            return false;
        line0 = String.format("%5d %8.4f %5d %5d %8.3f", 
                maxS.start, slope, minmaxDist, maxSize, maxE);

        String line1 = "";
        int maxSIdx = segList.indexOf(maxS);
        Segment uaS = Segment.getUpArrowSegment(segList, maxSIdx+1, 0.5, 0.76);
        if(uaS==null)                                       //criteria 3
            return false;
        ArrayList<Double> uaList = uaS.eList;
        int uaSize = uaList.size();
        if(!SAm.isAllGt0(uaList))                               //criteria 4
            return false;
        if(uaSize<60||uaSize>120)                           //criteria 5
            return false;
        double uaMaxE = Collections.max(uaList);
        int idx = uaList.indexOf(uaMaxE);
        line1 = String.format("|%5d %5d %8.3f", uaS.start, uaSize, uaMaxE);

        boolean bFiltered = filterInstances(maxS, uaS, 
                sDstPrefix, slope, minmaxDist);
        if(bFiltered) 
            return false;

        String[] fields = sDstPrefix.split(" +");
        String lastDate = tradeDates.nextDate(fields[1], maxCycle);
        AmManager amm = new AmManager(fields[0], fields[1], lastDate);
        double inPrice = amm.getUpPrice(fields[1], fields[2]);
        String cpt = AStockSdTime.getCloseQuotationTime();
        double[] v = amm.getExtremePrice(fields[1], fields[2], lastDate, cpt);
        double sProfit = v[0] - inPrice;

        String sFormat = "%s: %s %s | %8.3f\n";
        System.out.format(sFormat, 
                sDstPrefix, line0, line1, sProfit);

        if(tradeDateFound(fields[1]))
            return false;

        return true;
    }
    public boolean tradeDateFound(String tradeDate) {
        boolean bFound = false;

        //always return false for these tradeDates
        String[] theseDates = {
            "20131211",
        };
        for(int i=0; i<theseDates.length; i++) {
            if(tradeDate.equals(theseDates[i])) {
                bFound = true;
                break;
            }
        }
        return bFound;
    }
    public boolean filterInstances(Segment maxS, Segment uaS, 
            String sDstPrefix, double slope, int minmaxDist) {
        boolean bFiltered = false;

        String[] fields = sDstPrefix.split(" +");
        if(maxS.start==0) {
            //remove            <600030 20130110 131356> <600030 20131211 095357>
            //bFiltered = filter0BySlopeAndInTime(slope, fields);
            bFiltered = filter0ByCorrel(maxS, fields, sDstPrefix, false);
            //targetProfit=0.30 others
        } else if(maxS.start>0 && maxS.start<50) {
            //targetProfit=1.00 <600030 20200714 101557>
        } else if(maxS.start>=50 && maxS.start<130) {
            //remove            all
        } else if(maxS.start>=130 && maxS.start<180) {
            //targetProfit=0.30 <600030 20120907 100457> <600030 20170216 132756>
            //targetProfit=1.00 others
        } else if(maxS.start>=180 && maxS.start<250) {
            //targetProfit=0.50 <600030 20120116 131156> <600030 20091110 101757>
            //targetProfit=1.00 <600030 20190315 135656>
            //remove            others
        } else if(maxS.start>=250 && maxS.start<360) {
            //remove            <600030 20100111 092500>
            //targetProfit=0.20 others
        } else if(maxS.start>=360 && maxS.start<450) {
            //remove            <600030 20170126 100557> <600030 20140124 104857>
            //                  <600030 20170215 134456> <600030 20100301 135956>
            //targetProfit=1.00 <600030 20100108 142156> <600030 20190218 104757>
            //targetProfit=0.30 others
        } else if(maxS.start>=450 && maxS.start<490) {
            //remove            <600030 20121012 095457>
            //targetProfit=0.40 others
        } else if(maxS.start>=490 && maxS.start<520) {
            //targetProfit=1.00 <600030 20150529 104957>
            //targetProfit=0.20 <600030 20100401 094257> <600030 20201015 094557>
            //                  <600030 20090709 092500>
            //remove            others
        } else if(maxS.start>=520 && maxS.start<560) {
            //remove            <600030 20180814 092500> <600030 20160902 104057>
            //targetProfit=0.20 others
        } else if(maxS.start>=560 && maxS.start<620) {
            //targetProfit=0.20 <600030 20170411 144356>
            //remove            others
        } else {
            //targetProfit=0.20 all
        }

        return bFiltered;
    }


    public boolean filter0BySlopeAndInTime(double slope, String[] fields) {
        boolean bFiltered = false;

        if(slope>=-0.0075&&slope<=-0.0045 && fields[2].compareTo("112000")>0)
            bFiltered = true;
        else if(slope>-0.0045&&slope<=-0.0040 && fields[2].compareTo("094000")>0)
            bFiltered = true;

        return bFiltered;
    }
    public ArrayList<ArrayList<Double>> get0NegativeLists() {
        ArrayList<Double> l20130110_131356 = new ArrayList<Double>();
        AmDerUtils.loadAmder(l20130110_131356, "600030", "20130110", "131356");
        ArrayList<Double> l20131211_095357 = new ArrayList<Double>();
        AmDerUtils.loadAmder(l20131211_095357, "600030", "20131211", "095357");

        ArrayList<ArrayList<Double>> list = new ArrayList<ArrayList<Double>>();
        list.add(l20131211_095357);
        list.add(l20130110_131356);
        return list;
    }
    public ArrayList<ArrayList<Double>> get0PositiveLists() {
        ArrayList<Double> l20100519_105857 = new ArrayList<Double>();
        AmDerUtils.loadAmder(l20100519_105857, "600030", "20100519", "105857");
        ArrayList<Double> l20140924_103857 = new ArrayList<Double>();
        AmDerUtils.loadAmder(l20140924_103857, "600030", "20140924", "103857");
        ArrayList<Double> l20160621_095057 = new ArrayList<Double>();
        AmDerUtils.loadAmder(l20160621_095057, "600030", "20160621", "095057");
        ArrayList<Double> l20161018_112157 = new ArrayList<Double>();
        AmDerUtils.loadAmder(l20161018_112157, "600030", "20161018", "112157");
        ArrayList<Double> l20190715_103757 = new ArrayList<Double>();
        AmDerUtils.loadAmder(l20190715_103757, "600030", "20190715", "103757");
        ArrayList<Double> l20191225_143256 = new ArrayList<Double>();
        AmDerUtils.loadAmder(l20191225_143256, "600030", "20191225", "143256");
        ArrayList<Double> l20191226_102457 = new ArrayList<Double>();
        AmDerUtils.loadAmder(l20191226_102457, "600030", "20191226", "102457");

        ArrayList<ArrayList<Double>> list = new ArrayList<ArrayList<Double>>();
        list.add(l20191226_102457);
        list.add(l20191225_143256);
        list.add(l20190715_103757);
        list.add(l20161018_112157);
        list.add(l20160621_095057);
        list.add(l20140924_103857);
        list.add(l20100519_105857);
        return list;
    }
    public boolean filter0ByCorrel(Segment maxS, String[] fields, 
            String sDstPrefix, boolean bPrintCorrel) {
        boolean bFiltered = true;

        ArrayList<ArrayList<Double>> negativeLists = get0NegativeLists(); 
        ArrayList<ArrayList<Double>> positiveLists = get0PositiveLists();
        for(int i=0; i<positiveLists.size(); i++) {
            ArrayList<Double> l = positiveLists.get(i);
            ArrayList<Segment> sL0 = Segment.make(l);
            Segment maxS0 = Segment.getMaxLenSegment(sL0, SAm2.MAX_SEGMENT_MIN_LENGTH);
            double correl = SAm.getCorrel(maxS.eList, maxS0.eList, SAm.START_BASED);
            double threshold = get0CorrelThreshold(fields[1]);

            if(correl>=threshold) {
                if(bPrintCorrel)
                    System.out.format("%s: %d %8.3f\n", sDstPrefix, i, correl);
                bFiltered = false;
                break;
            }
        }

        return bFiltered;
    }
    public double get0CorrelThreshold(String tradeDate) {
        double thres = 0.95;
        if(tradeDate.equals("20130110"))
            thres = 0.98;
        return thres;
    }

}

