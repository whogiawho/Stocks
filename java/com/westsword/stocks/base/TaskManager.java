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
 
 
package com.westsword.stocks.base;


import com.westsword.stocks.base.time.Time;

public class TaskManager {
    public int mMaxThreads; 

    private volatile int mConcurrent;

    public TaskManager(int maxThreads) {
        mConcurrent= 0;
        mMaxThreads = maxThreads;
    }
    public TaskManager() {
        this(Settings.getMaxTasks());
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
        while(getConcurrent()>=mMaxThreads) {
            Utils.sleep(10);
        }
        //System.out.format("%s %s: mConcurrent=%d\n", Time.current(), Utils.getCallerName(getClass()), mConcurrent);
    }

    public void run() {
        maxThreadsCheck();

        Task t = new Task(this);
        t.start();
    }

}
