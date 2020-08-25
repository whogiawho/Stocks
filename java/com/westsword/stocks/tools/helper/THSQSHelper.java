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


import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.utils.THSQS;
import com.westsword.stocks.session.TradeSessionManager;

public class THSQSHelper {
    public static void getEntrust(String[] args) {
        if(args.length != 2 && args.length != 3) {
            usage();
            return;
        }

        String sEntrustID = args[1];
        String sKey = "";
        if(args.length == 3)
            sKey = args[2];

        THSQS iThsqs = new THSQS();
        iThsqs.queryEntrust(sEntrustID, sKey);
        




        //iThsqs.queryTradedTime(sEntrustID);
        //iThsqs.buy("600030", 21.00, 100);
        //iThsqs.wait4TradedPrice(sEntrustID);
        //iThsqs.queryEntrustState(sEntrustID);
        //iThsqs.queryEntrustAvgPrice(sEntrustID);
        //iThsqs.getStockAvaiVols(sEntrustID);
        //testAvaiRemains(iThsqs);
    }
    private static void testAvaiRemains(THSQS iThsqs) {
        double avaiRemains = iThsqs.getAvaiRemains();
        int amount = (int)(avaiRemains/1000)*10;
        System.out.format("avaiRemains=%8.3f, amount=%d\n", avaiRemains, amount);
    }
    private static void usage() {
        System.err.println("usage: java AnalyzeTools getentrust entrustID [key]");
        System.exit(-1);
    }



    public static TradeSessionManager getTradeSessionManager(String stockCode) {
        if(stockCode == null)
            stockCode = Settings.getStockCode();

        TradeSessionManager m = new TradeSessionManager(stockCode);

        return m;
    }
    public static String getStockCode(String[] args) {
        String stockCode = Settings.getStockCode();
        if(args.length == 2) {
            stockCode = args[1];
            System.out.format("stockCode=%s\n", stockCode);
        }

        return stockCode;
    }


    public static void submitAbs(String[] args) {
        if(args.length != 1 && args.length != 2) {
            submitAbsUsage();
            return;
        }

        String stockCode = getStockCode(args);
        TradeSessionManager m = getTradeSessionManager(stockCode);
        m.check2SubmitSession();
    }
    public static void checkAbss(String[] args) {
        if(args.length != 1 && args.length != 2) {
            checkAbssUsage();
            return;
        }

        String stockCode = getStockCode(args);
        TradeSessionManager m = getTradeSessionManager(stockCode);
        m.checkAbnormalSubmittedSessions(false);
    }
    public static void makeRRP(String[] args) {
        TradeSessionManager m = getTradeSessionManager(null);

        long tp = System.currentTimeMillis()/1000;
        AmRecord r = new AmRecord(tp, -1, -1, Double.NaN, Double.NaN);
        m.makeRRP(r);
    }




    private static void submitAbsUsage() {
        System.err.println("usage: java AnalyzeTools submitabs [stockCode]");
        System.exit(-1);
    }
    private static void checkAbssUsage() {
        System.err.println("usage: java AnalyzeTools checkabss [stockCode]");
        System.exit(-1);
    }

}
