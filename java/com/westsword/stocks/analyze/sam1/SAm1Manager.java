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


package com.westsword.stocks.analyze.sam1;

import java.util.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;

public class SAm1Manager extends SAmManager {
    public void run(String stockCode, String sDstTradeDate, 
            TradeDates tradeDates, SAmOption option, 
            ArrayList<Double> modelListL, ArrayList<Double> modelListR) {
        maxThreadsCheck();

        Thread t = new SAm1Task(this, stockCode, sDstTradeDate, 
                tradeDates, option, 
                modelListL, modelListR);

        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }
}

