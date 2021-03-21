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
import com.westsword.stocks.qr.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.ckpt.*;
import com.westsword.stocks.base.utils.*;

public class QRMMHelper {
    public void maxmatch(String[] args) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<2) {
            maxmatchUsage();
            return;
        }

        String stockCode = newArgs[0];
        String sqrFile = newArgs[1];

        CmManager cm = new CmManager();
        QualRangeManager qrm = new QualRangeManager(stockCode);
        qrm.load(sqrFile);

        int ckptIntSdLen = Utils.getCkptIntervalSdLength();

        TreeSet<QualRange> qrSet = new TreeSet<QualRange>();
        int start = getStart(cmd);
        int end = getEnd(cmd);
        double threshold = getThreshold(cmd);
        for(int i=start; i<=end; i++) {
            int sdLength = ckptIntSdLen*i;
            qrm.setSdLength(sdLength);
            double[][] cmm = qrm.getCorrMatrix(cm);
            qrm.getMatchedSet(cmm, qrSet, threshold);
        }
        cm.close();

        //print max elements of qrSet
        QualRange qr = qrSet.last();
        qr.print();

        /*
        qr = qrSet.first();
        qr.print();
        */
    }
    private int getStart(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "s", 1);
    }
    private int getEnd(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "e", 1);
    }
    public static double getThreshold(CommandLine cmd) {
        return SSUtils.getThreshold(cmd, SSUtils.Default_Threshold);
    }
    private void maxmatchUsage() {
        System.err.println("usage: java AnalyzeTools qrmaxmatch [-hse] stockCode qrFile");
        System.err.println("       qrFile;  a file generated from qualrange");
        System.err.println("       -h threshold ;  ");
        System.err.println("       -s start     ;  the min idx of sdLength");
        System.err.println("       -e end       ;  the max idx of sdLength");
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
        options.addOption("h", true,  "a threshold value to get matched qr");
        options.addOption("s", true,  "the min sdLength");
        options.addOption("e", true,  "the max sdLength");

        return options;
    }
}
