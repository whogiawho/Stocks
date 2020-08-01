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

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.tools.helper.man.*;
import com.westsword.stocks.analyze.ssanalyze.*;

public class SSUtils {
    public final static String Default_StockCode = Settings.getStockCode();
    public final static String Default_StartDate = Settings.getSdStartDate();
    public final static double Default_Threshold = 0.9;
    public final static int Default_Nearest_Day_To_End_TradeSession = 1;          //t0|t1
    public final static int Default_TradeType = 5;                                //Long(5)|Short(1)

    public final static double MINIMUM_AVG_NET_REVENUE = 0.000;




    public static String getStockCode(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "c", Default_StockCode);
    }
    public static double getThreshold(CommandLine cmd, double defaultThres) {
        return CmdLineUtils.getDouble(cmd, "h", defaultThres);
    }
    public static double getThreshold(CommandLine cmd) {
        return CmdLineUtils.getDouble(cmd, "h", Default_Threshold);
    }
    public static String getStartDate(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "d", Default_StartDate);
    }
    public static int getNearestOutDist(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "t", Default_Nearest_Day_To_End_TradeSession);
    }
    public static int getTradeType(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "s", Default_TradeType);
    }
    public static boolean getSwitchLog2File(CommandLine cmd) {
        return CmdLineUtils.getBoolean(cmd, "n", true);
    }
    public static boolean getSwitchResetLog(CommandLine cmd) {
        return CmdLineUtils.getBoolean(cmd, "r", false);
    }
    public static boolean getSwitchStdout(CommandLine cmd) {
        return CmdLineUtils.getBoolean(cmd, "o", true);
    }
    public static String getTradeDateList(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "l", null);
    }
    public static String getSSTableFile(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "f", null);
    }
    public static String getHMSList(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "m", null);
    }
    public static String getStartHMSList(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "a", null);
    }
    public static String getEndHMSList(CommandLine cmd) {
        return CmdLineUtils.getString(cmd, "b", null);
    }


    //in case that false is returned, out[0] is not a complete amcorrel for hms[]
    public static boolean isHMSMatched(String tradeDate0, String tradeDate1, String[] hms, AmManager am, 
            double threshold, String[] out) {
        String sAmCorrels = "";
        boolean bHMSMatched = true;
        for(int i=1; i<hms.length; i++) {
            double amcorrel = am.getAmCorrel(tradeDate0, tradeDate1, hms[0], hms[i]);
            sAmCorrels += String.format("%8.3f ", amcorrel);
            if(Double.isNaN(amcorrel) || amcorrel<threshold) {
                bHMSMatched = false;
                break;
            }
        }
        out[0] = sAmCorrels;

        return bHMSMatched;
    }
    public static ArrayList<String> getSimilarTradeDates(MMInstance r, AmManager am, double[][] corrM) {
        return getSimilarTradeDates(r.stockCode, r.startDate, r.threshold,
                r.tradeDate, r.hmsList, am, corrM);
    }
    public static ArrayList<String> getSimilarTradeDates(SSInstance r, AmManager am, double[][] corrM) {
        return getSimilarTradeDates(r.stockCode, r.startDate, r.threshold,
                r.tradeDate, r.hmsList, am, corrM);
    }
    public static ArrayList<String> getSimilarTradeDates(String stockCode, String startDate, double threshold, 
            String tradeDate, String hmsList, AmManager am) {
        return getSimilarTradeDates(stockCode, startDate, threshold,
                tradeDate, hmsList, am, null);
    }
    public static ArrayList<String> getSimilarTradeDates(String stockCode, String startDate, double threshold, 
            String tradeDate, String hmsList, AmManager am, double[][] corrM) {
        ArrayList<String> tradeDateList = new ArrayList<String>();

        if(corrM!=null) {
            getSimilarTradeDates(stockCode, startDate, threshold,
                    tradeDate, corrM, tradeDateList);
        } else {
            String[] hms = hmsList.split("_");
            TradeDates tradeDates = new TradeDates(stockCode);
            String tradeDate0 = startDate;
            while(tradeDate0 != null) {
                String[] out = new String[1]; 
                boolean bHMSMatched = isHMSMatched(tradeDate0, tradeDate, hms, am, threshold, out);
                if(bHMSMatched)
                    tradeDateList.add(tradeDate0);
                /*
                System.out.format("%s %s %s %s\n", 
                        tradeDate0, tradeDate, hmsList, out[0]);
                */

                tradeDate0 = tradeDates.nextDate(tradeDate0);
            }
        }

        return tradeDateList;
    }
    public static void getSimilarTradeDates(String stockCode, String startDate, double threshold, 
            String tradeDate, double[][] corrM, ArrayList<String> tradeDateList) {
        TradeDates tradeDates = new TradeDates(stockCode, startDate);
        int idx = tradeDates.getIndex(tradeDate);
        int h = corrM.length;
        int w = corrM[0].length;
        for(int i=0; i<w; i++) {
            if(corrM[idx][i] >= threshold) {
                String sMatchedDate = tradeDates.getDate(i);
                if(tradeDateList!=null)
                    tradeDateList.add(sMatchedDate);
            }
        }
    }


    public static ArrayList<String> getSimilarTradeDates(SSTRInstance r, AmManager am) {
        return getSimilarTradeDates(r.stockCode, r.startDate, r.threshold,
                r.sMatchExp, am);
    }
    public static ArrayList<String> getSimilarTradeDates(String stockCode, String startDate, double threshold, 
            String sMatchExp, AmManager am) {
        ArrayList<String> tradeDateList = new ArrayList<String>();

        TradeDates tradeDates = new TradeDates(stockCode);
        String tradeDate0 = startDate;
        while(tradeDate0 != null) {
            String[] fields = sMatchExp.split("\\&|\\|");

            boolean bHMSMatched = true;
            for(int i=0; i<fields.length; i++) {
                String[] subFields = fields[i].split(":");
                String tradeDate = subFields[0];
                String hmsList = subFields[1];
                String[] hms = hmsList.split("_");

                String[] out = new String[1]; 
                bHMSMatched = isHMSMatched(tradeDate0, tradeDate, hms, am, threshold, out);
                if(!bHMSMatched) {
                    break;
                }
                /*
                System.out.format("%s %s %s %s\n", 
                        tradeDate0, tradeDate, hmsList, out[0]);
                */
            }
            if(bHMSMatched)
                tradeDateList.add(tradeDate0);

            tradeDate0 = tradeDates.nextDate(tradeDate0);
        }

        return tradeDateList;
    }

    public static String getInHMS(String hmsList) {
        String[] fields = HMS.getHMSArray(hmsList);
        String inHMS = fields[fields.length-1];

        return inHMS;
    }

    public static boolean checkTargetRateList(String sTargetRateList) {
        boolean bCheck = true;
        String[] fields = sTargetRateList.split(" +");
        for(int i=0; i<fields.length; i++) {
            if(!checkTargetRate(fields[i]))
                return false;
        }

        return bCheck;
    }
    public static boolean checkTargetRate(String sTargetRate) {
        String regEx = "[0-9]{1,}.[0-9]{1,3}";
        return sTargetRate.matches(regEx);
    }

    public static boolean checkDates(String startDate, ArrayList<String> tradeDateList) {
        boolean bCheck = true;
        for(int i=0; i<tradeDateList.size(); i++) {
            String tradeDate = tradeDateList.get(i);
            if(!checkDate(startDate, tradeDate)) {
                bCheck = false;
                break;
            }
        }

        return bCheck;
    }
    public static boolean checkDate(String startDate, String tradeDate) {
        boolean bCheck = true;
        if(tradeDate.compareTo(startDate)<0) {
            String line = String.format("tradeDate=%s < startDate=%s", tradeDate, startDate);
            line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
            System.out.format("%s\n", line);
            bCheck = false;
        }

        return bCheck;
    }
    //minHMS>=2
    //maxHMS<=100
    public static boolean checkHMSList(String hmsList, int minHMS, int maxHMS) {
        minHMS -= 1;
        maxHMS -= 1;
        String regEx = "^[0-9]{6}(_[0-9]{6}){" + minHMS + "," + maxHMS + "}[flFL]?$";
        return hmsList.matches(regEx);
    }
    public static boolean checkHMSList(String hmsList, int minHMS) {
        return checkHMSList(hmsList, minHMS, 100);
    }

    private static ISearchAmRecord way = null;
    public static ISearchAmRecord getWay2SearchAmRecord() {
        if(way!=null)
            return way;

        int w = Settings.getWay2SearchAmRecord();
        if(w==0)
            way = new LoopWay();
        else
            way = new HashWay();

        return way;
    }

    public static ArrayList<SSTableRecord> getSSTableRecordList(String sSSTableFile) {
        ArrayList<SSTableRecord> list = new ArrayList<SSTableRecord>();

        if(sSSTableFile != null) {
            SSTableLoader l = new SSTableLoader();
            l.load(list, sSSTableFile, "");
        }

        return list;
    }


}
