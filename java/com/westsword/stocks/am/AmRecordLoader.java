package com.westsword.stocks.am;

import java.util.*;

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.utils.FileLoader;

public class AmRecordLoader extends FileLoader {
    private ArrayList<AmRecord> mAmRecordList = null;
    private TreeMap<Integer, AmRecord> mAmMap = null;
    private AmrHashtable mAmrTable = null;


    public boolean onLineRead(String line, int counter) {
        String[] fields=line.split(" +");
        //[0] time; [1] sdTime; [2] am; [3] upPrice; [4] downPrice;
        long time = Long.parseLong(fields[0], 16);
        int sdTime = Integer.valueOf(fields[1]);
        long am = Long.valueOf(fields[2]);
        double upPrice = Double.valueOf(fields[3]);
        double downPrice = Double.valueOf(fields[4]);
                
        AmRecord r = new AmRecord(time, sdTime, am, upPrice, downPrice);

        if(mAmRecordList != null)
            mAmRecordList.add(r);
        if(mAmMap != null)
            mAmMap.put(sdTime, r);
        if(mAmrTable != null) {
            mAmrTable.put(r, Stock.TRADE_TYPE_LONG);
            mAmrTable.put(r, Stock.TRADE_TYPE_SHORT);
        }

        return true;
    }

    public void load(ArrayList<AmRecord> amRecordList, TreeMap<Integer, AmRecord> amMap, 
            AmrHashtable hTable, String sAmRecordFile) {
        mAmRecordList = amRecordList;
        mAmMap = amMap;
        mAmrTable = hTable;

        load(sAmRecordFile);
    }
}
