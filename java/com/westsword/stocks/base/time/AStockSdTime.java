package com.westsword.stocks.base.time;


import com.westsword.stocks.base.Settings;

public class AStockSdTime extends SdTime {
    public AStockSdTime() {
        this(Settings.getSdInterval());
    }
    public AStockSdTime(int interval) {
        super(interval);

        addRanges();
    }

    private void addRanges() {
        addRange(getCallAuctionEndTime0(), getCallAuctionEndTime1());
        addRange("09:30:00", "11:30:00");
        addRange("13:00:00", "15:00:00");
    }

    public static String getCallAuctionEndTime0() {
        return Settings.getSdStartTime();           //09:25:00
    }
    public static String getCallAuctionEndTime1() {
        return "09:25:02";
    }

    public static String getCallAuctionEndTime() {
        return "09:25:00"; 
    }
    public static String getOpenQuotationTime() {
        return "09:30:00";
    }
    public static String getMidSuspensionTime() {
        return "11:30:00";
    }
    public static String getMidOpenQuotationTime() {
        return "13:00:00";
    }
    public static String getCloseQuotationTime() {
        return "15:00:00";
    }
}
