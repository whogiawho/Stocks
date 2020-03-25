package com.westsword.stocks.base;


import java.util.*;

import com.westsword.stocks.base.time.Time;

public class TaskManager {
    public final static int MaxThreads = Settings.getMaxTasks();

    private volatile int mConcurrent;

    public TaskManager() {
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

    public void maxThreadsCheck() {
        while(getConcurrent()>=MaxThreads) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //System.out.format("%s %s: mConcurrent=%d\n", Time.current(), Utils.getCallerName(getClass()), mConcurrent);
    }

    public void run() {
        maxThreadsCheck();

        Task t = new Task(this);
        t.start();
    }
}
