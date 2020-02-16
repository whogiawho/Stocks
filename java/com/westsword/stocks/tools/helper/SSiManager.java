package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.Utils;

public class SSiManager {
    public final static int MaxThreads = 3;

    private boolean mRunPsedoTask = false;
    private volatile int mConcurrent;

    public SSiManager() {
        this(false);
    }
    pblic SSiManager(boolean bRunPsedoTask) {
        mRunPsedoTask = bRunPsedoTask;
        mConcurrent= 0;
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
        while(getConcurrent()>=MaxThreads) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Thread t = new SSiThread(this);
        t.start();
    }

    public static class SSiThread extends Thread {
        private SSiManager mM;

        public SSiThread(SSiManager m) {
            mM = m;

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
            if(mRunPsedoTask)
                pseduoTask();
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
