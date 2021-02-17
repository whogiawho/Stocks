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
 
 
package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.cli.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.qr.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.ckpt.*;
import com.westsword.stocks.base.utils.*;

public class QrVerifier {

    public void verify(String[] args) {
        CommandLine cmd = QualRangeHelper.getCommandLine(args); //share -h
        String[] newArgs = cmd.getArgs();
        if(newArgs.length < 4) {
            verifyUsage();
            return;
        }

        double threshold = QualRangeHelper.getThreshold(cmd);

        String stockCode = newArgs[0];
        int tradeType = Integer.valueOf(newArgs[1]);
        int cycle = Integer.valueOf(newArgs[2]);
        double targetRate = Double.valueOf(newArgs[3]);

        ArrayList<QrMatchComponent> qrMatchList = new ArrayList<QrMatchComponent>();
        initQrMatchComponentList(qrMatchList, newArgs);

        int sdInterval = Settings.getSdInterval();
        int cycleSdLength = (int)Utils.roundUp((double)cycle/sdInterval, "#");

        SdTime1 sdt = new SdTime1(stockCode);
        AmManager am = new AmManager(stockCode);
        TradeDates tradeDates = new TradeDates(stockCode);
        CheckPoint0 ckpt0 = new CheckPoint0();
        TreeSet<String> hmsSet = ckpt0.get();

        String[] sTradeDates = tradeDates.getAllDates();
        for(int i=0; i<sTradeDates.length; i++) {
            String tradeDate = sTradeDates[i];
            for(String hms: hmsSet) {
                int baseEndSdt = sdt.getAbs(tradeDate, hms);
                long baseEndTp = Time.getSpecificTime(tradeDate, hms);
                String baseEndTradeDate = Time.getTimeYMD(baseEndTp, false);
                String baseEndHMS = Time.getTimeHMS(baseEndTp, false);

                double amCorrel1st=Double.NaN;
                boolean bMatch = true;
                for(int j=0; j<qrMatchList.size(); j++) {
                    QrMatchComponent component = qrMatchList.get(j);
                    int startIdx0 = component.getStartSdTime(sdt, baseEndTradeDate, baseEndHMS);
                    int endIdx0 = component.getEndSdTime(sdt, baseEndTradeDate, baseEndHMS);
                    int startIdx1 = component.getTemplateStartSdTime(sdt);
                    int endIdx1 = component.getTemplateEndSdTime(sdt);
                    if(startIdx0>=0&&endIdx0>=0&&startIdx1>=0&&endIdx1>=0) {
                        double amCorrel = am.getAmCorrel(startIdx0, endIdx0, startIdx1, endIdx1);
                        if(j==0)
                            amCorrel1st = amCorrel;
                        if(amCorrel<threshold||Double.isNaN(amCorrel)) {
                            bMatch = false;
                            break;
                        }
                    } else {
                        bMatch = false;
                        break;
                    }
                }
                if(bMatch) {
                    //the 1st component
                    QrMatchComponent component = qrMatchList.get(0);
                    double inPrice = am.getInPrice(tradeType, baseEndTp);
                    double targetProfit = Trade.getTargetProfit(targetRate, inPrice);
                    double outPrice = Utils.getOutPrice(inPrice, targetProfit, tradeType);
                    AmRecord ar = am.getTargetAmRecord(baseEndSdt, tradeType, cycleSdLength, targetRate);
                    if(ar==null) {
                        outPrice = am.getOutPrice(tradeType, baseEndSdt+cycleSdLength);
                    }
                    double profit = getNetProfit(inPrice, outPrice, tradeType);
                    
                    printMatchInfo(component, baseEndTradeDate, baseEndHMS, amCorrel1st, profit, sdt);
                }
            }
        }
    }


