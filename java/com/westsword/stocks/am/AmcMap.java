package com.westsword.stocks.am;


import java.io.*;
import java.util.*;

import com.westsword.stocks.base.utils.StockPaths;

public class AmcMap {

    //load amCorrelMap from tradeDate0's amcorrelmap dir
    public static HashMap<String, Double> load(String stockCode, String tradeDate0) {
        HashMap<String, Double> amCorrelMap = new HashMap<String, Double>();

        String amcorrelMapDir = StockPaths.getAmCorrelMapDir(stockCode, tradeDate0);
        File fAmCorrelMapDir = new File(amcorrelMapDir);
        String[] sFiles = fAmCorrelMapDir.list();

        AmcMapLoader l = new AmcMapLoader();
        if(sFiles != null) {
            for(int i=0; i<sFiles.length; i++) {
                String sFile = sFiles[i];
                sFile = amcorrelMapDir + sFile;
                HashMap<String, Double> map = new HashMap<String, Double>();
                l.load(map, sFile);
                amCorrelMap.putAll(map);
            }
        }

        return amCorrelMap;
    }

    public static HashMap<String, Double> load(String stockCode, String tradeDate0, String tradeDate1) {
        String sFile = StockPaths.getAmCorrelMapFile(stockCode, tradeDate0, tradeDate1);
        HashMap<String, Double> map = new HashMap<String, Double>();
        AmcMapLoader l = new AmcMapLoader();
        l.load(map, sFile);

        return map;
    }
}
