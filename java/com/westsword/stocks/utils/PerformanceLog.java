package com.westsword.stocks.utils;

import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.Settings;

public class PerformanceLog {
    public final static boolean bNoPerformanceLog = Settings.getSwitch(Settings.NO_PERFORMANCE_LOG);

    public static long start() {
        long tStart = 0;

        if(!bNoPerformanceLog) {
            tStart = System.currentTimeMillis();
        }

        return tStart;
    }

    public static void end(long tStart, String sFormat, Object... args) {
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
