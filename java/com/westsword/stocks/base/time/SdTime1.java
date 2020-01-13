package com.westsword.stocks.base.time;


import com.westsword.stocks.Settings;

public class SdTime1 extends AStockSdTime {
    private String mStockCode;

    private String mSdStartDate;
    private String mSdStartTime;
    private StockDates mStockDates;

    public SdTime1() {
        this(Settings.getStockCode(), 
                Settings.getSdStartDate(Settings.getStockCode()), 
                Settings.getSdStartTime(), 
                Settings.getSdInterval());
    }
    public SdTime1(int interval) {
        this(Settings.getStockCode(), 
                Settings.getSdStartDate(Settings.getStockCode()), 
                Settings.getSdStartTime(), 
                interval);
    }
    public SdTime1(String stockCode, int interval) {
        this(stockCode, 
                Settings.getSdStartDate(stockCode), 
                Settings.getSdStartTime(), 
                interval);
    }
    public SdTime1(String stockCode, String sdStartDate, String sdStartTime, int sdInterval) {
        super(sdInterval);

        mStockCode = stockCode;
        mSdStartDate = sdStartDate;
        mSdStartTime = sdStartTime;
        mStockDates = new StockDates(mSdStartDate, Time.currentDate(), stockCode);
    }

    //below 1 cond must be met:
    //1. timepoint's tradeDate is in mStockDates
    public void check(long timepoint) {
        String tradeDate = Time.getTimeYMD(timepoint, false);
        if(!mStockDates.contains(tradeDate)) {
            String msg = String.format("(0x%x,%s) is not valid for %s\n", 
                    timepoint, tradeDate, mStockCode);
            throw new RuntimeException(msg);
        }
    }

    //sdTime considering startDate&startTime with below conditions:
    //1. based on (mSdStartDate, mSdStartTime)
    public int getAbs(long timepoint) {
        int sdTime = 0;

        check(timepoint);

        int length = getLength();
        String tradeDate = Time.getTimeYMD(timepoint, false);
        String date = mStockDates.firstDate();
        while(date!=null && !date.equals(tradeDate)) {
            sdTime += length;

            date = mStockDates.nextDate(date);
        }
        sdTime += get(timepoint);

        return sdTime;
    }

    public int getAbs(String tradeDate, String tradeTime) {
        long tp = Time.getSpecificTime(tradeDate, tradeTime);
        return getAbs(tp);
    }
}
