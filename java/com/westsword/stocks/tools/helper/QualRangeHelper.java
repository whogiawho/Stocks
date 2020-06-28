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
import com.westsword.stocks.base.Settings;

public class QualRangeHelper {
    public void findQualified(String args[]) {
        if(args.length != 5) {
            usage();
            return;
        }

        String stockCode = args[1];
        int tradeType = Integer.valueOf(args[2]);
        int cycle = Integer.valueOf(args[3]);
        double targetProfit = Double.valueOf(args[4]);

        int sdInterval = Settings.getSdInterval();
        int cycleSdLength = (int)Utils.roundUp((double)cycle/sdInterval, "#");
        System.err.format("cycleSdLength=%d\n", cycleSdLength);

        loopAllSdts(stockCode, tradeType, cycleSdLength, targetProfit);
    }
    private void loopAllSdts(String stockCode, int tradeType, int sdLength, double targetProfit) {

        CheckPoint0 ckpt0 = new CheckPoint0();
        TreeSet<String> hmsSet = ckpt0.get();

        String sFormat = "%s %s %x %x %8.3f %8.3f\n";
        AmManager am = new AmManager(stockCode);
        SdTime1 sdt = new SdTime1(stockCode);

        TradeDates tradeDates = new TradeDates(stockCode);
        String[] sTradeDates = tradeDates.getAllDates();
        for(int i=0; i<sTradeDates.length; i++) {
            String tradeDate = sTradeDates[i];
            for(String hms: hmsSet) {
                int timeIdx = sdt.getAbs(tradeDate, hms);

                AmRecord r = am.getTargetAmRecord(timeIdx, tradeType, sdLength, targetProfit);
                if(r!=null) {
                    long inTp = sdt.rgetAbs(timeIdx);
                    String inYMDHMS = Time.getTimeYMDHMS(inTp, false, false);
                    String outYMDHMS = Time.getTimeYMDHMS(r.hexTimePoint, false, false);
                    double inPrice = am.getInPrice(tradeType, inTp);
                    double outPrice = r.getOutPrice(tradeType);
                    System.out.format(sFormat, 
                            inYMDHMS, outYMDHMS, inTp, r.hexTimePoint, inPrice, outPrice); 
                }
            }
        }
    }
    private void usage() {
        System.err.println("usage: java AnalyzeTools qualrange stockCode tradeType cycle targetProfit");
        System.err.println("       cycle - in seconds");
        System.exit(-1);
    }




