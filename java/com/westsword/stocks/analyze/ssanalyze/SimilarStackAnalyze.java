package com.westsword.stocks.analyze.ssanalyze;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.analyze.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.session.*;

public class SimilarStackAnalyze {

    private final AmManager mAm;

    private final ArrayList<SSTableRecord> mSSTableRecordList;
    private TradeSessionManager mTsMan = null;


    public SimilarStackAnalyze(String stockCode) {
        //set mSSTableRecordList
        mSSTableRecordList = new ArrayList<SSTableRecord>();
        loadSSTable(mSSTableRecordList);

        //set mAm
        ArrayList<String> tradeDateList = SSTableRecord.getTradeDates(mSSTableRecordList);
        StockDates stockDates = new StockDates(stockCode);
        mAm = new AmManager(stockCode, tradeDateList);
    }
    public void setTradeSessionManager(TradeSessionManager m) {
        mTsMan = m;
    }
    private void loadSSTable(ArrayList<SSTableRecord> sstrList) {
        SSTableLoader loader = new SSTableLoader();
        String sSSTable = StockPaths.getSSTableFile();
        loader.load(sstrList, sSSTable);
        //System.out.format("%s: size=%d\n", Utils.getCallerName(getClass()), sstrList.size());
    }



    public void analyze(TreeMap<Integer, AmRecord> amrMap) {
        //ckpt actions here
        Integer key = amrMap.lastKey();
        if(key!=null) {
            AmRecord currentR = amrMap.get(key);
            long currentTp = currentR.hexTimePoint;
            ArrayList<Integer> removedList = new ArrayList<Integer>();

            for(int i=0; i<mSSTableRecordList.size(); i++) {
                SSTableRecord sstr = mSSTableRecordList.get(i);
                Boolean bR = sstr.eval(currentTp, mAm, amrMap);
                if(bR != null) {
                    removedList.add(i);
                    if(bR&&mTsMan!=null) {
                        //check to open a new tradeSession
                        mTsMan.check2OpenSession(sstr, currentR, "");
                    } 
                    //print sstr
                    sstr.print(bR);
                }
            }

            //remove the elements of removedList from mSSTableRecordList
            for(int i=removedList.size()-1; i>=0; i--) {
                int j = removedList.get(i);
                SSTableRecord sstr = mSSTableRecordList.remove(j);
            }
        }
    }

}
