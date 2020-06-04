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
