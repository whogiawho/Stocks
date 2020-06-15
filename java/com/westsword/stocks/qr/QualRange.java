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
 
 
package com.westsword.stocks.qr;


import java.util.*;

import com.westsword.stocks.base.time.*;

public class QualRange implements Comparable<QualRange> {
    private String endTradeDate;
    private String endHMS;
    private int mSdLength;          //the length before <endTradeDate, endHMS>

    private int mMatchedQrCnt;
    private int mMatchedTdCnt;      //matched tradedate count
    private TreeSet<String> mTdSet; // 

    public QualRange(String endTradeDate, String endHMS, int sdLength, 
            int matchedQrCnt, int matchedTdCnt, TreeSet<String> tdSet) {
        this.endTradeDate = endTradeDate;
        this.endHMS = endHMS;
        mSdLength = sdLength;

        mMatchedQrCnt = matchedQrCnt;
        mMatchedTdCnt = matchedTdCnt;
        mTdSet = tdSet;
    }
    public QualRange(String endTradeDate, String endHMS) {
        this(endTradeDate, endHMS, -1, -1, -1, new TreeSet<String>());
    }
    public QualRange(QualRange qr) {
        this(qr.getEndDate(), qr.getEndHMS(), qr.getSdLength(), 
                qr.getMatchedQrCnt(), qr.getMatchedTdCnt(), qr.getTdSet());
    }


    public void setSdLength(int sdLength) {
        mSdLength = sdLength;
    }
    public int getSdLength() {
        return mSdLength;
    }
    public void setMatchedQrCnt(int cnt) {
        mMatchedQrCnt = cnt;
    }
    public int getMatchedQrCnt() {
        return mMatchedQrCnt;
    }
    public void setMatchedTdCnt(int cnt) {
        mMatchedTdCnt = cnt;
    }
    public int getMatchedTdCnt() {
        return mMatchedTdCnt;
    }
    public TreeSet<String> getTdSet() {
        return mTdSet;
    }
    public void setTdSet(TreeSet<String> tdSet) {
        mTdSet = tdSet;
        //mTdSet.addAll(tdSet);
    }
    public void print() {
        System.out.format("%s %s %4d %4d %4d %s\n", 
                endTradeDate, endHMS, mSdLength, mMatchedQrCnt, mMatchedTdCnt, getSimpleMatchedExpr());
    }
    //if more pairs with equal tradedate are matched, only one is returned
    public String getSimpleMatchedExpr() {
        String sExpr = "";

        HashSet<String> tradeDateSet = new HashSet<String>();
        for(String expr: mTdSet) {
            String endDate = expr.split(":")[0];
            if(tradeDateSet.contains(endDate))
                continue;

            tradeDateSet.add(endDate);
            sExpr += expr + " ";
        }

        return sExpr;
    }


    public int getEndSdt(SdTime1 sdTime) {
        return sdTime.getAbs(endTradeDate, endHMS);
    }
    public int getStartSdt(SdTime1 sdTime) {
        int sdt = getEndSdt(sdTime);
        return sdt - mSdLength;
    }


    public long getStartTp(SdTime1 sdTime) {
        int startSdt = getStartSdt(sdTime);
        return sdTime.rgetAbs(startSdt);
    }
    public String getStartDate(SdTime1 sdTime) {
        long startTp = getStartTp(sdTime);
        return Time.getTimeYMD(startTp, false);
    }
    public String getStartHMS(SdTime1 sdTime) {
        long startTp = getStartTp(sdTime);
        return Time.getTimeHMS(startTp, false);
    }
    public String getEndDate() {
        return endTradeDate;
    }
    public String getEndHMS() {
        return endHMS;
    }


    public int compareTo(QualRange qr) {
        return Integer.compare(mMatchedQrCnt, qr.getMatchedQrCnt());
    }
}
