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
import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.analyze.ssanalyze.*;
import com.westsword.stocks.session.*;

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
    private final SimilarStackAnalyze mSsAnalyzeh0;
    private final SimilarStackAnalyze mSsAnalyzeh1;
    private final SimilarStackAnalyze mSsAnalyzeh2;

    private final SimilarStackAnalyze mSsAnalyze20160302_134500_140700_2;
    private final SimilarStackAnalyze mSsAnalyze20140416_093800_095600_2;
    private final SimilarStackAnalyze mSsAnalyze20110221_094900_130200_2;
    private final SimilarStackAnalyze mSsAnalyze20131017_105000_133200_2;
    private final SimilarStackAnalyze mSsAnalyze20180117_093900_105800_2;
    private final SimilarStackAnalyze mSsAnalyze20180413_110900_111200_2;

    private final SimilarStackAnalyze mSsAnalyze20150529_093900_105800_2;
    private final SimilarStackAnalyze mSsAnalyze20150410_112600_131500_2;
    private final SimilarStackAnalyze mSsAnalyze20160817_142300_144100_2;
    private final SimilarStackAnalyze mSsAnalyze20170922_092500_141100_2;
    private final SimilarStackAnalyze mSsAnalyze20190924_101700_103800_2;
    private final SimilarStackAnalyze mSsAnalyze20110104_095500_110900_3;
    private final THSQS iThsqs;

    private long mStartAm;
    private TreeMap<Integer, AmRecord> mAmRecordMap;


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
        mTsMan = new TradeSessionManager(stockCode, tradeDate);
        mTsMan.check2SubmitSession();
        //set mSsAnalyzeh[012] etc
        mSsAnalyzeh0 = new SimilarStackAnalyze(stockCode, "h0");
        mSsAnalyzeh1 = new SimilarStackAnalyze(stockCode, "h1");
        mSsAnalyzeh2 = new SimilarStackAnalyze(stockCode, "h2");
        mSsAnalyze20160302_134500_140700_2 = new SimilarStackAnalyze(stockCode, "20160302_134500_140700_2");
        mSsAnalyze20140416_093800_095600_2 = new SimilarStackAnalyze(stockCode, "20140416_093800_095600_2");
        mSsAnalyze20110221_094900_130200_2 = new SimilarStackAnalyze(stockCode, "20110221_094900_130200_2");
        mSsAnalyze20131017_105000_133200_2 = new SimilarStackAnalyze(stockCode, "20131017_105000_133200_2");
        mSsAnalyze20180117_093900_105800_2 = new SimilarStackAnalyze(stockCode, "20180117_093900_105800_2");
        mSsAnalyze20180413_110900_111200_2 = new SimilarStackAnalyze(stockCode, "20180413_110900_111200_2");
        mSsAnalyze20150529_093900_105800_2 = new SimilarStackAnalyze(stockCode, "20150529_093900_105800_2");
        mSsAnalyze20150410_112600_131500_2 = new SimilarStackAnalyze(stockCode, "20150410_112600_131500_2");
        mSsAnalyze20160817_142300_144100_2 = new SimilarStackAnalyze(stockCode, "20160817_142300_144100_2");
        mSsAnalyze20170922_092500_141100_2 = new SimilarStackAnalyze(stockCode, "20170922_092500_141100_2");
        mSsAnalyze20190924_101700_103800_2 = new SimilarStackAnalyze(stockCode, "20190924_101700_103800_2");
        mSsAnalyze20110104_095500_110900_3 = new SimilarStackAnalyze(stockCode, "20110104_095500_110900_3");

        mSsAnalyzeh0.setTradeSessionManager(mTsMan);
        mSsAnalyzeh1.setTradeSessionManager(mTsMan);
        mSsAnalyzeh2.setTradeSessionManager(mTsMan);
        mSsAnalyze20160302_134500_140700_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20140416_093800_095600_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20110221_094900_130200_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20131017_105000_133200_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20180117_093900_105800_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20180413_110900_111200_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20150529_093900_105800_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20150410_112600_131500_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20160817_142300_144100_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20170922_092500_141100_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20190924_101700_103800_2.setTradeSessionManager(mTsMan);
        mSsAnalyze20110104_095500_110900_3.setTradeSessionManager(mTsMan);
        //set iThsqs
        iThsqs = new THSQS();

        //set mStartAm
        mStartAm = mAmu.loadPrevLastAm(tradeDate);
        //set mAmRecordMap
        mAmRecordMap = new TreeMap<Integer, AmRecord>();
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
        bCallAuctionComplete = callAuctionCompleted(rawDetailsList, bCallAuctionComplete);

        processRawTradeDetails(mIndexs, rawDetailsList, mAmRecordMap);
        processRawPankou(mIndexs, rawPankouList);

        doTsManStuff(mAmRecordMap, mTsMan);

        
        long startT=System.currentTimeMillis();
        mSsAnalyzeh0.analyze(mAmRecordMap);
        mSsAnalyzeh1.analyze(mAmRecordMap);
        mSsAnalyzeh2.analyze(mAmRecordMap);
        mSsAnalyze20150529_093900_105800_2.analyze(mAmRecordMap);
        mSsAnalyze20150410_112600_131500_2.analyze(mAmRecordMap);
        mSsAnalyze20160817_142300_144100_2.analyze(mAmRecordMap);
        mSsAnalyze20170922_092500_141100_2.analyze(mAmRecordMap);
        mSsAnalyze20190924_101700_103800_2.analyze(mAmRecordMap);
        mSsAnalyze20110104_095500_110900_3.analyze(mAmRecordMap);
        if(!bNoPerformanceLog) {
            long endT = System.currentTimeMillis();
            System.out.format("%s %s: ssanalyze duration=%d\n", 
                    Utils.getCallerName(getClass()), Time.current(), endT-startT);
        }

        refreshTHSQS(rawPankouList);

        if(isLastRawTradeDetailHandled()||isLastPankouHandled()) {
            System.out.format("%s: isLastRawTradeDetailHandled=%b, isLastPankouHandled=%b\n", 
                    Utils.getCallerName(getClass()), isLastRawTradeDetailHandled(), isLastPankouHandled());
            mTsMan.checkAbnormalSubmittedSessions(true);
        }
    }

    private void writeRange(int start, int end, long am, AmUtils.TrackExtreme ter, 
            String sAnalysisFile, long closeTP, TreeMap<Integer, AmRecord> amrMap) {
        mAmu.writeRange(start, end, am, mTer, 
                mAnalysisFile, mCloseTP, amrMap);
    }
    private void processRawTradeDetails(int[] indexs, ArrayList<RawTradeDetails> rawDetailsList, 
            TreeMap<Integer, AmRecord> amrMap) {
        int last = indexs[LAST_RAW_DETAILS_IDX];
        int current = rawDetailsList.size()-1;
        //prevSd starts from LAST_RAW_DETAILS_IDX; which can be -1(invalid) or valid ones
        int prevSd = getSdTime(last, rawDetailsList);
        long am = mStartAm;

        while(current > last) {
            last++;
            RawTradeDetails r = rawDetailsList.get(last);
            int rSd = mSdTime.getAbs(r.time);
            if(rSd != prevSd) {
                //skip writeRange if prevSd==-1
                if(prevSd != -1)
                    writeRange(prevSd, rSd, am, mTer, 
                            mAnalysisFile, mCloseTP, amrMap);
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


    private boolean callAuctionCompleted(ArrayList<RawTradeDetails> rawDetailsList, 
            boolean completed) {
        //get the ratio of ab/(ab+as) when call auction completes
        if(!completed && rawDetailsList.size() != 0) {
            RawTradeDetails current = rawDetailsList.get(rawDetailsList.size()-1);
            long callAuctionTime = AStockSdTime.getCallAuctionEndTime(current.time);

            if(current.time >= callAuctionTime) {
                int asSum=0, abSum=0;
                //only include those tradeDetails <= callAuctionTime
                //sum as&ab during callAuction;
                for(int i=0; i<rawDetailsList.size(); i++) {
                    RawTradeDetails element = rawDetailsList.get(i);
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

                String sFormat = "\nCallAuction Completed! ab/(ab+as) = %8.3f%%\n";
                System.out.format(sFormat, (double)abSum/(abSum+asSum)*100);
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


