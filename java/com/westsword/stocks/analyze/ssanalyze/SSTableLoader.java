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
 
 
package com.westsword.stocks.analyze.ssanalyze;


import java.util.*;

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.utils.FileLoader;

public class SSTableLoader extends FileLoader {
    private ArrayList<SSTableRecord> mList = null;
    private String mName = null;
    private String mStockCode = null;

    public boolean onLineRead(String line, int count) {
        if(line.matches("^ *#.*")||line.matches("^ *$"))
            return true;

        String[] fields = line.split(" +");
        int tradeCount = Integer.valueOf(fields[0]);
        String stockCode = fields[1];
        String startDate = fields[2];
        double threshold = Double.valueOf(fields[3]);
        int sTDistance = Integer.valueOf(fields[4]);
        int tradeType = Integer.valueOf(fields[5]);
        int maxCycle = Integer.valueOf(fields[6]);
        double targetRate = Double.valueOf(fields[7]);
        String sMatchExp = fields[8];
        SSTableRecord r = null;
        if(mStockCode.equals(stockCode)) {
            r = new SSTableRecord(tradeCount, 
                    stockCode, startDate, threshold, sTDistance, tradeType,
                    maxCycle, targetRate, sMatchExp, mName);
        }

        if(r!=null && mList != null)
            mList.add(r);

        return true;
    }

    public void load(ArrayList<SSTableRecord> rList, String sFile, String sName) {
        load(rList, sFile, sName, Settings.getStockCode());
    }
    public void load(ArrayList<SSTableRecord> rList, String sFile, String sName, String stockCode) {
        mList = rList;
        mName = sName;
        mStockCode = stockCode;

        load(sFile);
    }
}
