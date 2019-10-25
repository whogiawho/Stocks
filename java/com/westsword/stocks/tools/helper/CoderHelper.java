package com.westsword.stocks.tools.helper;

import com.westsword.stocks.base.ConvertDD2Double;

public class CoderHelper {
    public static void decode(String args[]) {
        if(args.length != 2) {
            usage();
            return;
        }

        ConvertDD2Double cDD = new ConvertDD2Double();
        double price = cDD.sub_48A0D0(args[1]);
        System.out.format("%s", price);
    }




    private static void usage() {
        System.err.println("usage: java AnalyzeTools decode hexPrice");
        System.exit(-1);
    }
}
