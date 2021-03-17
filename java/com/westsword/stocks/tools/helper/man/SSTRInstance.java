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
 
 
package com.westsword.stocks.tools.helper.man;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.Trade;
import com.westsword.stocks.tools.helper.SSUtils;
import com.westsword.stocks.analyze.ss.BaseSSTR;

public class SSTRInstance extends BaseSSTR {

    public SSTRInstance(String stockCode, String startDate, double threshold, int sTDistance, int tradeType,
            int maxCycle, double targetRate, String sMatchExp) {
        super(stockCode, startDate, threshold, sTDistance, tradeType,
                maxCycle, targetRate, sMatchExp);
    }
    public SSTRInstance(SSTRInstance r) {
        super(r);
    }



    public void run(AmManager am, StockDates stockDates, boolean bStdout) {
        SSInstance.BufR br = new SSInstance.BufR();
        _run(am, stockDates, br);

        //write tradeDetails to stdout
        if(bStdout)
            System.out.format("%s", br.sTradeDetails);

        TradeSumLogThread.write(sMatchExp, br, bStdout);

    }
    public String getInHMS() {
        TreeSet<AtomExpr> exprSet = getAtomExprSet();
        AtomExpr e = exprSet.last();

        return e.sLastHMS;
    }
    public void _run(AmManager am, StockDates stockDates, SSInstance.BufR br) {
        //
        String inHMS = getInHMS();

        ArrayList<String> similarTradeDates = SSUtils.getSimilarTradeDates(this, am);

        ISearchAmRecord w = SSUtils.getWay2SearchAmRecord();
        ArrayList<Long> outTimeList = new ArrayList<Long>();
        for(int i=0; i<similarTradeDates.size(); i++) {
            String tradeDate1 = similarTradeDates.get(i);
            String nextTradeDateN = stockDates.nextDate(tradeDate1, maxCycle);
            int distance = stockDates.getDistance(tradeDate1, nextTradeDateN);

            //search to get tradeDate1 inHMS's inPrice stats for next N day
            String[] outParms = new String[8];
            w.getTradeParms(tradeDate1, inHMS, nextTradeDateN, 
                    sTDistance, tradeType, targetRate,
                    am, stockDates, outParms);

            //skip those outParms when (outParms[0]==false&&distance!=maxCycle)
            if(distance == maxCycle||outParms[0].equals("true")) {
                long inTime = Time.getSpecificTime(tradeDate1, inHMS);
                //remove those that are smaller than inTime from outTimeList, for getting maxHangCount
                removeQuitedItems(outTimeList, inTime);

                br.handleParms(outParms, tradeType, outTimeList.size()+1);
                br.sMatchedTradeDates += tradeDate1 + " ";

                br.sTradeDetails += getTradeDetailsLine(stockDates, outTimeList.size()+1,
                        tradeDate1, tradeType, inHMS, br.netRevenue, outParms);

                //add outTime to outTimeList
                outTimeList.add(Long.valueOf(outParms[4]));
            }
        }
    }



    private String getTradeDetailsLine(StockDates stockDates, int currentHangCount,
            String tradeDate1, int tradeType, String inHMS, double netRevenue, String[] outParms) {
        String sFormat = "%s %s %s %s " + 
            "%8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %4d %4d\n";

        double inPrice = Double.valueOf(outParms[1]);
        double outPrice = Double.valueOf(outParms[2]);
        double profit = Double.valueOf(outParms[3]);
                    long outTime = Long.valueOf(outParms[4]);
                    String outDate = Time.getTimeYMD(outTime, false);
                    String outHMS = Time.getTimeHMS(outTime);
        double risk0 = Double.valueOf(outParms[6]);
        double risk1 = Double.valueOf(outParms[7]);
        double maxPosDelta = getMaxPosDelta(tradeType, inPrice, outParms);
        String line = String.format(sFormat,
                tradeDate1, inHMS, outDate, outHMS,  
                inPrice, outPrice, profit, netRevenue, maxPosDelta, risk0, risk1, 
                stockDates.getDistance(tradeDate1, outDate), currentHangCount);

        return line;
    }
    private static void removeQuitedItems(ArrayList<Long> outTimeList, long inTime) {
        ArrayList<Integer> removedIdxList = new ArrayList<Integer>();

        //System.out.format("outTimeList.size()=%d\n", outTimeList.size());
        for(int i=0; i<outTimeList.size(); i++) {
            Long outTime = outTimeList.get(i);
            if(inTime>outTime) {
                removedIdxList.add(i);
                //System.out.format("inTime=%x, outTime=%x\n", inTime, outTime);
            }
        }

        for(int i=removedIdxList.size()-1; i>=0; i--) {
            int idx = removedIdxList.get(i);
            outTimeList.remove(idx);
        } 
    }
    private static double getMaxPosDelta(int tradeType, double inPrice, String[] outParms) {
        double maxPosDelta;

        double maxPosPrice = Double.valueOf(outParms[5]);
        if(tradeType == Stock.TRADE_TYPE_LONG)
            maxPosDelta =  maxPosPrice - inPrice;
        else
            maxPosDelta =  inPrice - maxPosPrice;

        return maxPosDelta;
    }



    private static double getMaxRevenue(String[] outParms, int tradeType) {
        double buyPrice, sellPrice;
        if(tradeType == Stock.TRADE_TYPE_LONG) {
            buyPrice = Double.valueOf(outParms[1]);
            sellPrice = Double.valueOf(outParms[5]);
        }else {
            buyPrice = Double.valueOf(outParms[5]);
            sellPrice = Double.valueOf(outParms[1]);
        }
        double maxRevenue = Trade.getNetProfit(buyPrice, sellPrice);

        return maxRevenue;
    }

}
