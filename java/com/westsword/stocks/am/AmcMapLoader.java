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
 
 
package com.westsword.stocks.am;


import java.util.concurrent.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.FileLoader;

public class AmcMapLoader extends FileLoader {
    private String tradeDate0;
    private String tradeDate1;
    private ConcurrentHashMap<String, Double> mAmCorrelMap = null;

    public boolean onLineRead(String line, int count) {
        try {
            String[] fields=line.split(" +");
            String key = fields[0];
            Double value = Double.valueOf(fields[1]);
               
            if(mAmCorrelMap != null)
                mAmCorrelMap.put(tradeDate0+","+tradeDate1+","+key, value);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.format("%s: line=%s, count=%d\n",
                    Utils.getCallerName(getClass()), line, count);
            e.printStackTrace();
        } 

        return true;
    }
    //load amCorrelMap from sFile
    public void load(ConcurrentHashMap<String, Double> map, 
            String sFile, String tradeDate0, String tradeDate1) {
        this.tradeDate0 = tradeDate0;
        this.tradeDate1 = tradeDate1;
        mAmCorrelMap = map;

        load(sFile);
    }
}
