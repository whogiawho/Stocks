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
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.analyze.*;

public class AmRecord implements Comparable<AmRecord> {
    public long hexTimePoint;
    public int  timeIndex;
    public long am;
    public double upPrice;
    public double downPrice;

    //2nd added
    public long abVol;
    public double abAmount;
    public long asVol;
    public double asAmount;
    public long trVol;               //trVol=abVol+asVol
    public double trAmount;          //trAmount=abAmount+asAmount


    public AmRecord(long hexTimePoint, int timeIndex, long am, double upPrice, double downPrice,
            long abVol, long asVol, double abAmount, double asAmount) {
        this.hexTimePoint = hexTimePoint;
        this.timeIndex = timeIndex;
        this.am = am;
        this.upPrice = upPrice;
        this.downPrice = downPrice;

        this.abVol = abVol;
        this.asVol = asVol;
        this.abAmount = abAmount;
        this.asAmount = asAmount;
        this.trVol = abVol + asVol;
        this.trAmount = abAmount + asAmount;
    }
    public AmRecord(long hexTimePoint, int timeIndex, long am, double upPrice, double downPrice) {
        this(hexTimePoint, timeIndex, am, upPrice, downPrice, 0, 0, 0, 0);
    }

    public double getInPrice(int tradeType) {
        double inPrice = downPrice;

        if(tradeType == Stock.TRADE_TYPE_LONG) {
            inPrice = upPrice;
        }

        return inPrice;
    }
    public double getOutPrice(int tradeType) {
        double outPrice = upPrice;

        if(tradeType == Stock.TRADE_TYPE_LONG) {
            outPrice = downPrice;
        }

        return outPrice;
    }
    public double getProfit(int tradeType, double inPrice) {
        double profit, outPrice;

        if(tradeType == Stock.TRADE_TYPE_LONG) {
            outPrice = downPrice;
            profit = Trade.getNetProfit(inPrice, outPrice);
        } else {
            outPrice = upPrice;
            profit = Trade.getNetProfit(outPrice, inPrice);
        }

        return profit;
    }

    public int compareTo(AmRecord r) {
        return Long.compare(hexTimePoint, r.hexTimePoint);
    }

    public String toString() {
        String sFormat = "%-10x %8d %20d %8.3f %8.3f %15d %15d %20.3f %20.3f\n";
        String line = String.format(sFormat, hexTimePoint, timeIndex, am, upPrice, downPrice,
                abVol, asVol, abAmount, asAmount);

        return line;
    }
    public void append2File(String sAnalysisFile) {
        Utils.append2File(sAnalysisFile, toString());
    }
    public void print() {
        System.out.format("%s", toString());
    }

    public boolean time2Out(int tradeType, double inPrice, double targetProfit) {
        boolean bOut = false;

        if(tradeType == Stock.TRADE_TYPE_LONG && getOutPrice(tradeType)-inPrice>=targetProfit) 
            bOut = true;
        else if(tradeType == Stock.TRADE_TYPE_SHORT && inPrice-getOutPrice(tradeType)>=targetProfit)
            bOut = true; 

        return bOut;
    }



    public static AmRecord get(ArrayList<RawTradeDetails> rawDetailsList, long tp, SdTime1 sdt) {
        int timeIdx = sdt.getAbs(tp);

        double upPrice=Double.NEGATIVE_INFINITY, downPrice=Double.POSITIVE_INFINITY;
        int am = 0;
        long abVol=0, asVol=0;
        double abAmount=0, asAmount=0;
        for(int i=0; i<rawDetailsList.size(); i++) {
            RawTradeDetails rtd = rawDetailsList.get(i);
            if(rtd.time == tp) {
                if(rtd.type == Stock.TRADE_TYPE_LONG) {
                    upPrice = rtd.price>upPrice? rtd.price:upPrice;
                    am += rtd.count;
                    abVol += rtd.count;
                    abAmount += rtd.count*rtd.price;
                } else {
                    downPrice = rtd.price<downPrice? rtd.price:downPrice;
                    am -= rtd.count;
                    asVol += rtd.count;
                    asAmount += rtd.count*rtd.price;
                }
            }
        }
        AmRecord r = new AmRecord(tp, timeIdx, am, upPrice, downPrice,
                abVol, asVol, abAmount, asAmount);
        return r;
    }
}
