 /*
 Copyright (C) 1989-2020 Free Software Foundation, Inc.
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
