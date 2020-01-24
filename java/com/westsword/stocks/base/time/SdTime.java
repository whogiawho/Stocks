package com.westsword.stocks.base.time;

import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.Settings;

public class SdTime implements ISdTime {
    private int mSdInterval;
    private ArrayList<TimeRange> mRanges;

    public SdTime() {
        this(Settings.getSdInterval());
    }
    public SdTime(int sdInterval) {
        mSdInterval = sdInterval;

        mRanges = new ArrayList<TimeRange>();
    }



    //interface implementation
    public int get(String hms, int interval) {
        String tradeDate = Time.currentDate();
        long tp = Time.getSpecificTime(tradeDate, hms);
        return get(tp, interval);
    }
    //relsdtime>=0
    public String rget(int relsdtime, int interval) {
        String hms = null;

        for(int i=0; i<mRanges.size(); i++) {
            TimeRange r = mRanges.get(i);
            int rSdEnd = get(r.getEnd());
            if(rSdEnd>=relsdtime) {
                int rSdStart = get(r.getStart());
                hms = r.rget(relsdtime-rSdStart, interval);
                break;
            }
        }
        if(hms==null) {
            TimeRange r = mRanges.get(mRanges.size()-1);
            hms = r.getEnd();
        }
        return hms;
    }



    public int get(long timepoint, int interval) {
        int sdTime = 0;
        for(int i=0; i<mRanges.size(); i++) {
            TimeRange r = mRanges.get(i);
            int sdTimeP = r.get(timepoint, interval);
            /*
            System.out.format("%s: i=%d sdTimeP=%d sdTime=%d\n", 
                    Utils.getCallerName(getClass()), i, sdTimeP, sdTime);
            */
            if(sdTimeP == -2) {
                break;
            } else if(sdTimeP == -1) {
                //add r's relative Length, and continue
                sdTime += r.get(r.getEnd(timepoint), interval) + 1;
            } else {
                sdTime = sdTime+sdTimeP;
                break;
            }
        }

        return sdTime;
    }
    public int get(long timepoint) {
        return get(timepoint, mSdInterval);
    }
    //sdTime w/o considering startDate&startTime
    public int get(String hms) {
        return get(Time.getSpecificTime(Time.currentDate(), hms));
    }



    //[firstStartHMS, lastEndHMS]
    public String rget(int relsdtime) {
        return rget(relsdtime, mSdInterval);
    }



    public void addRange(String startHMS, String endHMS) {
        mRanges.add(new TimeRange(startHMS, endHMS));
    }
    public String getStartHMS() {
        String startHMS = null;

        TimeRange r = null;
        int s = mRanges.size();
        if(s != 0)
            r = mRanges.get(0);

        if(r != null) {
            startHMS = r.getStart();
        }

        return startHMS;
    }
    public int getLength() {
        int length = 0;

        TimeRange r = null;
        int s = mRanges.size();
        if(s != 0)
            r = mRanges.get(s-1);

        if(r != null) {
            length = get(r.getEnd()) + 1;
        }

        return length;
    }
    public int getInterval() {
        return mSdInterval;
    }
}
