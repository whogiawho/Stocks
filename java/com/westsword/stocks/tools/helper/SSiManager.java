package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Task;
import com.westsword.stocks.base.TaskManager;
import com.westsword.stocks.base.time.StockDates;

public class SSiManager extends TaskManager {

    public void run(SSInstance ssi, AmManager am, StockDates stockDates,
            boolean bLog2Files, boolean bResetLog, boolean bPrintTradeDetails) {
        maxThreadsCheck();

        Thread t = new SSiTask(this, ssi, am, stockDates, 
                bLog2Files, bResetLog, bPrintTradeDetails);
        t.start();
    }

    public static class SSiTask extends Task {
        private SSInstance mSSi;

        private AmManager am;
        private StockDates stockDates;
        private boolean bLog2Files;
        private boolean bResetLog;
        private boolean bPrintTradeDetails;

        public SSiTask(SSiManager m, SSInstance ssi, AmManager am, StockDates stockDates, 
                boolean bLog2Files, boolean bResetLog, boolean bPrintTradeDetails) {
            super(m);

            mSSi = ssi;

            this.am = am;
            this.stockDates = stockDates;
            this.bLog2Files = bLog2Files;
            this.bResetLog = bResetLog;
            this.bPrintTradeDetails = bPrintTradeDetails;
        }

        @Override
        public void runTask() {
            //run ssinstance
            if(mSSi!=null)
                mSSi.run(am, stockDates, bLog2Files, bResetLog, bPrintTradeDetails);
        }
    }
}
