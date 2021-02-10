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


package com.westsword.stocks.analyze.sam0;

import java.util.*;

import com.westsword.stocks.base.*;

public class ReGroup {
    private int mReTradeType;
    private ArrayList<String> mReTradeDates;

    public long mExtremeReAm;
    public String mExtremeTradeDate;
    public String mStartHMS;
    public String mExtremeHMS;

    public String toString() {
        String sReGroup = ""; 

        if(mExtremeTradeDate!=null&&mStartHMS!=null&&mExtremeHMS!=null) 
            sReGroup = String.format("ReGroup.toString %s %s %s: %d", 
                mExtremeTradeDate, mStartHMS, mExtremeHMS, mExtremeReAm);

        return sReGroup;
    }
    public ReGroup(int tradeType, ArrayList<String> reTradeDates) {
        mReTradeType = tradeType;
        mReTradeDates = reTradeDates;

        mExtremeReAm = Long.MAX_VALUE;
        if(tradeType == Stock.TRADE_TYPE_SHORT)
            mExtremeReAm = Long.MIN_VALUE;
        mExtremeTradeDate = null;
        mStartHMS = null;
        mExtremeHMS = null;
    }
    public int getTradeType() {
        return mReTradeType;
    }
    public int indexOf(String tradeDate) {
        return mReTradeDates.indexOf(tradeDate);
    }
    public void setExtreme(long amSum, String tradeDate, String startHMS, String extremeHMS) {
        if(mReTradeType == Stock.TRADE_TYPE_SHORT && amSum>mExtremeReAm) {
            mExtremeReAm = amSum;
            mExtremeTradeDate = tradeDate;
            mStartHMS = startHMS;
            mExtremeHMS = extremeHMS;
        } else if(mReTradeType == Stock.TRADE_TYPE_LONG && amSum<mExtremeReAm) {
            mExtremeReAm = amSum;
            mExtremeTradeDate = tradeDate;
            mStartHMS = startHMS;
            mExtremeHMS = extremeHMS;
        }
    }
}

