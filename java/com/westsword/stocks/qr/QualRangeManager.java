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
 
 
package com.westsword.stocks.qr;


import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.tools.helper.*;

public class QualRangeManager {
    private ArrayList<QualRange> mqrList = new ArrayList<QualRange>();

    private SdTime1 mSdTime;
    private AmManager mAm;


    public QualRangeManager(String stockCode) {
        mSdTime = new SdTime1(stockCode);
        mAm = new AmManager(stockCode);
    }


    public int getQRSize() {
        return mqrList.size();
    }
    public ArrayList<QualRange> getQRList() {
        return mqrList;
    }
    public void setSdLength(int sdLength) {
        for(int i=0; i<mqrList.size(); i++)
            mqrList.get(i).setSdLength(sdLength);
    }


    public void load(String sqrFile) {
        QualRangeLoader loader = new QualRangeLoader();
        loader.load(sqrFile, mqrList);
    }
    public double[][] getCorrMatrix(CmManager cm) {
        double[][] m = mAm.getAmMatrix(mqrList);
        return cm.getCorrMatrix(m);
    }
    public void getMatchedSet(double[][] corrM, TreeSet<QualRange> qrSet, double threshold) {
        for(int i=0; i<corrM.length; i++) {
            int count = 0;
            QualRange qr = mqrList.get(i);
            TreeSet<String> tdSet = new TreeSet<String>();
            for(int j=0; j<corrM[i].length; j++) {
                if(corrM[i][j]>=threshold) {
                    count++;
                    tdSet.add(mqrList.get(j).getEndDate());
                }
            }
            qr.setMatchedQrCnt(count);
            qr.setMatchedTdCnt(tdSet.size());
            qrSet.add(new QualRange(qr));
        }
    }


    public void printStartYMDHMS() {
        for(int i=0; i<mqrList.size(); i++) {
            QualRange qr = mqrList.get(i);
            String startDate = qr.getStartDate(mSdTime);
            String startHMS = qr.getStartHMS(mSdTime);
            String endDate = qr.getEndDate();
            String endHMS = qr.getEndHMS();
            System.out.format("%s %s %s %s\n", startDate, startHMS, endDate, endHMS);
        }
    }
}

