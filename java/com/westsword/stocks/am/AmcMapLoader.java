package com.westsword.stocks.am;


import java.util.concurrent.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.FileLoader;

public class AmcMapLoader extends FileLoader {
    private ConcurrentHashMap<String, Double> mAmCorrelMap = null;

    public boolean onLineRead(String line, int count) {
        try {
            String[] fields=line.split(" +");
            String key = fields[0];
            Double value = Double.valueOf(fields[1]);
               
            if(mAmCorrelMap != null)
                mAmCorrelMap.put(key, value);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.format("%s: line=%s, count=%d\n",
                    Utils.getCallerName(getClass()), line, count);
            e.printStackTrace();
        } 

        return true;
    }
    //load amCorrelMap from sFile
    public void load(ConcurrentHashMap<String, Double> map, String sFile) {
        mAmCorrelMap = map;

        load(sFile);
    }
}
