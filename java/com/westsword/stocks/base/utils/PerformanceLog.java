package com.westsword.stocks.base.utils;


import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;

public class PerformanceLog {

    public static long start() {
        long tStart = 0;

        boolean bNoPerformanceLog = Settings.getSwitch(Settings.NO_PERFORMANCE_LOG);
        if(!bNoPerformanceLog) {
            tStart = System.currentTimeMillis();
        }

        return tStart;
    }

    public static void end(long tStart, String sFormat, Object... args) {
        boolean bNoPerformanceLog = Settings.getSwitch(Settings.NO_PERFORMANCE_LOG);
        if(!bNoPerformanceLog) {
            long tEnd = System.currentTimeMillis();

            Object[] newArgs = new Object[args.length+1];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            int length = (int)(tEnd - tStart);
            newArgs[args.length] = length;

            String line = String.format(sFormat, newArgs);
            Utils.append2File(StockPaths.getPerformanceLogFile(), line);
        }
    }
}
