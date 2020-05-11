package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.ckpt.*;

public class RangeHelper {
    public static void findQualified(String args[]) {
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

    public static void loopAllSdts(String stockCode, int tradeType, int sdLength, double targetProfit) {

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
    



    private static void usage() {
        System.err.println("usage: java AnalyzeTools qualrange stockCode tradeType lengthInSeconds targetProfit");
        System.exit(-1);
    }
}
