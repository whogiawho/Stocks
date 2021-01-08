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
import com.westsword.stocks.analyze.sam2.*;

public class SAm2Helper extends SAmHelper {

    public SAm2Helper() {
    }

    public static void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            searchUsage("searchsam2", "600030 20210105 145956");
            return;
        }

        String stockCode = newArgs[0];
        String sModelTradeDate = newArgs[1];
        String sModelHMS = newArgs[2];
        String sModelPrefix = String.format("%s %s %s", stockCode, sModelTradeDate, sModelHMS);
        TradeDates tradeDates = new TradeDates(stockCode);

        String sSrcDerivativeDir = StockPaths.getDerivativeDir(stockCode, sModelTradeDate);
        String sSrcFile = sSrcDerivativeDir + sModelHMS + ".txt";
        AmDerLoader l = new AmDerLoader(0);
        ArrayList<Double> amderList = new ArrayList<Double>();
        l.load(amderList, sSrcFile);

        //info of base Model
        ArrayList<Segment> segList = Segment.make(amderList);
        System.err.format("%s %8s: size=%-4d\n", 
                sModelPrefix, "segList", segList.size());
        //max segment
        Segment maxS = Segment.getMaxLenSegment(segList, SAm2.MAX_SEGMENT_MIN_LENGTH);
        ArrayList<Double> maxList = maxS.eList;
        int maxSize = maxList.size();
        double[] vals = SAm.getSlopeR2(maxList);
        double slope = vals[0];
        Double maxE = Collections.max(maxList);
        int eIdx = maxList.indexOf(maxE);
        String sFormat = "%s %8s: size=%-4d max=%-8.3f maxEIdx=%-4d allLt0=%b slope=%-8.4f\n";
        System.err.format(sFormat, 
                sModelPrefix, "maxList", maxSize, maxE, eIdx, SAm.isAllLt0(maxList), slope);
        //upArrow segment
        int maxSIdx = segList.indexOf(maxS);
        Segment uaS = Segment.getUpArrowSegment(segList, maxSIdx+1, 0.5, 0.76);
        ArrayList<Double> uaList = uaS.eList;
        maxE = Collections.max(uaList);
        eIdx = uaList.indexOf(maxE);
        System.err.format("%s %8s: size=%-4d max=%-8.3f maxEIdx=%-4d allGt0=%b\n", 
                sModelPrefix, "upArrowS", uaList.size(), maxE, eIdx, SAm.isAllGt0(uaList));

        SAm2Manager man = new SAm2Manager();
        String startDate = getStartDate(cmd, tradeDates.firstDate());
        String endDate = getEndDate(cmd, tradeDates.lastDate());
        double threshold = getThreshold(cmd, 0.9);
        int maxCycle = getMaxCycle(cmd, 1);
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
        for(int i=0; i<sTradeDates.length; i++) {
            String sDstTradeDate = sTradeDates[i];
            String sDstDerivativeDir = StockPaths.getDerivativeDir(stockCode, sDstTradeDate);
            //System.out.format("sDstDerivativeDir=%s\n", sDstDerivativeDir);

            man.run(sDstDerivativeDir, stockCode, sDstTradeDate, 
                    threshold, tradeDates, maxCycle);
        }
    }
}
