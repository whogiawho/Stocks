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

import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.StockPaths;
import com.westsword.stocks.am.average.*;
import com.westsword.stocks.am.derivative.*;
import com.westsword.stocks.analyze.RawTradeDetails;
import com.westsword.stocks.analyze.RawTradeDetailsList;


public class AmUtils {
    public AmUtils(String stockCode, boolean bStartCopyManagerThread) {
        mStockCode = stockCode;
        mSdTime = new SdTime1(stockCode);   //get sdStartDate&sdStartTime&interval from settings.txt
        mStockDates = new StockDates(stockCode, false);

        //
        if(Settings.getAmDerPng())
            mAdm = new AmDerManager(bStartCopyManagerThread);
        else
            mAdm = null;
        //
        if(Settings.getAvgAmPng())
            mAam = new AvgAmManager();
        else
            mAam = null;
    }
    public AmUtils(String stockCode) {
        this(stockCode, true);
    }

    public void stopCopyManager() {
        if(mAdm!=null)
            mAdm.stopCopyManager();
    }
    public void writeAllAmRecords() {
        writeAmRecords(mStockDates.firstDate(), mStockDates.lastDate());
    }
    public void writeAmRecords(String tradeDate1, String tradeDate2) {
        tradeDate1 = mStockDates.ceiling(tradeDate1);
        tradeDate2= mStockDates.floor(tradeDate2);
        AmRecord startAmr = loadPrevLastAmRecord(tradeDate1);
        while(tradeDate1!=null&&tradeDate2!=null&&tradeDate1.compareTo(tradeDate2)<=0) {
            startAmr = writeAmRecords(startAmr, tradeDate1);

            tradeDate1 = mStockDates.nextDate(tradeDate1);
        }
    }
    //r[in]
    //amr[out]
    //ter[out]
    public void statsChanged(RawTradeDetails r, AmRecord amr, TrackExtreme ter) {
        if(r.type == Stock.TRADE_TYPE_UP) {
            amr.am += r.count;
            amr.abVol += r.count;
            amr.abAmount += r.count*r.price;
            ter.traceUp(r.price);
        } else {
            amr.am -= r.count;
            amr.asVol += r.count;
            amr.asAmount += r.count*r.price;
            ter.traceDown(r.price);
        }
        amr.trVol += r.count;
        amr.trAmount += r.count*r.price;
    }
    public AmRecord writeAmRecords(AmRecord amr, String tradeDate) {
        //any tp later than tradeDate's close_quotation_time is not written
        long closeTP = Time.getSpecificTime(tradeDate, AStockSdTime.getCloseQuotationTime());
        //System.out.format("%s: closeTP=%x\n", Utils.getCallerName(getClass()), closeTP);
        
        ArrayList<RawTradeDetails> rawDetailsList = RawTradeDetails.load(mStockCode, tradeDate);
        int lSize = rawDetailsList.size();
        TrackExtreme ter = new TrackExtreme(rawDetailsList);

        //reset sAnalysisFile
        String sAnalysisFile = StockPaths.getAnalysisFile(mStockCode, tradeDate);
        Utils.deleteFile(sAnalysisFile);

        long caeTp = Time.getSpecificTime(tradeDate, mSdTime.getCallAuctionEndTime());
        int caeSd = mSdTime.getAbs(caeTp);
        int prevSd = caeSd;                //prevSd starts from callAuctionEndTime
        for(int i=0; i<lSize; i++) {
            RawTradeDetails r = rawDetailsList.get(i);
            //System.out.format("%s: %x\n", Utils.getCallerName(getClass()), r.time);
            int rSd = mSdTime.getAbs(r.time);
            if(rSd != prevSd) {
                //write AmRecords between [prevSd, rSd) to sAnalysisFile
                writeRange(prevSd, rSd, amr, ter, sAnalysisFile, closeTP);

                prevSd = rSd;
                ter.resetEx();
            }

            statsChanged(r, amr, ter);
        }
        //last record
        if(lSize!=0) {
            //System.out.format("%s: %d\n", Utils.getCallerName(getClass()), prevSd);
            writeRange(prevSd, prevSd+1, amr, ter, sAnalysisFile, closeTP);
        }
        //_print(amr);
        
        return amr;
    }
    private void _print(AmRecord amr) {
        System.out.format("%s: trVol=%d trAmount=%-8.3f\n", 
                Utils.getCallerName(getClass()), amr.trVol, amr.trAmount);
        System.out.format("%s: abVol=%d abAmount=%-8.3f\n", 
                Utils.getCallerName(getClass()), amr.abVol, amr.abAmount);
        System.out.format("%s: asVol=%d asAmount=%-8.3f\n", 
                Utils.getCallerName(getClass()), amr.asVol, amr.asAmount);
    }
    //amrMap[out]
    //prevAmrMap[out]
    public void writeRange(int start, int end, AmRecord amr, TrackExtreme ter, String sAnalysisFile, long closeTP, 
            TreeMap<Integer, AmRecord> amrMap, TreeMap<Integer, AmRecord> prevAmrMap) {
        ter.setEx2Prev();
        for(int i=start; i<end; i++) {
            long tp = mSdTime.rgetAbs(i);
            if(tp<=closeTP) {
                //String tradeDate = Time.getTimeYMD(tp);
                //String tradeTime = Time.getTimeHMS(tp);
                AmRecord r = new AmRecord(tp, i, amr.am, ter.maxUP, ter.minDP,
                        amr.abVol, amr.asVol, amr.abAmount, amr.asAmount);
                if(amrMap!=null) {
                    amrMap.put(i, r);
                }
                if(prevAmrMap!=null) {
                    prevAmrMap.put(i, r);
                    //clone prevAmrMap to avoid writing synchronized codes
                    TreeMap<Integer, AmRecord> cMap = new TreeMap<Integer, AmRecord>(prevAmrMap);

                    if(mAdm!=null)
                        mAdm.run(mStockCode, r, cMap, mSdTime);
                    if(mAam!=null)
                        mAam.run(mStockCode, r, cMap, mSdTime);
                }
                r.append2File(sAnalysisFile);
            }
        }
    }
    public void writeRange(int start, int end, AmRecord amr, 
            TrackExtreme ter, String sAnalysisFile, long closeTP) {
        writeRange(start, end, amr, ter, sAnalysisFile, closeTP, null, null);
    }

