package com.westsword.stocks.am;

import java.util.*;

import com.westsword.stocks.utils.FileLoader;

public class AmRecordLoader extends FileLoader {
    private ArrayList<AmRecord> mAmRecordList = null;
    private TreeMap<Integer, Long> mAmMap = null;


    public boolean onLineRead(String line, int counter) {
        String[] fields=line.split(" +");
        //[0] time; [1] sdTime; [2] am; [3] upPrice; [4] downPrice;
        long time = Long.parseLong(fields[0], 16);
        int sdTime = new Integer(fields[1]);
        long am = Long.parseLong(fields[2]);
        double upPrice = Double.parseDouble(fields[3]);
        double downPrice = Double.parseDouble(fields[4]);
                
        AmRecord r = new AmRecord(time, sdTime, am, upPrice, downPrice);
        if(mAmRecordList != null)
            mAmRecordList.add(r);

        if(mAmMap != null)
            mAmMap.put(sdTime, am);

        return true;
    }

    public void load(ArrayList<AmRecord> amRecordList, TreeMap<Integer, Long> amMap, 
            String sAmRecordFile) {
        mAmRecordList = amRecordList;
        mAmMap = amMap;

        load(sAmRecordFile);
    }
}