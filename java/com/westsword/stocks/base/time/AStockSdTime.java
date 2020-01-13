package com.westsword.stocks.base.time;


import com.westsword.stocks.Settings;

public class AStockSdTime extends SdTime {
    public AStockSdTime() {
        this(Settings.getSdInterval());
    }
    public AStockSdTime(int interval) {
        super(interval);

        addRanges();
    }

    private void addRanges() {
        String sdStartTime = Settings.getSdStartTime();   //09:25:00

        addRange(sdStartTime, sdStartTime);
        addRange("09:30:00", "11:30:00");
        addRange("13:00:00", "15:00:00");
    }
}
