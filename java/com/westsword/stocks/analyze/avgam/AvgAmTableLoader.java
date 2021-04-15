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
 
 
package com.westsword.stocks.analyze.avgam;


import java.util.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.utils.FileLoader;

public class AvgAmTableLoader extends FileLoader {
    private ArrayList<AvgAmTableRecord> mList = null;
    private String mName = null;
    private String mStockCode = null;

    public boolean onLineRead(String line, int count) {
        if(line.matches("^ *#.*")||line.matches("^ *$"))
            return true;

        String[] fields = line.split(" +");
        String stockCode = fields[0];
        String tradeDate = fields[1];
        String hms = fields[2];
        String eHMS0 = fields[3];
        String eHMS1 = fields[4];
        //checkNULLString(stockCode, tradeDate, hms, eHMS0, eHMS1);
        int tradeType = Integer.valueOf(fields[5]);
        int tradeCount = Integer.valueOf(fields[6]);
        double targetRate = Double.valueOf(fields[7]);
        String sMaxCycle = fields[8];
        double dcThres = Double.valueOf(fields[9]);
        double scThres = Double.valueOf(fields[10]);

        AvgAmTableRecord r = null;
        if(mStockCode.equals(stockCode)) {
            r = new AvgAmTableRecord(stockCode, tradeDate, hms, 
                    eHMS0, eHMS1, tradeType, tradeCount, targetRate, sMaxCycle, 
                    dcThres, scThres, mName);
        }

        if(r!=null && mList != null)
            mList.add(r);

        return true;
    }

    private void checkNULLString(String stockCode, String tradeDate, String hms, 
            String eHMS0, String eHMS1) {
        if(eHMS0.equals(AvgAmTableRecord.NULL_STRING)||
                eHMS1.equals(AvgAmTableRecord.NULL_STRING))
            System.out.format("%s %s %s %s %s: null string!\n", 
                    stockCode, tradeDate, hms, eHMS0, eHMS1);
    }
    public void load(ArrayList<AvgAmTableRecord> rList, String sFile, String sName) {
        load(rList, sFile, sName, Settings.getStockCode());
    }
    public void load(ArrayList<AvgAmTableRecord> rList, String sFile, String sName, String stockCode) {
        mList = rList;
        mName = sName;
        mStockCode = stockCode;

        load(sFile);
    }
}
