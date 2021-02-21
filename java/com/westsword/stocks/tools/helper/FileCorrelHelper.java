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

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.utils.*;

public class FileCorrelHelper {
    public static void getCorrel(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            usage();
            return;
        }

        String sFile0 = newArgs[0];
        String sFile1 = newArgs[1];
        int col = Integer.valueOf(newArgs[2]);

        ColLoader l = new ColLoader();
        ArrayList<String> sList0 = new ArrayList<String>();
        ArrayList<String> sList1 = new ArrayList<String>();
        l.load(sList0, sFile0, col);
        l.load(sList1, sFile1, col);
        //min of sList0&sList1
        int size0 = sList0.size();
        int size1 = sList1.size();
        int bwDefault = Math.min(size0, size1);
        int sdbw = CmdLineUtils.getInteger(cmd, "b", bwDefault);
        //System.out.format("size0=%d size1=%d sdbw=%d\n", size0, size1, sdbw);
        double[] x = Utils.toDoubleArray(sList0, size0-sdbw, size0);
        double[] y = Utils.toDoubleArray(sList1, size1-sdbw, size1);

        PearsonsCorrelation pc = new PearsonsCorrelation();
        double correl = getCorrel(pc, x, y);
        System.out.format("%8.3f\n", correl);
    }
    private static double getCorrel(PearsonsCorrelation pc, double[] x, double[]y) {
        return pc.correlation(x, y);
    }



    private static void usage() {
        System.err.println("usage: java AnalyzeTools [-b] filecorrel sFile0 sFile1 idxCol");
        System.err.println("       -b sdbw       ; sdbw counts shall be looked backward from last;");
        System.err.println("                       default minLen(sFile0, sFile1)");
        System.err.println("       idxCol        ; starting from 0");
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
        options.addOption("b", true,  "counts shall be looked backward from last");

        return options;
    }

}
