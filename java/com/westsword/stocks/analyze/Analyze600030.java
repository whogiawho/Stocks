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
 
 
package com.westsword.stocks.analyze;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.session.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.analyze.ss.*;
import com.westsword.stocks.analyze.avgam.*;

public class Analyze600030 {
    public final static long THSQS_REFRESH_INTERVAL = 600;
    public final static int LAST_RAW_DETAILS_IDX   = 0;
    public final static int LAST_RAW_PANKOU_IDX    = 1;

    private final RealtimeAnalyze mRTAnalyzeFrame;
    private final String mAnalysisFile;
    private final long mCloseTP;
    private final SdTime1 mSdTime;
    private final AmUtils mAmu;
    private final AmUtils.TrackExtreme mTer;
    private final int[] mIndexs = {
        -1,               //raw details idx processed last time               LAST_RAW_DETAILS_IDX
        -1,               //raw pankou idx processed last time                LAST_RAW_PANKOU_IDX 
    };
    private final TradeSessionManager mTsMan;

    private final SimilarStackAnalyze[] mSsAnalyze;
    private final AvgAmAnalyze mAvgAmAnalyze;

    private final THSQS iThsqs;

    private long mStartAm;
    private TreeMap<Integer, AmRecord> mAmRecordMap;
    private TreeMap<Integer, AmRecord> mPrevAmRecordMap;
    private TreeMap<Integer, AmRecord> mPrevAmRecordMap4CAC;

    private AmRateViewer mAmRateViewer;

    Analyze600030(RealtimeAnalyze rtAnalyzeFrame) {
        String stockCode = Settings.getStockCode();
        String tradeDate = Settings.getTradeDate();

        mRTAnalyzeFrame = rtAnalyzeFrame;
        //set mAnalysisFile, and reset it
        mAnalysisFile = getAnalysisFile(mRTAnalyzeFrame);
        Utils.deleteFile(mAnalysisFile);
        //set mCloseTP
        mCloseTP = Time.getSpecificTime(tradeDate, AStockSdTime.getCloseQuotationTime());

        //set mSdTime
        mSdTime = new SdTime1(stockCode);
        //set mAmu
        mAmu = new AmUtils(stockCode);
        //set mTer
        mTer = new AmUtils.TrackExtreme();
        //set mTsMan
        mTsMan = new TradeSessionManager(stockCode);
        if(Settings.getSwitch(Settings.CHECK_2_SUBMIT_SESSION))
            mTsMan.check2SubmitSession();
        //set mSsAnalyzeh[012] etc
        String[] sSSTable = SSTable.getTableNames();
        mSsAnalyze = new SimilarStackAnalyze[sSSTable.length];
        for(int i=0; i<sSSTable.length; i++) {
            mSsAnalyze[i] = new SimilarStackAnalyze(stockCode, sSSTable[i], mSdTime);
            mSsAnalyze[i].setTradeSessionManager(mTsMan);
        }
        //avgam analyze
        mAvgAmAnalyze = new AvgAmAnalyze(stockCode, mSdTime);
        mAvgAmAnalyze.setTradeSessionManager(mTsMan);

        //set iThsqs
        iThsqs = new THSQS();

        //set mStartAm
        mStartAm = mAmu.loadPrevLastAm(tradeDate);
        //set mAmRecordMap
        mAmRecordMap = new TreeMap<Integer, AmRecord>();

        initAmDerAndAvgAm(stockCode, tradeDate);

        mAmRateViewer = new AmRateViewer();
        //start amrateviewer 
        mAmRateViewer.start();
    }
    private void initAmDerAndAvgAm(String stockCode, String tradeDate) {
        //amderivative||avgam
        int amderSdbw = Settings.getAmDerBackwardSd();
        int avgamSdbw = Settings.getAvgAmBackwardSd();
        int sdbw = Math.max(amderSdbw, avgamSdbw);
        AmManager amm = AmManager.get(stockCode, tradeDate, AStockSdTime.getCallAuctionEndTime(), sdbw, null);
        if(Settings.getSwitch(Settings.AM_DERIVATIVE)||Settings.getSwitch(Settings.AVGAM)) {
            mPrevAmRecordMap = new TreeMap<Integer, AmRecord>(amm.getAmRecordMap());
            mPrevAmRecordMap4CAC = new TreeMap<Integer, AmRecord>(mPrevAmRecordMap);
        } else {
            mPrevAmRecordMap = null;
            mPrevAmRecordMap4CAC = null;
        }
        //mkdir derivative&derivativePng
        Utils.mkDir(StockPaths.getDerivativeDir(stockCode, tradeDate));
        Utils.mkDir(StockPaths.getDerivativePngDir(stockCode, tradeDate));
        //mkdir avgam&avgamPng
        Utils.mkDir(StockPaths.getAvgAmDir(stockCode, tradeDate));
        Utils.mkDir(StockPaths.getAvgAmPngDir(stockCode, tradeDate));
    }

