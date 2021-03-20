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
 
 
package com.westsword.stocks.analyze.ss;


import java.util.*;

import com.westsword.stocks.analyze.Table;
import com.westsword.stocks.base.utils.StockPaths;;

public class SSTable extends Table {
    public static String[] getTableNames() {
        return Table.getTableNames(StockPaths.getSSTableDir());
    }

    //only the records of stockCode are loaded
    public static void load(String stockCode, ArrayList<SSTableRecord> sstrList, String sName) {
        SSTableLoader loader = new SSTableLoader();
        String sSSTable = StockPaths.getSSTableFile(sName);
        loader.load(sstrList, sSSTable, sName, stockCode);
        System.out.format("%s: sSSTable=%s, size=%d\n", 
                "SSTable.load", sSSTable, sstrList.size());
    }

}

