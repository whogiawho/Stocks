 /*
 Copyright (C) 2019-2050 WestSword, Inc.
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.  */
 
 /* Written by whogiawho <whogiawho@gmail.com>. */
 
 
package com.westsword.stocks.analyze;


import java.util.*;

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.am.AmRecord;
import com.westsword.stocks.base.utils.Trade;

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

        double targetProfit = Trade.getTargetProfit(targetRate, inPrice);
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
