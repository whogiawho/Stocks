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
 
 
package com.westsword.stocks.tools.helper;


import com.westsword.stocks.base.Settings;

public class TradeDetail {
    public String matchedTradeDate;
    public String inHMS;
    public String outTradeDate;
    public String outHMS;

    public double inPrice;
    public double outPrice;
    public double profit;
    public double netRevenue;

    public double maxPosDelta;
    public double risk0;
    public double risk1;
    public int    cycle;
    public int    currentHangCount;



    public TradeDetail(String[] fields) {
        matchedTradeDate = fields[0];
        inHMS = fields[1];
        outTradeDate = fields[2];
        outHMS = fields[3];

        inPrice = Double.valueOf(fields[4]);
        outPrice = Double.valueOf(fields[5]);
        profit = Double.valueOf(fields[6]);
        netRevenue = Double.valueOf(fields[7]);

        maxPosDelta = Double.valueOf(fields[8]);
        risk0 = Double.valueOf(fields[9]);
        risk1 = Double.valueOf(fields[10]);
        cycle = Integer.valueOf(fields[11]);
        currentHangCount = Integer.valueOf(fields[12]);

        boolean bSwitchOfRawData = Settings.getSwitch(Settings.SWITCH_OF_RAW_DATA);
        if(bSwitchOfRawData) {
            String sFormat = "%s %s %s %s " + 
                "%8.3f %8.3f %8.3f %8.3f " +
                "%8.3f %8.3f %8.3f %4d %4d\n";
            System.out.format(sFormat, 
                    matchedTradeDate, inHMS, outTradeDate, outHMS,
                    inPrice, outPrice, profit, netRevenue,
                    maxPosDelta, risk0, risk1, cycle, currentHangCount);
        }
    }
}
