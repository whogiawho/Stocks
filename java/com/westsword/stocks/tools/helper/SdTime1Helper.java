package com.westsword.stocks.tools.helper;


import com.westsword.stocks.base.time.*;

public class SdTime1Helper {
    public void getrel(String args[]) {
        if(args.length != 3) {
            usage();
            return;
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




    private static void usage() {
        System.err.println("usage: java AnalyzeTools getrel stockCode hmsList");
        System.exit(-1);
    }
}
