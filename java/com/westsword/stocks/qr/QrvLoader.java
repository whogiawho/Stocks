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
 
 
package com.westsword.stocks.qr;


import java.util.*;

import com.westsword.stocks.base.utils.FileLoader;

public class QrvLoader extends FileLoader {
    private ArrayList<Qrv> mqrvList = null;

    public boolean onLineRead(String line, int count) {
        if(line.matches("^ *#.*")||line.matches("^ *$"))
            return true;

        String[] fields=line.split(" +");
        String endTradeDate = fields[2];
        String endHMS = fields[3];
        double profit = Double.valueOf(fields[11]);

        //remaining fields
        String startTradeDate = fields[0];
        String startHMS = fields[1];
        String sOthers = "";
        for(int i=4; i<10; i++) {
            sOthers += " " + fields[i];
        }
        sOthers += String.format(" %8.3f", Double.valueOf(fields[10]));

        Qrv qrv = new Qrv(endTradeDate, endHMS, profit, 
                startTradeDate, startHMS, sOthers);
        if(mqrvList != null)
            mqrvList.add(qrv);

        return true;
    }

    public void load(String sqrvFile, ArrayList<Qrv> qrvList) {
        mqrvList = qrvList;

        load(sqrvFile);
    }
}

