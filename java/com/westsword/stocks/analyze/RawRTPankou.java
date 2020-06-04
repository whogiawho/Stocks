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
 
 
package com.westsword.stocks.analyze;

import java.io.*;
import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.Time;

public class RawRTPankou {
    public final static double INVALID_PRICE  = Double.NaN;
    public final static long   INVALID_VOLUME = Long.MAX_VALUE;

    public final static int PANKOU_LEVEL_NUMBER=10;

    public final static int SELL_10=0;
    public final static int SELL_9=1;
    public final static int SELL_8=2;
    public final static int SELL_7=3;
    public final static int SELL_6=4;
    public final static int SELL_5=5;
    public final static int SELL_4=6;
    public final static int SELL_3=7;
    public final static int SELL_2=8;
    public final static int SELL_1=9;

    public final static int BUY_1=0;
    public final static int BUY_2=1;
    public final static int BUY_3=2;
    public final static int BUY_4=3;
    public final static int BUY_5=4;
    public final static int BUY_6=5;
    public final static int BUY_7=6;
    public final static int BUY_8=7;
    public final static int BUY_9=8;
    public final static int BUY_10=9;

    public double[] mSellPrice = new double[10];
    public double[] mBuyPrice = new double[10];
    public long[] mSellVolume = new long[10];
    public long[] mBuyVolume = new long[10];

    public long       mSecondsFrom1970Time;
    public String     mStrTime;

