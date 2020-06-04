 /*
 Copyright (C) 1989-2020 Free Software Foundation, Inc.
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
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.ckpt.*;
import com.westsword.stocks.base.utils.AnsiColor;
import com.westsword.stocks.tools.helper.man.*;
import com.westsword.stocks.analyze.ssanalyze.*;

public class SSInstancesHelper {

    public void run(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<2) {
            usage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        String startDate = SSUtils.getStartDate(cmd);
        double threshold = SSUtils.getThreshold(cmd);
        int sTDistance = SSUtils.getNearestOutDist(cmd);
        int tradeType = SSUtils.getTradeType(cmd);

        double targetRate = Double.valueOf(newArgs[1]);
        if(!SSUtils.checkTargetRate(newArgs[1])) {
            usage();
            return;
        }
        int maxCycle = Integer.valueOf(newArgs[0]);

        String sSSTableFile = SSUtils.getSSTableFile(cmd);
        String hmsList = SSUtils.getHMSList(cmd);
        if(sSSTableFile!=null&&hmsList!=null) {
            usage();
            return;
        }

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        //
        boolean bResetLog = SSUtils.getSwitchResetLog(cmd);
        boolean bLog2Files = SSUtils.getSwitchLog2File(cmd);
        boolean bStdout = SSUtils.getSwitchStdout(cmd);
        AmManager am = new AmManager(stockCode, startDate, true);
        StockDates stockDates = new StockDates(stockCode);
        SSiManager ssim = new SSiManager();
        SSInstance template = new SSInstance(stockCode, startDate, threshold, sTDistance, tradeType,
                    "", "", maxCycle, targetRate);

        if(sSSTableFile != null) {
            ArrayList<SSTableRecord> list = getSSTableRecordList(sSSTableFile);
            handle01(template, list,
                    bLog2Files, bResetLog, bStdout,
                    stockDates, am, ssim);
        } else {
            handleX0(template, hmsList, 
                    bLog2Files, bResetLog, bStdout,
                    stockDates, am, ssim, cmd);
        }
    }

    private static void handle01(SSInstance template, ArrayList<SSTableRecord> list, 
            boolean bLog2Files, boolean bResetLog, boolean bStdout,
            StockDates stockDates, AmManager am, SSiManager ssim) {
        //loop the tradeSumList 
        for(int i=0; i<list.size(); i++) {
            SSTableRecord ts = list.get(i);

            String[] fields = ts.getComponents();
            //check fields.length == 1
            if(fields.length != 1) {
                //print a warning and skip
                String sWarn = String.format("illegal matchExp: %s", ts.sMatchExp);
                sWarn = AnsiColor.getColorString(sWarn, AnsiColor.ANSI_RED);
                System.out.format("%s\n", sWarn);
                continue;
            }

            SSInstance r = new SSInstance(template);
            //get tradeDate&hmsList from ts
            String[] subFields = fields[0].split(":");
            r.tradeDate = subFields[0];
            r.hmsList = subFields[1];
            System.out.format("%8s %8s %8.2f %4d %4d %8s %8s %4d %8.3f\n",
                    r.stockCode, r.startDate, r.threshold, r.sTDistance, r.tradeType,
                    r.tradeDate, r.hmsList, r.maxCycle, r.targetRate);

            ssim.run(r, am, stockDates,
                    bLog2Files, bResetLog, bStdout);
        }
    }
    private static void handleX0(SSInstance template, String hmsList,
            boolean bLog2Files, boolean bResetLog, boolean bStdout,
            StockDates stockDates, AmManager am, SSiManager ssim, CommandLine cmd) {
        CmManager cm = new CmManager();

        TradeDates tradeDates = new TradeDates(template.stockCode, template.startDate);
        if(hmsList!=null) {
            loopTradeDates(template, hmsList, tradeDates,
                    bLog2Files, bResetLog, bStdout,
                    stockDates, am, ssim, cm);
        } else {
            CheckPoint0 ckpt = new CheckPoint0();
            String startHMSList = SSUtils.getStartHMSList(cmd);
            int[] startIdxs = null;
            if(startHMSList!=null)
                startIdxs = ckpt.getIdxList(startHMSList);
            String endHMSList = SSUtils.getEndHMSList(cmd);
            int[] endIdxs = null;
            if(endHMSList!=null)
                endIdxs = ckpt.getIdxList(endHMSList);

            cm.startWorker(template.stockCode, template.startDate, 
                    startHMSList, endHMSList, am);

            int length = ckpt.getLength();
            Combinations c = new Combinations(length, 2);
            Comparator<int[]> iC = c.comparator();
            //loop hmsList combination(n,2)
            Iterator<int[]> itr = c.iterator();
            while(itr.hasNext()) {
                int[] e = itr.next();
                hmsList = ckpt.getHMSList(e);
                if(startIdxs!=null && iC.compare(e, startIdxs)<0) {
                    System.out.format("handleX0: skipping %s\n", hmsList);
                    continue;
                }
                if(endIdxs!=null && iC.compare(e, endIdxs)>=0) {
                    break;
                }

                //clear am's trBuf(optionaly)
                loopTradeDates(template, hmsList, tradeDates,
                        bLog2Files, bResetLog, bStdout,
                        stockDates, am, ssim, cm);
            }
            System.out.format("handleX0: %s\n", "finished!");
        }

        cm.close();
    }
    private static void loopTradeDates(SSInstance template, String hmsList, TradeDates tradeDates,
            boolean bLog2Files, boolean bResetLog, boolean bStdout,
            StockDates stockDates, AmManager am, SSiManager ssim, CmManager cm) {
        double[][] corrM = cm.getCorrMatrix(template.stockCode, template.startDate, hmsList, am);
        String tradeDate = tradeDates.firstDate();
        while(tradeDate!=null) {
            SSInstance r = new SSInstance(template);
            r.tradeDate = tradeDate;
            r.hmsList = hmsList;
            ssim.run(r, am, stockDates, corrM,
                    bLog2Files, bResetLog, bStdout);

            tradeDate = tradeDates.nextDate(tradeDate);
        }
    }

    private static ArrayList<SSTableRecord> getSSTableRecordList(String sSSTableFile) {
        ArrayList<SSTableRecord> list = new ArrayList<SSTableRecord>();

        if(sSSTableFile != null) {
            SSTableLoader l = new SSTableLoader();
            l.load(list, sSSTableFile, "");
        }

        return list;
    }


    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssinstances [-rnocdhtsfm] maxCycle targetRate");
        System.err.println("       targetRate      ; something like [0-9]{1,}.[0-9]{1,3}");
        System.err.println("                       relative(<=1): targetRate"); 
        System.err.println("                       absolute(>1) : targetRate-1");

             String line = "       only one, either -f or -m or None can be enabled!";
        line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
        System.err.println(line);
        SSInstanceHelper.commonUsageInfo();

        System.err.println("       -f sSSTableFile; [tradeDate, hmsList] in the file; exclusive to -m");
        System.err.println("       -m hmsList      ; loop all [tradeDate, hmsList]; exclusive to -f");
        System.err.println("       -a startHMSList ; loop from startHMSList(inclusive)");
        System.err.println("       -b endHMSList   ; loop until endHMSList(exclusive)");
        System.err.println("                         effective only when no -fm");
        System.exit(-1);
    }

    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = SSInstanceHelper.getOptions();

            options.addOption("f", true,  "a tradeSum file");
            options.addOption("m", true,  "hmsList");
            options.addOption("a", true,  "the start hmsList(inclusive) to be looped");
            options.addOption("b", true,  "the end hmsList(exclusive) to be looped");
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }

}
