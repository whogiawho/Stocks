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

public class ColLoader extends FileLoader {
    private int mIdxCol;
    private ArrayList<String> mLines = null;

    public boolean onLineRead(String line, int count) {
        String[] fields=line.split(" +");

        if(mIdxCol<fields.length&&mLines != null) {
            mLines.add(fields[mIdxCol]);
        }

        return true;
    }

    public void load(ArrayList<String> lines, String sFile, int idxCol) {
        mIdxCol = idxCol;
        mLines = lines;

        load(sFile);
    }
}
