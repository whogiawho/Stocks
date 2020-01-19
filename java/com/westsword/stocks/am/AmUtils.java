package com.westsword.stocks.am;

import java.util.*;

import com.westsword.stocks.Stock;
import com.westsword.stocks.Utils;
import com.westsword.stocks.RawTradeDetails;
import com.westsword.stocks.RawTradeDetailsList;
import com.westsword.stocks.utils.StockPaths;
import com.westsword.stocks.base.time.Time;
import com.westsword.stocks.base.time.SdTime1;
import com.westsword.stocks.base.time.StockDates;


public class AmUtils {
    public AmUtils(String stockCode) {
        mStockCode = stockCode;
        mSdTime = new SdTime1(stockCode);   //get sdStartDate&sdStartTime&interval from settings.txt
        mStockDates = new StockDates(stockCode);
    }

    public long writeAmRecords(long startAm, String tradeDate) {
        //load rawTradeDetails to rawDetailsList
        String sRawTradeDetailsFile = StockPaths.getRawTradeDetailsFile(mStockCode, tradeDate);
        RawTradeDetailsList rtdList = new RawTradeDetailsList();
        ArrayList<RawTradeDetails> rawDetailsList = new ArrayList<RawTradeDetails>();
        rtdList.load(rawDetailsList, sRawTradeDetailsFile);
        int lSize = rawDetailsList.size();
        //reset sAnalysisFile
        String sAnalysisFile = StockPaths.getAnalysisFile(mStockCode, tradeDate);
        Utils.deleteFile(sAnalysisFile);

        double maxUpPrice=Double.NEGATIVE_INFINITY;
        double minDownPrice=Double.POSITIVE_INFINITY;
        long am = startAm;
        long caeTp = Time.getSpecificTime(tradeDate, mSdTime.getCallAuctionEndTime());
        int caeSd = mSdTime.getAbs(caeTp);
        int prevSd = caeSd;
        for(int i=0; i<lSize; i++) {
            RawTradeDetails r = rawDetailsList.get(i);
            int rSd = mSdTime.getAbs(r.time);
            if(rSd != prevSd) {
                //write AmRecords between [prevSd, rSd) to sAnalysisFile
                writeRange(prevSd, rSd, am, maxUpPrice, minDownPrice, sAnalysisFile);

                prevSd = rSd;
                maxUpPrice=Double.NEGATIVE_INFINITY;
                minDownPrice=Double.POSITIVE_INFINITY;
            }
            if(r.type == Stock.TRADE_TYPE_UP) {
                am += r.count;
                maxUpPrice = r.price>=maxUpPrice? r.price:maxUpPrice;
            } else {
                am -= r.count;
                minDownPrice = r.price<=minDownPrice? r.price:minDownPrice;
            }
        }
        //last record
        if(lSize!=0)
            writeRange(prevSd, prevSd+1, am, maxUpPrice, minDownPrice, sAnalysisFile);

        return am;
    }
    private void writeRange(int start, int end, long am, double maxUpPrice, double minDownPrice, 
            String sAnalysisFile) {
        for(int i=start; i<end; i++) {
            long tp = mSdTime.getAbsTimePoint(i);
            //String tradeDate = Time.getTimeYMD(tp);
            //String tradeTime = Time.getTimeHMS(tp);
            String sFormat = "%-10x %8d %20d %8.3f %8.3f\n";
            if(maxUpPrice==Double.NEGATIVE_INFINITY)
                maxUpPrice = Double.NaN;
            if(minDownPrice==Double.POSITIVE_INFINITY)
                minDownPrice = Double.NaN;
            String line = String.format(sFormat, tp, i, am, maxUpPrice, minDownPrice);
            Utils.append2File(sAnalysisFile, line);
        }
    }

    public long loadPrevLastAm(String tradeDate) {
        long lastAm = 0;

        if(!mStockDates.contains(tradeDate)) {
            String msg = String.format("%s is not valid for %s\n", 
                    tradeDate, mStockCode);
            throw new RuntimeException(msg);
        }

        if(tradeDate.equals(mStockDates.firstDate()))
            return lastAm;

        tradeDate = mStockDates.prevDate(tradeDate);
        String sAnalysisFile = StockPaths.getAnalysisFile(mStockCode, tradeDate);
        AmRecordLoader amLoader = new AmRecordLoader();
        ArrayList<AmRecord> amList = new ArrayList<AmRecord>();
        amLoader.load(amList, null, sAnalysisFile);

        AmRecord r = amList.get(amList.size()-1);
        lastAm = r.am;
        //System.out.format("lastAm=%d\n", lastAm);

        return lastAm;
    }




    private String mStockCode;
    private StockDates mStockDates;
    private SdTime1 mSdTime;
}
