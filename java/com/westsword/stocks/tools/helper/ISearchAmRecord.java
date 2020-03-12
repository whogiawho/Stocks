package com.westsword.stocks.tools.helper;


import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.StockDates;

public interface ISearchAmRecord {
    //outParms[0] - bOK
    //outParms[1] - inPrice
    //outParms[2] - outPrice
    //outParms[3] - profit
    //outParms[4] - outTime
    //outParms[5] - maxPosPrice
    //outParms[6] - riskDelta
    //outParms[7] - maxDeltaPriceBias 
    //search to get tradeDate1 inHMS's stats(sTDistance, tradeType, targetRate) for next N day
    //outParms shall be used for 2 scenarios:
    //  bOK == true
    //  bOK == false && distance == maxCycle
    //the scenario bOK==false && distance != maxCycle will be skipped
    public void getTradeParms(String tradeDate1, String inHMS, String nextTradeDateN, 
            int sTDistance, int tradeType, double targetRate, 
            AmManager am, StockDates stockDates, String[] outParms);
}
