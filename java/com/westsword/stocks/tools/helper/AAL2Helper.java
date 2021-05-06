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
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.westsword.stocks.am.*;
import com.westsword.stocks.am.average.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class AAL2Helper {
    public static void get(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=5) {
            usage();
            return;
        }

        String stockCode = newArgs[0];
        String tradeDate0 = newArgs[1];
        String hms0 = newArgs[2];
        String tradeDate1 = newArgs[3];
        String hms1 = newArgs[4];
        SdTime1 sdt = new SdTime1(stockCode);
        int dist = CmdLineUtils.getInteger(cmd, "d", 300);
        //System.out.format("dist=%d\n", dist);

        if(dist<=0) {
            dist=Math.abs(dist);
        } else {
            int sd0 = sdt.getAbs(tradeDate0, hms0);
            int sd1 = sdt.getAbs(tradeDate1, hms1);
            sd0 += dist;
            sd1 += dist;
            long tp0 = sdt.rgetAbs(sd0);
            long tp1 = sdt.rgetAbs(sd1);
            tradeDate0 = Time.getTimeYMD(tp0, false);
            hms0 = Time.getTimeHMS(tp0, false);
            tradeDate1 = Time.getTimeYMD(tp1, false);
            hms1 = Time.getTimeHMS(tp1,false);
        }

        int sd0 = sdt.getAbs(tradeDate0, hms0);
        int sd1 = sdt.getAbs(tradeDate1, hms1);

        int sdbw = AvgAmUtils.getBackwardSd(cmd);
        int minSkippedSD = AvgAmUtils.getMinimumSkipSd(cmd);
        int interval = AvgAmUtils.getInterval(cmd);

        PearsonsCorrelation pc = new PearsonsCorrelation();
        AmManager amm0 = AmManager.get(stockCode, tradeDate0, hms0, sdbw+dist, null);
        TreeMap<Integer, AmRecord> amrMap0 = amm0.getAmRecordMap();
        AmManager amm1 = AmManager.get(stockCode, tradeDate1, hms1, sdbw+dist, null);
        TreeMap<Integer, AmRecord> amrMap1 = amm1.getAmRecordMap();
        double[] aal2 = new double[dist];
        for(int i=0; i<dist; i++) {
            double[] avgam0 = AvgAmUtils.getAvgAm(sd0-i, sdbw, minSkippedSD, interval, amrMap0);
            double[] avgam1 = AvgAmUtils.getAvgAm(sd1-i, sdbw, minSkippedSD, interval, amrMap1);
            double correl = pc.correlation(avgam0, avgam1);
            aal2[dist-1-i] = correl;
        }

        System.out.format("%s\n", Arrays.toString(aal2));
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools getaal2 [-bmid] stockCode tradeDate0 hms0 tradeDate1 hms1");
        System.err.println("       -b sdbw       ; at most sdbw shall be looked backward; default 1170");
        System.err.println("       -m mindist    ; default 60");
        System.err.println("       -i interval   ; default 1");
        System.err.println("       -d distance   ; default 300; -1=([tradeDate0,hms0],[tradeDate1,hms1])");
        System.err.println("                       list backward if dist<0, and forward if dist>0;");
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
        options.addOption("m", true,  "minimum skipped sd distance from current time");
        options.addOption("i", true,  "the step to get am derivative");
        options.addOption("d", true,  "the NO of avgamcorrel to make up aal2 correls");

        return options;
    }
}
