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

import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;


public class PermStatsHelper {
    public static void get(String args[]) {
        if(args.length!=2) {
            usage();
            return;
        }

        String sAmDerSorted = args[1];
        AmDerSeriesLoader loader = new AmDerSeriesLoader();
        loader.load(sAmDerSorted);
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools permstats sAmDerSorted");
        System.err.println("       list permIdx with its stats(matchedCnt, matchedDateCnt, minmaxProfit[01])");
        System.exit(-1);
    }

    public static class AmDerSeriesLoader extends FileLoader {
        private int prevPermIdx = -1;
        private int matchedCnt = 0;
        private double minmaxProfit0 = Double.POSITIVE_INFINITY;
        private double minmaxProfit1 = Double.POSITIVE_INFINITY;
        private TreeSet<String> matchedDateSet = new TreeSet<String>();

        public boolean onLineRead(String line, int counter) {
            String[] fields=line.split(" +");
            long hexTp = Long.parseLong(fields[0], 16);
            String ymd = Time.getTimeYMD(hexTp, false);
            double maxProfit0 = Double.valueOf(fields[3]);
            double maxProfit1 = Double.valueOf(fields[4]);
            int permIdx = Integer.valueOf(fields[15]);

            if(permIdx == prevPermIdx) {        //
                matchedCnt++;
                if(maxProfit0<minmaxProfit0) {
                    minmaxProfit0 = maxProfit0;
                }
                if(maxProfit1<minmaxProfit1) {
                    minmaxProfit1 = maxProfit1;
                }
                if(!matchedDateSet.contains(ymd))
                    matchedDateSet.add(ymd);
            } else {                            //print prevPermIdx
                if(prevPermIdx != -1) {
                    int matchedDateCnt = matchedDateSet.size();
                    String sCoord = PermHelper.getPermIdx(prevPermIdx);
                    System.out.format("%-10d %8d %8d %8.3f %8.3f %25s\n", 
                            prevPermIdx, matchedCnt, matchedDateCnt, minmaxProfit0, minmaxProfit1, sCoord);
                }
                prevPermIdx = permIdx;
                matchedCnt = 1;
                minmaxProfit0 = maxProfit0;
                minmaxProfit1 = maxProfit1;
                matchedDateSet.clear();
                matchedDateSet.add(ymd);
                //System.out.format("stats for %d\n", prevPermIdx);
            }

            return true;
        }

        public void load(String sAmDerSorted) {
            super.load(sAmDerSorted);

            //last element
            int matchedDateCnt = matchedDateSet.size();
            String sCoord = PermHelper.getPermIdx(prevPermIdx);
                    System.out.format("%-10d %8d %8d %8.3f %8.3f %25s\n", 
                    prevPermIdx, matchedCnt, matchedDateCnt, minmaxProfit0, minmaxProfit1, sCoord);
        }
    }
}

