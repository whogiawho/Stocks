package com.westsword.stocks.analyze.ssanalyze;


import java.util.*;

import com.westsword.stocks.base.utils.FileLoader;

public class SSTableLoader extends FileLoader {
    private ArrayList<SSTableRecord> mList = null;

    public boolean onLineRead(String line, int count) {
        if(line.matches("^ *#.*")||line.matches("^ *$"))
            return true;

        String[] fields=line.split(" +");
        int tradeCount = Integer.valueOf(fields[0]);
        String stockCode = fields[1];
        String startDate = fields[2];
        double threshold = Double.valueOf(fields[3]);
        int sTDistance = Integer.valueOf(fields[4]);
        int tradeType = Integer.valueOf(fields[5]);
        int maxCycle = Integer.valueOf(fields[6]);
        double targetRate = Double.valueOf(fields[7]);
        String sMatchExp = fields[8];
        SSTableRecord r = new SSTableRecord(tradeCount, 
                stockCode, startDate, threshold, sTDistance, tradeType,
                maxCycle, targetRate, sMatchExp);

        if(mList != null)
            mList.add(r);

        return true;
    }

    public void load(ArrayList<SSTableRecord> rList, String sFile) {
        mList = rList;

        load(sFile);
    }
}
