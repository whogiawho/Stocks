package com.westsword.stocks.tools.helper;


import java.util.*;
import com.mathworks.engine.MatlabEngine;
import java.util.concurrent.ExecutionException;

import com.westsword.stocks.am.AmUtils;

public class MatlabSync {
    public static void run() {
        try {
            MatlabEngine eng = MatlabEngine.startMatlab();

            String stockCode = "600030";
            String startDate = "20090105";
            String hmsList = "113000_130000";

            double[][] m = AmUtils.getAmMatrix(stockCode, startDate, hmsList);
            System.out.format("m.height=%d m.width=%d\n", m.length, m[0].length);

            long start = System.currentTimeMillis();
            double[][] rm = eng.feval("corrcoef", (Object)m);
            System.out.format("rm.height=%d rm.width=%d\n", rm.length, rm[0].length);
            long end = System.currentTimeMillis();
            System.out.format("MatlabSync.run: matlab.corrcoef duration=%4d\n", 
                    end-start);

            ArrayList<String> tradeDateList = new ArrayList<String>();
            SSUtils.getSimilarTradeDates(stockCode, startDate, SSUtils.Default_Threshold,
                    startDate, rm, tradeDateList);
            System.out.format("%s\n", tradeDateList.toString());
            
            eng.close();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
