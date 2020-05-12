package com.westsword.stocks.qr;


import java.util.*;

import com.westsword.stocks.base.time.*;

public class QualRange{
    private String endTradeDate;
    private String endHMS;

    private int mSdLength;          //the length before <endTradeDate, endHMS>

    public QualRange(String endTradeDate, String endHMS, int sdLength) {
        this.endTradeDate = endTradeDate;
        this.endHMS = endHMS;
        mSdLength = sdLength;
    }


    public void setSdLength(int sdLength) {
        mSdLength = sdLength;
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
}
