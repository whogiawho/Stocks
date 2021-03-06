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
 
 
package com.westsword.stocks.analyze.avgam;


import java.util.*;

import com.westsword.stocks.analyze.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.session.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;;

public class AvgAmTable extends Table {
    private final String mName;
    private final ArrayList<AvgAmTableRecord> mAvgAmTableRecordList;

    private TradeSessionManager mTsMan = null;

    public AvgAmTable(String stockCode, String sName, SdTime1 sdt) {
        mName = sName;

        int sdbw = Settings.getAvgAmBackwardSd();
        int minDist = Settings.getAvgAmMinimumSkipSd();
        int interval = Settings.getAvgAmInterval();
        mAvgAmTableRecordList = new ArrayList<AvgAmTableRecord>();
        AvgAmTable.load(stockCode, sName, mAvgAmTableRecordList);
        //load avgam
        for(int i=0; i<mAvgAmTableRecordList.size(); i++)
            mAvgAmTableRecordList.get(i).load(sdt, sdbw, minDist, interval); 
    }

    public void eval(AvgAmAnalyze.DeltaAvgAm daa) {
        for(int j=0; j<mAvgAmTableRecordList.size(); j++) {
            AvgAmTableRecord aatr = mAvgAmTableRecordList.get(j);
            boolean bEval = aatr.eval(daa.hms, daa.deltaCorrel, daa.avgam);
            if(bEval) {
                //check2OpenSession
                if(mTsMan!=null&&aatr.tSwitch==1) {
                    System.out.format("%s: running check2OpenSession!\n", 
                            Utils.getCallerName(getClass()));
                    TSRecord r = aatr.toTSRecord();
                    mTsMan.check2OpenSession(r, daa.ar, mName);
                    aatr.setSessionOpened(r.getSessionOpened());
                }

                Utils.asynBeep(30);
                String line = aatr.toString(daa.ar, daa.avgam);
                System.out.format("%s\n", line);
            }
        }
    }




    public static String[] getTableNames() {
        return Table.getTableNames(StockPaths.getAvgAmTableDir());
    }
    //only the records of stockCode are loaded
    public static void load(String stockCode, String sName, ArrayList<AvgAmTableRecord> aatrList) {
        AvgAmTableLoader loader = new AvgAmTableLoader();
        String sAATFile = StockPaths.getAvgAmTableFile(sName);
        loader.load(aatrList, sAATFile, sName, stockCode);
        System.out.format("%s: sAATFile=%s, size=%d\n", 
                "AvgAmTable.load", sAATFile, aatrList.size());
    }


    public static ArrayList<AvgAmTable> make(String stockCode, SdTime1 sdt) {
        ArrayList<AvgAmTable> aatl = new ArrayList<AvgAmTable>();

        String[] sAATName = AvgAmTable.getTableNames();
        for(int i=0; i<sAATName.length; i++) {
            aatl.add(new AvgAmTable(stockCode, sAATName[i], sdt));
        }

        return aatl;
    }

    public void setTradeSessionManager(TradeSessionManager m) {
        mTsMan = m;
    }
}

