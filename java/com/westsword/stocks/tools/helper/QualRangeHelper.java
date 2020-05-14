package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.qr.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.ckpt.*;
import com.westsword.stocks.base.Settings;

public class QualRangeHelper {
    public void findQualified(String args[]) {
        if(args.length != 5) {
            usage();
            return;
        }

        String stockCode = args[1];
        int tradeType = Integer.valueOf(args[2]);
        int lengthInSeconds = Integer.valueOf(args[3]);
        double targetProfit = Double.valueOf(args[4]);

        int interval = Settings.getSdInterval();
        int sdLength = (int)Utils.roundUp((double)lengthInSeconds/interval, "#");
        System.err.format("sdLength=%d\n", sdLength);

        loopAllSdts(stockCode, tradeType, sdLength, targetProfit);
    }
    private void loopAllSdts(String stockCode, int tradeType, int sdLength, double targetProfit) {

        CheckPoint0 ckpt0 = new CheckPoint0();
        TreeSet<String> hmsSet = ckpt0.get();

        String sFormat = "%x %x %s %s %8.3f %8.3f\n";
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
                            inTp, r.hexTimePoint, inYMDHMS, outYMDHMS, inPrice, outPrice); 
                }
            }
        }
    }
    private void usage() {
        System.err.println("usage: java AnalyzeTools qualrange stockCode tradeType lengthInSeconds targetProfit");
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

        int ckptInterval = Settings.getCkptInterval();

        TreeSet<QualRange> qrSet = new TreeSet<QualRange>();
        int start = getStart(cmd);
        int end = getEnd(cmd);
        double threshold = getThreshold(cmd);
        for(int i=start; i<=end; i++) {
            int sdLength = ckptInterval*i;
            qrm.setSdLength(sdLength);
            double[][] cm = qrm.getCorrMatrix(cmm);
            qrm.getMatchedSet(cm, qrSet, threshold);
        }

        //print max elements of qrSet
        QualRange qr = qrSet.last();
        qr.print();
        qr = qrSet.first();
        qr.print();
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
}
