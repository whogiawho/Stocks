package com.westsword.stocks.tools;

import java.io.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.base.utils.ILoadFile;
import com.westsword.stocks.base.utils.FileLoader;
import com.westsword.stocks.analyze.RawRTPankou;

public class SplitRawPankou extends FileLoader {
    private String mDstDir;
    private String mPankouYear;

    public SplitRawPankou(String dDstDir, String pankouYear) {
        mDstDir = dDstDir;
        mPankouYear = pankouYear;
    }

    public static void main(String args[]) throws Exception {
        // parse arguments
        if (args.length != 2 && args.length != 3) {
            System.out.format("length = %d\n", args.length);
            usage();
        }

        String dDstDir = args[1];
        Utils.mkDir(dDstDir);
        String pankouYear = null; 
        if(args.length == 3) {
            pankouYear = args[2];
        }

        SplitRawPankou splitPankou = new SplitRawPankou(dDstDir, pankouYear);
        splitPankou.load(args[0]);
    }


    public boolean onLineRead(String line, int count) {
        try {
            String[] fields=line.split(",");
            String thisTime = fields[4*RawRTPankou.PANKOU_LEVEL_NUMBER];
           
            long millis = Utils.timeWOyear2Long(thisTime, mPankouYear);
            String sMillis = String.format("%x", millis);

            line = line.replaceAll(thisTime, "");
            line += sMillis;

            //write line to dst/count.txt
            BufferedWriter w = new BufferedWriter(new FileWriter(mDstDir+"\\"+sMillis+".txt", true));
            w.write(line, 0, line.length());
            w.newLine();
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private static void usage() {
        System.err.println("usage: java SplitRawPankou fRawPankou dDstDir [pankouYear]");
        System.err.println("  split a rawPankou file into small files by hexTimePoint");
        System.err.println("  covnert 41th sTime missing year to hexTimePoint");
        System.exit(-1);
    }

}
