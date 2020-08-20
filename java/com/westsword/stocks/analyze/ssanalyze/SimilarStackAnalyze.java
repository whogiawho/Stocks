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
 
 
package com.westsword.stocks.analyze.ssanalyze;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.analyze.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.session.*;

public class SimilarStackAnalyze {
    private final String mName;
    private SdTime1 mSdTime;

    private final AmManager mAm;
    private final ArrayList<SSTableRecord> mSSTableRecordList;

    private TradeSessionManager mTsMan = null;


    public SimilarStackAnalyze(String stockCode, String sName, SdTime1 sdTime) {
        mName = sName;
        mSdTime = sdTime;

        //set mSSTableRecordList
        mSSTableRecordList = new ArrayList<SSTableRecord>();
        loadSSTable(mSSTableRecordList, sName);

        //set mAm
        ArrayList<String> tradeDateList = SSTableRecord.getTradeDates(mSSTableRecordList);
        System.out.format("%s: new AmManager with tradeDates=%s\n", 
                Utils.getCallerName(getClass()), tradeDateList.toString());
        mAm = new AmManager(stockCode, tradeDateList);
    }
    public void setTradeSessionManager(TradeSessionManager m) {
        mTsMan = m;
    }
    private void loadSSTable(ArrayList<SSTableRecord> sstrList, String sName) {
        SSTableLoader loader = new SSTableLoader();
        String sSSTable = StockPaths.getSSTableFile(sName);
        loader.load(sstrList, sSSTable, sName);
        System.out.format("%s: sSSTable=%s, size=%d\n", 
                Utils.getCallerName(getClass()), sSSTable, sstrList.size());
    }



    public void analyze(TreeMap<Integer, AmRecord> amrMap) {
        //ckpt actions here
        if(amrMap.size() != 0) {
            Integer key = amrMap.lastKey();
            AmRecord currentR = amrMap.get(key);
            long currentTp = currentR.hexTimePoint;
            ArrayList<Integer> removedList = new ArrayList<Integer>();

            for(int i=0; i<mSSTableRecordList.size(); i++) {
                SSTableRecord sstr = mSSTableRecordList.get(i);
                Boolean bR = sstr.eval(currentTp, mAm, amrMap, mSdTime);
                if(bR != null) {
                    removedList.add(i);
                    if(bR&&mTsMan!=null) {
                        //check to open a new tradeSession
                        mTsMan.check2OpenSession(sstr, currentR, mName);
                    } 
                    //print sstr of Traded only
                    sstr.print(currentR, bR, true);
                }
            }

            //remove the elements of removedList from mSSTableRecordList
            for(int i=removedList.size()-1; i>=0; i--) {
                int j = removedList.get(i);
                SSTableRecord sstr = mSSTableRecordList.remove(j);
            }
            if(removedList.size()!=0)
                System.out.format("%s: %d elements removed at %s!\n", 
                        Utils.getCallerName(getClass()), removedList.size(), mName);
        }
    }

}
