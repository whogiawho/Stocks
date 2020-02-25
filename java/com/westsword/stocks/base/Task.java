package com.westsword.stocks.base;

import java.util.*;

import com.westsword.stocks.Utils;

//override runTask() to do your own things
public class Task extends Thread {
    private TaskManager mM;

    public Task(TaskManager m) {
        mM = m;

        //inc counter
        mM.onThreadStarted();
    }
    public TaskManager getManager() {
        return mM;
    }
    public void run() {
        //do job
        runTask();

        //dec counter when job is done
        mM.onThreadFinished();
    }


    @Override 
    public void runTask() {
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

