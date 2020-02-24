package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.time.StockDates;

public class SSiManager {
    public final static int MaxThreads = 8;

    private boolean mRunPseudoTask = false;
    private volatile int mConcurrent;

    public SSiManager() {
        this(false);
    }
    public SSiManager(boolean bRunPsedoTask) {
        mRunPseudoTask = bRunPsedoTask;
        mConcurrent= 0;
    }

    public boolean getRunPseudoTask() {
        return mRunPseudoTask;
    }
    public synchronized void onThreadStarted() {
        mConcurrent++;
    }
    public synchronized void onThreadFinished() {
        mConcurrent--;
    }
    public synchronized int getConcurrent() {
        return mConcurrent;
    }

    public void run() {
        run(null, null, null, false, false, false);
    }
    public void run(SSInstance ssi, AmManager am, StockDates stockDates,
            boolean bLog2Files, boolean bResetLog, boolean bPrintTradeDetails) {
        while(getConcurrent()>=MaxThreads) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Thread t = new SSiThread(this, ssi, am, stockDates, 
                bLog2Files, bResetLog, bPrintTradeDetails);
        t.start();
    }

    public static class SSiThread extends Thread {
        private SSiManager mM;
        private SSInstance mSSi;

        private AmManager am;
        private StockDates stockDates;
        private boolean bLog2Files;
        private boolean bResetLog;
        private boolean bPrintTradeDetails;

        public SSiThread(SSiManager m, SSInstance ssi, AmManager am, StockDates stockDates, 
                boolean bLog2Files, boolean bResetLog, boolean bPrintTradeDetails) {
            mM = m;
            mSSi = ssi;

            this.am = am;
            this.stockDates = stockDates;
            this.bLog2Files = bLog2Files;
            this.bResetLog = bResetLog;
            this.bPrintTradeDetails = bPrintTradeDetails;

            //inc counter
            mM.onThreadStarted();
        }
        public void run() {
            //do job
            runTask();

            //dec counter when job is done
            mM.onThreadFinished();
        }


        private void runTask() {
            if(mM.getRunPseudoTask())
                pseduoTask();

            //run ssinstance
            if(mSSi!=null)
                mSSi.run(am, stockDates, bLog2Files, bResetLog, bPrintTradeDetails);
        }
        
        private void pseduoTask() {
            Random r = new Random();
            int interval = r.nextInt(10000);
            System.out.format("%s: interval=%d\n", Utils.getCallerName(getClass()), interval);
            try {
                Thread.sleep(interval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
