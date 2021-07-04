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


package com.westsword.stocks.am.avrate;
 
import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class AmVolRUtils {
    public final static int Default_Backward_SD = 1200;
    public final static int Default_Minimum_Skipped_SD = 60;
    public final static int Default_Interval = 1;
    public final static int Default_Step = 1;
    public final static String Default_AmVolRDeltaFile = "e:\\cygwin64\\tmp\512880\\amvolr\\default.delta";
    public final static String Default_AmVolROutDir = "e:\\cygwin64\\tmp\512880\\amvolr";
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
    public static String getAmVolRDeltaFile(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "f", Default_AmVolRDeltaFile);
    }
    public static String getAmVolROutDir(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "d", Default_AmVolROutDir);
    }
    public static double getThreshold(CommandLine cmd) {
        return CmdLineUtils.getDouble(cmd, "h", Default_Threshold);
    }


    public static double[] getAmVolR(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, int sdbw, int minSkippedSD, int interval, AmManager amm) {
        if(amm==null)
            amm = AmManager.get(stockCode, tradeDate, hms, sdbw, null);
        long tp = Time.getSpecificTime(tradeDate, hms);
        int sd = sdt.getAbs(tp);
        double[] amvolR = AmVolRUtils.getAmVolR(sd, sdbw, minSkippedSD, interval, amm.getAmRecordMap());

        return amvolR;
    }
    public static double[] getAmVolR(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, int sdbw, int minSkippedSD, int interval) {
        return getAmVolR(stockCode, tradeDate, hms, sdt, sdbw, minSkippedSD, interval, null);
    }
    public static double[] getAmVolR(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, CommandLine cmd, AmManager amm) {
        int sdbw = getBackwardSd(cmd);
        int minSkippedSD = getMinimumSkipSd(cmd);
        int interval = getInterval(cmd);

        return getAmVolR(stockCode, tradeDate, hms,
                sdt, sdbw, minSkippedSD, interval, amm);
    }
    public static double[] getAmVolR(String stockCode, String tradeDate, String hms, 
            SdTime1 sdt, CommandLine cmd) {
        return getAmVolR(stockCode, tradeDate, hms, sdt, cmd, null);
    }
    public static double[] getAmVolR(int sd, int sdbw, int minDist, int interval, 
            TreeMap<Integer, AmRecord> amrMap) {
        double[] amvolRs = new double[sdbw-minDist+1];

        double prevAmVolR = Double.NaN;
        double amvolR;
        AmRecord endR = amrMap.get(amrMap.floorKey(sd));
        long endAm = endR.am;
        long endTrVol = endR.trVol;
        int i=0;
        for(int dist=sdbw; dist>=minDist; dist-=interval) {
            int start=sd-dist;
            AmRecord startR = amrMap.get(amrMap.floorKey(start));
            long startAm = startR.am;
            long startTrVol = startR.trVol;
            long volDist = endTrVol - startTrVol;
            if(volDist!=0) {
                amvolR = (double)(endAm-startAm)/volDist;
                prevAmVolR = amvolR;
            } else {
                amvolR = prevAmVolR;
            }

            amvolRs[i++] = amvolR;
        }

        return amvolRs;
    }


    public static void listAmVolR(int sd, int sdbw, int minDist, int interval, 
            TreeMap<Integer, AmRecord> amrMap, boolean bStdOut, String sAmVolRFile) {
        double prevAmVolR = Double.NaN;
        double amvolR;
        AmRecord endR = amrMap.get(sd);
        long endAm = endR.am;
        long endTrVol = endR.trVol;
        //System.out.format("endSd=%d endAm=%d endTrVol=%d\n", sd, endAm, endTrVol);
        for(int dist=sdbw; dist>=minDist; dist-=interval) {
            int start=sd - dist;
            AmRecord startR = amrMap.get(amrMap.floorKey(start));
            long startAm = startR.am;
            long startTrVol = startR.trVol;
            //System.out.format("startSd=%d startAm=%d startTrVol=%d\n", start, startAm, startTrVol);
            long volDist = endTrVol - startTrVol;
            if(volDist!=0) {
                amvolR = (double)(endAm-startAm)/volDist;
                prevAmVolR = amvolR;
            } else {
                amvolR = prevAmVolR;
            }

            String line = String.format("%-20d %8.3f\n", startAm, amvolR);
            if(bStdOut)
                System.out.format("%s", line);
            if(sAmVolRFile!=null) {
                //write line to derivativeFile
                Utils.append2File(sAmVolRFile, line);
            }
        }
    }
}
