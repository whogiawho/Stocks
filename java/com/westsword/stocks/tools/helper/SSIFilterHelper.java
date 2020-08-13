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
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.ckpt.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.tools.helper.man.*;

public class SSIFilterHelper {

    public void run(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<4) {
            usage();
            return;
        }

        String stockCode = SSUtils.getStockCode(cmd);
        String startDate = SSUtils.getStartDate(cmd);
        double threshold = SSUtils.getThreshold(cmd);

        String tradeDate0 = newArgs[0];              //tradeDate0 must >= startDate
        if(!SSUtils.checkDate(startDate, tradeDate0)) {
            usage();
            return;
        }

        //
        boolean bMatchedTradeDates = SSUtils.getSwitchStdout(cmd);
        StockDates stockDates = new StockDates(stockCode);

        String hmsList = newArgs[1];
        int maxCycle = Integer.valueOf(newArgs[2]);

        //am
        String sTradeDetailFile = newArgs[3];
        System.err.format("%8s %8s %8.2f %8s %15s %4d %s\n",
                stockCode, startDate, threshold, tradeDate0, hmsList, maxCycle, sTradeDetailFile);
        TradeDetailLoader l = new TradeDetailLoader();
        ArrayList<TradeDetail> tdList = new ArrayList<TradeDetail>();
        l.load(sTradeDetailFile, tdList);
        ArrayList<String> tradeDateList = getTradeDateList(tdList);
        System.err.format("sizeof(tradeDateList) = %d\n", tradeDateList.size());
        AmManager am = new AmManager(stockCode, tradeDateList);

        //read sTradeDetailFile, and 
        //  put those matched tradeDates whose cycle<=maxCycle into set0 
        TreeSet<String> set0 = getMatchedSet(tdList, maxCycle);
        //ckpt
        String[] fields = hmsList.split("_");
        String endHMS = fields[fields.length-1];
        CheckPoint0 ckpt = new CheckPoint0(endHMS);
        System.err.format("length(ckpt) = %d\n", ckpt.getLength());

        SSifManager ssifm = new SSifManager();
        //loop all hmsLists before hmsList.endHMS in set0 to list sorted similarSet 
        int length = ckpt.getLength();
        Combinations c = new Combinations(length, 2);
        Iterator<int[]> itr = c.iterator();
        while(itr.hasNext()) {
            int[] e = itr.next();
            hmsList = ckpt.getHMSList(e);

            ssifm.run(tradeDate0, hmsList, 
                    tradeDateList, threshold, am, 
                    set0, bMatchedTradeDates);
        }
    }

    private ArrayList<String> getTradeDateList(ArrayList<TradeDetail> tdList) {
        ArrayList<String> tradeDateList = new ArrayList<String>();

        for(int i=0; i<tdList.size(); i++) {
            tradeDateList.add(tdList.get(i).matchedTradeDate);
        }

        return tradeDateList;
    }
    //get a set of TradeDates, with cycle<=maxCycle
    private TreeSet<String> getMatchedSet(ArrayList<TradeDetail> tdList, int maxCycle) {
        TreeSet<String> mSet = new TreeSet<String>();

        for(int i=0; i<tdList.size(); i++) {
            if(tdList.get(i).cycle<=maxCycle)
                mSet.add(tdList.get(i).matchedTradeDate);
        }

        return mSet;
    }


    public static void commonUsageInfo() {
        System.err.println("       -o          ; output all matched tradedates");
        System.err.println("       -c stockCode;");
        System.err.println("       -d startDate;");
        System.err.println("       -h threshold;");
    }
    private static void usage() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssifilter [-ocdh] tradeDate0 hmsList0 maxCycle fTradeDetails");
        System.err.println("       loop all hmsList before endHMS(hmsList0), to get matched tradeDates");
        System.err.println("       then intersected with those in fTradeDetails, to get a set"); 
        System.err.println("       all elements' outCycle of the set must be <= maxCycle "); 
        System.err.println("       maxCycle   ; those matched dates with quitting date <= maxCycle");
        commonUsageInfo();

        System.exit(-1);
    }

    public static Options getOptions() {
        Options options = new Options();
        options.addOption("o", false, "does not output matched tradedates");
        options.addOption("c", true,  "a stock's code");
        options.addOption("d", true,  "a tradeDate from which a ss search is started");
        options.addOption("h", true,  "a threshold value to get ss for tradeDates");

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
}