    private long prevRefreshTp=0;
    private void refreshTHSQS(ArrayList<RawRTPankou> rawPankouList) {
        if(Utils.isOfflineRun())
            return;

        int pankouListSize = rawPankouList.size();
        if(pankouListSize != 0) {
            long currentTp=rawPankouList.get(pankouListSize-1).mSecondsFrom1970Time;
            if(currentTp-prevRefreshTp>THSQS_REFRESH_INTERVAL) {
                System.out.format("%s: %s\n", 
                        Utils.getCallerName(getClass()), Time.getTimeYMDHMS(currentTp));
                iThsqs.refresh();
                prevRefreshTp = currentTp;
            }
        }
    }

    private void doTsManStuff(TreeMap<Integer, AmRecord> amrMap, TradeSessionManager tsMan) {
        int mapSize = amrMap.size();
        if(mapSize != 0) {
            Integer key = amrMap.lastKey();
            AmRecord r = amrMap.get(key);
            /*
            System.out.format("%s: %s mapSize=%d\n", 
                    Utils.getCallerName(getClass()), Time.getTimeYMDHMS(r.hexTimePoint), mapSize);
            */
            //check if there is a session that should be closed
            tsMan.check2CloseSession(r);
            //check if it is time to makeRRP
            tsMan.makeRRP(r);
        }
    }


    private boolean bNoPerformanceLog = Settings.getSwitch(Settings.NO_PERFORMANCE_LOG);
    private boolean bCallAuctionComplete = false;
    public void startAnalyze(ArrayList<RawTradeDetails> rawDetailsList, 
            ArrayList<RawRTPankou> rawPankouList) {
        bCallAuctionComplete = callAuctionCompleted(bCallAuctionComplete, rawDetailsList, rawPankouList);

        processRawTradeDetails(mIndexs, rawDetailsList, mAmRecordMap, mPrevAmRecordMap);
        processRawPankou(mIndexs, rawPankouList);

        doTsManStuff(mAmRecordMap, mTsMan);

        
        long startT=System.currentTimeMillis();
        for(int i=0; i<mSsAnalyze.length; i++) {
            mSsAnalyze[i].analyze(mAmRecordMap);
        }
        if(!bNoPerformanceLog) {
            long endT = System.currentTimeMillis();
            System.out.format("%s %s: ssanalyze duration=%d\n", 
                    Utils.getCallerName(getClass()), Time.current(), endT-startT);
        }

        refreshTHSQS(rawPankouList);

        if(isLastRawTradeDetailHandled()||isLastPankouHandled()) {
            System.out.format("%s: isLastRawTradeDetailHandled=%b, isLastPankouHandled=%b\n", 
                    Utils.getCallerName(getClass()), isLastRawTradeDetailHandled(), isLastPankouHandled());
            //in case that makeRRP is not called at doTsManStuff, call it here
            mTsMan.makeRRP(mAmRecordMap.get(mAmRecordMap.lastKey()), false);

            mTsMan.checkAbnormalSubmittedSessions(true);
            //stop the copyManager
            mAmu.stopCopyManager();
        }
    }

