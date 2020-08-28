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
 
 
package com.westsword.stocks.base.ckpt;

import java.util.*;

import com.westsword.stocks.base.time.AStockSdTime;

public class HMSPath1 extends HMSPath {
    public HMSPath1() {
        super();

        String sOQTime = AStockSdTime.getOpenQuotationTime();
        String sCQTime = AStockSdTime.getCloseQuotationTime();
        CheckPoint0 ckpt = new CheckPoint0(sCQTime);

        TreeSet<String> set0 = new TreeSet<String>(ckpt.get());
        set0.remove(set0.first());

        for(String e: set0) {
            CheckPoint start = ckpt.sub(e, true, sCQTime, true);
            CheckPoint end = ckpt.sub(e, true, e, true);
            HMSPath p = new HMSPath();
            p.addCkpt(start);
            p.addCkpt(end);
            p.make("", 0);
            add(p);
        }

    }
}

