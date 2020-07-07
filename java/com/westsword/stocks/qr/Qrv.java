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
 
 
package com.westsword.stocks.qr;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.Utils;

public class Qrv {
    private String endTradeDate;
    private String endHMS;
    private double profit;

    //remaining fields
    private String startTradeDate;
    private String startHMS;
    private String sOthers;


    public Qrv(String endTradeDate, String endHMS, double profit, 
            String startTradeDate, String startHMS, String sOthers) {
        this.endTradeDate = endTradeDate;
        this.endHMS = endHMS;
        this.profit = profit;

        this.startTradeDate = startTradeDate;
        this.startHMS = startHMS;
        this.sOthers = sOthers;
    }

    public Qrv(String endTradeDate, String endHMS, double profit) {
        this(endTradeDate, endHMS, profit, "", "", "");
    }

    public int getEndSdt(SdTime1 sdt) {
        return sdt.getAbs(endTradeDate, endHMS);
    }
    public String getEndDate() {
        return endTradeDate;
    }
    public String getEndHMS() {
        return endHMS;
    }
    public double getProfit() {
        return profit;
    }

    public double getAmCorrel(Qrv qrv, int sdLength, SdTime1 sdt, AmManager am) {
        int endIdx0 = getEndSdt(sdt);
        int startIdx0 = endIdx0 - sdLength;

        int endIdx1 = qrv.getEndSdt(sdt);
        int startIdx1 = endIdx1 - sdLength;

        return am.getAmCorrel(startIdx0, endIdx0, startIdx1, endIdx1);
    }

    public int getStartSdt(SdTime1 sdt, int sdLength) {
        int endSd = getEndSdt(sdt);
        int startSd = endSd - sdLength;
        return startSd;
    }
    public int getSdTime(SdTime1 sdt, int sdLength, int relIdx) {
        int startSd = getStartSdt(sdt, sdLength);

        return startSd + relIdx;
    }

    public String toString() {
        return String.format("%s %s %s %s%s %8.3f\n", 
                startTradeDate, startHMS, endTradeDate, endHMS, sOthers, profit);
    }

    public void print(int sdLength, double profitSum, int count, int[] e, SdTime1 sdt) {
        int ckptIntervalLen = Utils.getCkptIntervalSdLength();
        int start = getSdTime(sdt, sdLength, e[0]*ckptIntervalLen);
        int end = getSdTime(sdt, sdLength, e[1]*ckptIntervalLen);
        long startTp = sdt.rgetAbs(start);
        long endTp = sdt.rgetAbs(end);
        String sStart = Time.getTimeYMDHMS(startTp, false, false);
        String sEnd = Time.getTimeYMDHMS(endTp, false, false);

        System.out.format("%s    %s %s %4d %8.3f %8.3f (%d,%d)\n", 
                endTradeDate+"_"+endHMS, sStart, sEnd, count, profitSum, profitSum/count, e[0], e[1]);
    }
}


