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

import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.analyze.sam.*;
import com.westsword.stocks.analyze.sam1.*;

public class SAm1Helper extends SAmHelper {
    public void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            searchUsage("searchsam1", "600030 20201231 102357");
            return;
        }

        String stockCode = newArgs[0];
        String sModelTradeDate = newArgs[1];
        String sModelHMS = newArgs[2];
        SAm modelSAm = new SAm(stockCode, sModelTradeDate, sModelHMS);
        String sModelPrefix = modelSAm.toString();
        TradeDates tradeDates = new TradeDates(stockCode);

        //info of base Model
        Segment maxS = modelSAm.getSegmentOfMaxAm();
        ArrayList<Double> maxList = maxS.eList;
        System.out.format("%s: maxList.size()=%d\n", sModelPrefix, maxList.size());
        Double max = maxS.maxAm();
        int idx = maxS.indexOf(max);
        System.out.format("%s: max=%8.3f idx=%d\n", sModelPrefix, max, idx);
        //list L
        ArrayList<Double> modelListL = new ArrayList<Double>(maxS.left(idx));
        //list R
        ArrayList<Double> modelListR = new ArrayList<Double>(maxS.right(idx));
        System.out.format("%s: modelListL.size()=%d modelListR.size()=%d\n", 
                sModelPrefix, modelListL.size(), modelListR.size());

        SAm1Manager man = new SAm1Manager();
        //options
        int maxCycle = getMaxCycle(cmd, 1);
        boolean bAllHMS = getSwitchAllHMS(cmd);
        SAmOption option = new SAmOption(maxCycle, bAllHMS);
        //loop tradeDates
        String startDate = getStartDate(cmd, tradeDates.firstDate());
        String endDate = getEndDate(cmd, tradeDates.lastDate());
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
        for(int i=0; i<sTradeDates.length; i++) {
            String sDstTradeDate = sTradeDates[i];

            man.run(stockCode, sDstTradeDate, tradeDates, option,
                    modelListL, modelListR);
        }
    }


}
