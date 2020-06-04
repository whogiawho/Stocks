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

public class ThreadRemoveAmc extends Thread {
    private ArrayList<String> tradeDateList;
    private String[] hms;

    public ThreadRemoveAmc(ArrayList<String> tradeDateList, String[] hms) {
        this.tradeDateList = tradeDateList;
        this.hms = hms;
    }

    public void run() {
        removeAmCorrels(tradeDateList, hms);
    }

    private void removeAmCorrels(ArrayList<String> tradeDateList, String[] hms) {
        for(int i=0; i<tradeDateList.size(); i++) {
            String tradeDate0 = tradeDateList.get(i);
            for(int j=0; j<tradeDateList.size(); j++) {
                String tradeDate1 = tradeDateList.get(j);
                AmcMap.removeAmCorrel(tradeDate0, tradeDate1, hms[0], hms[1]);
            }
        }
    }
}