    private void initQrMatchComponentList(ArrayList<QrMatchComponent> qrMatchList, String[] newArgs) {
        //1st QrMatchComponent
        String sIn = newArgs[4];
        String[] fields = sIn.split(",");
        String endTradeDate = fields[0];
        String endHMS = fields[1];
        int matchedSdLen = Integer.valueOf(fields[2]);
        QrMatchComponent component = new QrMatchComponent(0, endTradeDate, endHMS, matchedSdLen);
        qrMatchList.add(component);

        //optional QrMatchComponents
        int length = newArgs.length-5;
        for(int i=0; i<length; i++) {
            String sComponent = newArgs[i+5];
            fields = sComponent.split(",");
            component = new QrMatchComponent(Integer.valueOf(fields[0]), fields[1], fields[2], Integer.valueOf(fields[3]));
            qrMatchList.add(component);
        }
    }
    private void printMatchInfo(QrMatchComponent component, String baseEndTradeDate, String baseEndHMS, 
            double amCorrel1st, double profit, SdTime1 sdt) {
        String startDate0 = component.getStartTradeDate(sdt, baseEndTradeDate, baseEndHMS);
        String startHMS0 = component.getStartHMS(sdt, baseEndTradeDate, baseEndHMS);
        String startDate1 = component.getTemplateStartTradeDate(sdt);
        String startHMS1 = component.getTemplateStartHMS(sdt);
        String endDate1 = component.getTemplateEndTradeDate();
        String endHMS1 = component.getTemplateEndHMS();
        System.out.format("%s %s %s %s - %s %s %s %s : %8.3f %8.3f\n",
                startDate0, startHMS0, baseEndTradeDate, baseEndHMS,
                startDate1, startHMS1, endDate1, endHMS1,
                amCorrel1st, profit);
    }

    public double getNetProfit(double inPrice, double outPrice, int tradeType) {
        double profit = 0;
        if(tradeType == Stock.TRADE_TYPE_LONG)
            profit = Trade.getNetProfit(inPrice, outPrice);
        else
            profit = Trade.getNetProfit(outPrice, inPrice);

        return profit;
    }

    private void verifyUsage() {
        System.err.println("usage: java AnalyzeTools qrverify [-h] stockCode tradeType cycle targetRate endDate0,endHMS0,matchedSdLen [sMatch0] [sMatch1] ...");
        System.err.println("       stockCode   - which stock is to be operated");
        System.err.println("       tradeType   - long or short");
        System.err.println("       <cycle, targetRate>");
        System.err.println("         cycle by seconds");
        System.err.println("       <endDate0, endHMS0, matchedSdLen> - first QrMatchComponent");
        System.err.println("         matchedSdLen by sdtime is positive");
        System.err.println("       sMatch[0-n] - optional; like <offset,endDate,endHMS,matchedSdLen>");
        System.err.println("         matchedSdLen based on endHMS");
        System.err.println("         offset by sdtime is positive relative to baseEndTradeDate,baseEndHMS");
        System.exit(-1);
    }



    public static class QrMatchComponent {
        public int sdOffset;
        public String endTradeDate;
        public String endHMS;
        public int matchedSdLen;

        public QrMatchComponent(int sdOffset, String endTradeDate, String endHMS, int matchedSdLen) {
            this.sdOffset = sdOffset;
            this.endTradeDate = endTradeDate;
            this.endHMS = endHMS;
            this.matchedSdLen = matchedSdLen;
        }


        public String getStartTradeDate(SdTime1 sdt, String baseEndTradeDate, String baseEndHMS) {
            int startSdt = getStartSdTime(sdt, baseEndTradeDate, baseEndHMS);
            return Time.getTimeYMD(sdt.rgetAbs(startSdt), false);
        }
        public String getStartHMS(SdTime1 sdt, String baseEndTradeDate, String baseEndHMS) {
            int startSdt = getStartSdTime(sdt, baseEndTradeDate, baseEndHMS);
            return Time.getTimeHMS(sdt.rgetAbs(startSdt), false);
        }
        public int getEndSdTime(SdTime1 sdt, String baseEndTradeDate, String baseEndHMS) {
            return sdt.getAbs(baseEndTradeDate, baseEndHMS) - sdOffset;
        }
        public int getStartSdTime(SdTime1 sdt, String baseEndTradeDate, String baseEndHMS) {
            return getEndSdTime(sdt, baseEndTradeDate, baseEndHMS) - matchedSdLen;
        }


        public String getTemplateStartTradeDate(SdTime1 sdt) {
            return Time.getTimeYMD(sdt.rgetAbs(getTemplateStartSdTime(sdt)), false);
        }
        public String getTemplateStartHMS(SdTime1 sdt) {
            return Time.getTimeHMS(sdt.rgetAbs(getTemplateStartSdTime(sdt)), false);
        }
        public String getTemplateEndTradeDate() {
            return endTradeDate;
        }
        public String getTemplateEndHMS() {
            return endHMS;
        }
        public int getTemplateEndSdTime(SdTime1 sdt) {
            return sdt.getAbs(endTradeDate, endHMS);
        }
        public int getTemplateStartSdTime(SdTime1 sdt) {
            return getTemplateEndSdTime(sdt) - matchedSdLen;
        }
    }


}
