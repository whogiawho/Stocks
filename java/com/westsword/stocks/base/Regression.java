package com.westsword.stocks.base;


import java.util.*;

import com.westsword.stocks.Stock;
import com.westsword.stocks.Utils;
import com.westsword.stocks.am.AmRecord;

public class Regression {

    //Return value
    // null if targetRate is not achieved
    // an AmRecord item if targetRate is achieved
    //out[0] - maxDeltaPriceBias, risk before achieving targetRate or if not achieving targetRate
    //       - used for those OK tradeDates 
    //out[1] - maxPosPrice, most optimistic
    //out[2] - riskDelta, risk if not achieving targetRate
    //       - used for those Fail tradeDates
    public static AmRecord getTradeResult(NavigableMap<Integer, AmRecord> itemMap,
            int tradeType, double inPrice, double targetRate, double[] out) {
        AmRecord rItem = null;
        double maxDeltaPriceBias = 0.0;

        double targetProfit = Utils.getTargetProfit(targetRate, inPrice);
        if(tradeType == Stock.TRADE_TYPE_LONG) {
            for(Integer key: itemMap.keySet()) {
                AmRecord item = itemMap.get(key);
                double outPrice = item.downPrice;
                double currentDelta = outPrice - inPrice;

                if(currentDelta<=0 && Math.abs(currentDelta) > maxDeltaPriceBias) {
                    maxDeltaPriceBias = Math.abs(currentDelta);
                }
                if(currentDelta >= targetProfit) {
                    rItem = item;
                    break;
                }
            }
        } else {
            for(Integer key: itemMap.keySet()) {
                AmRecord item = itemMap.get(key);
                double outPrice = item.upPrice;
                double currentDelta = inPrice - outPrice;

                if(currentDelta<=0 && Math.abs(currentDelta) > maxDeltaPriceBias) {
                    maxDeltaPriceBias = Math.abs(currentDelta);
                }
                if(currentDelta >= targetProfit) {
                    rItem = item;
                    break;
                }
            }
        }
        if(out != null) {
            out[0] = maxDeltaPriceBias;
            out[1] = getMaxPosPrice(itemMap, tradeType);
            out[2] = getRiskDelta(itemMap, inPrice, tradeType);
        }

        return rItem;
    }

    private static double getRiskDelta(NavigableMap<Integer, AmRecord> itemMap, double inPrice, int tradeType) {
        double riskDelta = 0.0;

        if(itemMap.size() != 0) {
            AmRecord lastItem = itemMap.get(itemMap.lastKey());
            if(tradeType == Stock.TRADE_TYPE_LONG) {
                riskDelta = lastItem.downPrice - inPrice;
            } else {
                riskDelta = inPrice - lastItem.upPrice;
            }
        }

        return riskDelta;
    }
    private static double getMaxPosPrice(NavigableMap<Integer, AmRecord> itemMap, int tradeType) {
        double maxPosPrice = 0;

        if(itemMap.size()!=0) {
            maxPosPrice = Double.POSITIVE_INFINITY;
            if(tradeType == Stock.TRADE_TYPE_LONG)
                maxPosPrice = Double.NEGATIVE_INFINITY;

            for(Integer key: itemMap.keySet()) {
                AmRecord item = itemMap.get(key);
                if(tradeType == Stock.TRADE_TYPE_LONG) {
                    if(item.downPrice > maxPosPrice)
                        maxPosPrice = item.downPrice;
                } else {
                    if(item.upPrice < maxPosPrice)
                        maxPosPrice = item.upPrice;
                }
            }
        }

        return maxPosPrice;
    }

}
