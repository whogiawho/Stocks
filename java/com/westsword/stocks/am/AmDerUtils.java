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


package com.westsword.stocks.am;
 
import java.util.*;
import org.apache.commons.cli.*;
import org.apache.commons.math3.stat.regression.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.CmdLineUtils;

public class AmDerUtils {
    public final static int Default_Backward_SD = 60*5;
    public final static double Default_R2Threshold = 0.5;
    public final static double Default_NaThreshold = 0.9;
    public final static int Default_Minimum_Skipped_SD = 5;


    public static int getBackwardSd(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "b", Default_Backward_SD);
    }
    public static int getBackwardSd(CommandLine cmd, int defaultSdbw) {
        return CmdLineUtils.getInteger(cmd, "b", defaultSdbw);
    }
    public static double getR2Threshold(CommandLine cmd) {
        return CmdLineUtils.getDouble(cmd, "h", Default_R2Threshold);
    }
    public static double getNaThreshold(CommandLine cmd) {
        return CmdLineUtils.getDouble(cmd, "n", Default_NaThreshold);
    }
    public static int getMinimumSkipSd(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "m", Default_Minimum_Skipped_SD);
    }
    public static boolean getHighest(CommandLine cmd) {
        return CmdLineUtils.getBoolean(cmd, "s", false);
    }


    public static double getNaRate(int sd, double r2Threshold, int sdbw, int minSkippedSD,
            TreeMap<Integer, AmRecord> amrMap) {
        int naCount=0;
        int minDist=minSkippedSD;
        int count=sdbw-minDist+1;
        for(int dist=sdbw; dist>=minDist; dist--) {
            int start=sd-dist;
            int end=sd;
            SimpleRegression sr = new SimpleRegression();
            for(int i=start; i<=end; i++) {
                int x = i - start;
                long y = amrMap.get(i).am;
                sr.addData((double)x, (double)y);
            }

            double r2 = sr.getRSquare();
            if(r2<r2Threshold)
                naCount++;
        }

        return (double)naCount/(double)count;
    }
    public static void listSingleSd(int sd, double r2Threshold, int sdbw, int minSkippedSD,
            TreeMap<Integer, AmRecord> amrMap, boolean bStdOut, String sDerivativeFile) {
        
        int minDist=minSkippedSD;
        for(int dist=sdbw; dist>=minDist; dist--) {

            int start=sd-dist;
            int end=sd;
            SimpleRegression sr = new SimpleRegression();
            for(int i=start; i<=end; i++) {
                int x = i - start;
                AmRecord r = amrMap.get(i);
                if(r!=null) {
                    long y = r.am;
                    sr.addData((double)x, (double)y);
                }
            }

            String sSlope = translateSlope(1, sr, r2Threshold, sr.getRSquare());
            String line = String.format("%-8.3f %8s\n", sr.getRSquare(), sSlope);
            if(bStdOut)
                System.out.format("%s", line);
            if(sDerivativeFile!=null) {
                //write line to derivativeFile
                Utils.append2File(sDerivativeFile, line);
            }
        }
    }
    //type=0: direct
    //type=1: indirect
    public static String translateSlope(int type, SimpleRegression sr, double r2Threshold, double r2) {
        String sSlope = translateSlopeD(sr);  //default directly
        if(type==1)    //ind
            sSlope = translateSlopeInd(sr, r2Threshold, r2);

        return sSlope;
    }
    public static String translateSlopeD(SimpleRegression sr) {
        return ""+Utils.roundUp(sr.getSlope());
    }
    public static String translateSlopeInd(SimpleRegression sr, double r2Threshold, double r2) {
        String sSlope = "#N/A";

        if(r2>=r2Threshold)
            sSlope = ""+Utils.roundUp(sr.getSlope(), "#.###");

        return sSlope;
    }


    public static void makeAmDerPng(String stockCode, String tradeDate, String hms) {
        ThreadMakeAmDer t = new ThreadMakeAmDer(stockCode, tradeDate, hms);
        t.start();
    }
}
