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
 
 
package com.westsword.stocks.base.ckpt;


import java.util.*;

import com.westsword.stocks.base.time.*;

public class AACheckPoint extends CheckPoint {

    //always starts from 09:25:00
    //interval - in seconds
    public AACheckPoint(int interval) {
        super();

        AStockSdTime sdt = new AStockSdTime();
        
        for(int i=0; i<=sdt.getLength(); i+=interval) {
            String hms = sdt.rget(i);
            add(hms);
        }
    }

    public String[] next(StockDates stockDates, String tradeDate, String hms) {
        String[] sRet = new String[2];

        if(contains(hms)) {
            if(hms.equals(last())) {
                sRet[0] = stockDates.nextDate(tradeDate);
                sRet[1] = first();
            } else {
                sRet[0] = tradeDate;
                sRet[1] = next(hms);
            }
        }

        return sRet;
    }

    public String[] prev(StockDates stockDates, String tradeDate, String hms) {
        String[] sRet = new String[2];

        if(contains(hms)) {
            if(hms.equals(first())) {
                sRet[0] = stockDates.prevDate(tradeDate);
                sRet[1] = last();
            } else {
                sRet[0] = tradeDate;
                sRet[1] = prev(hms);
            }
        }

        return sRet;
    }

}
