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


package com.westsword.stocks.analyze.sam4;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;

public class SAm4Task extends SAmTask {
    public SAm4Task(SAm4Manager man, 
            String stockCode, String sDstTradeDate, TradeDates tradeDates, SAmOption option) {
        super(man, stockCode, sDstTradeDate, tradeDates, option);
    }

    @Override
    public boolean printMatched(SAm dstSAm, TradeDates tradeDates, SAmOption option, AmManager amm) {
        boolean bFound = false;

        String[] lines = new String[3];
        if(!SAm4.inTriggered(dstSAm, lines))
            return bFound;

        String stockCode = dstSAm.getStockCode();
        String tradeDate = dstSAm.getTradeDate();
        String hms = dstSAm.getHMS();

        String lastDate = tradeDates.nextDate(tradeDate, option.maxCycle);

        String sLong = SAmUtils.getLongInfo(amm, tradeDate, hms, lastDate);
        String sShort = SAmUtils.getShortInfo(amm, tradeDate, hms, lastDate);

        String sFormat = "%s: %s %s | %s %s\n";
        System.out.format(sFormat, 
                dstSAm, lines[0], lines[1], sLong, sShort);

        if(!option.bAllHMS)
            bFound = true;
            
        return bFound;
    }

}

