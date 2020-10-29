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
 
 
package com.westsword.stocks.am;


import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.*;

public class AmrParallelLoader {
    private String mStockCode;

    public AmrParallelLoader(String stockCode) {
        mStockCode = stockCode;
    }


    //parallel load
    //  rMap is TreeMap; AmrHashtable is composed of 3 TreeMap
    //  TreeMap is not thread-safe; so for each tradeDate's load, seperate TreeMap should be used
    //  And then they should be merged into one, that is <rMap, hTable>
    public void load(TreeMap<Integer, AmRecord> rMap, AmrHashtable hTable, String[] sTradeDates) {
        int MaxLoadThread=5;
        int lN=0;
        AmrLoadThread[] tGroup = new AmrLoadThread[MaxLoadThread]; //loading threads
        AmrHashtable[] t0Group = new AmrHashtable[MaxLoadThread];  
        ArrayList<TreeMap<Integer, AmRecord>> m0Group = new ArrayList<TreeMap<Integer, AmRecord>>();
        for(int i=0; i<sTradeDates.length; i++) {
            String tradeDate = sTradeDates[i];
            String sAmRecordFile = StockPaths.getAnalysisFile(mStockCode, tradeDate);
            TreeMap<Integer, AmRecord> m0 = new TreeMap<Integer, AmRecord>();
            AmrHashtable t0 = new AmrHashtable();

            m0Group.add(m0);
            t0Group[lN]=t0;
            AmrLoadThread t = new AmrLoadThread(m0, t0, sAmRecordFile);
            tGroup[lN] = t;
            lN++;
            t.start();

            if(lN>=MaxLoadThread) {
                //wait until tGroup[] completes
                join(tGroup, lN);
                //merge m0Group&t0Group into rMap&hTable
                merge(m0Group, t0Group, rMap, hTable, lN);
                //reset lN, tGroup&m0Group&t0Group
                reset(tGroup, t0Group, m0Group, lN);
                lN=0;
            }
        }
        //wait&merge the last
        join(tGroup, lN);
        merge(m0Group, t0Group, rMap, hTable, lN);

        System.err.format("%s: loading complete\n", 
                Utils.getCallerName(getClass()));
    }
    private void reset(AmrLoadThread[] tGroup, AmrHashtable[] t0Group, 
            ArrayList<TreeMap<Integer, AmRecord>> m0Group, int lN) {
        for(int j=0; j<lN; j++) {
            tGroup[j]=null;
            t0Group[j]=null;
        }
        m0Group.clear();
    }
    private void merge(ArrayList<TreeMap<Integer, AmRecord>> m0Group, AmrHashtable[] t0Group, 
            TreeMap<Integer, AmRecord>rMap, AmrHashtable hTable, int lN) {
        for(int i=0; i<lN; i++) {
            TreeMap<Integer, AmRecord> m0 = m0Group.get(i);
            rMap.putAll(m0);
        }
        for(int i=0; i<lN; i++) {
            AmrHashtable t0 = t0Group[i];
            hTable.merge(t0);
        }
    }
    private void join(AmrLoadThread[] tGroup, int last) {
        //wait until tGroup[] completes
        for(int j=0; j<last; j++) {
            try {
                tGroup[j].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static class AmrLoadThread extends Thread {
        private TreeMap<Integer, AmRecord> m0;
        private AmrHashtable t0;
        private String sAmRecordFile;
        public AmrLoadThread(TreeMap<Integer, AmRecord> m0, AmrHashtable t0, String sAmRecordFile) {
            this.m0 = m0;
            this.t0 = t0;
            this.sAmRecordFile = sAmRecordFile;
        }

        public void run() {
            AmRecordLoader amLoader = new AmRecordLoader();
            amLoader.load(null, m0, t0, sAmRecordFile);
        }
    }

}
