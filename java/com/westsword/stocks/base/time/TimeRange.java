package com.westsword.stocks.base.time;

import com.westsword.stocks.base.time.Time;

public class TimeRange implements ISdTime {
    private String mStart;
    private String mEnd;

    public TimeRange(String sStart, String sEnd) {
        mStart = sStart;
        mEnd = sEnd;
    }

    public String getStart() {
        return mStart;
    }
    public String getEnd() {
        return mEnd;
    }

    public long getStart(long timepoint) {
        return Time.getSpecificTime(timepoint, mStart);
    }
    public long getEnd(long timepoint) {
        return Time.getSpecificTime(timepoint, mEnd);
    }


    //interface implementation
    public int get(String hms, int interval) {
        String tradeDate = Time.currentDate();
        long tp = Time.getSpecificTime(tradeDate, hms);
        return get(tp, interval);
    }
    public String rget(int relsdtime, int interval) {
        String tradeDate = Time.currentDate();
        long startTp = Time.getSpecificTime(tradeDate, mStart);
        return Time.getTimeHMS(startTp+relsdtime*interval);
    }




    //3 scenarios:
    //  timepoint<start          -2
    //  start<=timepoint<=end    0,(end-start)/interval
    //  timepoint>end            -1
    public int get(long timepoint, int interval) {
        int sdTime = 0;

        long start = getStart(timepoint);
        long end = getEnd(timepoint);

        if(start<=timepoint && timepoint<=end)
            sdTime=(int)(timepoint-start);
        else if(timepoint>end) {
            int dist = (int)(end-start)/interval+1;
            if(timepoint<start+dist*interval)
                sdTime=(int)(timepoint-start);
            else
                sdTime=-interval;
        } else if(timepoint<start)
            sdTime=-interval*2;

        return sdTime/interval;
    }
}
