package com.westsword.stocks.qr;


import java.util.*;

import com.westsword.stocks.base.utils.FileLoader;

public class QualRangeLoader extends FileLoader {
    private ArrayList<QualRange> mqrList = null;

    public boolean onLineRead(String line, int count) {
        if(line.matches("^ *#.*")||line.matches("^ *$"))
            return true;

        String[] fields=line.split(" +");
        String[] subFileds = fields[0].split("_");
        String tradeDate = subFileds[0];
        String hms = subFileds[1];

        QualRange qr = new QualRange(tradeDate, hms);
        if(mqrList != null)
            mqrList.add(qr);

        return true;
    }

    public void load(String sqrFile, ArrayList<QualRange> qrList) {
        mqrList = qrList;

        load(sqrFile);
    }
}

