 /*
 Copyright (C) 1989-2020 Free Software Foundation, Inc.
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
 
 
package com.westsword.stocks.session;

import java.io.*;
import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.Time;
import com.westsword.stocks.base.utils.Trade;
import com.westsword.stocks.base.utils.StockPaths;


//possible combinations:
//     Submitted    Normal    Open
//a    0            1         1   
//b    0            0         1   
//c    1            0         1   
//d    X            X         0   
//e    1            1         1   
//state changes:
//1. a->b   a->e
//2. b->c 
//3. c->b   c->d 
//4. e->d

public class TradeSession {
    public final static String sKeyStockCode="stockCode";
    public final static String sKeyTargetYearRate="targetYearRate";
    public final static String sKeyInHexTimePoint="inHexTimePoint";
    public final static String sKeyOutHexTimePoint="outHexTimePoint";
    public final static String sKeyTargetProfit="targetProfit";
    public final static String sKeyState="state";
    public final static String sKeyInPrice="inPrice";
    public final static String sKeyTradeType="tradeType";
    public final static String sKeyTradeVol="tradeVol";
    public final static String sKeyActualInPrice="actualInPrice";
    public final static String sKeyEntrustNO="entrustNO";
    public final static String sKeyOutPrice="outPrice";
    public final static String sKeyActualOutPrice="actualOutPrice";
    public final static String sKeyActualOutHexTimePoint="actualOutHexTimePoint";

    public final static int OPEN = 0x1;
    public final static int NORMAL = 0x2;
    public final static int SUBMITTED = 0x4;


    private long mInHexTimePoint;               //5; inTime
    private double mInPrice;                    //7;
    private double mActualInPrice;              //10;
    private int mTradeVolume;                   //9;

    private double mTargetYearRate;             //4;

    private String mStockCode;                  //3;
    private String mMatchExp;
    private int mTradeType;                     //8;
    private int mT0;
    private Long mOutHexTimePoint;              //1; expected outTime for normal session
    private Double mTargetProfit;               //2; expected profit

    private int mState;                         //6; (submitted,idle)|(normal,abnormal)|(open,close)
    private String mEntrustNO;                  //11; used for abnormal&submitted session

    private double mOutPrice;                   //12;
    private double mActualOutPrice;             //13;
    private long mActualOutHexTimePoint;        //14; actual outTime


    public TradeSession() {
        mState = 0;
        mEntrustNO = "";

        mInPrice = Double.NaN;
        mActualInPrice = Double.NaN;
        mOutPrice = Double.NaN;
        mActualOutPrice = Double.NaN;
    }

    public double getAbnormalOutPrice(long current) {
        String currentDate = Time.getTimeYMD(current, false);
        String inDate = Time.getTimeYMD(mInHexTimePoint, false);

        if(mTradeType == Stock.TRADE_TYPE_LONG)
            return Trade.getMinSellPrice(mActualInPrice, mTradeVolume, mTargetYearRate, inDate, currentDate);
        else
            return Trade.getMaxBuyPrice(mActualInPrice, mTradeVolume, mTargetYearRate, inDate, currentDate);

    }
    public double getAbnormalOutPrice() {
        long currentTp = System.currentTimeMillis()/1000;
        return getAbnormalOutPrice(currentTp);
    }
    public double getTargetOutPrice() {
        double outPrice = Double.NaN;

        double targetProfit = getTargetProfit();
        if(mTradeType == Stock.TRADE_TYPE_LONG)
            outPrice = getInPrice() + targetProfit;
        else
            outPrice = getInPrice() - targetProfit;

        if(Settings.getSwitch(Settings.MAX_OUT_PRICE))
            return Math.max(outPrice, getAbnormalOutPrice());
        else
            return outPrice;
    }
    public void open(long inTime, double inPrice, double actualInPrice, int tradeVol) {
        mInHexTimePoint = inTime;
        mInPrice = inPrice;
        mActualInPrice = actualInPrice;
        mTradeVolume = tradeVol;

        //set state
        setOpen(); setNormal(); setIdle();
    }
    public void setTargetYearRate(double targetYearRate) {
        mTargetYearRate = targetYearRate;
    }
    public void setMatchMode(String stockCode, String matchExp, int tradeType, int sT) {
        mStockCode = stockCode;
        mMatchExp = matchExp;
        mTradeType = tradeType;
        mT0 = sT;
    }

    public void removeOpenSessionFile() {
        String sSessionFile = StockPaths.getTradeSessionFile(mInHexTimePoint, true);
        Utils.deleteFile(sSessionFile);
        System.out.format("%s: %s was removed!\n", Utils.getCallerName(getClass()), sSessionFile);
    }
    public void save() {
        String sSessionFile = StockPaths.getTradeSessionFile(mInHexTimePoint, isOpen());
        save(sSessionFile);
    }
    public void save(String sSessionFile) {
        Settings s = new Settings();

        //quit conditions
        s.setValue(sSessionFile, sKeyOutHexTimePoint, 
                mOutHexTimePoint==null?"":String.format("%x", mOutHexTimePoint));
        s.setValue(sSessionFile, sKeyTargetProfit, 
                mTargetProfit==null?"":String.format("%-8.3f", mTargetProfit));

        s.setValue(sSessionFile, sKeyStockCode, mStockCode);
        s.setValue(sSessionFile, sKeyTargetYearRate, String.format("%-8.3f", mTargetYearRate));
        s.setValue(sSessionFile, sKeyInHexTimePoint, String.format("%x", mInHexTimePoint));

        s.setValue(sSessionFile, sKeyState, ""+mState);
        s.setValue(sSessionFile, sKeyInPrice, String.format("%-8.3f", mInPrice));
        s.setValue(sSessionFile, sKeyTradeType, ""+mTradeType);
        s.setValue(sSessionFile, sKeyTradeVol, ""+mTradeVolume);
        s.setValue(sSessionFile, sKeyActualInPrice, String.format("%-8.3f", mActualInPrice));
        s.setValue(sSessionFile, sKeyEntrustNO, mEntrustNO);
        s.setValue(sSessionFile, sKeyOutPrice, String.format("%-8.3f", mOutPrice));
        s.setValue(sSessionFile, sKeyActualOutPrice, String.format("%-8.3f", mActualOutPrice));
        s.setValue(sSessionFile, sKeyActualOutHexTimePoint, String.format("%x", mActualOutHexTimePoint));
    }
    public void load(String sSessionFile) {
        Settings s = new Settings();

        mOutHexTimePoint = s.getLong(sSessionFile, sKeyOutHexTimePoint, 16);
        mTargetProfit = s.getDouble(sSessionFile, sKeyTargetProfit);

        mStockCode = s.getString(sSessionFile, sKeyStockCode);
        mTargetYearRate = s.getDouble(sSessionFile, sKeyTargetYearRate);
        mInHexTimePoint = s.getLong(sSessionFile, sKeyInHexTimePoint, 16);
        mState = s.getInteger(sSessionFile, sKeyState);
        mInPrice = s.getDouble(sSessionFile, sKeyInPrice);
        mTradeType = s.getInteger(sSessionFile, sKeyTradeType);
        mTradeVolume = s.getInteger(sSessionFile, sKeyTradeVol); 
        mActualInPrice = s.getDouble(sSessionFile, sKeyActualInPrice);
        mEntrustNO = s.getString(sSessionFile, sKeyEntrustNO);
        mOutPrice = s.getDouble(sSessionFile, sKeyOutPrice);
        mActualOutPrice = s.getDouble(sSessionFile, sKeyActualOutPrice);
        mActualOutHexTimePoint = s.getLong(sSessionFile, sKeyActualOutHexTimePoint, 16);
    }


    public void setOutPrice(double outPrice) {
        mOutPrice = outPrice;
    }
    public void setActualOutPrice(double actualOutPrice) {
        mActualOutPrice = actualOutPrice;
    }
    public void setActualOutHexTimePoint(long actualOutHexTimePoint) {
        mActualOutHexTimePoint = actualOutHexTimePoint;
    }
    public String getStockCode() {
        return mStockCode;
    }
    public int getTradeType() {
        return mTradeType;
    }
    public int getTradeVolume() {
        return mTradeVolume;
    }
    public String getEntrustNO() {
        return mEntrustNO;
    }
    public double getInPrice() {
        return mInPrice;
    }
    public void setEntrustNO(String sEntrustNO) {
        mEntrustNO = sEntrustNO;
    }
    public void setOutHexTimePoint(Long outHexTimePoint) {
        mOutHexTimePoint = outHexTimePoint;
    }
    public Long getOutHexTimePoint() {
        return mOutHexTimePoint;
    }
    public void setTargetProfit(Double targetProfit) {
        mTargetProfit = targetProfit;
    }
    public Double getTargetProfit() {
        return mTargetProfit;
    }
    public long getInHexTimePoint() {
        return mInHexTimePoint;
    }


    public void setIdle() {
        mState &= ~SUBMITTED;
    }
    public void setSubmitted() {
        mState |= SUBMITTED;
    }
    public void setAbnormal() {
        mState &= ~NORMAL;
    }
    public void setNormal() {
        mState |= NORMAL;
    }
    public void setClose() {
        mState &= ~OPEN;
    }
    public void setOpen() {
        mState |= OPEN;
    }
    public boolean isOpen() {
        return (mState&OPEN)==0?false:true;
    }
    public boolean isNormal() {
        return (mState&NORMAL)==0?false:true;
    }
    public boolean isSubmitted() {
        return (mState&SUBMITTED)==0?false:true;
    }

    public static void loadTradeSessions(ArrayList<TradeSession> sessionList, boolean bOpen) {
        String sessionDir = StockPaths.getTradeSessionDir(bOpen);
        System.out.format("%s: sessionDir=%s\n", 
                "TradeSession.loadTradeSessions", sessionDir);

        File fDir = new File(sessionDir);
        String[] sTradeSessions = fDir.list();

        if(sTradeSessions != null) {
            //System.out.format("size=%d\n", sTradeSessions.length);
            for(int i=0; i<sTradeSessions.length; i++) {
                //System.out.format("%s\n", sTradeSessions[i]);
                String sSessionFile = sessionDir + sTradeSessions[i];
                TradeSession s = new TradeSession();
                s.load(sSessionFile);
                sessionList.add(s);
            }
        }
    }


}
