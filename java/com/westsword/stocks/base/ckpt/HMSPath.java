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
 
 
package com.westsword.stocks.base.ckpt;

import java.util.*;

import com.westsword.stocks.base.Utils;

public class HMSPath {
    protected ArrayList<CheckPoint> mCkptList;

    protected ArrayList<String> mPathList;

    public HMSPath() {
        mCkptList = new ArrayList<CheckPoint>();

        mPathList = new ArrayList<String>();
    }
    public HMSPath(HMSPath p) {
        this();
        add(p);
    }

    public void addCkpt(CheckPoint ckpt) {
        mCkptList.add(ckpt);
    }
    public void add(HMSPath p) {
        Iterator<String> itr = p.iterator();
        while(itr.hasNext()) {
            String s = itr.next();
            mPathList.add(s);
        }
    }

    public void make(String sPath, int n) {
        if(n==mCkptList.size()) {
            sPath = sPath.trim();
            sPath = sPath.replace(" ", "_");
            mPathList.add(sPath);
            //System.out.format("%s\n", sPath);
        } else {
            String sPath0 = new String(sPath);
            CheckPoint ckpt = mCkptList.get(n);
            TreeSet<String> hmsSet = ckpt.get();
            for(String hms: hmsSet) {
                sPath += hms + " ";
                make(sPath, n+1);
                sPath = sPath0;
            }
        }
    }

    public Iterator<String> iterator() {
        return mPathList.iterator();
    }

    public void print() {
        Iterator<String> itr = iterator();
        while(itr.hasNext()) {
            String s = itr.next();
            System.out.format("%s: %s\n", Utils.getCallerName(getClass()), s);
        }
    }
}
