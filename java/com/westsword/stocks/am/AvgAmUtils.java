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


package com.westsword.stocks.am;
 
import java.util.*;

import com.westsword.stocks.base.Utils;

public class AvgAmUtils {

    public static double[] getAvgAm(int sd, int sdbw, int minDist, TreeMap<Integer, AmRecord> amrMap) {
        double[] avgams = new double[sdbw-minDist+1];

        long endAm = amrMap.get(amrMap.floorKey(sd)).am;
        int i=0;
        for(int dist=sdbw; dist>=minDist; dist--) {
            int start=sd-dist;
            int end=sd;
            long startAm = amrMap.get(amrMap.floorKey(start)).am;
            double avgAm = (endAm-startAm)/dist;
            avgams[i++] = avgAm;
        }

        return avgams;
    }
    public static void listAvgAm(int sd, int sdbw, int minDist, int interval,
            TreeMap<Integer, AmRecord> amrMap, boolean bStdOut, String sAvgAmFile) {
        long endAm = amrMap.get(sd).am;
        for(int dist=sdbw; dist>=minDist; dist-=interval) {
            int start=sd-dist;
            int end=sd;
            long startAm = amrMap.get(amrMap.floorKey(start)).am;
            double avgAm = (endAm-startAm)/dist;

            String line = String.format("%-20d %8.3f\n", startAm, avgAm);
            if(bStdOut)
                System.out.format("%s", line);
            if(sAvgAmFile!=null) {
                //write line to derivativeFile
                Utils.append2File(sAvgAmFile, line);
            }
        }
    }
}
