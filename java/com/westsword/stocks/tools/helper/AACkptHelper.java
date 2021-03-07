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

import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.ckpt.*;
import com.westsword.stocks.base.utils.*;

public class AACkptHelper {
    public static void next(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            usageNext();
        }

        boolean bBackward = CmdLineUtils.getBoolean(cmd, "b", false);
        int interval = CmdLineUtils.getInteger(cmd, "i", 60);

        String stockCode = newArgs[0];
        String tradeDate = newArgs[1];
        String hms = newArgs[2];
        StockDates stockDates = new StockDates(stockCode);

        AACheckPoint aackpt = new AACheckPoint(interval);
        String[] sRet;
        if(!bBackward)
            sRet = aackpt.next(stockDates, tradeDate, hms);
        else
            sRet = aackpt.prev(stockDates, tradeDate, hms);

        System.out.format("%s %s\n", sRet[0], sRet[1]);
    }
    private static void usageNext() {
        System.err.println("usage: java AnalyzeTools nextaackpt [-ib] stockCode tradeDate hms");
        System.err.println("       -b            ; look backward to get next aackpt; default false");
        System.err.println("       -i            ; interval between 2 continuous aackpt; default 60");
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
        options.addOption("b", false,  "look backward to get next aackpt; default false");
        options.addOption("i", true,   "interval between 2 continuous aackpt; default 60");

        return options;
    }


}
