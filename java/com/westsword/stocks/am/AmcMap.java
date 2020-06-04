 /*
 Copyright (C) 1989-2020 Free Software Foundation, Inc.
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
 
 
package com.westsword.stocks.am;


import java.io.*;
import java.util.concurrent.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.StockPaths;

public class AmcMap {

    //load AmcMap from tradeDate0's amcorrelmap dir
    public static ConcurrentHashMap<String, Double> load(String stockCode, String tradeDate0) {
        ConcurrentHashMap<String, Double> amcMap = new ConcurrentHashMap<String, Double>();

        String amcorrelMapDir = StockPaths.getAmCorrelMapDir(stockCode, tradeDate0);
        File fAmCorrelMapDir = new File(amcorrelMapDir);
        String[] sFiles = fAmCorrelMapDir.list();

        AmcMapLoader l = new AmcMapLoader();
        if(sFiles != null) {
            for(int i=0; i<sFiles.length; i++) {
                String sFile = sFiles[i];
                String tradeDate1 = sFile.substring(0,8);
                sFile = amcorrelMapDir + sFile;
                ConcurrentHashMap<String, Double> map = new ConcurrentHashMap<String, Double>();
                l.load(map, sFile, tradeDate0, tradeDate1);
                amcMap.putAll(map);
            }
        }

        return amcMap;
    }
    public static ConcurrentHashMap<String, Double> load(String stockCode, String tradeDate0, String tradeDate1) {
        String sFile = StockPaths.getAmCorrelMapFile(stockCode, tradeDate0, tradeDate1);
        ConcurrentHashMap<String, Double> map = new ConcurrentHashMap<String, Double>();
        AmcMapLoader l = new AmcMapLoader();
        l.load(map, sFile, tradeDate0, tradeDate1);

        return map;
    }




    public static void removeAmCorrel(String tradeDate0, String tradeDate1, 
            String startHMS, String endHMS) {
        String key01 = Utils.getAmcKey(tradeDate0, tradeDate1, startHMS, endHMS);
        bufAmcMap.remove(key01);
    }
    private static boolean CONSIDER10 = true;
    //three levels of getting amcorrel:
    //  bufAmcMap 
    //  getAmCorrelMapFile(stockCode, tradeDate0, tradeDate1)
    //  calculation from Fragments
    private static ConcurrentHashMap<String, Double> bufAmcMap = new ConcurrentHashMap<String, Double>();
    public static double getAmCorrel(String tradeDate0, String tradeDate1, 
            String startHMS, String endHMS, AmManager am, String stockCode) {
        Double amCorrel = 0.0;

        String key01 = Utils.getAmcKey(tradeDate0, tradeDate1, startHMS, endHMS);

        //level0: bufAmcMap(key01)
        amCorrel = bufAmcMap.get(key01);
        if(amCorrel == null) {
            //level1: load from bufAmcMap(key10)
            String key10 = Utils.getAmcKey(tradeDate1, tradeDate0, startHMS, endHMS);
            amCorrel = bufAmcMap.get(key10);
            if(amCorrel == null) {
                //level2: load from amCorrelMapFile01
                ConcurrentHashMap<String, Double> map01 = load(stockCode, tradeDate0, tradeDate1);
                amCorrel = map01.get(key01);
                if(amCorrel == null) {
                    if(CONSIDER10) {
                        amCorrel = getAmCorrel10(tradeDate0, tradeDate1, startHMS, endHMS, am, stockCode);
    
                        bufAmcMap.put(key10, amCorrel);
                        //bufAmcMap.putAll(map10);
                    } else
                        amCorrel = am.getAmCorrel(tradeDate0, tradeDate1, startHMS, endHMS);
    
                    //just skip it now
                    /*
                    //write <key01, amCorrel> to level1: amCorrelMapFile01
                    write2MapFile(stockCode, tradeDate0, tradeDate1, key01, amCorrel);
                    */
                }
            }
            bufAmcMap.put(key01, amCorrel);
            //bufAmcMap.putAll(map01);
        }

        return amCorrel;
    }
    private static double getAmCorrel10(String tradeDate0, String tradeDate1,
            String startHMS, String endHMS, AmManager am, String stockCode) {
        Double amCorrel = 0.0;

        //level1: load from amCorrelMapFile10
        String key10 = Utils.getAmcKey(tradeDate1, tradeDate0, startHMS, endHMS);

        ConcurrentHashMap<String, Double> map10 = load(stockCode, tradeDate1, tradeDate0);
        amCorrel = map10.get(key10);
        if(amCorrel == null) {
            //level2: dynamic calculation
            amCorrel = am.getAmCorrel(tradeDate0, tradeDate1, startHMS, endHMS);

            String key01 = Utils.getAmcKey(tradeDate0, tradeDate1, startHMS, endHMS);

            //just skip it now
            /*
            //write <key10, amCorrel> to level1: amCorrelMapFile10
            if(!key10.equals(key01))
                write2MapFile(stockCode, tradeDate1, tradeDate0, key10, amCorrel);
            */
        }

        return amCorrel;
    }
}
