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
import org.apache.commons.math3.util.Combinations;

public class HMSPath0 extends HMSPath {
    private Comparator<int[]> mC = null;
    private CheckPoint0 mCkpt = null;
    private int[] startIdxs = null;
    private int[] endIdxs = null;

    public HMSPath0() {
        super();

        mCkpt = new CheckPoint0();
        addCkpt(mCkpt);
        addCkpt(mCkpt);


        int length = mCkpt.getLength();
        mC = new Combinations(length, 2).comparator();
    }
    public HMSPath0(String startHMSList, String endHMSList) {
        this();

        if(startHMSList!=null)
            startIdxs = mCkpt.getIdxList(startHMSList);
        if(endHMSList!=null)
            endIdxs = mCkpt.getIdxList(endHMSList);
    }


    public void make(ArrayList<String> hmsList, int n) {
        if(n==mCkptList.size()) {
            String sPath = getHMSPath(hmsList);
            //convert sPath to int[], skip if it fall out of [startIdxs, endIdxs)
            int[] e = mCkpt.getIdxList(sPath);
            if(startIdxs!=null && mC.compare(e, startIdxs)<0) {
                return;
            }
            if(endIdxs!=null && mC.compare(e, endIdxs)>=0) {
                return;
            }

            mPathList.add(sPath);
            //System.out.format("%s\n", sPath);
        } else {
            CheckPoint ckpt = mCkptList.get(n);
            TreeSet<String> hmsSet = ckpt.get();
            for(String hms: hmsSet) {
                if(hmsList.size()!=0 && hms.compareTo(hmsList.get(hmsList.size()-1))<=0)
                    continue;

                hmsList.add(hms);
                make(hmsList, n+1);
                hmsList.remove(hms);
            }
        }
    }

    private String getHMSPath(ArrayList<String> hmsList) {
        String sPath = "";
        for(int i=0; i<hmsList.size(); i++) {
            sPath += hmsList.get(i) + " ";
        }
        sPath = sPath.trim();
        sPath = sPath.replace(" ", "_");

        return sPath;
    }
}
