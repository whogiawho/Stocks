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
 
 
package com.westsword.stocks.tools.helper;

import java.util.*;

public class AvgAmRecord {
    public String stockCode;
    public String tradeDate;
    public String hms;
    public double correl0;           //delta correl with previous sd
    public double upPrice;
    public double downPrice;

    public double correl1;

    public AvgAmRecord(String[] fields) {
        stockCode = fields[0];
        tradeDate = fields[1];
        hms = fields[2];
        correl0 = Double.valueOf(fields[3]);
        upPrice = Double.valueOf(fields[4]);
        downPrice = Double.valueOf(fields[5]);

        if(fields.length>=7)
            correl1=Double.valueOf(fields[6]);
        else
            correl1=Double.NaN;
    }

    public static ArrayList<AvgAmRecord> getList(String sAvgAmDeltaFile) {
        AvgAmRecordLoader l = new AvgAmRecordLoader();
        ArrayList<AvgAmRecord> aarList = new ArrayList<AvgAmRecord>();
        l.load(sAvgAmDeltaFile, aarList);

        return aarList;
    }
}

