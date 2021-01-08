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
 
 
package com.westsword.stocks.tools.helper;

import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.analyze.sam.*;

public class SAm1Helper extends SAmHelper {
    public static void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            searchUsage("searchsam1", "600030 20201231 102357");
            return;
        }

        String stockCode = newArgs[0];
        String sModelTradeDate = newArgs[1];
        String sModelHMS = newArgs[2];
        String sModelPrefix = String.format("<%s,%s,%s>", stockCode, sModelTradeDate, sModelHMS);
        TradeDates tradeDates = new TradeDates(stockCode);

        String sSrcDerivativeDir = StockPaths.getDerivativeDir(stockCode, sModelTradeDate);
        String sSrcFile = sSrcDerivativeDir + sModelHMS + ".txt";
        AmDerLoader l = new AmDerLoader(0);
        ArrayList<Double> amderList = new ArrayList<Double>();
        l.load(amderList, sSrcFile);

        //info of base Model
        ArrayList<Segment> segList = Segment.make(amderList);
        System.out.format("%s: segList.size()=%d\n", sModelPrefix, segList.size());
        Segment maxS = Segment.getMaxSegment(segList, 270);
        ArrayList<Double> maxList = maxS.eList;
        System.out.format("%s: maxList.size()=%d\n", sModelPrefix, maxList.size());
        Double max = Collections.max(maxList);
        int idx = maxList.indexOf(max);
        System.out.format("%s: max=%8.3f idx=%d\n", sModelPrefix, max, idx);
        //list L
        ArrayList<Double> modelListL = new ArrayList<Double>(maxList.subList(0, idx));
        //list R
        ArrayList<Double> modelListR = new ArrayList<Double>(maxList.subList(idx, maxList.size()));
        System.out.format("%s: modelListL.size()=%d modelListR.size()=%d\n", 
                sModelPrefix, modelListL.size(), modelListR.size());

        SAm1Manager man = new SAm1Manager();
        String startDate = getStartDate(cmd, tradeDates.firstDate());
        String endDate = getEndDate(cmd, tradeDates.lastDate());
        double threshold = getThreshold(cmd, 0.9);
        int maxCycle = getMaxCycle(cmd, 1);
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
        for(int i=0; i<sTradeDates.length; i++) {
            String sDstTradeDate = sTradeDates[i];
            String sDstDerivativeDir = StockPaths.getDerivativeDir(stockCode, sDstTradeDate);
            //System.out.format("sDstDerivativeDir=%s\n", sDstDerivativeDir);

            man.run(sDstDerivativeDir, stockCode, sDstTradeDate, 
                    threshold, tradeDates, 
                    maxCycle, modelListL, modelListR);
        }
    }

    public static class SAm1Manager extends TaskManager {
        public void run(String sDstDerivativeDir, String stockCode, String sDstTradeDate, 
                double threshold, TradeDates tradeDates, 
                int maxCycle, ArrayList<Double> modelListL, ArrayList<Double> modelListR) {
            maxThreadsCheck();

            Thread t = new SAm1Task(this, sDstDerivativeDir, stockCode, sDstTradeDate, 
                    threshold, tradeDates,
                    maxCycle, modelListL, modelListR);

            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
        }
    }
    public static class SAm1Task extends Task {
        private String sDstDerivativeDir;
        private String stockCode;
        private String sDstTradeDate;
        private double threshold;
        private TradeDates tradeDates;
        private int maxCycle;
        private ArrayList<Double> modelListL; 
        private ArrayList<Double> modelListR;

        public SAm1Task(SAm1Manager man, 
                String sDstDerivativeDir, String stockCode, String sDstTradeDate, double threshold, 
                TradeDates tradeDates, int maxCycle, 
                ArrayList<Double> modelListL, ArrayList<Double> modelListR) {
            super(man);

            this.sDstDerivativeDir = sDstDerivativeDir;
            this.stockCode = stockCode;
            this.sDstTradeDate = sDstTradeDate;
            this.threshold = threshold;
            this.tradeDates = tradeDates;
            this.maxCycle = maxCycle;
            this.modelListL = modelListL;
            this.modelListR = modelListR;
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
                        tradeDates, maxCycle, modelListL, modelListR);
                if(bFound) {
                    break;
                }
            }
        }
        //add restriction to maxSegment end position(<1188 & >700)
        //getMaxCycle(..., 300)
        //if(listL.size()>=500 && listR.size()>=30) {
        private boolean printMatched(String sDstFile, String sDstPrefix, double threshold, 
                TradeDates tradeDates, int maxCycle, 
                ArrayList<Double> modelListL, ArrayList<Double> modelListR) {
            AmDerLoader l = new AmDerLoader(0);
            ArrayList<Double> amderList = new ArrayList<Double>();
            l.load(amderList, sDstFile);
    
            //get maxLength segment S
            ArrayList<Segment> segList = Segment.make(amderList);
            Segment maxS= Segment.getMaxSegment(segList, 300);
            if(maxS==null)
                return false;
            ArrayList<Double> maxList = maxS.eList;
            //all ams of S must be >0
            if(!SAm.isAllGt0(maxList))
                return false;
            //no larger positve segment is allowed
            int maxSIdx = segList.indexOf(maxS);
            int posSize = getFollowPositiveLargerSegment(segList, maxSIdx);
            if(posSize!=-1)
                return false;
            //700<maxS.end<1188
            if(maxS.end<700||maxS.end>=1188)
                return false;
    
            //started from its maxAm, split S into 2 parts: L&R
            double max = Collections.max(maxList);
            int idx = maxList.indexOf(max);
            ArrayList<Double> listL = new ArrayList<Double>(maxList.subList(0, idx));
            ArrayList<Double> listR = new ArrayList<Double>(maxList.subList(idx, maxList.size()));
            //restrictions of listL&listR's size&slope
            double lSlope=SAm.getSlopeR2(listL)[0], rSlope=SAm.getSlopeR2(listR)[0];

            //calculate 2 correls <listL, modelListL> & <listR, modelListR> 
            if(listL.size()>=500 && listR.size()>=30) {
                double lCorrel = SAm.getCorrel(listL, modelListL, SAm.END_BASED);
                double rCorrel = SAm.getCorrel(listR, modelListR, SAm.START_BASED);
                if(lCorrel>=threshold&&rCorrel>=threshold) {
                    int negSize = getFollowNegativeLargerSegment(segList, maxSIdx);
                    //is there a following segment, its negative max amder is greater than maxSIdx's
                    if(negSize!=-1) {
                        String[] fields = sDstPrefix.split(" +");
                        String lastDate = tradeDates.nextDate(fields[1], maxCycle);
                        AmManager amm = new AmManager(fields[0], fields[1], lastDate);
                        double inPrice = amm.getDownPrice(fields[1], fields[2]);
                        String cpt = AStockSdTime.getCloseQuotationTime();
                        double[] v = amm.getExtremePrice(fields[1], fields[2], lastDate, cpt);
                        double sProfit = inPrice - v[1];
    
                        String sFormat = "%s: %5d %5d %8.3f %8.3f " + 
                            "%8.3f %8.3f %5d %8.3f %8.3f\n";
                        System.out.format(sFormat, 
                                sDstPrefix, listL.size(), listR.size(), lCorrel, rCorrel, 
                                max, sProfit, negSize, lSlope, rSlope);
                        return true;
                    }
                }
            }
    
            return false;
        }
        private int getFollowPositiveLargerSegment(ArrayList<Segment> segList, int maxSIdx) {
            int size = -1;
    
            double rThres = (double)3/3;
            ArrayList<Double> maxList = segList.get(maxSIdx).eList;
            double maxUpAmder = Collections.max(maxList);
    
            for(int i=maxSIdx+1; i<segList.size(); i++) {
                ArrayList<Double> l = segList.get(i).eList;
                double currMax = Collections.max(l);
                if(currMax>maxUpAmder*rThres) {
                    /*
                    System.out.format("currMax=%8.3f maxUpAmder=%8.3f\n", 
                            currMax, maxUpAmder);
                    */
                    size = l.size();
                    break;
                }
            }
    
            return size;
        }
        private int getFollowNegativeLargerSegment(ArrayList<Segment> segList, int maxSIdx) {
            int size = -1;
    
            ArrayList<Double> maxList = segList.get(maxSIdx).eList;
            double maxUpAmder = Collections.max(maxList);
    
            for(int i=maxSIdx+1; i<segList.size(); i++) {
                ArrayList<Double> l = segList.get(i).eList;
                double minDownAmder = Collections.min(l);
                if(minDownAmder<0 && Math.abs(minDownAmder)>maxUpAmder) {
                    /*
                    System.out.format("minDownAmder=%8.3f maxUpAmder=%8.3f\n", 
                            minDownAmder, maxUpAmder);
                    */
                    size = l.size();
                    break;
                }
            }
    
            return size;
        }
    }

}
