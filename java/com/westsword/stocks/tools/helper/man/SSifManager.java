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
 
 
package com.westsword.stocks.tools.helper.man;


import java.util.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.Task;
import com.westsword.stocks.base.TaskManager;
import com.westsword.stocks.tools.helper.SSUtils;

public class SSifManager extends TaskManager {

    public void run(String tradeDate, String hmsList, 
           ArrayList<String> tradeDateList, double threshold, AmManager am, 
           TreeSet<String> set0, boolean bMatchedTradeDates) {
        maxThreadsCheck();

        Thread t = new SSifTask(this, tradeDate, hmsList, 
                tradeDateList, threshold, am, 
                set0, bMatchedTradeDates);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    public static class SSifTask extends Task {
        private String tradeDate;
        private String hmsList;
        private ArrayList<String> tradeDateList;
        private double threshold;
        private AmManager am;
        private TreeSet<String> set0;
        private boolean bMatchedTradeDates;


        public SSifTask(SSifManager m, String tradeDate, String hmsList, 
                ArrayList<String> tradeDateList, double threshold, AmManager am, 
                TreeSet<String> set0, boolean bMatchedTradeDates) {
            super(m);

            this.tradeDate = tradeDate;
            this.hmsList = hmsList;
            this.tradeDateList = tradeDateList;
            this.threshold = threshold;
            this.am = am;
            this.set0 = set0;
            this.bMatchedTradeDates = bMatchedTradeDates;
        }

        @Override
        public void runTask() {
            //run ssinstance
            ArrayList<String> sMatchedList = SSUtils.getSimilarTradeDates(tradeDateList, threshold, 
                    tradeDate, hmsList, am, null);
            int listSize = sMatchedList.size();
            if(listSize>=2&&set0.containsAll(sMatchedList)) {
                String line = "";
                if(!bMatchedTradeDates)
                    line = String.format("%s %4d\n", hmsList, sMatchedList.size());
                else {
                    //get matched tradedates string
                    line = String.format("%s", sMatchedList.toString());
                    line = line.replaceAll("[\\[\\] ]", "");
                    line = String.format("%s %4d %s\n", hmsList, listSize, line);
                }
                System.out.format("%s", line);
            }

        }
    }
}
