package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.Regression;

public class HashWay implements ISearchAmRecord {
    //outParms[0] - bOK
    //outParms[1] - inPrice
    //outParms[2] - outPrice
    //outParms[3] - profit
    //outParms[4] - outTime
    //outParms[5] - maxPosPrice
    //outParms[6] - riskDelta
    //outParms[7] - maxDeltaPriceBias 
    //search to get tradeDate1 inHMS's stats(sTDistance, tradeType, targetRate) for next N day
    public void getTradeParms(String tradeDate1, String inHMS, String nextTradeDateN, 
            int sTDistance, int tradeType, double targetRate, 
            AmManager am, StockDates stockDates, String[] outParms) {
        boolean bOK = true;

        long inTime = Time.getSpecificTime(tradeDate1, inHMS);
        double inPrice = am.getInPrice(tradeType, inTime);

        double[] out = new double[3];
        AmRecord outItem = am.getTradeResult(inTime, tradeType, targetRate, 
                nextTradeDateN, sTDistance, stockDates, out);
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
