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

import com.westsword.stocks.am.*;
import com.westsword.stocks.am.derivative.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.tools.helper.man.*;

public class AmHoleHelper {
    public static void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=4) {
            usage();
        }

        double r2Threshold = AmDerUtils.getR2Threshold(cmd);
        int sdbw = AmDerUtils.getBackwardSd(cmd, 14405);
        double naThreshold = AmDerUtils.getNaThreshold(cmd);

        String stockCode = newArgs[0];
        int startSd = Integer.valueOf(newArgs[1]);
        int endSd = Integer.valueOf(newArgs[2]);
        int step = Integer.valueOf(newArgs[3]);

        AmHoleManager ahm = new AmHoleManager();
        AmManager amm = new AmManager(stockCode);
        TreeMap<Integer, AmRecord> amrMap = amm.getAmRecordMap();
        int minSkippedSD = AmDerUtils.getMinimumSkipSd(null);
        SdTime1 sdt = new SdTime1(stockCode);
        int i=startSd;
        while(i<endSd) {
            ahm.run(i, r2Threshold, naThreshold, sdbw, minSkippedSD, sdt, amrMap);

            i+=step;
        }
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools ssamhole [-bhn] stockCode startSd endSd step");
        System.err.println("       search am hole with specified criteria");
        System.err.println("       -b sdbw     ; at most sdbw shall be looked backward; default 14405");
        System.err.println("       -h r2Threshold; default 0.5");
        System.err.println("       -n naThreshold; default 0.9");
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
        options.addOption("n", true,  "naThreshold to define an am hole");

        return options;
    }
}

