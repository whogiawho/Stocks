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
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class SAmHelper {
    public static void search1(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            search1Usage();
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

        AmDerLoader l = new AmDerLoader(0);
        String sSrcDerivativeDir = StockPaths.getDerivativeDir(stockCode, sModelTradeDate);
        String sSrcFile = sSrcDerivativeDir + sModelHMS + ".txt";
        ArrayList<Double> amderList = new ArrayList<Double>();
        l.load(amderList, sSrcFile);

        //info of base Model
        ArrayList<ArrayList<Double>> segList = getSegments(amderList);
        System.out.format("%s: segList.size()=%d\n", sModelPrefix, segList.size());
        ArrayList<Double> maxList = getMaxSegment(segList, 270);
        System.out.format("%s: maxList.size()=%d\n", sModelPrefix, maxList.size());
        Double max = Collections.max(maxList);
        int idx = maxList.indexOf(max);
        System.out.format("%s: max=%8.3f idx=%d allGt0=%b\n", 
                sModelPrefix, max, idx, isAllGt0(maxList));
        //list L
        ArrayList<Double> modelListL = new ArrayList<Double>(maxList.subList(0, idx));
        //list R
        ArrayList<Double> modelListR = new ArrayList<Double>(maxList.subList(idx, maxList.size()));
        System.out.format("%s: modelListL.size()=%d modelListR.size()=%d\n", 
                sModelPrefix, modelListL.size(), modelListR.size());

        int count = 0;
        String startDate = getStartDate(cmd, tradeDates.firstDate());
        String endDate = getEndDate(cmd, tradeDates.lastDate());
        double threshold = getThreshold(cmd, 0.9);
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
        for(int i=0; i<sTradeDates.length; i++) {
            String sDstTradeDate = sTradeDates[i];
            String sDstDerivativeDir = StockPaths.getDerivativeDir(stockCode, sDstTradeDate);
            //System.out.format("sDstDerivativeDir=%s\n", sDstDerivativeDir);

            String[] sFiles = Utils.getSubNames(sDstDerivativeDir);
            for(int j=0; j<sFiles.length; j++) {
                String sDstFile = sFiles[j];
                String hms1 = sDstFile.replace(".txt", "");
                sDstFile = sDstDerivativeDir + sDstFile;
                String sDstPrefix = String.format("%s %s %s", stockCode, sDstTradeDate, hms1);

                boolean bFound = printMatched(l, amderList, sDstFile, sDstPrefix, threshold,  
                        tradeDates, 180, modelListL, modelListR);
                if(bFound) {
                    count++;
                    break;
                }
            }
        }
        System.out.format("count=%d\n", count);
    }
    private static boolean printMatched(AmDerLoader l, ArrayList<Double> amderList, 
            String sDstFile, String sDstPrefix, double threshold, 
            TradeDates tradeDates, int maxCycle, 
            ArrayList<Double> modelListL, ArrayList<Double> modelListR) {
        amderList.clear();
        l.load(amderList, sDstFile);

        //get maxLength segment S
        ArrayList<ArrayList<Double>> segList = getSegments(amderList);
        ArrayList<Double> maxList = getMaxSegment(segList, 270);
        if(maxList==null)
            return false;
        int maxListIdx = segList.indexOf(maxList);
        //all ams of S must be >0
        if(!isAllGt0(maxList))
            return false;

        //started from its maxAm, split S into 2 parts: L&R
        double max = Collections.max(maxList);
        int idx = maxList.indexOf(max);
        ArrayList<Double> listL = new ArrayList<Double>(maxList.subList(0, idx));
        ArrayList<Double> listR = new ArrayList<Double>(maxList.subList(idx, maxList.size()));
        //calculate 2 correls <listL, modelListL> & <listR, modelListR> 
        if(listL.size()>=240 && listR.size()>=30) {
            double lCorrel = getCorrel(listL, modelListL);
            double rCorrel = getCorrel(listR, modelListR);
            if(lCorrel>=threshold&&rCorrel>=threshold) {
                //is there a following segment, its negative max amder is greater than maxListIdx's
                if(getFollowNegativeLargerSegment(segList, maxListIdx)) {
                    String[] fields = sDstPrefix.split(" +");
                    String lastDate = tradeDates.nextDate(fields[1], maxCycle);
                    AmManager amm = new AmManager(fields[0], fields[1], lastDate);
                    double inPrice = amm.getDownPrice(fields[1], fields[2]);
                    String cpt = AStockSdTime.getCloseQuotationTime();
                    double[] v = amm.getExtremePrice(fields[1], fields[2], lastDate, cpt);
                    double sProfit = inPrice - v[1];

                    String sFormat = "%s: %8d %8d %8.3f %8.3f %8.3f %8.3f\n";
                    System.out.format(sFormat, 
                            sDstPrefix, listL.size(), listR.size(), lCorrel, rCorrel, max, sProfit);
                    return true;
                }
            }
        }

        return false;
    }
    private static boolean getFollowNegativeLargerSegment(ArrayList<ArrayList<Double>> segList, int maxListIdx) {
        boolean bFound = false;

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
                bFound = true;
                break;
            }
        }

        return bFound;
    }
    private static double getCorrel(ArrayList<Double> l0, ArrayList<Double> l1) {
        int l0Size = l0.size();
        int l1Size = l1.size();
        int minSize = l0Size;
        if(minSize > l1Size)
            minSize = l1Size;
        List<Double> m0 = l0.subList(l0Size-minSize, l0Size);
        List<Double> m1 = l1.subList(l1Size-minSize, l1Size);
        Double[] X = m0.toArray(new Double[0]);
        Double[] Y = m1.toArray(new Double[0]);
        double[] x = ArrayUtils.toPrimitive(X);
        double[] y = ArrayUtils.toPrimitive(Y);

        PearsonsCorrelation pc = new PearsonsCorrelation();
        double correl = pc.correlation(x, y);

        return correl;
    }
    private static boolean isAllGt0(ArrayList<Double> amderList) {
        boolean bAllGt0 = true;

        for(int i=0; i<amderList.size(); i++) {
            if(amderList.get(i)<0)
                return false;
        }

        return bAllGt0;
    }
    private static ArrayList<ArrayList<Double>> getSegments(ArrayList<Double> amderList) {
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
    private static ArrayList<Double> getMaxSegment(ArrayList<ArrayList<Double>> segList, 
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
    private static void search1Usage() {
        System.err.println("usage: java AnalyzeTools searchsam1 [-hse] stockCode tradeDate hms");
        System.err.println("  specific search those amderivatives like <600030, 20201231, 102357>");
        System.err.println("       -h threshold       ; default 0.8");
        System.err.println("       -s startDate       ; start date to begin search");
        System.err.println("       -e endDate         ; last date to end search");
        System.exit(-1);
    }





    public static void search0(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            search0Usage();
            return;
        }

        AmDerLoader l = new AmDerLoader();
        ArrayList<Double> amderList = new ArrayList<Double>();
        PearsonsCorrelation pc = new PearsonsCorrelation();

        //get src file amder series
        String stockCode = newArgs[0];
        String tradeDate = newArgs[1];
        String hms = newArgs[2];
        String sSrcDerivativeDir = StockPaths.getDerivativeDir(stockCode, tradeDate);
        String sSrcFile = sSrcDerivativeDir + hms + ".txt";
        l.load(amderList, sSrcFile);
        Double[] X = amderList.toArray(new Double[0]);
        double[] x = ArrayUtils.toPrimitive(X);

        //loop dst file amder series
        TradeDates tradeDates = new TradeDates(stockCode);
        String startDate = getStartDate(cmd, tradeDates.firstDate());
        String endDate = getEndDate(cmd, tradeDates.lastDate());
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
        for(int i=0; i<sTradeDates.length; i++) {
            String sDstTradeDate = sTradeDates[i];
            String sDstDerivativeDir = StockPaths.getDerivativeDir(stockCode, sDstTradeDate);

            String[] sFiles = Utils.getSubNames(sDstDerivativeDir);
            for(int j=0; j<sFiles.length; j++) {
                String sDstFile = sFiles[j];
                String hms1 = sDstFile.replace(".txt", "");
                sDstFile = sDstDerivativeDir + sDstFile;

                amderList.clear();
                l.load(amderList, sDstFile);
                Double[] Y = amderList.toArray(new Double[0]);
                double[] y = ArrayUtils.toPrimitive(Y);

                double correl = pc.correlation(x, y);
                System.out.format("%s,%s %s,%s %8.3f\n", 
                        tradeDate, hms, sDstTradeDate, hms1, correl);
            }
        }
    }
    private static void search0Usage() {
        System.err.println("usage: java AnalyzeTools searchsam0 [-hse] stockCode tradeDate hms");
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
}
