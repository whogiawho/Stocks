package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;

public class HashWay implements ISearchAmRecord {
    public void getTradeParms(String tradeDate1, String inHMS, String nextTradeDateN, 
            int sTDistance, int tradeType, double targetRate, 
            AmManager am, StockDates stockDates, String[] outParms) {
        boolean bOK = true;

        long inTime = Time.getSpecificTime(tradeDate1, inHMS);
        double inPrice = am.getInPrice(tradeType, inTime);

        double[] out = new double[] {
            Double.NaN,
            Double.NaN,
            Double.NaN,
        };
        AmRecord outItem = am.getTradeResult(inTime, nextTradeDateN, targetRate, 
                sTDistance, tradeType, stockDates, out);
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
        outParms[5] = "" + out[1];
        outParms[6] = "" + out[2];
        outParms[7] = "" + out[0];
    }
}
