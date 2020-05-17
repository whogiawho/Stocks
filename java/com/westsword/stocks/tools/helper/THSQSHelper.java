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



    public static TradeSessionManager getTradeSessionManager() {
        String stockCode = Settings.getStockCode();
        String tradeDate = Settings.getTradeDate();

        TradeSessionManager m = new TradeSessionManager(stockCode, tradeDate);

        return m;
    }
    public static void submitAbs(String[] args) {
        TradeSessionManager m = getTradeSessionManager();
        m.check2SubmitSession();
    }
    public static void checkAbsS(String[] args) {
        TradeSessionManager m = getTradeSessionManager();
        m.checkAbnormalSubmittedSessions(false);
    }
    public static void makeRRP(String[] args) {
        TradeSessionManager m = getTradeSessionManager();

        long tp = System.currentTimeMillis()/1000;
        AmRecord r = new AmRecord(tp, -1, -1, Double.NaN, Double.NaN);
        m.makeRRP(r);
    }
}
