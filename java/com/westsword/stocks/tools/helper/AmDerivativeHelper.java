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
import org.apache.commons.math3.stat.regression.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.base.Utils;

public class AmDerivativeHelper {
    public static void list(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3 && newArgs.length!=2) {
            usage();
            return;
        }

        String stockCode = newArgs[0];
        String tradeDate = newArgs[1];
        String hms = null;
        if(newArgs.length==3) {
            hms = newArgs[2];
        }

        
        boolean bHighestR2 = AmDerUtils.getHighest(cmd);
        if(!bHighestR2) {
            if(hms!=null) {
                handleSingleHMS(stockCode, tradeDate, hms, cmd);
            } else {
                //mkdir derivative
                Utils.resetDir(StockPaths.getDerivativeDir(stockCode, tradeDate));

                //make am derivatives for all sds of tradeDate
                handleAllHMS(stockCode, tradeDate, cmd);
            }
        } else {
            //divide the backward sdtimes into segments
            //for each segment, there is a highest R2
            //the biggest one is selected, and the others are listed as following for reference
            writeAmDerivateives(stockCode, tradeDate);
        }
    }


    private static int getCodec(int[] idxs, double[] slopes, long[] factorials) {
        int codec = 0;
        int n=idxs.length;
        int[] coords = new int[n];
        for(int i=0; i<n; i++) {
            int cnt = 0;
            int idxBase = idxs[i];
            for(int j=0; j<i; j++) {
                int idx = idxs[j];
                if(slopes[idxBase]>slopes[idx])
                    cnt++;
            }
            coords[i] = cnt;
            codec += coords[i] * factorials[i];
        }

        return codec;
    } 

    /*    group0
    private static int baseU=60;
    private static int maxL=143;
    static int[] sdbwList = {
        baseU*1, baseU*2, baseU*4, baseU*7, baseU*12, baseU*20, baseU*33, baseU*54, baseU*88, baseU*maxL,
    };
    */
    /*    group1
    */    
    private static int baseU=1;
    private static int maxL=10;
    static int[] sdbwList = {
        baseU*1, baseU*2, baseU*3, baseU*4, baseU*5, baseU*6, baseU*7, baseU*8, baseU*9, baseU*maxL,
    };

    private static int[] idxs0 = {
        9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
    };
    private static double getPrice(TreeMap<Integer, AmRecord> amrMap, int sd) {
        double price = Double.NaN;
        AmRecord r0 = amrMap.get(sd);
        if(r0!=null)
            price = r0.upPrice;
        return price;
    }
    private static double getMaxPrice(TreeMap<Integer, AmRecord> amrMap, int startSd, int endSd) {
        if(startSd>endSd)
            return amrMap.get(endSd).downPrice;

        double maxPrice = 0;
        for(int sd=startSd; sd<=endSd; sd++) {
            AmRecord r = amrMap.get(sd);
            if(r!=null) {
                double currentPrice = r.downPrice;
                if(currentPrice>maxPrice)
                    maxPrice = currentPrice;
            }
        }
        return maxPrice;
    }
    private static void writeAmDerivateives(String stockCode, String tradeDate) {
        StockDates stockDates = new StockDates(stockCode);

        //reset sAmDerivativeFile
        String sAmDerivativeFile = StockPaths.getAmDerivativeFile(stockCode, tradeDate);
        Utils.deleteFile(sAmDerivativeFile);

        AmManager amm = AmManager.get(stockCode, tradeDate, AStockSdTime.getCallAuctionEndTime(), baseU*maxL, stockDates);
        TreeMap<Integer, AmRecord> amrMap = amm.getAmRecordMap();

        long[] factorials0 = Utils.getFactorials(idxs0);
        SdTime1 sdt = new SdTime1(stockCode);
        int startSd = sdt.getAbs(sdt.getCallAuctionEndTime(tradeDate));
        int endSd = sdt.getAbs(sdt.getCloseQuotationTime(tradeDate));
        String nD1 = stockDates.nextDate(tradeDate);
        int endSdNd1 = endSd;
        if(nD1!=null)
            endSdNd1 = sdt.getAbs(sdt.getCloseQuotationTime(nD1));
        /*
        System.out.format("tradeDate=%s, nD1=%s endSd=%d, endSdNd1=%d\n", 
                tradeDate, nD1, endSd, endSdNd1);
        */
        //cqPricet0
        double cqPricet0 = getPrice(amrMap, endSd);

        for(int sd=startSd; sd<=endSd; sd++) {
            long hexTp = sdt.rgetAbs(sd);
            String hms = Time.getTimeHMS(hexTp, false);
            double price = getPrice(amrMap, sd);
            //t0
            double maxPricet0 = getMaxPrice(amrMap, sd+1, endSd);
            //t1
            double maxPricet1 = getMaxPrice(amrMap, sd+1, endSdNd1);

            String sAmDer = String.format("%x %8d %8.3f %8.3f %8.3f", 
                    hexTp, sd, price, maxPricet0-price, maxPricet1-price);
            sAmDer = getString(sd, sAmDer, amrMap, factorials0);

            String line = String.format("%s\n", sAmDer);
            System.out.format("%s", line);
        }
    }
    private static String getString(int sd, String sPrefix, TreeMap<Integer, AmRecord> amrMap, 
            long[] factorials0) {
        String sAmDer = sPrefix;

        double[] slopes = new double[sdbwList.length];

        for(int i=0; i<sdbwList.length; i++) {
            int sdbw = sdbwList[i];

            int start = sd - sdbw;
            int end = sd;
            SimpleRegression sr = new SimpleRegression();
            for(int j=start; j<=end; j++) {
                int x = j - start;
                AmRecord r = amrMap.get(j);
                if(r!=null) {
                    long y = r.am;
                    sr.addData((double)x, (double)y);
                }
            }
            String sSlope = AmDerUtils.translateSlopeD(sr);
            slopes[i] = Double.valueOf(sSlope);

            sAmDer = String.format("%s %8s", sAmDer, sSlope); 
        }
        int codec0 = getCodec(idxs0, slopes, factorials0);
        sAmDer = String.format("%s %10d", sAmDer, codec0);

        return sAmDer;
    }


    public static void handleAllHMS(String stockCode, String tradeDate, CommandLine cmd) {
        double r2Threshold = AmDerUtils.getR2Threshold(cmd);
        int sdbw = AmDerUtils.getBackwardSd(cmd);
        int minSkippedSD = AmDerUtils.getMinimumSkipSd(cmd);

        AmManager amm = AmManager.get(stockCode, tradeDate, AStockSdTime.getCallAuctionEndTime(), sdbw, null);

        //loop all sds of tradeDate
        SdTime1 sdt = new SdTime1(stockCode);
        int startSd = sdt.getAbs(sdt.getCallAuctionEndTime(tradeDate));
        int endSd = sdt.getAbs(sdt.getCloseQuotationTime(tradeDate));
        for(int sd=startSd; sd<=endSd; sd++) {
            //convert sd to sDerivativeFile
            long tp = sdt.rgetAbs(sd);
            String hms = Time.getTimeHMS(tp, false);
            String sDerivativeFile = StockPaths.getDerivativeFile(stockCode, tradeDate, hms);

            listSingleSd(sd, r2Threshold, sdbw, minSkippedSD,
                    amm, false, sDerivativeFile);
        }
    }
    public static void handleSingleHMS(String stockCode, String tradeDate, String hms, CommandLine cmd) {
        double r2Threshold = AmDerUtils.getR2Threshold(cmd);
        int sdbw = AmDerUtils.getBackwardSd(cmd);
        int minSkippedSD = AmDerUtils.getMinimumSkipSd(cmd);

        AmManager amm = AmManager.get(stockCode, tradeDate, hms, sdbw, null);

        SdTime1 sdt = new SdTime1(stockCode);
        long tp = Time.getSpecificTime(tradeDate, hms);
        int sd = sdt.getAbs(tp);
        System.err.format("%s_%s tp=%x, sd=%d am=%d\n", tradeDate, hms, tp, sd, amm.getAm(sd));

        listSingleSd(sd, r2Threshold, sdbw, minSkippedSD, 
                amm, true, null);
    } 
    public static void listSingleSd(int sd, double r2Threshold, int sdbw, int minSkippedSD, 
            AmManager amm, boolean bStdOut, String sDerivativeFile) {

        AmDerUtils.listSingleSd(sd, r2Threshold, sdbw, minSkippedSD,
                amm.getAmRecordMap(), bStdOut, sDerivativeFile);
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools listamderivatives [-bhm] stockCode tradeDate [hms]");
        System.err.println("       only print but not write to file when hms is specified");
        System.err.println("       -b sdbw       ; at most sdbw shall be looked backward; default 300");
        System.err.println("       -h r2Threshold; default 0.5");
        System.err.println("       -m mindist    ; default 5");
        System.err.println("       -s            ; list only the one with highest R2 from all backward sdtimes");
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
        options.addOption("b", true,  "at most sdtime shall be looked backward when calculating derivatives");
        options.addOption("h", true,  "a R2 threshold for effective derivative");
        options.addOption("m", true,  "minimum skipped sd distance from current time");
        options.addOption("s", false, "list only the one with highest R2 from all backward sdtimes");

        return options;
    }


}
