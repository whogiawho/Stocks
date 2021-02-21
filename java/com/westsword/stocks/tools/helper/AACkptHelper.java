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
import com.westsword.stocks.base.ckpt.*;

public class AACkptHelper {
    public static void next(String args[]) {
        if(args.length !=4) {
            usageNext();
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        String hms = args[3];
        StockDates stockDates = new StockDates(stockCode);

        AACheckPoint aackpt = new AACheckPoint(60);
        String[] sRet = aackpt.next(stockDates, tradeDate, hms);
        System.out.format("%s %s\n", sRet[0], sRet[1]);
    }
    private static void usageNext() {
        System.err.println("usage: java AnalyzeTools nextaackpt stockCode tradeDate hms");
        System.exit(-1);
    }
}
