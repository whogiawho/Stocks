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
 
 
package com.westsword.stocks.analyze.avgam;


import java.util.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.session.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class AvgAmTableRecord {
    public final static String NULL_STRING = "\"\"";

    public String sTableName;

    public String stockCode;
    public String tradeDate;
    public String hms;

    public String eHMS0;
    public String eHMS1;
    public int tradeType;
    public int tradeCount;
    public double targetRate;
    public String sMaxCycle;
    public double dcThres;
    public double scThres;
    public int tSwitch;

    public double[] avgam;
    public PearsonsCorrelation mPC;

    public AvgAmTableRecord(String stockCode, String tradeDate, String hms,
            String eHMS0, String eHMS1, int tradeType, int tradeCount, double targetRate, String sMaxCycle,
            double dcThres, double scThres, int tSwitch, String sName) {
        this.stockCode = stockCode;
        this.tradeDate = tradeDate;
        this.hms = hms;
        this.eHMS0 = eHMS0;
        this.eHMS1 = eHMS1;
        this.tradeType = tradeType;
        this.tradeCount = tradeCount;
        this.targetRate = targetRate;
        this.sMaxCycle = sMaxCycle;
        this.dcThres = dcThres; 
        this.scThres = scThres; 
        this.tSwitch = tSwitch; 

        this.sTableName = sName;
        mPC = new PearsonsCorrelation();
    }

    public void load(SdTime1 sdt, int sdbw, int minSkippedSD, int interval) {
        avgam = AvgAmUtils.getAvgAm(stockCode, tradeDate, hms,
                sdt, sdbw, minSkippedSD, interval);
    }

    public boolean eval(String hms, double deltaCorrel, double[] inAvgam) {
        boolean bRet = true;

        //eHMS0&eHMS1
        if(!eHMS0.equals(NULL_STRING)&&hms.compareTo(eHMS0)<0||
                !eHMS1.equals(NULL_STRING)&&hms.compareTo(eHMS1)>0)
            return false;
        
        //dcThres
        if(deltaCorrel>dcThres)
            return false;

        //scThres
        double sCorrel = mPC.correlation(avgam, inAvgam);
        if(sCorrel<scThres)
            return false;

        return bRet;
    }
    public String toString(AmRecord r, double[] inAvgam) {
        double sCorrel = Double.NaN;
        if(inAvgam!=null&&avgam!=null)
            sCorrel = mPC.correlation(avgam, inAvgam);

        double inPrice = r.getInPrice(tradeType);
        double targetProfit = Trade.getTargetProfit(targetRate, inPrice);
        String line = String.format("%-10s %s %s %s %8.3f %8.3f %8.3f %8d", 
                sTableName, stockCode, tradeDate, hms, sCorrel, inPrice, targetProfit, tradeCount);
        if(tradeType==Stock.TRADE_TYPE_LONG)
            line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
        else
            line = AnsiColor.getColorString(line, AnsiColor.ANSI_GREEN);

        return line;
    }

    public TSRecord toTSRecord() {
        int sTDistance = 0;
        String sMatchExp = tradeDate+":"+hms+":"+eHMS0+":"+eHMS1;
        TSRecord r = new TSRecord(tradeCount, sTDistance, tradeType, 
                targetRate, sMatchExp, getSessionOpened());

        return r;
    }


    private boolean bSessionOpened;
    public boolean getSessionOpened() {
        return bSessionOpened;
    }
    public void setSessionOpened(boolean bSessionOpened) {
        this.bSessionOpened = bSessionOpened;
    }


}
