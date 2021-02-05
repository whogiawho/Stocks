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
 
 
package com.westsword.stocks.base;

import java.util.*;

import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class PVTable {
    private TreeMap<Double, Long> mPVMap;

    public PVTable() {
        mPVMap = new TreeMap<Double, Long>();
    }
    public PVTable(String stockCode, String tradeDate) {
        this();
        mPVMap = PVTUtils.load(stockCode, tradeDate);
    }
    //make sure full.txt of daysBackward already exists
    public PVTable(String stockCode, String tradeDate, String endHMS, int daysBackward) {
        this();

        //load tradeDate,endHMS firstly
        mPVMap = PVTUtils.load(stockCode, tradeDate, endHMS);

        //merge mPVMap with those of daysBackward
        TradeDates tradeDates = new TradeDates(stockCode);
        for(int i=1; i<=daysBackward; i++) {
            String pDate = tradeDates.prevDate(tradeDate, i, true);
            if(pDate!=null) {
                PVTable pvTable = new PVTable(stockCode, pDate);
                merge(pvTable);
            }
        }
    }
    public String toString() {
        return PVTUtils.toString(mPVMap);
    }


    public TreeMap<Double, Long> getPVMap() {
        return mPVMap;
    }
    public void merge(PVTable pvTable) {
        TreeMap<Double, Long> pvMap = pvTable.getPVMap();

        for(Double price: pvMap.keySet()) {
            Long cnt = pvMap.get(price);

            Long prevCnt = mPVMap.get(price);
            if(prevCnt==null)
                prevCnt=(long)0;

            mPVMap.put(price, prevCnt+cnt); 
        }
    }


    public double getTopPercent(int tradeType, double price) {
        double topPercent = Double.NaN;

        double sum = 0;
        double sumWin = 0;
        for(Double p: mPVMap.keySet()) {
            long cnt = mPVMap.get(p);
            sum += cnt;
            if(tradeType==Stock.TRADE_TYPE_LONG) {
                if(p>price) 
                    sumWin += cnt;
            } else {
                if(p<price)
                    sumWin += cnt;
            }
        }
        topPercent = sumWin/sum;

        return topPercent;
    }
}
