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


package com.westsword.stocks.analyze.sam;

import java.util.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;

public class SAmManager extends TaskManager {
    private ArrayList<Thread> tList = new ArrayList<Thread>();

    public void run(String stockCode, String sDstTradeDate, 
            TradeDates tradeDates, SAmOption option) {
        maxThreadsCheck();

        Thread t = new SAmTask(this, stockCode, sDstTradeDate, 
                tradeDates, option);
        addThread(t);

        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    public void addThread(Thread t) {
        tList.add(t);
    }
    public void join() {
        try {
            //System.out.format("%s: size=%d\n", 
            //        Utils.getCallerName(getClass()), tList.size());
            for(int i=0; i<tList.size(); i++) {
                Thread t = tList.get(i);
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

