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

import com.westsword.stocks.base.utils.*;

public class AmLineHelper {
    public static void getType(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(args.length < 2) {
            usage();
        }

        int maxThres = getMaximumThres(cmd);
        int minThres = getMinimumThres(cmd);
        //System.out.format("maxThres=%d minThres=%d\n", maxThres, minThres);

        String amLine = args[args.length-1];
        String[] amders = amLine.split(" +");
        int nPosCnt = 0;
        int nNegCnt = 0;
        for(int i=0; i<amders.length; i++) {
            double am = Double.valueOf(amders[i]);  
            if(am>0)
                nPosCnt++;
            else if(am<0)
                nNegCnt++;
        }

        //System.out.format("nPosCnt=%d nNegCnt=%d\n", nPosCnt, nNegCnt);
        int type = 0;
        if(nPosCnt>=minThres && nPosCnt<maxThres && nNegCnt==0) {
            type = 1;
        } else if(nNegCnt>=minThres && nNegCnt<maxThres  && nPosCnt==0) {
            type = -1;
        }

        System.out.format("%d\n", type);
    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools getamlinetype [-ia] amLine");
        System.err.println("  amLine - a series of am derivatives");
        System.err.println("       -i min; cnt >= min");
        System.err.println("       -a max; cnt < max");
        System.exit(-1);
    }
    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length-1);
            Options options = getOptions();

            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch(UnrecognizedOptionException e) {
        }catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }
    public static Options getOptions() {
        Options options = new Options();
        options.addOption("a", true,  "maximum thres");
        options.addOption("i", true,  "minimum thres");

        return options;
    }
    public static int getMaximumThres(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "a", Integer.MAX_VALUE);
    }
    public static int getMinimumThres(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "i", Integer.MIN_VALUE);
    }
}
