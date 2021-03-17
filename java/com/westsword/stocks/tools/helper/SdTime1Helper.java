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


import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.*;

public class SdTime1Helper {
    public void getRel(String args[]) {
        if(args.length != 3) {
            usage();
        }

        String stockCode = args[1];
        String hmsList = args[2];
        String[] fields = hmsList.split("_");

        String sSdTime = "";
        SdTime1 sdTime = new SdTime1(stockCode);
        for(int i=0; i<fields.length; i++) {
            String hms = fields[i];
            int sd = sdTime.get(hms);
            //System.out.format("i=%d sd=%d\n", i, sd);
            sSdTime += String.format("%8d ", sd);
        }

        System.out.format("%s\n", sSdTime);
    }
    public void rgetRel(String args[]) {
        if(args.length != 2) {
            rgetrelUsage();
        }

        int rel = Integer.valueOf(args[1]);

        AStockSdTime sdt = new AStockSdTime();
        String hms = sdt.rget(rel);
        System.out.format("%s\n", hms);
    }
    public void getAbs(String args[]) {
        if(args.length != 4) {
            getabsUsage();
        }

        String stockCode = args[1];
        String tradeDate = args[2];
        String hmsList = args[3];
        String[] fields = hmsList.split("_");

        String sSdTime = "";
        SdTime1 sdTime = new SdTime1(stockCode);
        for(int i=0; i<fields.length; i++) {
            String hms = fields[i];
            int sd = sdTime.getAbs(tradeDate, hms);
            //System.out.format("i=%d sd=%d\n", i, sd);
            sSdTime += String.format("%8d ", sd);
        }

        System.out.format("%s\n", sSdTime);
    }
    public void rgetAbs(String args[]) {
        if(args.length != 3 && args.length != 4) {
            rgetabsUsage();
        }

        String stockCode = args[1];
        Integer abssdtime = Integer.valueOf(args[2]);

        SdTime1 sdTime = new SdTime1(stockCode);
        int sdt = 0;
        if(args.length==4) {
            String[] fields = args[3].split(",");
            //System.out.format("%s %s\n", fields[0], fields[1]);
            sdt = sdTime.getAbs(fields[0], fields[1]);
        } 

        long tp = sdTime.rgetAbs(abssdtime+sdt);
        
        System.out.format("%x\n", tp);
    }





    private static void usage() {
        System.err.println("usage: java AnalyzeTools getrel stockCode hmsList");
        System.exit(-1);
    }
    private static void rgetrelUsage() {
        System.err.println("usage: java AnalyzeTools rgetrel rel");
        System.exit(-1);
    }
    private static void getabsUsage() {
        System.err.println("usage: java AnalyzeTools getabs stockCode tradeDate hmsList");
        System.exit(-1);
    }
    private static void rgetabsUsage() {
        System.err.println("usage: java AnalyzeTools rgetabs stockCode abssdtime [startDate,startHMS]");
        System.exit(-1);
    }
}
