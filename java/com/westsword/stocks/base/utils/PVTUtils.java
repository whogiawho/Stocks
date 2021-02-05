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


package com.westsword.stocks.base.utils;
 
import java.util.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.analyze.RawTradeDetails;

public class PVTUtils {
    public static String write(String stockCode, String tradeDate) {
        TreeMap<Double, Long> pvTable = make(stockCode, tradeDate);

        //write pvTable to file
        Utils.mkDir(StockPaths.getPVTableDir(stockCode, tradeDate));
        String sPVTableFile = StockPaths.getPVTableFile(stockCode, tradeDate);
        Utils.deleteFile(sPVTableFile);
        String line = toString(pvTable);
        Utils.append2File(sPVTableFile, line);

        return line;
    }
    //make pvMap from rawTradeDetails
    public static TreeMap<Double, Long> make(String stockCode, String tradeDate, String endHMS) {
        //load rawTradeDetails
        ArrayList<RawTradeDetails> rtdList = RawTradeDetails.load(stockCode, tradeDate);

        long endTp = Time.getSpecificTime(tradeDate, endHMS);
        return pvtFromRTDList(rtdList, endTp);
    }
    public static TreeMap<Double, Long> make(String stockCode, String tradeDate) {
        //load rawTradeDetails
        ArrayList<RawTradeDetails> rtdList = RawTradeDetails.load(stockCode, tradeDate);

        long endTp = AStockSdTime.getCloseQuotationTime(tradeDate);
        return pvtFromRTDList(rtdList, endTp);
    }


    //assuming pvtable of <stockCode, tradeDate> is already made
    public static TreeMap<Double, Long> load(String stockCode, String tradeDate) {
        TreeMap<Double, Long> pvTable = new TreeMap<Double, Long>();

        String sPVTableFile = StockPaths.getPVTableFile(stockCode, tradeDate);
        PVTableLoader l = new PVTableLoader();
        l.load(pvTable, sPVTableFile);

        return pvTable;
    }
    //write pvtable of <stockCode, tradeDate, endHMS> dynamically
    public static TreeMap<Double, Long> load(String stockCode, String tradeDate, String endHMS) {
        TreeMap<Double, Long> pvTable = new TreeMap<Double, Long>();

        String sPVTableFile = StockPaths.getPVTableFile(stockCode, tradeDate, endHMS);
        //if the file of endHMS exists, load it
        if(Utils.isFile(sPVTableFile)) {
            PVTableLoader l = new PVTableLoader();
            l.load(pvTable, sPVTableFile);
        } else {    //make and write it
            pvTable = make(stockCode, tradeDate, endHMS);
            String line = toString(pvTable);
            Utils.append2File(sPVTableFile, line);
        }
        
        return pvTable;
    }



    public static String toString(TreeMap<Double, Long> pvTable) {
        String line = "";
        for(Double price: pvTable.keySet()) {
            Long cnt = pvTable.get(price);
            line += String.format("%-8.3f %8d\n", price, cnt);
        }

        return line;
    }
    private static TreeMap<Double, Long> pvtFromRTDList(ArrayList<RawTradeDetails> rtdList, long endTp) {
        //make pvTable from rawTradeDetails
        TreeMap<Double, Long> pvTable = new TreeMap<Double, Long>();
        for(int i=0; i<rtdList.size(); i++) {
            RawTradeDetails r = rtdList.get(i);
            if(r.time>endTp)
                break;

            Long prevCnt = pvTable.get(r.price);
            if(prevCnt==null) {
                prevCnt = (long)0;
            }
            pvTable.put(r.price, prevCnt + r.count);
        }

        return pvTable;
    }
}

