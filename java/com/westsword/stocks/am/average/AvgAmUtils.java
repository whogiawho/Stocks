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

public class AvgAmUtils {
    public final static int Default_Backward_SD = 1170;
    public final static int Default_Minimum_Skipped_SD = 60;
    public final static int Default_Interval = 1;
    public final static int Default_Step = 1;
    public final static String Default_AmVolRDeltaFile = "e:\\cygwin64\\tmp\512880\\avgam\\default.delta";
    public final static String Default_AmVolROutDir = "e:\\cygwin64\\tmp\512880\\avgam";
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
    public static String getAvgAmDeltaFile(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "f", Default_AmVolRDeltaFile);
    }
    public static String getAvgAmOutDir(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "d", Default_AmVolROutDir);
    }
    public static double getThreshold(CommandLine cmd) {
        return CmdLineUtils.getDouble(cmd, "h", Default_Threshold);
    }


    public static double[] getAvgAm(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, int sdbw, int minSkippedSD, int interval, AmManager amm) {
        if(amm==null)
            amm = AmManager.get(stockCode, tradeDate, hms, sdbw, null);
        long tp = Time.getSpecificTime(tradeDate, hms);
        int sd = sdt.getAbs(tp);
        double[] avgam = AvgAmUtils.getAvgAm(sd, sdbw, minSkippedSD, interval, amm.getAmRecordMap());

        return avgam;
    }
    public static double[] getAvgAm(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, int sdbw, int minSkippedSD, int interval) {
        return getAvgAm(stockCode, tradeDate, hms, sdt, sdbw, minSkippedSD, interval, null);
    }
    public static double[] getAvgAm(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, CommandLine cmd, AmManager amm) {
        int sdbw = getBackwardSd(cmd);
        int minSkippedSD = getMinimumSkipSd(cmd);
        int interval = getInterval(cmd);

        return getAvgAm(stockCode, tradeDate, hms,
                sdt, sdbw, minSkippedSD, interval, amm);
    }
    public static double[] getAvgAm(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, CommandLine cmd) {
        return getAvgAm(stockCode, tradeDate, hms, sdt, cmd, null);
    }
    public static double[] getAvgAm(int sd, int sdbw, int minDist, int interval, 
            TreeMap<Integer, AmRecord> amrMap) {
        double[] avgams = new double[sdbw-minDist+1];

        long endAm = amrMap.get(amrMap.floorKey(sd)).am;
        int i=0;
        for(int dist=sdbw; dist>=minDist; dist-=interval) {
            int start=sd-dist;
            long startAm = amrMap.get(amrMap.floorKey(start)).am;
            double avgAm = (endAm-startAm)/dist;
            avgams[i++] = avgAm;
        }

        return avgams;

    }


    public static void listAvgAm(int sd, int sdbw, int minDist, int interval,
            TreeMap<Integer, AmRecord> amrMap, boolean bStdOut, String sAvgAmFile) {
        long endAm = amrMap.get(sd).am;
        for(int dist=sdbw; dist>=minDist; dist-=interval) {
            int start=sd-dist;
            long startAm = amrMap.get(amrMap.floorKey(start)).am;
            double avgAm = (endAm-startAm)/dist;

            String line = String.format("%-20d %8.3f\n", startAm, avgAm);
            if(bStdOut)
                System.out.format("%s", line);
            if(sAvgAmFile!=null) {
                //write line to derivativeFile
                Utils.append2File(sAvgAmFile, line);
            }
        }
    }
}
