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
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class AvgAmAnalyze {
    private String mStockCode;

    private final String mName;
    private SdTime1 mSdTime;

    private int mSdbw;
    private int mMinDist;
    private int mInterval;
    private PearsonsCorrelation mPC;

    private final ArrayList<AvgAmTableRecord> mAvgAmTableRecordList;

    public AvgAmAnalyze(String stockCode, String sName, SdTime1 sdTime) {
        mStockCode = stockCode;

        mName = sName;
        mSdTime = sdTime;

        mSdbw = Settings.getAvgAmBackwardSd();
        mMinDist = Settings.getAvgAmMinimumSkipSd();
        mInterval = Settings.getAvgAmInterval();

        mPC = new PearsonsCorrelation();

        //set mAvgAmTableRecordList
        mAvgAmTableRecordList = new ArrayList<AvgAmTableRecord>();
        AvgAmTable.load(stockCode, mAvgAmTableRecordList, sName);
        //load avgams
        for(int i=0; i<mAvgAmTableRecordList.size(); i++)
            mAvgAmTableRecordList.get(i).load(mSdTime, mSdbw, mMinDist, mInterval); 
    }

    //[start, end)
    public void analyze(int start, int end, long closeTP, TreeMap<Integer, AmRecord> amrMap) {
        double[] prevAvgam = AvgAmUtils.getAvgAm(start-1, mSdbw, mMinDist, mInterval, amrMap);

        for(int i=start; i<end; i++) {
            long tp = mSdTime.rgetAbs(i);
            if(tp<=closeTP) {
                String tradeDate = Time.getTimeYMD(tp, false);
                String hms = Time.getTimeHMS(tp, false);
                AmRecord ar = amrMap.get(amrMap.floorKey(i));
                double[] avgam = AvgAmUtils.getAvgAm(i, mSdbw, mMinDist, mInterval, amrMap);
                double deltaCorrel = mPC.correlation(prevAvgam, avgam);
                String line = getString(mStockCode, tradeDate, hms, 
                        ar, deltaCorrel, Utils.getCallerName(getClass()));
                System.out.format("%s\n", line);

                for(int j=0; j<mAvgAmTableRecordList.size(); j++) {
                    AvgAmTableRecord aatr = mAvgAmTableRecordList.get(j);
                    boolean bEval = aatr.eval(hms, deltaCorrel, avgam);
                    if(bEval) {
                        line = aatr.toString(ar);
                        System.out.format("%s\n", line);
                    }
                }

                prevAvgam = avgam;
            }
        }
    }

    public String getString(String stockCode, String tradeDate, String hms, 
            AmRecord ar, double deltaCorrel, String sPrefix) {
        String sFormat = "%s: %s %s %s %8.3f %8.3f %8.3f";
        String line = String.format(sFormat,
                sPrefix, stockCode, tradeDate, hms, deltaCorrel, ar.upPrice, ar.downPrice);

        return line;
    }
}
