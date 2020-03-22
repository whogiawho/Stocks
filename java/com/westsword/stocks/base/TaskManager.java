package com.westsword.stocks.base;


import java.util.*;

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
    }

    public void run() {
        maxThreadsCheck();

        Task t = new Task(this);
        t.start();
    }
}
