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
 
 
package com.westsword.stocks.tools.helper;


import com.westsword.stocks.base.Settings;

public class TradeSum {
    public String tradeDate;
    public int matchedCnt;
    public double winRate;
    public double avgNetRevenue;
    public double avgMaxRevenue;
    public double expRisk0;
    public double expRisk1;
    public String sMatchedTradeDates;
    public String hmsList;
    public double actualAvgNetRevenue;

    public TradeSum(String[] fields) {
        tradeDate = fields[0];
        matchedCnt = Integer.valueOf(fields[1]);
        winRate = Double.valueOf(fields[2].substring(0, fields[2].length()-1))/100;
        avgNetRevenue = Double.valueOf(fields[3]);
        avgMaxRevenue = Double.valueOf(fields[4]);
        expRisk0 = Double.valueOf(fields[5]);
        expRisk1 = Double.valueOf(fields[6]);
        sMatchedTradeDates = fields[7];
        hmsList = fields[8];
        actualAvgNetRevenue = Double.valueOf(fields[9]);

        boolean bSwitchOfRawData = Settings.getSwitch(Settings.SWITCH_OF_RAW_DATA);
        if(bSwitchOfRawData) {
            String sFormat = "%s %4d %8.0f%% %8.3f %8.3f %8.3f %8.3f %s %s %8.3f\n";
            System.out.format(sFormat, 
                    tradeDate, matchedCnt, winRate*100, avgNetRevenue, avgMaxRevenue, expRisk0, expRisk1, 
                    sMatchedTradeDates, hmsList, actualAvgNetRevenue);
        }
    }

    public TradeSum(String tradeDate, String hmsList) {
        this.tradeDate = tradeDate;
        this.hmsList = hmsList;
    }
}
