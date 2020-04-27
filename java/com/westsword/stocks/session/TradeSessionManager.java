package com.westsword.stocks.session;

import java.util.*;

import com.westsword.stocks.am.AmRecord;
import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.analyze.ssanalyze.SSTableRecord;

//key methods:
//  check2OpenSession                           check to open new sessions as needed
//  check2CloseSession                          check if it is time to close open&normal session
//  checkAbnormalSubmittedSessions              check if there are 已成 entrust and close such sessions
//  check2SubmitSession                         submit abnormal sessions before 09:25:00
//  makeRRP                                     make RRP trade at 14:56:00
public class TradeSessionManager {
    public final static int MAX_OPEN_SESSION = 1;
    public final static double TARGET_YEAR_RATE = 0.20;

    private String mStockCode;
    private String mTradeDate;

    private ArrayList<TradeSession> mTradeSessionList = null;
    private int mOpenSession;
    private boolean mBOnlyLongTradeSession = false;

    public TradeSessionManager(String stockCode, String tradeDate) {
        mStockCode = stockCode;
        mTradeDate = tradeDate;

        mTradeSessionList = new ArrayList<TradeSession>();
        TradeSession.loadTradeSessions(mTradeSessionList, true);
        System.out.format("%s: mTradeSessionList.size=%d\n", 
                Utils.getCallerName(getClass()), mTradeSessionList.size());

        mOpenSession = 0;
        mBOnlyLongTradeSession = Settings.getSwitch(Settings.ONLY_LONG_TRADESESSION);
    }

    //state: c->b c->d
    //take a look at TradeSession.java for more
    public void checkAbnormalSubmittedSessions() {
        System.out.format("%s: entering\n", Utils.getCallerName(getClass()));

        if(!Utils.isMarketOff())
            return;

        ArrayList<TradeSession> removedList = new ArrayList<TradeSession>();
        THSQS iThsqs = new THSQS();
        for(int i=0; i<mTradeSessionList.size(); i++) {
            TradeSession s = mTradeSessionList.get(i);
            if(s.getStockCode().equals(mStockCode) && s.isOpen() && !s.isNormal() && s.isSubmitted()) {
                //those Open&Abnormal&Submitted sessions, close them or reset them to Idle
                String sEntrustNO = s.getEntrustNO();
                String state = iThsqs.queryEntrustState(sEntrustNO);
                if(state.equals(iThsqs.getStringEntrustTraded())) {
                    entrustComplete(sEntrustNO, s, iThsqs, removedList);
                } else {
                    entrustUnchange(s, iThsqs);
                }
                /*
                if(state.equals("已撤"))
                    System.out.format("%s: %s canceled!\n", Utils.getCallerName(getClass()), sEntrustNO);
                */
            }
        }

        remove(removedList);
    }
    private void entrustComplete(String sEntrustNO, TradeSession s, THSQS iThsqs, 
            ArrayList<TradeSession> removedList) {
        double outPrice = iThsqs.queryEntrustAvgPrice(sEntrustNO);

        s.setOutPrice(outPrice);
        s.setActualOutPrice(outPrice);
        String tradeTime = iThsqs.queryTradedTime(sEntrustNO);
        long outTp = Time.getSpecificTime(mTradeDate, tradeTime);
        s.setActualOutHexTimePoint(outTp);

        s.setClose();
        s.setEntrustNO("");
        s.setIdle();
        s.save();
        //remove the original open file 
        s.removeOpenSessionFile();
        removedList.add(s);
                    
        String line = String.format("%s: %x was closed\n", 
                Utils.getCallerName(getClass()), s.getInHexTimePoint());
        System.out.format("%s", line);
    }
    private void entrustUnchange(TradeSession s, THSQS iThsqs) {
        //cancel entrust

        s.setEntrustNO("");
        s.setIdle();
        s.save();

        String line = String.format("%s: %x keeps unchanged\n", 
                Utils.getCallerName(getClass()), s.getInHexTimePoint());
        System.out.format("%s", line);
    }


