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


    public final static String CALL_AUCTION_END_TIME = "09:25:00";
    public static String getCallAuctionEndTime() {
        return CALL_AUCTION_END_TIME; 
    }
    public final static String OPEN_QUOTATION_TIME = "09:30:00";
    public static String getOpenQuotationTime() {
        return OPEN_QUOTATION_TIME;
    }
    public final static String MID_SUSPEND_TIME = "11:30:00";
    public static String getMidSuspensionTime() {
        return MID_SUSPEND_TIME;
    }
    public final static String MID_OPEN_QUOTATION_TIME = "13:00:00";
    public static String getMidOpenQuotationTime() {
        return MID_OPEN_QUOTATION_TIME;
    }
    public final static String CLOSE_QUOTATION_TIME = "15:00:00";
    public static String getCloseQuotationTime() {
        return CLOSE_QUOTATION_TIME;
    }
    public final static String LAST_RAWTRADEDETAIL_TIME= "15:01:00";
    public static String getLastRawTradeDetailTime() {
        return LAST_RAWTRADEDETAIL_TIME;
    }
    public final static String RRP_START_TIME = "14:56:00";
    public static String getRrpStartTime() {
        return RRP_START_TIME;
    }
    public final static String RRP_END_TIME = "15:30:00";
    public static String getRrpEndTime() {
        return RRP_END_TIME;
    }


    public static long getCloseQuotationTime(String tradeDate) {
        return Time.getSpecificTime(tradeDate, getCloseQuotationTime());
    }


    public static long getRrpStartTime(long timepoint) {
        return Time.getSpecificTime(timepoint, getRrpStartTime());
    }
    public static long getRrpEndTime(long timepoint) {
        return Time.getSpecificTime(timepoint, getRrpEndTime());
    }
    public static long getCloseQuotationTime(long timepoint) {
        return Time.getSpecificTime(timepoint, getCloseQuotationTime());
    }
    public static long getCallAuctionEndTime(long timepoint) {
        return Time.getSpecificTime(timepoint, getCallAuctionEndTime());
    }
    public static long getLastRawTradeDetailTime(long timepoint) {
        return Time.getSpecificTime(timepoint, getLastRawTradeDetailTime());
    }
}