    private void debugPrint0(RawTradeDetails r) {
        //print r.time
        System.err.format("%s: %x\n", Utils.getCallerName(getClass()), r.time);
    }
    private void debugPrint1(int prevSd, int rSd) {
        //print
        long prevTp = mSdTime.rgetAbs(prevSd);
        long rTp = mSdTime.rgetAbs(rSd);
        System.err.format("%s: (%x, %x)\n", 
                Utils.getCallerName(getClass()), prevTp, rTp);
    }
    private void processRawTradeDetails(int[] indexs, ArrayList<RawTradeDetails> rawDetailsList, 
            TreeMap<Integer, AmRecord> amrMap, TreeMap<Integer, AmRecord> prevAmrMap) {
        int last = indexs[LAST_RAW_DETAILS_IDX];
        int current = rawDetailsList.size()-1;
        //prevSd starts from LAST_RAW_DETAILS_IDX; which can be -1(invalid) or valid ones
        int prevSd = getSdTime(last, rawDetailsList);
        long am = mStartAm;

        while(current > last) {
            last++;
            RawTradeDetails r = rawDetailsList.get(last);
            //debugPrint0(r);

            int rSd = mSdTime.getAbs(r.time);
            if(rSd != prevSd) {
                //skip writeRange if prevSd==-1
                if(prevSd != -1) {
                    //debugPrint1(prevSd, rSd);
                    mAmu.writeRange(prevSd, rSd, am, mTer, mAnalysisFile, mCloseTP, 
                            amrMap, prevAmrMap);
                    //avgam analyze
                    mAvgAmAnalyze.analyze(prevSd, rSd, mCloseTP, prevAmrMap);
                }
                prevSd = rSd;
                mTer.resetEx();
            }
            if(r.type == Stock.TRADE_TYPE_UP) {
                am += r.count;
                mTer.traceUp(r.price);
            } else {
                am -= r.count;
                mTer.traceDown(r.price);
            }
        }

        mStartAm = am;
        indexs[LAST_RAW_DETAILS_IDX] = current;
        checkLastRawTradeDetail(indexs, rawDetailsList);
    }

    private void processRawPankou(int[] indexs, ArrayList<RawRTPankou> rawPankouList) {
        int last = indexs[LAST_RAW_PANKOU_IDX];
        int current = rawPankouList.size()-1;
        while(current > last) {
            last++;
            handleRawPankou(rawPankouList.get(last));
        }

        indexs[LAST_RAW_PANKOU_IDX] = current;
        checkLastPankou(indexs, rawPankouList);
    }
    private void handleRawPankou(RawRTPankou raw) {
    }


    private boolean mLastRawTradeDetailHandled = false;
    public boolean isLastRawTradeDetailHandled() {
        return mLastRawTradeDetailHandled;
    }
    public void setLastRawTradeDetailHandled(boolean bLast) {
        mLastRawTradeDetailHandled = bLast;
    }
    private boolean mLastPankouHandled = false;
    public boolean isLastPankouHandled() {
        return mLastPankouHandled;
    }
    public void setLastPankouHandled(boolean bLast) {
        mLastPankouHandled = bLast;
    }
    //set it if LAST_RAW_PANKOU_IDX >15:00:00
    private void checkLastPankou(int[] indexs, ArrayList<RawRTPankou> rawPankouList) {
        int idx = indexs[LAST_RAW_PANKOU_IDX];
        if(idx != -1) {
            long timePt = rawPankouList.get(idx).mSecondsFrom1970Time;

            if(timePt > AStockSdTime.getCloseQuotationTime(timePt))
                setLastPankouHandled(true);
        }
    }
    //set it if LAST_RAW_DETAILS_IDX == 15:01:00
    private void checkLastRawTradeDetail(int[] indexs, ArrayList<RawTradeDetails> rawDetailsList) {
        int idx = indexs[LAST_RAW_DETAILS_IDX];
        if(idx != -1) {
            long timePt = rawDetailsList.get(idx).time;

            if(timePt == AStockSdTime.getLastRawTradeDetailTime(timePt))
                setLastRawTradeDetailHandled(true);
        }
    }