    //state: b->c
    //take a look at TradeSession.java for more
    public void check2SubmitSession() {
        System.out.format("%s: entering\n", Utils.getCallerName(getClass()));

        if(Utils.isOfflineRun())
            return;

        for(int i=0; i<mTradeSessionList.size(); i++) {
            TradeSession s = mTradeSessionList.get(i);

            if(s.getOutHexTimePoint()!=null) {//for outHexTimePoint!=null
                if(ok2Submit(s, mStockCode)) {
                    //submit Open&Abnormal&Idle sessions and change Idle->Submitted
                    double outPrice = s.getAbnormalOutPrice();
                    submitTradeSession(s, mStockCode, outPrice);
                }
            } else {//for targetProfit!=null
                if(ok2Submit(s, mStockCode)) {
                    double outPrice = s.getTargetOutPrice();
                    submitTradeSession(s, mStockCode, outPrice);
                }
            }
        }
    }
    private boolean ok2Submit(TradeSession s, String stockCode) {
        return s.getStockCode().equals(stockCode) && s.isOpen() && !s.isNormal() && !s.isSubmitted();
    }
    private void submitTradeSession(TradeSession s, String stockCode, double outPrice) {
        String line = String.format("%s: submitted %x\n", 
                Utils.getCallerName(getClass()), s.getInHexTimePoint());
        System.out.format("%s", line);

        int amount = s.getTradeVolume();
        THSQS iThsqs = new THSQS();
        String sEntrustNO = "";
        int tradeType = s.getTradeType();
        if(tradeType == Stock.TRADE_TYPE_LONG)
            sEntrustNO = iThsqs.sell(stockCode, outPrice, amount);
        else
            sEntrustNO = iThsqs.buy(stockCode, outPrice, amount);
        s.setEntrustNO(sEntrustNO);
        s.setSubmitted();
        s.save();
    }

    private boolean bRRPDone = false;
    public void makeRRP(AmRecord item) {
        //System.out.format("%s: entering\n", Utils.getCallerName(getClass()));

        if(bRRPDone)
            return;
        if(Utils.isOfflineRun())
            return;

        long currentItemTp = item.hexTimePoint;
        long rrpTime = AStockSdTime.getRrpTime(currentItemTp);
        if(currentItemTp<rrpTime)
            return;

        THSQS iThsqs = new THSQS();

        //make rrp trade
        String rrpCode="131810";
        double avaiRemains = iThsqs.getAvaiRemains();
        int amount = (int)(avaiRemains/1000)*10;
        double price = 1.00;
        if(amount<=0) {
            System.out.format("%s: not enough balance\n", 
                    Utils.getCallerName(getClass()));
            return;
        }

        String sEntrustNO = iThsqs.marketSell(rrpCode, price, amount);
        double actualOutPrice = iThsqs.wait4EntrustPrice(sEntrustNO);
        System.out.format("%s: completed with rrpCode=%s, price=%8.3f amount=%d\n", 
                Utils.getCallerName(getClass()), rrpCode, actualOutPrice, amount);

        bRRPDone = true;
    }
    //state: a->b b->c a->e e->d
    //take a look at TradeSession.java for more
    public void check2CloseSession(AmRecord item) {
        //System.out.format("%s: entering\n", Utils.getCallerName(getClass()));

        if(Utils.isOfflineRun())
            return;

        ArrayList<TradeSession> removedList = new ArrayList<TradeSession>();
        long currentItemTp = item.hexTimePoint;
        for(int i=0; i<mTradeSessionList.size(); i++) {
            TradeSession s = mTradeSessionList.get(i);
            //check to close those sessions(Open&Normal&Idle) which reach outHexTimePoint and win
            if(s.getStockCode().equals(mStockCode) && s.isOpen() && s.isNormal() && !s.isSubmitted()) {
                Long outTp = s.getOutHexTimePoint();
                if(outTp!=null) {
                    if(!isTime2Close(currentItemTp, outTp))
                        continue;

                    int tradeType = s.getTradeType();
                    double outPrice = item.getOutPrice(tradeType);
                    double thresPrice = s.getAbnormalOutPrice(currentItemTp);
                    if(tradeType == Stock.TRADE_TYPE_LONG && outPrice >= thresPrice) {
                        //do marketSell for this session
                        sellAndcloseTradeSession(s, removedList, outPrice, currentItemTp);
                    } else if(tradeType == Stock.TRADE_TYPE_SHORT && outPrice <= thresPrice) {
                        //do marketBuy for this session
                        buyAndcloseTradeSession(s, removedList, outPrice, currentItemTp);
                    } else {
                        setTradeSessionAbnormal(s, thresPrice);
                    }
                }
            }
        }

        remove(removedList);
    }
    private boolean isTime2Close(long currentItemTp, long outTp) {
        boolean bClose = true;

        if(currentItemTp<outTp)
            bClose=false;

        return bClose;
    }
    private void setTradeSessionAbnormal(TradeSession s, double thresPrice) {
        //set Abnormal
        s.setAbnormal();
        //submit Open&Abnormal&Idle sessions and change Idle->Submitted
        submitTradeSession(s, mStockCode, thresPrice);

        String line = String.format("%s: set %x abnormal and submitted it", 
                Utils.getCallerName(getClass()), s.getInHexTimePoint());
        System.out.format("%s", line);
    }

