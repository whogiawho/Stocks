package com.westsword.stocks.qr;


import java.util.*;

import com.westsword.stocks.base.time.*;

public class QualRange implements Comparable<QualRange> {
    private String endTradeDate;
    private String endHMS;
    private int mSdLength;          //the length before <endTradeDate, endHMS>

    private int mMatchedCnt;

    public QualRange(String endTradeDate, String endHMS, int sdLength, int matchedCnt) {
        this.endTradeDate = endTradeDate;
        this.endHMS = endHMS;
        mSdLength = sdLength;

        mMatchedCnt = matchedCnt;
    }
    public QualRange(String endTradeDate, String endHMS) {
        this(endTradeDate, endHMS, -1, -1);
    }
    public QualRange(QualRange qr) {
        this(qr.getEndDate(), qr.getEndHMS(), qr.getSdLength(), qr.getMatchedCnt());
    }


    public void setSdLength(int sdLength) {
        mSdLength = sdLength;
    }
    public int getSdLength() {
        return mSdLength;
    }
    public void setMatchedCnt(int cnt) {
        mMatchedCnt = cnt;
    }
    public int getMatchedCnt() {
        return mMatchedCnt;
    }
    public void print() {
        System.out.format("%s %s %4d %4d\n", endTradeDate, endHMS, mSdLength, mMatchedCnt);
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
        return Integer.compare(mMatchedCnt, qr.getMatchedCnt());
    }
}
