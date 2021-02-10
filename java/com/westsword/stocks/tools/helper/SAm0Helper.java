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

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.analyze.sam.*;
import com.westsword.stocks.analyze.sam0.*;

public class SAm0Helper extends SAmHelper {

    public void search(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            searchUsage("searchsam0", "600030 20210120 101757");
            return;
        }

        String stockCode = newArgs[0];
        String sModelTradeDate = newArgs[1];
        String sModelHMS = newArgs[2];
        SAm modelSAm = new SAm(stockCode, sModelTradeDate, sModelHMS);
        String sModelPrefix = modelSAm.toString();
        TradeDates tradeDates = new TradeDates(stockCode);

        //info of base Model
        Segment uaS0 = modelSAm.getUpArrowSegment(0, 
                SAm0.getUaSegment0LThres(), SAm0.getUaSegment0RThres());
        double uaMaxE = uaS0.maxAm();
        double uaMaxLoc = uaS0.getLocationOfMaxAm();
        int uaS0Idx = modelSAm.indexOfSeg(uaS0);
        System.err.format("%s %8s: size=%-4d max=%-8.3f maxLoc=%-8.3f allGt0=%b\n", 
                sModelPrefix, "upArrowS0", uaS0.getLength(), uaMaxE, uaMaxLoc, uaS0.isAllGt0());

        //max upArrow's right nearest upArrow segment
        Segment nextS = modelSAm.nextSegment(uaS0Idx);
        Segment uaS1 = modelSAm.getUpArrowSegment(uaS0Idx+1, 
                SAm0.getUaSegment1LThres(), SAm0.getUaSegment1RThres());
        uaMaxE = uaS1.maxAm();
        uaMaxLoc = uaS1.getLocationOfMaxAm();
        System.err.format("%s %8s: size=%-4d max=%-8.3f maxLoc=%-8.3f allGt0=%b\n", 
                sModelPrefix, "upArrowS1", uaS1.getLength(), uaMaxE, uaMaxLoc, uaS1.isAllGt0());
        if(nextS!=uaS1) {
            System.exit(-1);
        }


        SAm0Manager man = new SAm0Manager();
        //options
        int maxCycle = getMaxCycle(cmd, 1);
        boolean bAllHMS = getSwitchAllHMS(cmd);
        SAmOption option = new SAmOption(maxCycle, bAllHMS);
        //loop tradeDates
        String startDate = getStartDate(cmd, tradeDates.firstDate());
        String endDate = getEndDate(cmd, tradeDates.lastDate());
        String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
        //get regroup0
        ReGroup grp = getReGroup0();
        for(int i=0; i<sTradeDates.length; i++) {
            String sDstTradeDate = sTradeDates[i];

            man.run(stockCode, sDstTradeDate, tradeDates, option, grp);
        }

        //how to wait for last thread to be ended?
        man.join();
        System.out.format("%s\n", grp.toString());
    }

    public ReGroup getReGroup0() {
        ArrayList<String> reTradeDates = new ArrayList<String>();
        reTradeDates.add("20120709");
        reTradeDates.add("20130116");
        reTradeDates.add("20190408");
        reTradeDates.add("20210111");
        //reTradeDates.add("20200701");
        int reTradeType = Stock.TRADE_TYPE_SHORT;
        ReGroup grp = new ReGroup(reTradeType, reTradeDates);

        return grp;
    }

    public ReGroup getReGroup1() {
        ArrayList<String> reTradeDates = new ArrayList<String>();
        reTradeDates.add("20091229");
        reTradeDates.add("20160330");
        reTradeDates.add("20150421");
        reTradeDates.add("20201127");
        reTradeDates.add("20160216");
        reTradeDates.add("20180508");
        reTradeDates.add("20140121");
        reTradeDates.add("20141112");
        reTradeDates.add("20200915");
        reTradeDates.add("20161110");
        reTradeDates.add("20200701");
        reTradeDates.add("20100930");
        reTradeDates.add("20101013");

        //reTradeDates.add("20210108");
        //reTradeDates.add("20130730");
        //reTradeDates.add("20101124");
        //reTradeDates.add("20090727");
        //reTradeDates.add("20090401");
        //reTradeDates.add("20090728");
        //reTradeDates.add("20200304");
        //reTradeDates.add("20150930");
        //reTradeDates.add("20141201");
        //reTradeDates.add("20170616");
        //reTradeDates.add("20210111");
        int reTradeType = Stock.TRADE_TYPE_LONG;
        ReGroup grp = new ReGroup(reTradeType, reTradeDates);

        return grp;
    }

}
