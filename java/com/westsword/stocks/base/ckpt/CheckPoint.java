package com.westsword.stocks.base.ckpt;


import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.time.*;

public class CheckPoint {
    private TreeSet<String> mCkptList = new TreeSet<String>();

    public TreeSet<String> get() {
        return mCkptList;
    }

    public int getLength() {
        return mCkptList.size();
    }

    public boolean contains(String hms) {
        hms = HMS.unformalize(hms);
        return mCkptList.contains(hms);
    }
    public boolean contains(long tp) {
        return contains(Time.getTimeHMS(tp));
    }
    public void add(String hms) {
        hms = HMS.unformalize(hms);
        mCkptList.add(hms);
    }
    public void add(long tp) {
        add(Time.getTimeHMS(tp));
    }

    //[start, end]
    public void add(long start, long end, int interval) {
        long tp = start;
        while(tp <= end) {
            add(tp);
            tp += interval;
        }
        
        //last element
        if(!contains(end))
            add(end);
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

    public int[] getIdxList(String hmsList) {
        String[] sCkpt = mCkptList.toArray(new String[0]);
        String[] fields = hmsList.split("_");
        int[] idxs = new int[fields.length];
        for(int i=0; i<fields.length; i++) {
            String hms = fields[i];
            //there should be a check here to throw runtime exception
            idxs[i] = Utils.getIdx(sCkpt, hms);
        }
        return idxs;
    }
}
