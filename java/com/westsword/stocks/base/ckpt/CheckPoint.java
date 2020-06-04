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
