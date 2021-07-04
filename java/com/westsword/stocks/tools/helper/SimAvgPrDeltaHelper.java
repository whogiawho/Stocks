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
import com.mathworks.engine.MatlabEngine;
import com.mathworks.engine.EngineException;
import java.util.concurrent.ExecutionException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.westsword.stocks.am.*;
import com.westsword.stocks.am.average.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class SimAvgPrDeltaHelper {
    public static void get(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=0 && newArgs.length!=1) {
            usage();
        }

        String tsTriple = null;
        if(newArgs.length==1) {
            tsTriple = newArgs[0];
        }

        String sAvgPrDeltaFile = CmdLineUtils.getString(cmd, "f", null);
        String sDir = CmdLineUtils.getString(cmd, "d", null);
        if(sAvgPrDeltaFile==null||sDir==null)
            usage();
        //System.out.format("sAvgPrDeltaFile=%s sDir=%s\n", sAvgPrDeltaFile, sDir);

        if(tsTriple!=null) {
            String[] fields = tsTriple.split(",");
            String stockCode = fields[0];
            String tradeDate = fields[1];
            String hms = fields[2];
            handleSingle(stockCode, tradeDate, hms, cmd);
        } else {
            //get avgpr delta for all sds of tradeDate
            handleAll(cmd);
        }
    }


    private static void handleSingle(String stockCode, String tradeDate, String hms, CommandLine cmd) {
        //System.out.format("stockCode=%s tradeDate=%s hms=%s\n", stockCode, tradeDate, hms);

        String sAvgPrDeltaFile = CmdLineUtils.getString(cmd, "f", null);
        String sDir = CmdLineUtils.getString(cmd, "d", null);
        String sAvgPr0 = sDir+"\\"+tradeDate+"."+hms+".txt";
        double threshold = AvgPrUtils.getThreshold(cmd);

        ArrayList<DeltaSimRecord> avrrList = DeltaSimRecord.getList(sAvgPrDeltaFile);
        //System.out.format("avrrList.size=%d\n", avrrList.size());


        ColLoader cl = new ColLoader();
        ArrayList<String> sList0 = new ArrayList<String>();
        cl.load(sList0, sAvgPr0, 1);
        int size0 = sList0.size();

        PearsonsCorrelation pc = new PearsonsCorrelation();
        ArrayList<String> sList1 = new ArrayList<String>();
        for(int i=0; i<avrrList.size(); i++) {
            DeltaSimRecord r = avrrList.get(i);
            String sAvgPr1 = sDir+"\\"+r.tradeDate+"."+r.hms+".txt";
            sList1.clear();
            cl.load(sList1, sAvgPr1, 1);
            int size1 = sList1.size();
            int sdbw = Math.min(size0, size1);

            double[] x = Utils.toDoubleArray(sList0, size0-sdbw, size0);
            double[] y = Utils.toDoubleArray(sList1, size1-sdbw, size1);
            double correl = pc.correlation(x, y);

            if(correl>=threshold)
                System.out.format("%s %s %s %8.3f %8.3f %8.3f %8.3f\n", 
                        r.stockCode, r.tradeDate, r.hms, r.correl0, r.upPrice, r.downPrice, correl);
        }
    }
    private static double[][] getAvgPrDeltaMatrix(ArrayList<DeltaSimRecord> aarList, String sDir) {
        int cols = aarList.size();

        DeltaSimRecord r = aarList.get(0);
        String sAvgPr = sDir+"\\"+r.tradeDate+"."+r.hms+".txt";
        ColLoader cl = new ColLoader();
        ArrayList<String> sList0 = new ArrayList<String>();
        cl.load(sList0, sAvgPr, 1);
        int rows = sList0.size();

        double[][] m = new double[rows][cols];
        for(int i=0; i<cols; i++) {
            r = aarList.get(i);
            sAvgPr = sDir+"\\"+r.tradeDate+"."+r.hms+".txt";
            sList0.clear();
            cl.load(sList0, sAvgPr, 1);

            for(int j=0; j<rows; j++) {
                m[j][i] = Double.valueOf(sList0.get(j));
            }
        }

        return m;
    }
    private static void handleAll(CommandLine cmd) {
        String sAvgPrDeltaFile = CmdLineUtils.getString(cmd, "f", null);
        String sDir = CmdLineUtils.getString(cmd, "d", null);
        String sResDir = sDir+".res";
        Utils.mkDir(sResDir);

        ArrayList<DeltaSimRecord> aarList = DeltaSimRecord.getList(sAvgPrDeltaFile);
        double[][] m = getAvgPrDeltaMatrix(aarList, sDir);
        System.out.format("m.rows=%d m.cols=%d\n", m.length, m[0].length);
        double threshold = AvgPrUtils.getThreshold(cmd);

        double[][] cm = null;
        try {
            MatlabEngine eng = MatlabEngine.startMatlab();
            cm = eng.feval("corrcoef", (Object)m);
            if(eng!=null)
                eng.close();
            System.out.format("cm.rows=%d cm.cols=%d\n", cm.length, cm[0].length);

            //write to files
            write2Files(cm, sResDir, aarList, threshold);
        } catch(EngineException e) {
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    private static void write2Files(double[][] cm, String sResDir, ArrayList<DeltaSimRecord> aarList,
            double threshold) {
        for(int i=0; i<cm.length; i++) {
            DeltaSimRecord r0 = aarList.get(i);
            String sResFile = sResDir + "\\" + r0.tradeDate + "." + r0.hms + ".correl";
            String line = "";
            for(int j=0; j<cm[i].length; j++) {
                DeltaSimRecord r = aarList.get(j);
                if(cm[i][j]>=threshold)
                    line += String.format("%s %s %s %8.3f %8.3f %8.3f %8.3f\n", 
                            r.stockCode, r.tradeDate, r.hms, r.correl0, r.upPrice, r.downPrice, cm[i][j]);
            }
            Utils.append2File(sResFile, line, false);
        }
    }

    private static void usage() {
        System.err.println("usage: java AnalyzeTools simavgprdelta [-fd] [stockCode,tradeDate,hms]");
        System.err.println("       loop avgprdeltaFile to get correls with each other or");
        System.err.println("         with a specified [stockCode,tradeDate,hms]");
        System.err.println("       the correls are saved to the dir.res when using -f or");
        System.err.println("         printed with [stockCode,tradeDate,hms]");
        System.err.println("       -f avgprdeltaFile   ; the file generated from avgprdelta");
        System.err.println("       -d dir              ; the dir where the avgpr files of avgprdelta are");
        System.err.println("       -h threshold        ; only those whose threshold above are listed");

             String line = "       both -f&-d must be enabled!";
        line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
        System.err.println(line);

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
        options.addOption("f", true,  "the file generated from avgprdelta");
        options.addOption("d", true,  "the dir where the avgprdelta files are");
        options.addOption("h", true,  "a threshold to filter item");

        return options;
    }

}
