package com.westsword.stocks.base.ckpt;


import java.util.*;

import com.westsword.stocks.base.time.*;

public class CheckPoint {
    private TreeSet<String> mCkptList = new TreeSet<String>();

    public int getLength() {
        return mCkptList.size();
    }
    public void add(String hms) {
        hms = HMS.unformalize(hms);
        mCkptList.add(hms);
    }
    //[start, end]
    public void add(long start, long end, int interval) {
        long tp = start;
        while(tp <= end) {
            add(Time.getTimeHMS(tp));
            tp += interval;
        }
    }
    public void print() {
        int idx = 0;
        for(String ckpt: mCkptList) {
            System.out.format("%8d %s\n", idx++, ckpt);
        }
    }
    public String getHMSList(int[] idxs) {
        String sHMSList = "";

        String[] sCkpt = mCkptList.toArray(new String[0]);
        for(int i=0; i<idxs.length; i++) {
            sHMSList += sCkpt[idxs[i]] + " ";
        }
        sHMSList = sHMSList.trim();
        sHMSList = sHMSList.replace(" ", "_");

        return sHMSList;
    }


    public String[][] getPairs() {
        return getPairs(mCkptList.toArray(new String[0]));
    }




    public static String[][] getPairs(String[] array) {
        String[][] pairs = new String[array.length*(array.length-1)/2][2];

        int k=0;
        for(int i=0; i<array.length; i++) {
            for(int j=i+1; j<array.length; j++) {
                pairs[k][0] = array[i];
                pairs[k][1] = array[j];

                k++;
            }
        }

        return pairs;
    }
}
