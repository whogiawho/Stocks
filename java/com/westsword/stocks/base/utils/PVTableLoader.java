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
 
 
package com.westsword.stocks.base.utils;

import java.util.*;

public class PVTableLoader extends FileLoader {
    private TreeMap<Double, Long> mPVTable = null;

    public boolean onLineRead(String line, int count) {
        String[] fields=line.split(" +");
        //[0] price; [1] volume; 
        Double price = Double.valueOf(fields[0]);
        Long volume = Long.valueOf(fields[1]);

        if(mPVTable!= null)
            mPVTable.put(price, volume);

        return true;
    }

    public void load(TreeMap<Double, Long> pvTable, String sFile) {
        mPVTable = pvTable;

        load(sFile);
    }
}
