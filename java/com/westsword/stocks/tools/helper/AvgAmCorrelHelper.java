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

public class AvgAmCorrelHelper {
    public static void getCorrel(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=5 && newArgs.length!=3) {
            usage();
            return;
        }

        String stockCode = newArgs[0];
        String tradeDate0 = newArgs[1];
        String hms0 = newArgs[2];
        String tradeDate1 = null;
        String hms1 = null;
        if(newArgs.length==5) {
            tradeDate1 = newArgs[3];
            hms1 = newArgs[4];
        }


        SdTime1 sdt = new SdTime1(stockCode);

        if(newArgs.length==5) 
            handleSingle(stockCode, sdt, cmd, tradeDate0, hms0, tradeDate1, hms1);
        else
            handleAll(stockCode, sdt, cmd, tradeDate0, hms0);
    }


    private static void handleAll(String stockCode, SdTime1 sdt, CommandLine cmd, 
            String tradeDate0, String hms0) {
        TradeDates tradeDates = new TradeDates(stockCode);
        String startDate = CmdLineUtils.getString(cmd, "s", tradeDates.nextDate(tradeDates.firstDate()));
        String endDate = CmdLineUtils.getString(cmd, "e", tradeDates.lastDate());
        int startSd = sdt.getAbs(startDate, sdt.getCallAuctionEndTime());
        int endSd = sdt.getAbs(endDate, sdt.getCloseQuotationTime());
        int sdbw = AvgAmUtils.getBackwardSd(cmd);
        AmManager amm = AmManager.get(stockCode, sdbw, null, startDate, endDate);

        AACMan aacm = new AACMan();
        //part0
        double[] avgam0 = AvgAmUtils.getAvgAm(stockCode, tradeDate0, hms0, sdt, cmd);
        for(int sd=startSd; sd<=endSd; sd++) {
            aacm.run(avgam0, stockCode, sdt, sd, cmd, amm);
        }
    }
    private static void handleSingle(String stockCode, SdTime1 sdt, CommandLine cmd, 
            String tradeDate0, String hms0, String tradeDate1, String hms1) {
        //part0
        double[] avgam0 = AvgAmUtils.getAvgAm(stockCode, tradeDate0, hms0, sdt, cmd);
        //part1
        double[] avgam1 = AvgAmUtils.getAvgAm(stockCode, tradeDate1, hms1, sdt, cmd);

        PearsonsCorrelation pc = new PearsonsCorrelation();
        double correl = pc.correlation(avgam0, avgam1);
        System.out.format("%8.3f\n", correl);
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools [-bmise] avgamcorrel stockCode tradeDate0 hms0 [tradeDate1] [hms1]");
        System.err.println("       -b sdbw       ; at most sdbw shall be looked backward; default 1170");
        System.err.println("       -m mindist    ; default 60");
        System.err.println("       -i interval   ; default 1");
        System.err.println("       -s startDate  ; start of tradeDate");
        System.err.println("       -e endDate    ; end of tradeDate");

             String line = "       when -s&-e are on, [tradeDate1, hms1] is not effective; vice versa";
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
        options.addOption("b", true,  "at most sdtime shall be looked backward when calculating derivatives");
        options.addOption("m", true,  "minimum skipped sd distance from current time");
        options.addOption("i", true,  "the step to get am derivative");
        options.addOption("s", true,  "startDate");
        options.addOption("e", true,  "endDate");

        return options;
    }




    public static class AACMan extends TaskManager {
        public void run(double[] avgam0, String stockCode, SdTime1 sdt, int sd, CommandLine cmd, AmManager amm) {
            maxThreadsCheck();

            Thread t = new AACTask(this, avgam0, stockCode, sdt, sd, cmd, amm);
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
        }
    }
    public static class AACTask extends Task {
        private double[] avgam0;
        private String stockCode;
        private SdTime1 sdt;
        private int sd;
        private CommandLine cmd;
        private AmManager amm;

        public AACTask(AACMan aacm, double[] avgam0, 
                String stockCode, SdTime1 sdt, int sd, CommandLine cmd, AmManager amm) {
            super(aacm);

            this.avgam0 = avgam0;
            this.stockCode = stockCode;
            this.sdt = sdt; 
            this.sd = sd;
            this.cmd = cmd;
            this.amm = amm;
        }
        @Override
        public void runTask() {
            PearsonsCorrelation pc = new PearsonsCorrelation();
            long hexTp = sdt.rgetAbs(sd);
            String tradeDate1 = Time.getTimeYMD(hexTp, false);
            String hms1 = Time.getTimeHMS(hexTp, false);

            double[] avgam1 = AvgAmUtils.getAvgAm(stockCode, tradeDate1, hms1, sdt, cmd, amm);
            double correl = pc.correlation(avgam0, avgam1);

            System.out.format("%s %s %s %8.3f\n", 
                    stockCode, tradeDate1, hms1, correl);
        }
    }

}
