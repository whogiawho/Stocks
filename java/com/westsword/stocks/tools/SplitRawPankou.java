package com.westsword.stocks.tools;

import java.io.*;
import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.base.time.Time;
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
           
            long millis = timeWOyear2Long(thisTime, mPankouYear);
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

    //append pankouYear to timeMissingYear, thus forming a time of format
    //    year-month-day hour:min:second
    //timeMissingYear[in]
    //    month-day hour:min:second
    public static long timeWOyear2Long(String timeMissingYear, String pankouYear) {
        String[] list0 = timeMissingYear.split("-"); 
        String sMonth = list0[0]; 
        int month = Integer.valueOf(sMonth);
        String[] list1 = list0[1].split(" "); 
        String sDay = list1[0];
        int day = Integer.valueOf(sDay);
        String[] list2 = list1[1].split(":"); 
        String sHour = list2[0];
        int hour = Integer.valueOf(sHour);
        String sMinute = list2[1];
        int minute = Integer.valueOf(sMinute);
        String sSecond = list2[2];
        int second = Integer.valueOf(sSecond);

        //Year, Month, Day, Hour, Minute, Second
        Calendar cal = Time.getCalendar();
        int year;
        if(pankouYear == null) {
            cal.setTimeInMillis(System.currentTimeMillis());
            year = cal.get(Calendar.YEAR);                             //get current year
        } else {
            year = Integer.valueOf(pankouYear);
        }
        cal.set(year, month-1, day, hour, minute, second);
               
        long millis = cal.getTimeInMillis()/1000;

        return millis;
    }


}
