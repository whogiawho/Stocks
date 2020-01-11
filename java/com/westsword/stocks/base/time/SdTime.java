package com.westsword.stocks.base.time;

import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.Settings;

public class SdTime {
    private String mSdStartDate;
    private String mSdStartTime;
    private long mSdStartUTC;

    private int mSdInterval;
    private ArrayList<TimeRange> mRanges;

    public SdTime() {
        this(Settings.getSdStartDate(), Settings.getSdStartTime(), Settings.getSdInterval());
    }
    public SdTime(int interval) {
        this(Settings.getSdStartDate(), Settings.getSdStartTime(), interval);
    }
    public SdTime(String sdStartDate, String sdStartTime, int sdInterval) {
        mSdStartDate = sdStartDate;
        mSdStartTime = sdStartTime;
        mSdStartUTC = Time.getSpecificTime(sdStartDate, sdStartTime);

        mSdInterval = sdInterval;

        mRanges = new ArrayList<TimeRange>();
    }

    public void addRange(String startHMS, String endHMS) {
        mRanges.add(new TimeRange(startHMS, endHMS));
    }

    public int get(String hms) {
        return get(Time.getSpecificTime(Time.currentDate(), hms));
    }
    public int get(long timepoint) {
        int sdTime = 0;
        for(int i=0; i<mRanges.size(); i++) {
            TimeRange r = mRanges.get(i);
            int sdTimeP = r.getSdTime(timepoint, mSdInterval);
            /*
            System.out.format("%s: i=%d sdTimeP=%d sdTime=%d\n", 
                    Utils.getCallerName(getClass()), i, sdTimeP, sdTime);
            */
            if(sdTimeP == -2) {
                break;
            } else if(sdTimeP == -1) {
                //add sdTimeP to sdTime, and continue
                sdTime += r.getSdTime(r.getEnd(timepoint), mSdInterval) + 1;
            } else {
                sdTime = sdTime+sdTimeP;
                break;
            }
        }

        return sdTime;
    }

    public int getRelative(long timepoint) {
        int sdTime = 0;

        return sdTime;
    }
}
