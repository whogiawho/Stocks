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
 
 
package com.westsword.stocks.tools.matlab;


import java.util.*;
import com.mathworks.engine.MatlabEngine;
import java.util.concurrent.ExecutionException;

import com.westsword.stocks.am.AmUtils;
import com.westsword.stocks.tools.helper.SSUtils;

public class MatlabSync {
    public static void run() {
        try {
            MatlabEngine eng = MatlabEngine.startMatlab();

            String stockCode = "600030";
            String startDate = "20090105";
            String hmsList = "113000_130000";

            double[][] m = AmUtils.getAmMatrix(stockCode, startDate, hmsList);
            System.out.format("m.height=%d m.width=%d\n", m.length, m[0].length);

            long start = System.currentTimeMillis();
            double[][] rm = eng.feval("corrcoef", (Object)m);
            System.out.format("rm.height=%d rm.width=%d\n", rm.length, rm[0].length);
            long end = System.currentTimeMillis();
            System.out.format("MatlabSync.run: matlab.corrcoef duration=%4d\n", 
                    end-start);

            ArrayList<String> tradeDateList = new ArrayList<String>();
            SSUtils.getSimilarTradeDates(stockCode, startDate, SSUtils.Default_Threshold,
                    startDate, rm, tradeDateList);
            System.out.format("%s\n", tradeDateList.toString());
            
            eng.close();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
