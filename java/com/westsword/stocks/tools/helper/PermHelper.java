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

import com.westsword.stocks.base.Utils;

public class PermHelper {
    public final static int permDim = 10;

    public static void getPermIdx(String args[]) {
        if(args.length != 2) {
            usage();
            return;
        }
        long permIdx = Long.valueOf(args[1]);

        String sCoord = _getPermIdx(permIdx, permDim); 
        System.out.format("%s\n", sCoord);
    }


    public static String getPermIdx(long permIdx) {
        return _getPermIdx(permIdx, permDim);
    }
    public static String _getPermIdx(long permIdx, int permDim) {
        String sCoord = "";
        long[] factorials = Utils.getFactorials(permDim);
        for(int i=0; i<permDim; i++) {
            int n = permDim-1-i;
            int coord = (int)(permIdx/factorials[n]);
            permIdx = (long)(permIdx % factorials[n]);
            sCoord += coord + ",";
        }

        return sCoord;
    }


    private static void usage() {
        System.err.println("usage: java AnalyzeTools getpermcoord permIdx");
        System.exit(-1);
    }
}
