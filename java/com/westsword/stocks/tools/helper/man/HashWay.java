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
        outParms[5] = "" + out[1];    //maxPosPrice
        outParms[6] = "" + out[2];    //riskDelta
        outParms[7] = "" + out[0];    //maxDeltaPriceBias
    }
}
