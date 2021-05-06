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

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.utils.FileLoader;

public class AmRecordLoader extends FileLoader {
    private ArrayList<AmRecord> mAmRecordList = null;
    private TreeMap<Integer, AmRecord> mAmMap = null;
    private AmrHashtable mAmrTable = null;


    public boolean onLineRead(String line, int counter) {
        String[] fields=line.split(" +");
        //[0] time; [1] sdTime; [2] am; [3] upPrice; [4] downPrice;
        long time = Long.parseLong(fields[0], 16);
        int sdTime = Integer.valueOf(fields[1]);
        long am = Long.valueOf(fields[2]);
        double upPrice = Double.valueOf(fields[3]);
        double downPrice = Double.valueOf(fields[4]);
        //2nd added
        long abVol = Long.valueOf(fields[5]);
        long asVol = Long.valueOf(fields[6]);
        double abAmount = Double.valueOf(fields[7]);
        double asAmount = Double.valueOf(fields[8]);
                
        AmRecord r = new AmRecord(time, sdTime, am, upPrice, downPrice, 
                abVol, asVol, abAmount, asAmount);

        if(mAmRecordList != null)
            mAmRecordList.add(r);
        if(mAmMap != null)
            mAmMap.put(sdTime, r);
        if(mAmrTable != null) {
            //put r to mAmrTable's two table per its upPrice&downPrice
            mAmrTable.put(r, Stock.TRADE_TYPE_LONG);
            mAmrTable.put(r, Stock.TRADE_TYPE_SHORT);
        }

        return true;
    }

    public void load(ArrayList<AmRecord> amRecordList, TreeMap<Integer, AmRecord> amMap, 
            AmrHashtable hTable, String sAmRecordFile) {
        mAmRecordList = amRecordList;
        mAmMap = amMap;
        mAmrTable = hTable;

        load(sAmRecordFile);
    }
}
