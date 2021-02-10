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

import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;
import com.westsword.stocks.analyze.sam5.*;

public class SAm5Helper extends SAmHelper {

    public void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            searchUsage("searchsam4", "600030 20150630 135356");
            return;
        }

        String stockCode = newArgs[0];
        String sModelTradeDate = newArgs[1];
        String sModelHMS = newArgs[2];
        SAm modelSAm = new SAm(stockCode, sModelTradeDate, sModelHMS);


        //info of base Model
        modelSAmInfo(modelSAm);


        SAm5Manager man = new SAm5Manager();
        //options
        int maxCycle = getMaxCycle(cmd, 1);
        boolean bAllHMS = getSwitchAllHMS(cmd);
        SAmOption option = new SAmOption(maxCycle, bAllHMS);
        //loop tradeDates
        TradeDates tradeDates = new TradeDates(stockCode);
        String startDate = getStartDate(cmd, tradeDates.firstDate());
        String endDate = getEndDate(cmd, tradeDates.lastDate());
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
        for(int i=0; i<sTradeDates.length; i++) {
            String sDstTradeDate = sTradeDates[i];

            man.run(stockCode, sDstTradeDate, tradeDates, option);
        }
    }

    private void modelSAmInfo(SAm modelSAm) {
        String sModelPrefix = modelSAm.toString();
        //max segment
        Segment maxS = modelSAm.getSegmentOfMaxLen();
        int maxSize = maxS.getLength();
        double maxE = maxS.maxAm();
        double maxLoc = maxS.getLocationOfMaxAm();
        String sFormat = "%s %8s: size=%-4d max=%-6.2f maxLoc=%-6.2f start=%-6d allGt0=%b\n";
        System.err.format(sFormat, 
                sModelPrefix, "maxS", maxSize, maxE, maxLoc, maxS.start, maxS.isAllGt0());

        //last segment
        int cnt = modelSAm.getNumberOfSegments();
        if(cnt!=1) {
            System.err.format("Exception: %d segments exist!\n", cnt);
            System.exit(-1);
        }

        //
        int idxOfFLMax= maxS.getIdxOfLocalMax(0, SAm5.LOCAL_EXTREME_ERR_OFFSET);
        double[] ret = maxS.getSlopeR2(0, idxOfFLMax);
        sFormat = "%s %8s: idxOfFLMax=%-4d FLMax=%-8.3f slope=%-8.3f r2=%-8.3f\n";
        System.err.format(sFormat, 
                sModelPrefix, "maxS", idxOfFLMax, maxS.get(idxOfFLMax), ret[0], ret[1]);

        int idxOfFLMin= maxS.getIdxOfLocalMin(idxOfFLMax, SAm5.LOCAL_EXTREME_ERR_OFFSET);
        ret = maxS.getSlopeR2(idxOfFLMax, idxOfFLMin);
        sFormat = "%s %8s: idxOfFLMin=%-4d FLMin=%-8.3f slope=%-8.3f r2=%-8.3f\n";
        System.err.format(sFormat, 
                sModelPrefix, "maxS", idxOfFLMin, maxS.get(idxOfFLMin), ret[0], ret[1]);

        ret = maxS.getSlopeR2(idxOfFLMin, maxS.getLength());
        sFormat = "%s %8s: idxOfLast=%-4d slope=%-8.3f r2=%-8.3f\n";
        System.err.format(sFormat, 
                sModelPrefix, "maxS", idxOfFLMin, ret[0], ret[1]);
    }
}
