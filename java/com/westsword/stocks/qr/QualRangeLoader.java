 /*
 Copyright (C) 1989-2020 Free Software Foundation, Inc.
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

