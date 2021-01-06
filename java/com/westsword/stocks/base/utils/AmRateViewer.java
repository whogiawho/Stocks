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


import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;

public class AmRateViewer extends Thread {
    private String sRateFile = StockPaths.getAmRatePngFile();
    private boolean bAmRateViewerStarted = false;

    public void run() {
        _start();
    }
    public void _start() {
        while(!bAmRateViewerStarted) {
            if(sRateFile!=null && Utils.isFile(sRateFile)) {
                String sAmRateViewer = Settings.getAmRateViewer();
                //command to start AmRateViewer
                try {
                    String sCommand = "";
                    sCommand += "\"" + sAmRateViewer + "\"";
                    sCommand += " " + sRateFile;
                    System.out.format("%s: %s\n", 
                            Utils.getCallerName(getClass()), sCommand);
                    String[] cmd = {"cmd", "/C", sCommand};
                    Process proc = Runtime.getRuntime().exec(cmd);

                    bAmRateViewerStarted = Utils.wait4Alive(proc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.format("%s: thread AmRateViewer quitted!\n", 
                Utils.getCallerName(getClass()));
    }
}
