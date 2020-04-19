package com.westsword.stocks.analyze;


import java.util.*;

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.StockPaths;

public class Analyze600030 {
    public final static int LAST_RAW_DETAILS_IDX   = 0;
    public final static int LAST_RAW_PANKOU_IDX    = 1;

    private RealtimeAnalyze mRTAnalyzeFrame;
    private int[] mIndexs = {
        -1,               //raw details idx processed last time               LAST_RAW_DETAILS_IDX
        -1,               //raw pankou idx processed last time                LAST_RAW_PANKOU_IDX 
    };


    Analyze600030(RealtimeAnalyze rtAnalyzeFrame) {
        mRTAnalyzeFrame = rtAnalyzeFrame;

        //remove analysis.txt
        String outAnalysisFile = getAnalysisFile(mRTAnalyzeFrame);
        Utils.deleteFile(outAnalysisFile);

        //load am
    }


    private boolean bCallAuctionComplete = false;
    public void startAnalyze(ArrayList<RawTradeDetails> rawDetailsList, 
            ArrayList<RawRTPankou> rawPankouList) {
        bCallAuctionComplete = callAuctionCompleted(rawDetailsList, bCallAuctionComplete);

        processRawTradeDetails(mIndexs, rawDetailsList);
        processRawPankou(mIndexs, rawPankouList);

        if(isLastRawTradeDetailHandled()||isLastPankouHandled()) {
            System.out.format("%s: isLastRawTradeDetailHandled=%b, isLastPankouHandled=%b\n", 
                    Utils.getCallerName(getClass()), isLastRawTradeDetailHandled(), isLastPankouHandled());
        }
    }

    private void processRawTradeDetails(int[] indexs, ArrayList<RawTradeDetails> rawDetailsList) {
        int last = indexs[LAST_RAW_DETAILS_IDX];
        int current = rawDetailsList.size()-1;
        while(current > last) {
            last++;
            handleRawTradeDetail(rawDetailsList.get(last));
        }

        indexs[LAST_RAW_DETAILS_IDX] = current;
        checkLastRawTradeDetail(indexs, rawDetailsList);
    }
    private void handleRawTradeDetail(RawTradeDetails raw) {
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
        String sAnalyze1File = frame.getAnalysisFile();

        if(!sAnalyze1File.equals("")) {
            return sAnalyze1File;
        }

        return StockPaths.getAnalysisFile();
    }
}


