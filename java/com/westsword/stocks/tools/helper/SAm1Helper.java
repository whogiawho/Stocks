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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class SAm1Helper {
    public static void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            searchUsage();
            return;
        }

        /*
        String stockCode="600030";
        String sModelTradeDate="20201231";
        String sModelHMS="102357";
        */
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
        ArrayList<Segment> segList = makeSegments(amderList);
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
        private boolean printMatched(String sDstFile, String sDstPrefix, double threshold, 
                TradeDates tradeDates, int maxCycle, 
                ArrayList<Double> modelListL, ArrayList<Double> modelListR) {
            AmDerLoader l = new AmDerLoader(0);
            ArrayList<Double> amderList = new ArrayList<Double>();
            l.load(amderList, sDstFile);
    
            //get maxLength segment S
            ArrayList<ArrayList<Double>> segList = getSegments(amderList);
            ArrayList<Double> maxList = getMaxSegment(segList, 300);
            if(maxList==null)
                return false;
            //all ams of S must be >0
            if(!isAllGt0(maxList))
                return false;
            //no larger positve segment is allowed
            int maxListIdx = segList.indexOf(maxList);
            int posSize = getFollowPositiveLargerSegment(segList, maxListIdx);
            if(posSize!=-1)
                return false;
    
            //started from its maxAm, split S into 2 parts: L&R
            double max = Collections.max(maxList);
            int idx = maxList.indexOf(max);
            ArrayList<Double> listL = new ArrayList<Double>(maxList.subList(0, idx));
            ArrayList<Double> listR = new ArrayList<Double>(maxList.subList(idx, maxList.size()));
            //calculate 2 correls <listL, modelListL> & <listR, modelListR> 
            if(listL.size()>=500 && listR.size()>=30) {
                double lCorrel = getCorrel(listL, modelListL, END_BASED);
                double rCorrel = getCorrel(listR, modelListR, START_BASED);
                if(lCorrel>=threshold&&rCorrel>=threshold) {
                    int negSize = getFollowNegativeLargerSegment(segList, maxListIdx);
                    //is there a following segment, its negative max amder is greater than maxListIdx's
                    if(negSize!=-1) {
                        String[] fields = sDstPrefix.split(" +");
                        String lastDate = tradeDates.nextDate(fields[1], maxCycle);
                        AmManager amm = new AmManager(fields[0], fields[1], lastDate);
                        double inPrice = amm.getDownPrice(fields[1], fields[2]);
                        String cpt = AStockSdTime.getCloseQuotationTime();
                        double[] v = amm.getExtremePrice(fields[1], fields[2], lastDate, cpt);
                        double sProfit = inPrice - v[1];
    
                        String sFormat = "%s: %8d %8d %8.3f %8.3f " + 
                            "%8.3f %8.3f %8d\n";
                        System.out.format(sFormat, 
                                sDstPrefix, listL.size(), listR.size(), lCorrel, rCorrel, 
                                max, sProfit, negSize);
                        return true;
                    }
                }
            }
    
            return false;
        }
        private int getFollowPositiveLargerSegment(ArrayList<ArrayList<Double>> segList, int maxListIdx) {
            int size = -1;
    
            double rThres = (double)3/3;
            ArrayList<Double> maxList = segList.get(maxListIdx);
            double maxUpAmder = Collections.max(maxList);
    
            for(int i=maxListIdx+1; i<segList.size(); i++) {
                ArrayList<Double> l = segList.get(i);
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
        private int getFollowNegativeLargerSegment(ArrayList<ArrayList<Double>> segList, int maxListIdx) {
            int size = -1;
    
            ArrayList<Double> maxList = segList.get(maxListIdx);
            double maxUpAmder = Collections.max(maxList);
    
            for(int i=maxListIdx+1; i<segList.size(); i++) {
                ArrayList<Double> l = segList.get(i);
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
        public static int START_BASED = 0;
        public static int END_BASED = 1;
        private double getCorrel(ArrayList<Double> l0, ArrayList<Double> l1, int type, 
                int length) {
            int l0Size = l0.size();
            int l1Size = l1.size();
            List<Double> m0=null, m1=null;
            if(type == END_BASED) {
                m0 = l0.subList(l0Size-length, l0Size);
                m1 = l1.subList(l1Size-length, l1Size);
            } else {
                m0 = l0.subList(0, length);
                m1 = l1.subList(0, length);
            }
            Double[] X = m0.toArray(new Double[0]);
            Double[] Y = m1.toArray(new Double[0]);
            double[] x = ArrayUtils.toPrimitive(X);
            double[] y = ArrayUtils.toPrimitive(Y);
    
            PearsonsCorrelation pc = new PearsonsCorrelation();
            double correl = pc.correlation(x, y);
    
            return correl;
        }
        private double getCorrel(ArrayList<Double> l0, ArrayList<Double> l1, int type) {
            int l0Size = l0.size();
            int l1Size = l1.size();
            int minSize = l0Size;
            if(minSize > l1Size)
                minSize = l1Size;
    
            return getCorrel(l0, l1, type, minSize);
        }
        private boolean isAllGt0(ArrayList<Double> amderList) {
            boolean bAllGt0 = true;
    
            for(int i=0; i<amderList.size(); i++) {
                if(amderList.get(i)<0)
                    return false;
            }
    
            return bAllGt0;
        }
        private ArrayList<ArrayList<Double>> getSegments(ArrayList<Double> amderList) {
            ArrayList<ArrayList<Double>> segList = new ArrayList<ArrayList<Double>>();
    
            ArrayList<Double> eList = new ArrayList<Double>();
            boolean bStartCnt = false;
            for(int i=0; i<amderList.size(); i++) {
                double e = amderList.get(i);
    
                if(Double.isNaN(e)) {
                    if(!bStartCnt)
                        continue;
                    else {
                        //it is over to count elements
                        bStartCnt = false;
                        segList.add(eList);
                        eList = new ArrayList<Double>(); 
                    }
                } else {
                    if(!bStartCnt) {
                        bStartCnt = true;
                    }
                    eList.add(e);
                }
            }
            if(eList.size()!=0) {
                segList.add(eList);
            }
    
            return segList;
        }
        //the segment with length>=minLen, and with max amder
        private ArrayList<Double> getMaxSegment(ArrayList<ArrayList<Double>> segList, 
                int minLen) {
            ArrayList<Double> maxList = null;
    
            double maxAmder = Double.MIN_VALUE;
            for(int i=0; i<segList.size(); i++) {
                ArrayList<Double> eList = segList.get(i);
                double currMaxAmder = Collections.max(eList);
                if(eList.size()>minLen && currMaxAmder>maxAmder) {
                    maxList = eList;
                    maxAmder = currMaxAmder;
                }
            }
    
            return maxList;
        }
    }




    private static void searchUsage() {
        System.err.println("usage: java AnalyzeTools searchsam1 [-mhse] stockCode tradeDate hms");
        System.err.println("  specific search those amderivatives like <600030, 20201231, 102357>");
        System.err.println("       -m maxCycle        ; default 1");
        System.err.println("       -h threshold       ; default 0.8");
        System.err.println("       -s startDate       ; start date to begin search");
        System.err.println("       -e endDate         ; last date to end search");
        System.exit(-1);
    }
    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = getOptions();

            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }
    public static Options getOptions() {
        Options options = new Options();
        options.addOption("m", true,  "maxCycle to get maxProfit; default 1");
        options.addOption("h", true,  "a threshold to list similar amder hms or not");
        options.addOption("s", true,  "starDate to begin; default SdStartDate");
        options.addOption("e", true,  "endDate to end search; default lastTradeDate of stockCode");

        return options;
    }

    public static String getStartDate(CommandLine cmd, String defaultDate) {
        return CmdLineUtils.getString(cmd, "s", defaultDate);
    }
    public static String getEndDate(CommandLine cmd, String defaultDate) {
        return CmdLineUtils.getString(cmd, "e", defaultDate);
    }
    public static double getThreshold(CommandLine cmd, double defaultThreshold) {
        return CmdLineUtils.getDouble(cmd, "h", defaultThreshold);
    }
    public static int getMaxCycle(CommandLine cmd, int defaultMaxCycle) {
        return CmdLineUtils.getInteger(cmd, "m", defaultMaxCycle);
    }

    public static class Segment {
        public int start;
        public int end;
        public ArrayList<Double> eList;

        public Segment() {
            eList = new ArrayList<Double>();
        }

        public static Segment getMaxSegment(ArrayList<Segment> segList, int minLen) {
            Segment maxS = null;

            double maxAmder = Double.MIN_VALUE;
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
    }
    private static ArrayList<Segment> makeSegments(ArrayList<Double> amderList) {
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
