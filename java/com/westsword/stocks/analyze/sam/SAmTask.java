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


package com.westsword.stocks.analyze.sam;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.analyze.sam.*;

public class SAmTask extends Task {

    private String stockCode;
    private String sDstTradeDate;
    private TradeDates tradeDates;
    private SAmOption option;

    public SAmTask(SAmManager man, 
            String stockCode, String sDstTradeDate, 
            TradeDates tradeDates, SAmOption option) {
        super(man);

        this.stockCode = stockCode;
        this.sDstTradeDate = sDstTradeDate;
        this.tradeDates = tradeDates;
        this.option = option;
    }
    @Override
    public void runTask() {
        String lastDate = tradeDates.nextDate(sDstTradeDate, option.maxCycle);
        AmManager amm = new AmManager(stockCode, sDstTradeDate, lastDate);

        //run instance
        String sDstDerivativeDir = StockPaths.getDerivativeDir(stockCode, sDstTradeDate);
        //System.out.format("sDstDerivativeDir=%s\n", sDstDerivativeDir);
        String[] sFiles = Utils.getSubNames(sDstDerivativeDir);
        for(int j=0; j<sFiles.length; j++) {
            String sDstFile = sFiles[j];
            String hms = sDstFile.replace(".txt", "");
            SAm dstSAm = new SAm(stockCode, sDstTradeDate, hms);

            boolean bFound = printMatched(dstSAm, tradeDates, option, amm);
            if(bFound) {
                break;
            }
        }
    }

    public boolean printMatched(SAm dstSAm, TradeDates tradeDates, SAmOption option, AmManager amm) {
        return false;
    }

}

