package com.westsword.stocks.am;

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.utils.Trade;

public class AmRecord implements Comparable<AmRecord> {
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

    public double getInPrice(int tradeType) {
        double inPrice = downPrice;

        if(tradeType == Stock.TRADE_TYPE_LONG) {
            inPrice = upPrice;
        }

        return inPrice;
    }
    public double getOutPrice(int tradeType) {
        double outPrice = upPrice;

        if(tradeType == Stock.TRADE_TYPE_LONG) {
            outPrice = downPrice;
        }

        return outPrice;
    }
    public double getProfit(int tradeType, double inPrice) {
        double profit, outPrice;

        if(tradeType == Stock.TRADE_TYPE_LONG) {
            outPrice = downPrice;
            profit = Trade.getNetProfit(inPrice, outPrice);
        } else {
            outPrice = upPrice;
            profit = Trade.getNetProfit(outPrice, inPrice);
        }

        return profit;
    }

    public int compareTo(AmRecord r) {
        return Long.compare(hexTimePoint, r.hexTimePoint);
    }
}
