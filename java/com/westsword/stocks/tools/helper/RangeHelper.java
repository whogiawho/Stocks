package com.westsword.stocks.tools.helper;


import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.*;

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

        int ckptInterval = Settings.getCkptInterval();
        int sdInterval = (int)Utils.roundUp((double)ckptInterval/interval, "#");
        System.err.format("sdInterval=%d\n", sdInterval);

        String sFormat = "%x %x %s %s %8.3f %8.3f\n";
        AmManager am = new AmManager(stockCode);
        SdTime1 sdt = new SdTime1(stockCode);
        for(int i=0; i<am.last().timeIndex; i+=sdInterval) {
            AmRecord r = am.getTargetAmRecord(i, tradeType, sdLength, targetProfit);
            if(r!=null) {
                long inTp = sdt.rgetAbs(i);
                String inYMDHMS = Time.getTimeYMDHMS(inTp, false, false);
                String outYMDHMS = Time.getTimeYMDHMS(r.hexTimePoint, false, false);
                double inPrice = am.getInPrice(tradeType, inTp);
                double outPrice = r.getOutPrice(tradeType);
                System.out.format(sFormat, 
                        inTp, r.hexTimePoint, inYMDHMS, outYMDHMS, inPrice, outPrice); 
            }
        }
    }

    



    private static void usage() {
        System.err.println("usage: java AnalyzeTools qualrange stockCode tradeType lengthInSeconds targetProfit");
        System.exit(-1);
    }
}
