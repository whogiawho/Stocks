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
 
 
package com.westsword.stocks.tools.helper.man;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.Task;
import com.westsword.stocks.base.TaskManager;

public class AmHoleManager extends TaskManager {

    public void run(int sdIdx, double r2Threshold, double naThreshold, int sdbw, 
            int minSkippedSD, SdTime1 sdt, TreeMap<Integer, AmRecord> amrMap) {
        maxThreadsCheck();

        Thread t = new AmHoleTask(this, sdIdx, r2Threshold, naThreshold, sdbw, minSkippedSD,
                sdt, amrMap);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    public static class AmHoleTask extends Task {
        private int sdIdx;
        private double r2Threshold;
        private double naThreshold;
        private int sdbw;
        private int minSkippedSD;
        private SdTime1 sdt;
        private TreeMap<Integer, AmRecord> amrMap;


        public AmHoleTask(AmHoleManager ahm, int sdIdx, double r2Threshold, double naThreshold, int sdbw, 
                int minSkippedSD, SdTime1 sdt, TreeMap<Integer, AmRecord> amrMap) {
            super(ahm);

            this.sdIdx = sdIdx;
            this.r2Threshold = r2Threshold;
            this.naThreshold = naThreshold;
            this.sdbw = sdbw;
            this.minSkippedSD = minSkippedSD;
            this.sdt = sdt;
            this.amrMap = amrMap;
        }

        @Override
        public void runTask() {
            //run ssinstance
            double naRate = AmDerUtils.getNaRate(sdIdx, r2Threshold, sdbw, minSkippedSD, amrMap);
            if(naRate >= naThreshold) {
                long tp = sdt.rgetAbs(sdIdx);
                String tradeDate = Time.getTimeYMD(tp, false);
                String hms = Time.getTimeHMS(tp, false);
                System.out.format("%10d %8s %8s %8.3f\n", sdIdx, tradeDate, hms, naRate);
            } else {
                System.out.format("%10d %8s %8s %8.3f\n", sdIdx, tradeDate, hms, naRate);
            }
        }
    }
}