    private void remove(ArrayList<TradeSession> removedList) {
        for(int i=0; i<removedList.size()-1; i++) {
            TradeSession s = removedList.get(i);
            mTradeSessionList.remove(s);
        }
    }
    private void tradeAndCloseTradeSession(TradeSession s, ArrayList<TradeSession> removedList, 
            int tradeType, double outPrice, long currentItemTp) {
        s.setSubmitted();
        int amount = s.getTradeVolume();
        THSQS iThsqs = new THSQS();
        String sEntrustNO = "";
        if(tradeType == Stock.TRADE_TYPE_LONG)
            sEntrustNO = iThsqs.marketSell(s.getStockCode(), outPrice, amount);
        else
            sEntrustNO = iThsqs.marketBuy(s.getStockCode(), outPrice, amount);
        //query actualOutPrice
        double actualOutPrice = iThsqs.wait4EntrustPrice(sEntrustNO);

        s.setOutPrice(outPrice);
        s.setActualOutPrice(actualOutPrice);
        s.setActualOutHexTimePoint(currentItemTp);

        s.setClose();
        s.setIdle();
        s.setEntrustNO("");
        s.save();
        //remove the original open file 
        s.removeOpenSessionFile();
        removedList.add(s);

        String line = String.format("%s: close %x", 
                Utils.getCallerName(getClass()), s.getInHexTimePoint());
        System.out.format("%s", line);
    }
    private void buyAndcloseTradeSession(TradeSession s, ArrayList<TradeSession> removedList, 
            double outPrice, long currentItemTp) {
        tradeAndCloseTradeSession(s, removedList, Stock.TRADE_TYPE_SHORT, outPrice, currentItemTp);
    }
    private void sellAndcloseTradeSession(TradeSession s, ArrayList<TradeSession> removedList, 
            double outPrice, long currentItemTp) {
        tradeAndCloseTradeSession(s, removedList, Stock.TRADE_TYPE_LONG, outPrice, currentItemTp);
    }



