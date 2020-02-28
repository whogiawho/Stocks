package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.utils.AnsiColor;
import com.westsword.stocks.base.utils.LineLoader;
import com.westsword.stocks.base.ckpt.CheckPoint0;

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
        m.run(r, tradeDateList, hms, am);
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
        Combinations c = new Combinations(length, 2);
        Iterator<int[]> itr = c.iterator();
        while(itr.hasNext()) {
            int[] e = itr.next();
            String hmsList = ckpt.getHMSList(e);                     //
            String[] hms = hmsList.split("_");

            MinStdDevR r = new MinStdDevR(stockCode, hmsList);
            m.run(r, tradeDateList, hms, am);
        }
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


}