    public AmRecord loadPrevLastAmRecord(String tradeDate) {
        if(!mStockDates.contains(tradeDate)) {
            String msg = String.format("%s is not valid for %s\n", 
                    tradeDate, mStockCode);
            throw new RuntimeException(msg);
        }

        AmRecord nullAmr = new AmRecord(0,0,0,0,0);
        if(tradeDate.equals(mStockDates.firstDate()))
            return nullAmr;

        tradeDate = mStockDates.prevDate(tradeDate);
        String sAnalysisFile = StockPaths.getAnalysisFile(mStockCode, tradeDate);
        AmRecordLoader amLoader = new AmRecordLoader();
        ArrayList<AmRecord> amList = new ArrayList<AmRecord>();
        amLoader.load(amList, null, null, sAnalysisFile);

        if(amList.size() == 0)
            return nullAmr;

        return amList.get(amList.size()-1);
    }



    private String mStockCode;
    private StockDates mStockDates;
    private SdTime1 mSdTime;
    private AmDerManager mAdm;
    private AvgAmManager mAam;

    public static class TrackExtreme {
        public double maxUP;
        public double minDP;
        public double prevMaxUP;
        public double prevMinDP;

        public TrackExtreme() {
            this(new ArrayList<RawTradeDetails>());
        }
        public TrackExtreme(ArrayList<RawTradeDetails> rawDetailsList) {
            maxUP=Double.NEGATIVE_INFINITY; 
            prevMaxUP=Double.NaN;
            minDP=Double.POSITIVE_INFINITY; 
            prevMinDP=Double.NaN;
            if(rawDetailsList.size()>0) {
                RawTradeDetails r = rawDetailsList.get(0);
                prevMaxUP=r.price; prevMinDP=r.price;
            }
        }
        public void traceUp(double price) {
                if(price>=maxUP) {
                    maxUP = price;
                    prevMaxUP = maxUP;
                }
        }
        public void traceDown(double price) {
                if(price<=minDP) {
                    minDP = price;
                    prevMinDP = minDP;
                }
        }
        public void resetEx() {
                maxUP = Double.NEGATIVE_INFINITY;
                minDP = Double.POSITIVE_INFINITY;
        }
        public void setEx2Prev() {
            maxUP = maxUP==Double.NEGATIVE_INFINITY? prevMaxUP:maxUP;
            minDP = minDP==Double.POSITIVE_INFINITY? prevMinDP:minDP;
        }
    }



    public static double[][] getAmMatrix(String stockCode, String startDate, String hmsList) {
        AmManager am = new AmManager(stockCode, true);

        String[] sTradeDates = new TradeDates(stockCode, startDate).getAllDates();
        double[][] m = am.getAmMatrix(hmsList, sTradeDates);

        return m;
    }
    //[(startDate, startHMS), (endDate, endHMS)]
    public static NavigableMap<Integer, AmRecord> getItemMap(NavigableMap<Integer, AmRecord> amrMap, SdTime1 sdTime, 
            String startDate, String startHMS, String endDate, String endHMS) {
        long startTp = Time.getSpecificTime(startDate, startHMS);
        int startIdx = sdTime.getAbs(startTp);
        long endTp = Time.getSpecificTime(endDate, endHMS);
        int endIdx = sdTime.getAbs(endTp);

        NavigableMap<Integer, AmRecord> itemMap = amrMap.subMap(startIdx, true, endIdx, true);

        return itemMap;
    }
}