    //state: null->a
    //take a look at TradeSession.java for more
    public void check2OpenSession(SSTableRecord r, AmRecord item, String sName) {
        //System.out.format("%s: entering\n", Utils.getCallerName(getClass()));

        if(Utils.isOfflineRun())
            return;

        //only consider Long
        if(mBOnlyLongTradeSession) {
            if(r.tradeType != Stock.TRADE_TYPE_LONG)
                return;
        }
        //only consider sName==h0
        //if(!sName.equals("h0"))
        //    return;
        //its tradeSession has been opened, just skip it
        if(r.getSessionOpened())
            return;

        if(mOpenSession < MAX_OPEN_SESSION) {
            //open a new TradeSession
            TradeSession s = openTradeSession(item, r);
            if(s!=null) {
                mTradeSessionList.add(s);
                r.setSessionOpened(true);
                mOpenSession++;
            }
        }
    }
    private boolean enoughBalance(THSQS iThsqs, double inPrice, int tradeVol) {
        double minBalance = Trade.getMinBuyFee(inPrice, tradeVol);
        double currentAvai = iThsqs.getAvaiRemains();
        return currentAvai>minBalance?true:false;
    }
    private boolean enoughVolume(THSQS iThsqs, String stockCode, int tradeVol) {
        int currentAvai = iThsqs.getStockAvaiVols(stockCode);
        return currentAvai>=tradeVol?true:false;
    }
    private TradeSession openTradeSession(AmRecord item, SSTableRecord r) {
        long inHexTimePoint = item.hexTimePoint;
        int tradeType = r.tradeType;
        int sTDistance = r.sTDistance;

        THSQS iThsqs = new THSQS();
        double inPrice = item.getInPrice(tradeType);
        int tradeVol = r.tradeCount;
        System.out.format("%s: tradeVol=%d tradeType=%d!\n", 
                Utils.getCallerName(getClass()), tradeVol, tradeType);

        //do marketBuy|marketSell and get entrustNO
        String sEntrustNO = "";
        if(tradeType == Stock.TRADE_TYPE_LONG) {
            //check if there is enough balance for this trade
            if(!enoughBalance(iThsqs, inPrice, tradeVol)) {
                System.out.format("%s: not enough balance to buy!\n", 
                        Utils.getCallerName(getClass()));
                return null;
            }
            sEntrustNO = iThsqs.marketBuy(mStockCode, inPrice, tradeVol);
        } else {
            //check if there is enough stock's volume for this trade
            if(!enoughVolume(iThsqs, mStockCode, tradeVol)) {
                System.out.format("%s: not enough volume to sell!\n", 
                        Utils.getCallerName(getClass()));
                return null;
            }
            sEntrustNO = iThsqs.marketSell(mStockCode, inPrice, tradeVol);
        }
        //query actualInPrice
        double actualInPrice = iThsqs.wait4EntrustPrice(sEntrustNO);

        TradeSession s = new TradeSession();
        s.open(inHexTimePoint, inPrice, actualInPrice, tradeVol);
        s.setTargetYearRate(TARGET_YEAR_RATE);
        s.setMatchMode(mStockCode, r.sMatchExp, tradeType, sTDistance);
        setQuitConditions(s, r, tradeType, sTDistance);
        s.save();

        return s;
    }
    private void setQuitConditions(TradeSession s, SSTableRecord r, int tradeType, int sTDistance) {
        if(r.targetRate == Double.NaN) {
            //get outHexTimePoint per (mStockCode, r.tradeDate, r.hmsList, tradeType, sTDistance)
            long outHexTimePoint = getOutHexTimePoint(mStockCode, r, tradeType, sTDistance);
            s.setOutHexTimePoint(outHexTimePoint);
            s.setTargetProfit(Double.NaN);
        } else {
            s.setOutHexTimePoint(null);
            s.setTargetProfit(Trade.getTargetProfit(r.targetRate, s.getInPrice()));

            //always abnormal for fixed targetProfit
            s.setAbnormal();
            //submit s if Short
            if(tradeType==Stock.TRADE_TYPE_SHORT) {
                submitTradeSession(s, mStockCode, s.getTargetOutPrice());
            }
        }
    }
    private long getOutHexTimePoint(String stockCode, SSTableRecord r, int tradeType, int sTDistance) {
        long outTp = -1;

        return outTp;
    }

}
