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
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.CmdLineUtils;

public class AnalysisTxtHelper {
    public static Options getOptions() {
        Options options = new Options();
        options.addOption("i", true,  "the step to print AmReocrd; default 1");

        return options;
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
    public static int getInterval(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "i", 1);
    }

    public static void getRange(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();

        if(newArgs.length != 3) {
            usage();
        }

        String stockCode = newArgs[0];
        String startS = newArgs[1];
        String endS = newArgs[2];
        int interval = getInterval(cmd);
        //System.out.format("interval=%d\n", interval);

        String[] fields = startS.split("_");
        String startDate = fields[0];
        String startHMS = fields[1];

        fields = endS.split("_");
        String endDate = fields[0];
        String endHMS = fields[1];

        SdTime1 sdTime = new SdTime1(stockCode);
        StockDates stockDates = new StockDates(stockCode);
        ArrayList<String> dates = new ArrayList<String>();
        String next = startDate;
        while(next!=null && next.compareTo(endDate)<=0) {
            //System.out.format("%s\n", next);
            dates.add(next);
            next = stockDates.nextDate(next);
        }

        int cnt = 0;
        AmManager am = new AmManager(stockCode, dates);
        NavigableMap<Integer, AmRecord> itemMap = am.getItemMap(startDate, startHMS, endDate, endHMS);
        for(Integer k: itemMap.keySet()) {
            AmRecord r = itemMap.get(k);
            if(cnt%interval==0) {
                //System.out.format("cnt=%d\n", cnt);
                r.print();
            }
            cnt++;
        }
    }
    private static void usage() {
        System.err.println("usage: java AnalyzeTools getanalysis [-i] stockCode sDate_sHMS eDate_eHMS ");
        System.exit(-1);
    }




    public static void getPrice(String args[]) {
        if(args.length != 4) {
            if(args[0].equals("getupprice"))
                usage2();
            else
                usage3();
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        String[] tradeDates = new String[] {
            tradeDate,
        };
        String hms = args[3];
        AmManager am = new AmManager(stockCode, tradeDates, true);
        long tp = Time.getSpecificTime(tradeDate, hms);
        double price=0.0;
        if(args[0].equals("getupprice"))
            price = am.getUpPrice(tp); 
        else
            price = am.getDownPrice(tp); 

        System.out.format("%8.3f\n", price);
    }

    private static void usage2() {
        System.err.println("usage: java AnalyzeTools getupprice stockCode tradeDate hms");
        System.exit(-1);
    }
    private static void usage3() {
        System.err.println("usage: java AnalyzeTools getdownprice stockCode tradeDate hms");
        System.exit(-1);
    }
}
