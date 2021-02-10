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

/*
 *      ^
 * \
 *  \
 *   \
 *
*/
public class SAm2Helper extends SAmHelper {

    public SAm2Helper() {
    }

    public void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            searchUsage("searchsam2", "600030 20210105 145956");
            return;
        }

        String stockCode = newArgs[0];
        String sModelTradeDate = newArgs[1];
        String sModelHMS = newArgs[2];
        SAm modelSAm = new SAm(stockCode, sModelTradeDate, sModelHMS);
        String sModelPrefix = modelSAm.toString();
        TradeDates tradeDates = new TradeDates(stockCode);

        //info of base Model
        //max segment
        Segment maxS = modelSAm.getSegmentOfMaxLen();
        int maxSize = maxS.getLength();
        double[] vals = maxS.getSlopeR2();
        double slope = vals[0];
        Double maxE = maxS.maxAm();
        double maxLoc = maxS.getLocationOfMaxAm();
        String sFormat = "%s %8s: size=%-4d max=%-8.3f maxLoc=%-8.3f allLt0=%b slope=%-8.4f\n";
        System.err.format(sFormat, 
                sModelPrefix, "maxS", maxSize, maxE, maxLoc, maxS.isAllLt0(), slope);
        //upArrow segment
        int maxSIdx = modelSAm.indexOfSeg(maxS);
        Segment uaS = modelSAm.getUpArrowSegment(maxSIdx+1, 
                SAm2.getUaSegmentLThres(), SAm2.getUaSegmentRThres());
        maxE = uaS.maxAm();
        maxLoc = uaS.getLocationOfMaxAm();
        System.err.format("%s %8s: size=%-4d max=%-8.3f maxLoc=%-8.3f allGt0=%b\n", 
                sModelPrefix, "upArrowS", uaS.getLength(), maxE, maxLoc, uaS.isAllGt0());

        SAm2Manager man = new SAm2Manager();
        //options
        int maxCycle = getMaxCycle(cmd, 1);
        boolean bAllHMS = getSwitchAllHMS(cmd);
        int filter = getFilter(cmd, 0);
        SAm2Option option = new SAm2Option(maxCycle, bAllHMS, filter);
        //loop tradeDates
        String startDate = getStartDate(cmd, tradeDates.firstDate());
        String endDate = getEndDate(cmd, tradeDates.lastDate());
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
        for(int i=0; i<sTradeDates.length; i++) {
            String sDstTradeDate = sTradeDates[i];

            man.run(stockCode, sDstTradeDate, tradeDates, option);
        }
    }

    public void searchUsage(String cmdName, String sInstanceName) {
        System.err.println("usage: java AnalyzeTools " + cmdName + " [-mafse] stockCode tradeDate hms");
        System.err.println("  specific search those amderivatives like " + 
                "<" + sInstanceName + ">");
        System.err.println("       -m maxCycle        ; default 1");
        System.err.println("       -a                 ; loop all hms of tested tradedate");
        System.err.println("       -f filterNO        ; enable specific filters; default 0(no filters)");
        System.err.println("       -s startDate       ; start date to begin search");
        System.err.println("       -e endDate         ; last date to end search");
        System.exit(-1);
    }
    public Options getOptions() {
        Options options = new Options();
        options.addOption("m", true,  "maxCycle to get maxProfit; default 1");
        options.addOption("a", false, "loop all hms of tested tradedate");
        options.addOption("f", true,  "use specific filters");
        options.addOption("s", true,  "starDate to begin; default SdStartDate");
        options.addOption("e", true,  "endDate to end search; default lastTradeDate of stockCode");

        return options;
    }



}
