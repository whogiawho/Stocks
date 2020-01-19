package com.westsword.stocks.am;


public class AmRecord {
    public long hexTimePoint;
    public int  timeIndex;
    public long am;
    public double upPrice;
    public double downPrice;

    public AmRecord(long hexTimePoint, int timeIndex, long am, double upPrice, double downPrice) {
        this.hexTimePoint = hexTimePoint;
        this.timeIndex = timeIndex;
        this.am = am;
        this.upPrice = upPrice;
        this.downPrice = downPrice;
    }

}
