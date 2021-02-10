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
import com.westsword.stocks.analyze.sam4.*;

public class SAm4Helper extends SAmHelper {

    public void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            searchUsage("searchsam4", "600030 20210115 102536");
            return;
        }

        String stockCode = newArgs[0];
        String sModelTradeDate = newArgs[1];
        String sModelHMS = newArgs[2];
        SAm modelSAm = new SAm(stockCode, sModelTradeDate, sModelHMS);


        //info of base Model
        modelSAmInfo(modelSAm);


        SAm4Manager man = new SAm4Manager();
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
        //max downArrow segment
        Segment maxS = modelSAm.getSegmentOfMaxLen();
        int maxSize = maxS.getLength();
        Double maxE = maxS.maxAm();
        Double minE = maxS.minAm();
        double maxLoc = maxS.getLocationOfMaxAm();
        double minLoc = maxS.getLocationOfMinAm();
        int maxSIdx = modelSAm.indexOfSeg(maxS);
        String sFormat = "%s %8s: size=%-4d min=%-6.2f minLoc=%-6.2f allLt0=%b\n";
        System.err.format(sFormat, 
                sModelPrefix, "maxList", maxSize, minE, minLoc, maxS.isAllLt0());
        //check
        Segment daS = modelSAm.getDownArrowSegment(maxSIdx, 
                SAm4.getMaxdaSegmentLThres(), SAm4.getMaxdaSegmentRThres());
        if(daS!=maxS) {
            System.err.format("exception: maxLenSegment is not an downArrowSegment!\n");
            System.exit(-1);
        }

        Segment nextUaS = modelSAm.getUpArrowSegment(maxSIdx+1, 
                SAm4.getUaSegmentLThres(), SAm4.getUaSegmentRThres());
        if(nextUaS==null||!nextUaS.isAllGt0()) {
            System.exit(-1);
        }
        int uaSize = nextUaS.getLength();
        double uaMaxE = nextUaS.maxAm();
        double uaMaxER = nextUaS.getLocationOfMaxAm();
        System.err.format("%s %8s: size=%-4d max=%-6.2f maxLoc=%-6.2f allGt0=%b\n", 
                sModelPrefix, "upArrowS", uaSize, uaMaxE, uaMaxER, nextUaS.isAllGt0());
    }

}
