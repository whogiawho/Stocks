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
 
 
package com.westsword.stocks.tools.helper.man;


import java.util.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Task;
import com.westsword.stocks.base.TaskManager;
import com.westsword.stocks.base.time.StockDates;

public class SSiManager extends TaskManager {

    public void run(SSInstance ssi, AmManager am, StockDates stockDates,
            boolean bLog2Files, boolean bResetLog, boolean bPrintTradeDetails) {
        run(ssi, am, stockDates, null,
                bLog2Files, bResetLog, bPrintTradeDetails);
    }
    public void run(SSInstance ssi, AmManager am, StockDates stockDates, double[][] corrM, 
            boolean bLog2Files, boolean bResetLog, boolean bPrintTradeDetails) {
        maxThreadsCheck();

        Thread t = new SSiTask(this, ssi, am, stockDates, corrM, 
                bLog2Files, bResetLog, bPrintTradeDetails);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    public static class SSiTask extends Task {
        private SSInstance mSSi;

        private AmManager am;
        private StockDates stockDates;
        double[][] corrM;
        private boolean bLog2Files;
        private boolean bResetLog;
        private boolean bPrintTradeDetails;

        public SSiTask(SSiManager m, SSInstance ssi, AmManager am, StockDates stockDates, double[][] corrM, 
                boolean bLog2Files, boolean bResetLog, boolean bPrintTradeDetails) {
            super(m);

            mSSi = ssi;

            this.am = am;
            this.stockDates = stockDates;
            this.corrM = corrM;
            this.bLog2Files = bLog2Files;
            this.bResetLog = bResetLog;
            this.bPrintTradeDetails = bPrintTradeDetails;
        }

        @Override
        public void runTask() {
            //run ssinstance
            if(mSSi!=null)
                mSSi.run(am, stockDates, corrM, 
                        bLog2Files, bResetLog, bPrintTradeDetails);
        }
    }
}
