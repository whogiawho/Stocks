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


package com.westsword.stocks.analyze.sam0;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;

public class SAm0Task extends SAmTask {
    ReGroup mGrp;

    public SAm0Task(SAm0Manager man, 
            String stockCode, String sDstTradeDate, TradeDates tradeDates, SAmOption option, 
            ReGroup grp) {
        super(man, stockCode, sDstTradeDate, tradeDates, option);
        mGrp = grp;
    }

    @Override
    public boolean printMatched(SAm dstSAm, TradeDates tradeDates, SAmOption option, AmManager amm) {
        boolean bFound = false;

        SAm0 sam0 = new SAm0();
        String[] lines = new String[4];
        if(!sam0.inTriggered(dstSAm, lines, amm, mGrp))
            return bFound;

        String stockCode = dstSAm.getStockCode();
        String tradeDate = dstSAm.getTradeDate();
        String hms = dstSAm.getHMS();

        String lastDate = tradeDates.nextDate(tradeDate, option.maxCycle);

        String sLong = SAmUtils.getLongInfo(amm, tradeDate, hms, lastDate);
        String sShort = SAmUtils.getShortInfo(amm, tradeDate, hms, lastDate);

        String sFormat = "%s: %s %s %s %3s | %s %s\n";
        System.out.format(sFormat, 
                dstSAm, lines[0], lines[1], lines[2], lines[3], sLong, sShort);

        if(!option.bAllHMS)
            bFound = true;
            
        return bFound;
    }

}

