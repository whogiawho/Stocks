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
 
 
package com.westsword.stocks.am.average;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class AvgPrManager extends TaskManager {
    private TreeSet<Long> mHexTpSet; 

    public AvgPrManager() {
        super();
        mHexTpSet = new TreeSet<Long>();
    }

    //for AmUtils
    public void run(String stockCode, AmRecord r, TreeMap<Integer, AmRecord> prevAmrMap, SdTime1 sdt) {
        String hms = Time.getTimeHMS(r.hexTimePoint, false);
        if(mHexTpSet.contains(r.hexTimePoint)) {
            String line = String.format("%s: %x(%s) was already processed!", 
                    Utils.getCallerName(getClass()), r.hexTimePoint, hms);
            line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
            System.err.format("%s\n", line);
            return;
        }

        maxThreadsCheck();
        //System.err.format("%s: add %x(%s)\n", Utils.getCallerName(getClass()), r.hexTimePoint, hms);
        mHexTpSet.add(r.hexTimePoint);

        AvgPrTask t = new AvgPrTask(this, stockCode, r, prevAmrMap, sdt);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    public static class AvgPrTask extends Task {
        private String stockCode;
        private AmRecord r;
        private TreeMap<Integer, AmRecord> prevAmrMap;
        private SdTime1 sdt;

        private int sdbw;
        private int minSkippedSD;
        private int interval;

        public AvgPrTask(AvgPrManager ahm, 
                String stockCode, AmRecord r, TreeMap<Integer, AmRecord> prevAmrMap, SdTime1 sdt,
                int sdbw, int minSkippedSD, int interval) {
            super(ahm);

            this.stockCode = stockCode;
            this.r = r;
            this.prevAmrMap = prevAmrMap;
            this.sdt = sdt;

            this.sdbw = sdbw;
            this.minSkippedSD = minSkippedSD;
            this.interval = interval;

        }
        public AvgPrTask(AvgPrManager ahm, 
                String stockCode, AmRecord r, TreeMap<Integer, AmRecord> prevAmrMap, SdTime1 sdt) {
            //get sdbw&minSkippedSD&interval from settings.txt
            this(ahm, stockCode, r, prevAmrMap, sdt,
                    Settings.getAvgPrBackwardSd(),
                    Settings.getAvgPrMinimumSkipSd(),
                    Settings.getAvgPrInterval());
        }

        @Override
        public void runTask() {
            //convert r.hexTimePoint to hms
            String tradeDate = Time.getTimeYMD(r.hexTimePoint, false);
            String hms = Time.getTimeHMS(r.hexTimePoint, false);
            //get sAvgPrFile
            String sAvgPrFile = StockPaths.getAvgPrFile(stockCode, tradeDate, hms);

            //make derivative file for r 
            makeAvgPrFile(r, sAvgPrFile, prevAmrMap, sdt);

            //call cscript 
            ThreadMakeAvgPr.run(stockCode, tradeDate, hms);
        }

        private void makeAvgPrFile(AmRecord r, String sAvgPrFile, 
                TreeMap<Integer, AmRecord> amrMap, SdTime1 sdt) {
            //get sd from r.timeIndex
            int sd = r.timeIndex;

            //call AvgPrUtils.listAvgPr
            AvgPrUtils.listAvgPr(sd, sdbw, minSkippedSD, interval,
                    amrMap, false, sAvgPrFile);
        }
    }
}
