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
 
 
package com.westsword.stocks.am.avrate;

import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.StockPaths;

public class ThreadMakeAmVolR extends Thread {
    private String stockCode;
    private String tradeDate;
    private String hms;

    public ThreadMakeAmVolR(String stockCode, String tradeDate, String hms) {
        this.stockCode = stockCode;
        this.tradeDate = tradeDate;
        this.hms = hms;
    }

    public void run() {
        run(stockCode, tradeDate, hms);
    }

    public static void run(String stockCode, String tradeDate, String hms) {
        try {
            String sCommand = "";
            sCommand += "cscript";
            sCommand += " " + StockPaths.getMakeAmVolRPngVbs();
            sCommand += " " + StockPaths.getAmVolRFile(stockCode, tradeDate, hms);
            sCommand += " " + StockPaths.getAmVolRPngFile(stockCode, tradeDate, hms);
            //System.out.format("ThreadMakeAmVolR.run(): %s\n", sCommand);
            String[] cmd = {"cmd", "/C", sCommand};
            Process proc = Runtime.getRuntime().exec(cmd);
            int exitVal=Utils.wait4ExitValue(proc);
            if(exitVal!=0) {
                System.err.format("ThreadMakeAmVolR.run(): Process exitValue: %d\n", 
                        exitVal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
