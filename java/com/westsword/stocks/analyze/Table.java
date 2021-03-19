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
 
 
package com.westsword.stocks.analyze;


import com.westsword.stocks.base.Utils;

public class Table {
    public static String[] getTableNames(String sDir) {
        String[] sTables = Utils.getSubNames(sDir);

        for(int i=0; i<sTables.length; i++) {
            String sTable = sTables[i];

            //pls make sure all files of ssTableDir are like *.txt
            //remove .txt
            sTables[i] = sTable.substring(0, sTable.length()-4);
        }

        return sTables;
    }
}

