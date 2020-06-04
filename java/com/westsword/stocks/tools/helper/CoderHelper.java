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

import com.westsword.stocks.base.ConvertDD2Double;

public class CoderHelper {
    public static void decode(String args[]) {
        if(args.length != 2) {
            usage();
            return;
        }

        ConvertDD2Double cDD = new ConvertDD2Double();
        double price = cDD.sub_48A0D0(args[1]);
        System.out.format("%s", price);
    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools decode hexPrice");
        System.exit(-1);
    }
}
