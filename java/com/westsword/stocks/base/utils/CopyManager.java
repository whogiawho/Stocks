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


import java.util.concurrent.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;

public class CopyManager extends Thread {
    private volatile boolean bStopped;
    private ArrayBlockingQueue<CopyRequest> mRequestQueue; 

    public CopyManager() {
        bStopped = false;
        mRequestQueue = new ArrayBlockingQueue<CopyRequest>(Settings.getMaxTasks()*10);
    }
    public void requestCopy(String sSrcFile, String sDstFile) {
        try {
            mRequestQueue.put(new CopyRequest(sSrcFile, sDstFile));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void stopIt() {
        bStopped = true;
    }
    public void run() {
        try {
            while(!bStopped) {
                CopyRequest r = mRequestQueue.take();
                Utils.guardedCopy(r.sSrc, r.sDst, 5);
            }
        } catch (InterruptedException e) {
            String line = String.format("%s: CopyManager stopped!", Utils.getCallerName(getClass()));
            line = AnsiColor.getColorString(line, AnsiColor.ANSI_RED);
            System.out.format("%s\n", line);
            e.printStackTrace();
        }

        System.out.format("%s: quitted!", Utils.getCallerName(getClass()));
    }

    public static class CopyRequest {
        public String sSrc;
        public String sDst;

        public CopyRequest(String sSrc, String sDst) {
            this.sSrc = sSrc;
            this.sDst = sDst;
        }
    }
}