    public double getUpPrice(int level) {
        double price = 0;

        if(level >= PANKOU_LEVEL_NUMBER)
            return Double.NaN;

        price = mSellPrice[level];

        return price;
    }
    public double getDownPrice(int level) {
        double price = 0;

        if(level >= PANKOU_LEVEL_NUMBER)
            return Double.NaN;

        price = mBuyPrice[level];

        return price;
    }
    //level: [0-9]
    public long getUpSupply(int level) {
        int supply = 0;

        if(level >= PANKOU_LEVEL_NUMBER)
            return supply;
        for(int i=PANKOU_LEVEL_NUMBER-1; i>=level; i--) {
            supply += mSellVolume[i];
        }

        return supply;
    }
    //level: [0-9]
    public long getDownSupply(int level) {
        int supply = 0;

        if(level >= PANKOU_LEVEL_NUMBER)
            return supply;
        for(int i=0; i<=level; i++) {
            supply += mBuyVolume[i];
        }

        return supply;
    }
    public long getUpSupplyOf10Level(){
        long sum = 0;
        for(int i=0; i<mSellVolume.length; i++) {
            sum += mSellVolume[i];
        }

        return sum;
    }
    public long getDownSupplyOf10Level(){
        long sum = 0;
        for(int i=0; i<mBuyVolume.length; i++) {
            sum += mBuyVolume[i];
        }

        return sum;
    }
    public String price2Str(double d) {
        String str; 

        Double d0 = Double.valueOf(d);
        if(d0.equals(INVALID_PRICE))
            str = "--";
        else
            str = String.format("%.3f", d);

        return str;
    }
    public String vol2Str(long l) {
        String str;

        if(l == INVALID_VOLUME)
            str = "--";
        else
            str = String.valueOf(l);

        return str;
    }
    //pankouString format
    //sell10-sell1 buy1-buy10 sellC10-sellC1 buyC1-buyC10 hexTimePoint
    public RawRTPankou(String pankouString){
        String[] s0;

        s0=pankouString.split(",");

        for(int i=0; i<PANKOU_LEVEL_NUMBER; i++) {
            try {
                mSellPrice[i] = Double.valueOf(s0[i]);
            } catch (NumberFormatException e) {
                mSellPrice[i] = INVALID_PRICE;
            }
        }

        for(int i=0; i<PANKOU_LEVEL_NUMBER; i++) {
            try {
                mBuyPrice[i] = Double.valueOf(s0[i+PANKOU_LEVEL_NUMBER]);   //buy1 buy2 buy3
            } catch (NumberFormatException e){
                mBuyPrice[i] = INVALID_PRICE;
            }
        }

        for(int i=0; i<PANKOU_LEVEL_NUMBER; i++) {
            try {
                mSellVolume[i] = Long.valueOf(s0[i+2*PANKOU_LEVEL_NUMBER]);
            } catch (NumberFormatException e) {
                mSellVolume[i] = INVALID_VOLUME;
            }
        }

        for(int i=0; i<PANKOU_LEVEL_NUMBER; i++) {
            try {
                mBuyVolume[i] = Long.valueOf(s0[i+3*PANKOU_LEVEL_NUMBER]);
            } catch (NumberFormatException e) {
                mBuyVolume[i] = INVALID_VOLUME;
            }
        }

        mSecondsFrom1970Time = Long.parseLong(s0[4*PANKOU_LEVEL_NUMBER], 16);

        mStrTime = Time.getTimeYMD(mSecondsFrom1970Time);
        mStrTime += " ";
        String hms = Time.getTimeHMS(mSecondsFrom1970Time);
        mStrTime += hms;

        boolean bSwitchOfRawData = Settings.getSwitch(Settings.SWITCH_OF_RAW_DATA);

        if(bSwitchOfRawData) {
            String sFormat = "%8s %8s %8s\n";
            String str = String.format("time=%s\n", mStrTime);
            str += String.format(sFormat, "sell10", price2Str(mSellPrice[SELL_10]), vol2Str(mSellVolume[SELL_10]));
            str += String.format(sFormat, "sell9",  price2Str(mSellPrice[SELL_9]), vol2Str(mSellVolume[SELL_9]));
            str += String.format(sFormat, "sell8",  price2Str(mSellPrice[SELL_8]), vol2Str(mSellVolume[SELL_8]));
            str += String.format(sFormat, "sell7",  price2Str(mSellPrice[SELL_7]), vol2Str(mSellVolume[SELL_7]));
            str += String.format(sFormat, "sell6",  price2Str(mSellPrice[SELL_6]), vol2Str(mSellVolume[SELL_6]));
            str += String.format(sFormat, "sell5",  price2Str(mSellPrice[SELL_5]), vol2Str(mSellVolume[SELL_5]));
            str += String.format(sFormat, "sell4",  price2Str(mSellPrice[SELL_4]), vol2Str(mSellVolume[SELL_4]));
            str += String.format(sFormat, "sell3",  price2Str(mSellPrice[SELL_3]), vol2Str(mSellVolume[SELL_3]));
            str += String.format(sFormat, "sell2",  price2Str(mSellPrice[SELL_2]), vol2Str(mSellVolume[SELL_2]));
            str += String.format(sFormat, "sell1",  price2Str(mSellPrice[SELL_1]), vol2Str(mSellVolume[SELL_1]));
            str += String.format(sFormat, "buy1",   price2Str(mBuyPrice[BUY_1]), vol2Str(mBuyVolume[BUY_1]));
            str += String.format(sFormat, "buy2",   price2Str(mBuyPrice[BUY_2]), vol2Str(mBuyVolume[BUY_2]));
            str += String.format(sFormat, "buy3",   price2Str(mBuyPrice[BUY_3]), vol2Str(mBuyVolume[BUY_3]));
            str += String.format(sFormat, "buy4",   price2Str(mBuyPrice[BUY_4]), vol2Str(mBuyVolume[BUY_4]));
            str += String.format(sFormat, "buy5",   price2Str(mBuyPrice[BUY_5]), vol2Str(mBuyVolume[BUY_5]));
            str += String.format(sFormat, "buy6",   price2Str(mBuyPrice[BUY_6]), vol2Str(mBuyVolume[BUY_6]));
            str += String.format(sFormat, "buy7",   price2Str(mBuyPrice[BUY_7]), vol2Str(mBuyVolume[BUY_7]));
            str += String.format(sFormat, "buy8",   price2Str(mBuyPrice[BUY_8]), vol2Str(mBuyVolume[BUY_8]));
            str += String.format(sFormat, "buy9",   price2Str(mBuyPrice[BUY_9]), vol2Str(mBuyVolume[BUY_9]));
            str += String.format(sFormat, "buy10",  price2Str(mBuyPrice[BUY_10]), vol2Str(mBuyVolume[BUY_10]));

            System.out.println(str);
        }
    }

}
