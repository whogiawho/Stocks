package com.westsword.stocks.tools.helper;


import java.util.*;
import com.mathworks.engine.MatlabEngine;
import com.mathworks.engine.EngineException;
import java.util.concurrent.ExecutionException;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;

public class CmManager {
    private MatlabEngine eng = null;

    public CmManager() {
        try {
            eng = MatlabEngine.startMatlab();
        } catch (EngineException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public double[][] getCorrMatrix(String stockCode, String startDate, 
            String hmsList, AmManager am) {
        double[][] cm = null;

        try {
            String[] sTradeDates = new TradeDates(stockCode, startDate).getAllDates();
            double[][] m0 = am.getAmMatrix(hmsList, sTradeDates);

            cm = eng.feval("corrcoef", (Object)m0);
            //eng.close();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return cm;
    }

    public void close() {
        try {
            if(eng!=null)
                eng.close();
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }
}
