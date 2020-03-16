package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.Regression;

public class LoopWay implements ISearchAmRecord {
    public void getTradeParms(String tradeDate1, String inHMS, String nextTradeDateN, 
            int sTDistance, int tradeType, double targetRate, 
            AmManager am, StockDates stockDates, String[] outParms) {
        boolean bOK = true;

        long inTime = Time.getSpecificTime(tradeDate1, inHMS);
        double inPrice = am.getInPrice(tradeType, inTime);

        //t0 or t1
        NavigableMap<Integer, AmRecord> itemMap = getItemMap(am, stockDates, 
                tradeDate1, inHMS, nextTradeDateN, sTDistance);

        double[] out = new double[3];
        AmRecord outItem = Regression.getTradeResult(itemMap, 
                tradeType, inPrice, targetRate, out);
        if(outItem == null) {
            long outTime = Time.getSpecificTime(nextTradeDateN, AStockSdTime.getCloseQuotationTime());
            outItem = am.getFloorItem(outTime);
            bOK = false;
        }

        outParms[0] = "" + bOK;
        outParms[1] = "" + inPrice;
        outParms[2] = "" + outItem.getOutPrice(tradeType);
        outParms[3] = "" + outItem.getProfit(tradeType, inPrice);
        outParms[4] = "" + outItem.hexTimePoint;
        outParms[5] = "" + out[1];    //maxPosPrice
        outParms[6] = "" + out[2];    //riskDelta
        outParms[7] = "" + out[0];    //maxDeltaPriceBias
    }
    private NavigableMap<Integer, AmRecord> getItemMap(AmManager am, StockDates stockDates, 
            String tradeDate, String inHMS, String nextTradeDateN, int sTDistance) {
        NavigableMap<Integer, AmRecord> itemMap = new TreeMap<Integer, AmRecord>();

        if(sTDistance==0) {                                                        //t+0
            itemMap = am.getItemMap(tradeDate, inHMS, 
                    nextTradeDateN, AStockSdTime.getCloseQuotationTime());
        } else if(sTDistance==1) {                                                 //t+1
            String nextTradeDate1 = stockDates.nextDate(tradeDate);
            if(nextTradeDate1 != null) {
                itemMap = am.getItemMap(nextTradeDate1, AStockSdTime.getCallAuctionEndTime(), 
                        nextTradeDateN, AStockSdTime.getCloseQuotationTime());
                /*
                if(tradeDate.equals("20090720")) {
                    System.out.format("nextTradeDate1=%s, nextTradeDateN=%s, itemMap.size=%d\n", 
                            nextTradeDate1, nextTradeDateN, itemMap.size());
                }
                */
            }
        } else {
            String msg = String.format("%d is not a valid N for T+N!\n", sTDistance); 
            throw new RuntimeException(msg);
        }

        return itemMap;
    }
}
