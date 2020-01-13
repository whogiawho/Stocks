package com.westsword.stocks.base.time;

import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.Settings;

public class SdTime {
    private int mSdInterval;
    private ArrayList<TimeRange> mRanges;

    public SdTime() {
        this(Settings.getSdInterval());
    }
    public SdTime(int sdInterval) {
        mSdInterval = sdInterval;

        mRanges = new ArrayList<TimeRange>();
    }

    public void addRange(String startHMS, String endHMS) {
        mRanges.add(new TimeRange(startHMS, endHMS));
    }

    //sdTime w/o considering startDate&startTime
    public int get(String hms) {
        return get(Time.getSpecificTime(Time.currentDate(), hms));
    }
    public int get(long timepoint) {
        int sdTime = 0;
        for(int i=0; i<mRanges.size(); i++) {
            TimeRange r = mRanges.get(i);
            int sdTimeP = r.getRelative(timepoint, mSdInterval);
            /*
            System.out.format("%s: i=%d sdTimeP=%d sdTime=%d\n", 
                    Utils.getCallerName(getClass()), i, sdTimeP, sdTime);
            */
            if(sdTimeP == -2) {
                break;
            } else if(sdTimeP == -1) {
                //add r's relative Length, and continue
                sdTime += r.getRelative(r.getEnd(timepoint), mSdInterval) + 1;
            } else {
                sdTime = sdTime+sdTimeP;
                break;
            }
        }

        return sdTime;
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
}
