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
    public void getMatchedSet(double[][] cm, TreeSet<QualRange> qrSet, double threshold) {
        for(int i=0; i<cm.length; i++) {
            int count = 0;
            QualRange qr = mqrList.get(i);
            for(int j=0; j<cm[i].length; j++) {
                if(cm[i][j]>=threshold) {
                    count++;
                }
            }
            qr.setMatchedCnt(count);
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