    private void abRate4CAC(ArrayList<RawTradeDetails> rawDetailsList, long callAuctionTime) {
        int asSum=0, abSum=0;
        //only include those tradeDetails <= callAuctionTime
        //sum as&ab during callAuction;
        for(int i=0; i<rawDetailsList.size(); i++) {
            RawTradeDetails element = rawDetailsList.get(i);
            if(Settings.getSwitch(Settings.SWITCH_OF_RAW_DATA)) {
                System.out.format("%x %8.3f %4d %4d\n", 
                        element.time, element.price, element.count, element.type);
            }
            if(element.time > callAuctionTime)
                break;

            if(element.type == Stock.TRADE_TYPE_UP)
                abSum += element.count;
            else if(element.type == Stock.TRADE_TYPE_DOWN)
                asSum += element.count;
            else
                System.out.format("%s: unsupported type = %d\n", 
                        Utils.getCallerName(getClass()), element.type);
        }

        double abRate = (double)abSum/(abSum+asSum)*100;
        String sFormat = "\nCallAuction Completed! ab=%d as=%d ab/(ab+as)=%-8.3f%%\n";
        System.out.format(sFormat, abSum, asSum, abRate);
    }
    //assuming it is called between [092530, 093000]
    private void avgamAnalyze4CAC(ArrayList<RawTradeDetails> rawDetailsList, long callAuctionTime, 
            TreeMap<Integer, AmRecord> amrMap) {
        if(Settings.getSwitch(Settings.AVGAM) && amrMap !=null) {
            //clone a new one
            AmRecord last = amrMap.lastEntry().getValue();

            //modify am with last AmRecord
            AmRecord r = AmRecord.get(rawDetailsList, callAuctionTime, mSdTime);
            r.am += last.am;
            int caeIdx = mSdTime.getAbs(callAuctionTime);
            amrMap.put(caeIdx, r);

            System.out.format("%s: caeIdx=%d lastIdx=%d callAuctionTime=%x sizeRP=%d am=%d\n", 
                    Utils.getCallerName(getClass()), caeIdx, last.timeIndex, 
                    callAuctionTime, rawDetailsList.size(), r.am);

            //avgam analyze
            mAvgAmAnalyze.analyze(caeIdx, caeIdx+1, mCloseTP, amrMap);
        }
    }
    private boolean callAuctionCompleted(boolean completed, 
            ArrayList<RawTradeDetails> rawDetailsList, ArrayList<RawRTPankou> rawPankouList) {
        //get the ratio of ab/(ab+as) when call auction completes
        if(!completed && rawDetailsList.size()!=0 && rawPankouList.size()!=0) {
            RawTradeDetails currentRTD = rawDetailsList.get(rawDetailsList.size()-1);
            long callAuctionTime = AStockSdTime.getCallAuctionEndTime(currentRTD.time);
            RawRTPankou currentRP = rawPankouList.get(rawPankouList.size()-1);
            long timeRTD = currentRTD.time;
            long timeRP = currentRP.mSecondsFrom1970Time;
            int sizeRTD = rawDetailsList.size();
            int sizeRP = rawPankouList.size();
            System.out.format("%s: sizeRTD=%d sizeRP=%d timeRTD=%x timeRP=%x callAuctionTime=%x\n",
                    Utils.getCallerName(getClass()), sizeRTD, sizeRP, timeRTD, timeRP, callAuctionTime);

            //maybe callAuctionTime+30 is not enough long
            if(timeRTD >= callAuctionTime && timeRP >= callAuctionTime+30) {
                abRate4CAC(rawDetailsList, callAuctionTime);

                //avgam analyze specially for 09:25:00
                avgamAnalyze4CAC(rawDetailsList, callAuctionTime, mPrevAmRecordMap4CAC);

                completed= true;
            }
        }

        return completed;
    }
    private String getAnalysisFile(RealtimeAnalyze frame) {
        String sAnalysisFile = frame.getAnalysisFile();

        if(!sAnalysisFile.equals("")) {
            return sAnalysisFile;
        }

        return StockPaths.getAnalysisFile();
    }
    private int getSdTime(int idx, ArrayList<RawTradeDetails> rawDetailsList) {
        if(idx == -1)
            return -1;

        RawTradeDetails r = rawDetailsList.get(idx);
        
        return mSdTime.getAbs(r.time); 
    }
}
