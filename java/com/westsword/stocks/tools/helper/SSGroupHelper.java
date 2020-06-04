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
 
 /* Written by whogiawho <whogiawho@gmail.com>. */
 
 
package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.utils.AnsiColor;
import com.westsword.stocks.base.utils.LineLoader;
import com.westsword.stocks.base.ckpt.CheckPoint0;
import com.westsword.stocks.tools.helper.man.*;

public class SSGroupHelper {

    public void listChar(String args[]) {
        if(args.length!=4) {
            usage1();
            return;
        }

        String stockCode = args[1];
        String hmsList = args[2];
        if(!SSUtils.checkHMSList(hmsList, 2, 2)) {
            usage1();
            return;
        }
        String[] hms = hmsList.split("_");


        SSgmsdrManager m = new SSgmsdrManager();

        LineLoader loader = new LineLoader();
        ArrayList<String> tradeDateList= new ArrayList<String>();
        loader.load(tradeDateList, args[3]);
        AmManager am = new AmManager(stockCode, tradeDateList);

        MinStdDevR r = new MinStdDevR(stockCode, hmsList);
        //m.run(r, tradeDateList, hms, am);
        m.run(r, tradeDateList, hmsList, am);
    }


    public void listChars(String args[]) {
        if(args.length!=3) {
            usage2();
            return;
        }

        String stockCode = args[1];


        LineLoader loader = new LineLoader();
        ArrayList<String> tradeDateList= new ArrayList<String>();
        loader.load(tradeDateList, args[2]);
        AmManager am = new AmManager(stockCode, tradeDateList);


        SSgmsdrManager m = new SSgmsdrManager();
        CheckPoint0 ckpt = new CheckPoint0();
        int length = ckpt.getLength();
        Combinations c = new Combinations(length-1, 2);     //exclude the last ckpt
        Iterator<int[]> itr = c.iterator();
        while(itr.hasNext()) {
            int[] e = itr.next();
            String hmsList = ckpt.getHMSList(e);                     //
            String[] hms = hmsList.split("_");

            MinStdDevR r = new MinStdDevR(stockCode, hmsList);
            //m.run(r, tradeDateList, hms, am);
            m.run(r, tradeDateList, hmsList, am);
        }
    }

    public void verify(String args[]) {
        if(args.length!=6) {
            usage3();
            return;
        }

        String stockCode = args[1];
        String hmsList = args[2];
        String sListAll = args[3];
        String sList0 = args[4];
        String sList1 = args[5];

        //all tradeDates
        LineLoader loader = new LineLoader();
        ArrayList<String> tradeDateList = new ArrayList<String>();
        loader.load(tradeDateList, sListAll);
        String[] sTradeDatesAll = tradeDateList.toArray(new String[0]);

        //tradeDates0
        ArrayList<String> tradeDateList0 = new ArrayList<String>();
        loader.load(tradeDateList0, sList0);
        ArrayList<Integer> idxs0 = getIdxs(tradeDateList, tradeDateList0);

        //tradeDates1
        ArrayList<String> tradeDateList1 = new ArrayList<String>();
        loader.load(tradeDateList1, sList1);
        ArrayList<Integer> idxs1 = getIdxs(tradeDateList, tradeDateList1);

        AmManager am = new AmManager(stockCode, sTradeDatesAll);
        CmManager cm = new CmManager();
        double[][] m = cm.getCorrMatrix(sTradeDatesAll, hmsList, am);

        //loop all tradeDates
        for(int i=0; i<tradeDateList.size(); i++) {
            String tradeDate = tradeDateList.get(i);

            int sgFact=0;
            double avg0=0.0, avg1=0.0;
            int idx = tradeDateList0.indexOf(tradeDate);
            if(idx != -1) {      //tradeDate in tradeDateList0
                double sum0 = getSum(idxs0, i, m);
                avg0 = sum0/(idxs0.size()-1);
                double sum1 = getSum(idxs1, i, m);
                avg1 = sum1/idxs1.size();
                sgFact=0;
            } else {             //tradeDate in tradeDateList1
                double sum0 = getSum(idxs0, i, m);
                avg0 = sum0/idxs0.size();
                double sum1 = getSum(idxs1, i, m);
                avg1 = sum1/(idxs1.size()-1);
                sgFact=1;
            }
            System.out.format("%s %8.3f %8.3f %8.3f %4d\n", 
                    tradeDate, avg0, avg1, avg0-avg1, sgFact);
        }
    }

    private double getSum(ArrayList<Integer> idxs, int i, double[][] m) {
        double sum = 0.0;

        for(int j=0; j<idxs.size(); j++) {
            int k = idxs.get(j);
            if(i!=k) {
                sum += m[i][k];
            }
        }

        return sum;
    }
    private ArrayList<Integer> getIdxs(ArrayList<String> tradeDateList, ArrayList<String> tradeDateList0) {
        ArrayList<Integer> list = new ArrayList<Integer>();

        for(int i=0; i<tradeDateList0.size(); i++) {
            list.add(tradeDateList.indexOf(tradeDateList0.get(i)));
        }

        return list;
    }

    private static void usage1() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssgroupchar stockCode hmsList sTradeDateFile");
        System.err.println("       sTradeDateFile ; one tradeDate each line");

             String line = "       hmsList        ; ";
                                    String line1 = "make sure only 2 hms exists, like hhmmss_hhmmss";
        line1 = AnsiColor.getColorString(line1, AnsiColor.ANSI_RED);
        line += line1;

        System.err.println(line);
        System.exit(-1);
    }
    private static void usage2() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssgroupchars stockCode sTradeDateFile");
        System.err.println("       sTradeDateFile ; one tradeDate each line");
        System.exit(-1);
    }
    private static void usage3() {
        String sPrefix = "usage: java AnalyzeTools ";
        System.err.println(sPrefix+"ssgroupverify stockCode hmsList sListAll sList0 sList1");
        System.err.println("       sListAll,sList0,sList1; one tradeDate each line");
        System.err.println("       sListAll; all tradeDates matched hmsList");
        System.err.println("       sList0; those tradeDates whose cycle==1");
        System.err.println("       sList1; those tradeDates whose cycle>1");
        System.err.println("       make sure sListAll is composed of sList0&sList1");
        System.exit(-1);
    }

}
