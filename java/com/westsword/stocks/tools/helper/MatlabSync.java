package com.westsword.stocks.tools.helper;


import java.util.concurrent.ExecutionException;
import com.mathworks.engine.MatlabEngine;

import com.westsword.stocks.tools.GetSettings;
import com.westsword.stocks.base.time.TradeDates;

public class MatlabSync {
    public static void run() {
        try {
            MatlabEngine eng = MatlabEngine.startMatlab();

            String stockCode = "600030";
            String hmsList = "113000_130000";
            String startDate = "20090105";
            double[][] m = GetSettings.getAmMatrix(stockCode, hmsList, startDate);
            System.out.format("m.height=%d m.width=%d\n", m.length, m[0].length);

            long start = System.currentTimeMillis();
            double[][] rm = eng.feval("corrcoef", (Object)m);
            System.out.format("rm.height=%d rm.width=%d\n", rm.length, rm[0].length);
            long end = System.currentTimeMillis();
            System.out.format("MatlabSync.run: matlab.corrcoef duration=%4d\n", 
                    end-start);

            listMatchedTradeDates(stockCode, startDate, hmsList, startDate, rm);
            
            eng.close();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void listMatchedTradeDates(String stockCode, String startDate, String hmsList, 
        String tradeDate, double[][] rm) {
        TradeDates tradeDates = new TradeDates(stockCode, startDate);
        int idx = tradeDates.getIndex(startDate);
        int h = rm.length;
        int w = rm[0].length;
        String sMatchedDates = "";
        for(int i=0; i<w; i++) {
            if(rm[idx][i] >= SSUtils.Default_Threshold)
                sMatchedDates += tradeDates.getDate(i) + ",";
        }
        System.out.format("%s\n", sMatchedDates);
    }


}
