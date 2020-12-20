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


import java.io.*;
import java.util.*;
import org.apache.commons.io.FileUtils;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.*;
import com.westsword.stocks.base.Settings;

public class AmRateWriter extends Thread {
    private String sRateFile = StockPaths.getAmRatePngFile();
    private CopyManager mCopyMan;

    public AmRateWriter(CopyManager m) {
        mCopyMan = m;
    }

    public void run() {
        _start();
    }
    public void _start() {
        Utils.randSleep();

        //randomly select a png file from derivativePng/
        String sPngDir = StockPaths.getDerivativePngDir();
        String[] sFiles = Utils.getSubNames(sPngDir);
        int idx = new Random().nextInt(sFiles.length);
        String sSrcFile = sFiles[idx];
        sSrcFile = sPngDir + sSrcFile;

        mCopyMan.requestCopy(sSrcFile, sRateFile);
    }

}
