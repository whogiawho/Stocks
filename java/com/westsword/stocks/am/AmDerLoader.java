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

import java.util.*;

import com.westsword.stocks.base.utils.FileLoader;

public class AmDerLoader extends FileLoader {
    private final static int DEFAULT_MAX_NA_ADJUSTED = 30;

    public AmDerLoader() {
        this(DEFAULT_MAX_NA_ADJUSTED);
    }
    public AmDerLoader(int maxNAAdjusted) {
        mMaxNAAdjusted = maxNAAdjusted;
    }

    private int mMaxNAAdjusted;
    private int mNACnt=0;
    private ArrayList<Double> mAmderList = null;
    private double mPrev = Double.NaN;
    public boolean onLineRead(String line, int counter) {
        String[] fields=line.split(" +");
        //[0] r2; [1] amder; 
        double r2= Double.valueOf(fields[0]);
        double amder = Double.NaN;
        String sAmder = fields[1];
        if(sAmder.compareTo("#N/A")!=0) {
            amder = Double.valueOf(sAmder);
            mPrev = amder;
        } else {
            if(mNACnt<mMaxNAAdjusted) {
                amder = mPrev;
                mNACnt++;
            }
        }

        mAmderList.add(amder);

        return true;
    }

    public void load(ArrayList<Double> amderList, String sAmDerFile) { 
        mAmderList = amderList;
        mPrev = Double.NaN;
        mNACnt=0;

        load(sAmDerFile);
    }
}