    public void maxmatch(String[] args) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length<2) {
            maxmatchUsage();
            return;
        }

        String stockCode = newArgs[0];
        String sqrFile = newArgs[1];

        CmManager cmm = new CmManager();
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
            double[][] cm = qrm.getCorrMatrix(cmm);
            qrm.getMatchedSet(cm, qrSet, threshold);
        }

        //print max elements of qrSet
        QualRange qr = qrSet.last();
        qr.print();

        /*
        qr = qrSet.first();
        qr.print();
        */
    }
    private int getStart(CommandLine cmd) {
        return SSUtils.getInteger(cmd, "s", 1);
    }
    private int getEnd(CommandLine cmd) {
        return SSUtils.getInteger(cmd, "e", 1);
    }
    private double getThreshold(CommandLine cmd) {
        return SSUtils.getDouble(cmd, "h", SSUtils.Default_Threshold);
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





    public void verify(String[] args) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length != 7) {
            verifyUsage();
            return;
        }

        double threshold = getThreshold(cmd);

        String stockCode = newArgs[0];
        int tradeType = Integer.valueOf(newArgs[1]);
        //2nd part
        int cycle = Integer.valueOf(newArgs[2]);
        double targetProfit = Double.valueOf(newArgs[3]);
        //1st part
        String endDate = newArgs[4];
        String endHMS = newArgs[5];
        int matchedSdLen = Integer.valueOf(newArgs[6]);


        int sdInterval = Settings.getSdInterval();
        int cycleSdLength = (int)Utils.roundUp((double)cycle/sdInterval, "#");
        int ckptIntSdLen = Utils.getCkptIntervalSdLength();

        SdTime1 sdt = new SdTime1(stockCode);
        QualRange qr = new QualRange(endDate, endHMS);
        qr.setSdLength(matchedSdLen);
        String endDate1 = qr.getEndDate();
        String endHMS1 = qr.getEndHMS();
        String startDate1 = qr.getStartDate(sdt);
        String startHMS1 = qr.getStartHMS(sdt);

        CheckPoint0 ckpt0 = new CheckPoint0();
        TreeSet<String> hmsSet = ckpt0.get();
        AmManager am = new AmManager(stockCode);
        TradeDates tradeDates = new TradeDates(stockCode);
        String[] sTradeDates = tradeDates.getAllDates();
        for(int i=0; i<sTradeDates.length; i++) {
            String tradeDate = sTradeDates[i];
            for(String hms: hmsSet) {
                int startSdt = sdt.getAbs(tradeDate, hms);
                long startTp = Time.getSpecificTime(tradeDate, hms);
                int endSdt = startSdt + matchedSdLen;
                long endTp = sdt.rgetAbs(endSdt);
                String startDate0 = Time.getTimeYMD(startTp, false);
                String startHMS0 = Time.getTimeHMS(startTp, false);
                String endDate0 = Time.getTimeYMD(endTp, false);
                String endHMS0 = Time.getTimeHMS(endTp, false);
                double amCorrel = am.getAmCorrel(startDate0, startHMS0, endDate0, endHMS0,
                        startDate1, startHMS1, endDate1, endHMS1); 
                if(amCorrel>=threshold) {
                    double inPrice = am.getInPrice(tradeType, endTp);
                    double outPrice = getOutPrice(inPrice, targetProfit, tradeType);
                    AmRecord ar = am.getTargetAmRecord(endSdt, tradeType, cycleSdLength, targetProfit);
                    if(ar==null) {
                        outPrice = am.getOutPrice(tradeType, endSdt+cycleSdLength);
                    }
                    double profit = getNetProfit(inPrice, outPrice, tradeType);

                    System.out.format("%s %s %s %s - %s %s %s %s : %8.3f %8.3f\n",
                            startDate0, startHMS0, endDate0, endHMS0,
                            startDate1, startHMS1, endDate1, endHMS1,
                            amCorrel, profit);
                }
            }
        }
    }

    public double getNetProfit(double inPrice, double outPrice, int tradeType) {
        double profit = 0;
        if(tradeType == Stock.TRADE_TYPE_LONG)
            profit = Trade.getNetProfit(inPrice, outPrice);
        else
            profit = Trade.getNetProfit(outPrice, inPrice);

        return profit;
    }
    public double getOutPrice(double inPrice, double targetProfit, int tradeType) {
        double outPrice = 0;
        if(tradeType == Stock.TRADE_TYPE_LONG) 
            outPrice = inPrice + targetProfit;
        else
            outPrice = inPrice - targetProfit;

        return outPrice;
    }

    private void verifyUsage() {
        System.err.println("usage: java AnalyzeTools qrverify [-h] stockCode tradeType cycle targetProfit endDate endHMS matchedSdLen");
        System.err.println("       stockCode - which stock is to be operated");
        System.err.println("       tradeType - long or short");
        System.err.println("       <cycle, targetProfit> - second part");
        System.err.println("         cycle by seconds");
        System.err.println("       <endDate, endHMS, matchedSdLen> - first part");
        System.err.println("         matchedSdLen by sdtime");
        System.exit(-1);
    }



    public void searchSS(String[] args) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length != 5) {
            searchSSUsage();
            return;
        }

        String stockCode = newArgs[0];
        String qrFile = newArgs[1];
        String sStart = newArgs[2];
        String sEnd = newArgs[3];
        String qrgDir = newArgs[4];

        //threshold
        double threshold = getThreshold(cmd);
        System.out.format("threshold=%8.3f\n", threshold);

        //load qrFile
        AmRecordLoader l = new AmRecordLoader();
        TreeMap<Integer, AmRecord> map0 = new TreeMap<Integer, AmRecord>();
        l.load(null, map0, null, qrFile);
        System.out.format("%s size=%d\n", qrFile, map0.size());
        //load <sStart, sEnd> into modeMap
        SdTime1 sdTime = new SdTime1(stockCode);
        String[] fields = sStart.split(",");
        int sdt0 = sdTime.getAbs(fields[0], fields[1]);
        fields = sEnd.split(",");
        int sdt1 = sdTime.getAbs(fields[0], fields[1]);
        NavigableMap<Integer, AmRecord> modeMap = map0.subMap(sdt0, true, sdt1, true);
        System.out.format("sdt0=%d, sdt1=%d, modeMap size=%d\n", sdt0, sdt1, modeMap.size());

        if(Utils.isFile(qrgDir)) {
            TreeMap<Integer, AmRecord> targetMap = new TreeMap<Integer, AmRecord>();
            l.load(null, targetMap, null, qrgDir);
            searchMode(modeMap, targetMap, qrgDir, sdTime, threshold);
        } else {
            //loop all qrgDir's files
            String[] qrList = Utils.getSubNames(qrgDir);
            for(int i=0; i<qrList.length; i++) {
                String sPath = qrgDir+"\\"+qrList[i];
                TreeMap<Integer, AmRecord> targetMap = new TreeMap<Integer, AmRecord>();
                l.load(null, targetMap, null, sPath);
                searchMode(modeMap, targetMap, sPath, sdTime, threshold);
            }
        }
    }
    private void searchMode(NavigableMap<Integer, AmRecord> modeMap, TreeMap<Integer, AmRecord> map0, 
            String qrFile, SdTime1 sdTime, double threshold) {
        int mS = modeMap.firstKey();
        int mE = modeMap.lastKey();
        int length = mE - mS;

        int ckptInterval = Settings.getCkptInterval();
        for(int s=map0.firstKey(); s<map0.lastKey()-length; s+=ckptInterval) {
            int e = s + length;
            NavigableMap<Integer, AmRecord> map1 = map0.subMap(s, true, e, true);
            double amCorrel = AmManager.getAmCorrel(modeMap, map1);
            //System.out.format("amCorrel=%8.3f map1.size()=%d\n", amCorrel, map1.size());
            if(amCorrel>=threshold) {
                //print map1
                long startTp = sdTime.rgetAbs(s);
                long endTp = sdTime.rgetAbs(e);
                String sStart = Time.getTimeYMDHMS(startTp, false, false).replace("_", ",");
                String sEnd = Time.getTimeYMDHMS(endTp, false, false).replace("_", ",");
                System.out.format("%x %x %s %s\n", 
                        startTp, endTp, sStart, sEnd);
            }
        }
    }
    private void searchSSUsage() {
        System.err.println("usage: java AnalyzeTools qrsearchss [-h] stockCode srcQrFile sDate,sHMS eDate,eHMS target");
        System.err.println("       search similar range <srcQrFile, sDate,sHMS_eDate,eHMS> in target");
        System.err.println("       target can either be a qrFile or a dir containing qrFiles");
        System.err.println("       -h threshold ;  ");
        System.exit(-1);
    }
}
