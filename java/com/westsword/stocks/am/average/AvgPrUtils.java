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


package com.westsword.stocks.am.average;
 
import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class AvgPrUtils {
    public final static int Default_Backward_SD = 1200;
    public final static int Default_Minimum_Skipped_SD = 1;
    public final static int Default_Interval = 1;
    public final static int Default_Step = 1;
    public final static double Default_Threshold = 0.8;

    public static int getBackwardSd(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "b", Default_Backward_SD);
    }
    public static int getMinimumSkipSd(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "m", Default_Minimum_Skipped_SD);
    }
    public static int getInterval(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "i", Default_Interval);
    }
    public static int getStep(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "e", Default_Step);
    }
    public static double getThreshold(CommandLine cmd) {
        return CmdLineUtils.getDouble(cmd, "h", Default_Threshold);
    }


    public static double[] getAvgPr(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, int sdbw, int minSkippedSD, int interval, AmManager amm) {
        if(amm==null)
            amm = AmManager.get(stockCode, tradeDate, hms, sdbw, null);
        long tp = Time.getSpecificTime(tradeDate, hms);
        int sd = sdt.getAbs(tp);
        double[] avgPr = AvgPrUtils.getAvgPr(sd, sdbw, minSkippedSD, interval, amm.getAmRecordMap());

        return avgPr;
    }
    public static double[] getAvgPr(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, int sdbw, int minSkippedSD, int interval) {
        return getAvgPr(stockCode, tradeDate, hms, sdt, sdbw, minSkippedSD, interval, null);
    }
    public static double[] getAvgPr(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, CommandLine cmd, AmManager amm) {
        int sdbw = getBackwardSd(cmd);
        int minSkippedSD = getMinimumSkipSd(cmd);
        int interval = getInterval(cmd);

        return getAvgPr(stockCode, tradeDate, hms,
                sdt, sdbw, minSkippedSD, interval, amm);
    }
    public static double[] getAvgPr(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, CommandLine cmd) {
        return getAvgPr(stockCode, tradeDate, hms, sdt, cmd, null);
    }
    public static double[] getAvgPr(int sd, int sdbw, int minDist, int interval, 
            TreeMap<Integer, AmRecord> amrMap) {
        GetAvgPr gap = new GetAvgPr(sdbw, minDist);
        loopAmrMap(sd, sdbw, minDist, interval, amrMap, gap);
        return gap.avgPrs;
    }
    public static class GetAvgPr implements ILoopAmrMap {
        public double[] avgPrs;

        public GetAvgPr(int sdbw, int minDist) {
            avgPrs = new double[sdbw-minDist+1];
        }
        public void onLoopItemCompleted(int idx, double avgPr, AmRecord r) {
            avgPrs[idx] = avgPr;
        }
    }


    public static void listAvgPr(int sd, int sdbw, int minDist, int interval, 
            TreeMap<Integer, AmRecord> amrMap, boolean bStdOut, String sAvgPrFile) {
        ListAvgPr lap = new ListAvgPr(bStdOut, sAvgPrFile);
        loopAmrMap(sd, sdbw, minDist, interval, amrMap, lap);
    }
    public static class ListAvgPr implements ILoopAmrMap {
        public boolean bStdOut;
        public String sAvgPrFile;

        public ListAvgPr(boolean bStdOut, String sAvgPrFile) {
            this.bStdOut = bStdOut;
            this.sAvgPrFile = sAvgPrFile;
        }
        public void onLoopItemCompleted(int idx, double avgPr, AmRecord r) {
            String line = String.format("%-20d %f\n", r.am, avgPr);
            if(bStdOut)
                System.out.format("%s", line);
            if(sAvgPrFile!=null) {
                //write line to derivativeFile
                Utils.append2File(sAvgPrFile, line);
            }
        }
    }





    public static void loopAmrMap(int sd, int sdbw, int minDist, int interval, 
            TreeMap<Integer, AmRecord> amrMap, ILoopAmrMap ilam) {
        double prevAvgPr = Double.NaN;
        double avgPr;
        AmRecord endR = amrMap.get(amrMap.floorKey(sd));
        long endTrVol = endR.trVol;
        double endTrAmount = endR.trAmount;
        int i=0;
        for(int dist=sdbw; dist>=minDist; dist-=interval) {
            int start = sd - dist;
            AmRecord startR = amrMap.get(amrMap.floorKey(start));
            long startTrVol = startR.trVol;
            double startTrAmount = startR.trAmount;
            long volDist = endTrVol - startTrVol;
            double amountDist = endTrAmount - startTrAmount;
            if(volDist!=0) {
                avgPr = (double)amountDist/volDist;
                prevAvgPr = avgPr;
            } else {
                avgPr = prevAvgPr;
            }

            ilam.onLoopItemCompleted(i++, avgPr, startR);
        }
    }
    public static interface ILoopAmrMap {
        public void onLoopItemCompleted(int idx, double avgPr, AmRecord r);
    }
}
