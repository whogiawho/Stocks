package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.StockPaths;

public class SSDates {
    public String stockCode;
    public String startDate;
    public double threshold;
    public String tradeDate;
    public String hmsList;                       //


    public SSDates(String stockCode, String startDate, double threshold, 
            String tradeDate, String hmsList) {
        this.stockCode = stockCode;
        this.startDate = startDate;
        this.threshold = threshold;
        this.tradeDate = tradeDate;
        this.hmsList = hmsList;
    }



    public void run(AmManager am) {
        String sOutFile = StockPaths.getSSDatesFile(stockCode, threshold, tradeDate, hmsList);
        ArrayList<String> ssDates = SSUtils.getSimilarTradeDates(stockCode, startDate, threshold, 
                tradeDate, hmsList, am);
        //write ssDates to sOutFile
        String line = String.format("%s\n", ssDates.toString());
        line = line.replaceAll("[\\[\\] ]", "");
        //System.out.format("%s", line);
        Utils.append2File(sOutFile, line, false);
    }
}
