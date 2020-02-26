package com.westsword.stocks.tools;

import java.io.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.base.utils.ILoadFile;
import com.westsword.stocks.base.utils.FileLoader;

public class SplitRawTradeDetails extends FileLoader {
    private String mDstDir;
    private String mPrevStartTime;

    public SplitRawTradeDetails(String dDstDir, String prevStartTime) {
        mDstDir = dDstDir;
        mPrevStartTime = prevStartTime;
    }

    public static void main(String args[]) throws Exception {
        // parse arguments
        if (args.length == 0 || args.length > 2)
            usage();

        String dDstDir = args[1];
        Utils.mkDir(dDstDir);

        SplitRawTradeDetails splitRawTradeDetails = new SplitRawTradeDetails(dDstDir, "");
        splitRawTradeDetails.load(args[0]);
    }

    public boolean onLineRead(String line, int count) {
        try {
            String[] fields=line.split(" +");
            //[0] time; [1] price; [2] count; [3] type
            String thisTime = fields[0];
            if(!mPrevStartTime.equals(thisTime)) {
                //System.out.format("not equal! thisTime=%s, prevStartTime=%s\n", thisTime, prevStartTime);
                if(!mPrevStartTime.equals("")) {
                    //System.out.println("not equal null!");
                }
                mPrevStartTime = thisTime;
            }

            //write line to dst/thisTime.txt
            BufferedWriter w = new BufferedWriter(new FileWriter(mDstDir+"\\"+thisTime+".txt", true));
            w.write(line, 0, line.length());
            w.newLine();
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private static void usage() {
        System.err.println("usage: java splitRawTradeDetails fRawTradeDetails dDstDir");
        System.exit(-1);
    }
}
