package com.westsword.stocks.base.time;


import com.westsword.stocks.base.Settings;

public class SdTime1 extends AStockSdTime implements ISdTimeAbs {
    private String mStockCode;

    private String mSdStartDate;
    private String mSdStartTime;
    private StockDates mStockDates;

    public SdTime1() {
        this(Settings.getStockCode()); 
    }
    public SdTime1(String stockCode) {
        this(stockCode, Settings.getSdInterval());
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


    //interface implementation
    public int getAbs(long timepoint, int interval) {
        int sdTime = 0;

        check(timepoint);

        int length = getLength();
        String tradeDate = Time.getTimeYMD(timepoint, false);
        String date = mStockDates.firstDate();
        while(date!=null && !date.equals(tradeDate)) {
            sdTime += length;

            date = mStockDates.nextDate(date);
        }
        sdTime += get(timepoint, interval);

        return sdTime;
    }
    public long rgetAbs(int abssdtime, int interval) {
        long tp = 0;

        int length = getLength();
        int datesDist = abssdtime/length;
        int relsdtime = abssdtime%length;

        String tradeDate = mStockDates.nextDate(mSdStartDate, datesDist);

        String hms = rget(relsdtime, interval);

        return Time.getSpecificTime(tradeDate, hms);
    }



    //sdTime considering startDate&startTime with below conditions:
    //1. based on (mSdStartDate, mSdStartTime)
    public int getAbs(long timepoint) {
        return getAbs(timepoint, getInterval());
    }
    public int getAbs(String tradeDate, String tradeTime) {
        long tp = Time.getSpecificTime(tradeDate, tradeTime);
        return getAbs(tp);
    }

    public long rgetAbs(int abssdtime) {
        return rgetAbs(abssdtime, getInterval());
    }




    //below 1 cond must be met:
    //1. timepoint's tradeDate is in mStockDates
    private void check(long timepoint) {
        String tradeDate = Time.getTimeYMD(timepoint, false);
        if(!mStockDates.contains(tradeDate)) {
            String msg = String.format("(0x%x,%s) is not valid for %s\n", 
                    timepoint, tradeDate, mStockCode);
            throw new RuntimeException(msg);
        }
    }
}
