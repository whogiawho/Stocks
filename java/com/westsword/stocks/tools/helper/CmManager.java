package com.westsword.stocks.tools.helper;


import java.util.*;
import java.util.concurrent.*;
import com.mathworks.engine.MatlabEngine;
import com.mathworks.engine.EngineException;
import java.util.concurrent.ExecutionException;
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.am.*;
import com.westsword.stocks.base.ckpt.*;
import com.westsword.stocks.base.time.*;

public class CmManager {
    public final static int MAX_CORRMATRIX_BUFF_SIZE = 10;

    private MatlabEngine mEng = null;
    private Worker mWThread = null;
    //key = stockCode+startDate+hmsList;
    private ConcurrentHashMap<String, double[][]> cmMap = new ConcurrentHashMap<String, double[][]>();

    public CmManager() {
        try {
            mEng = MatlabEngine.startMatlab();
        } catch (EngineException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public double[][] getCorrMatrix(String stockCode, String startDate, String hmsList, 
            AmManager am) {
        double[][] cm = null;

        String key = stockCode+startDate+hmsList;
        if(cmMap.get(key)!=null) {
            cm = cmMap.remove(key);
            //notify the worker an element is removed
            if(mWThread!=null) {
                synchronized(mWThread) {
                    mWThread.notify();
                }
            }

            return cm;
        }

        try {
            String[] sTradeDates = new TradeDates(stockCode, startDate).getAllDates();
            double[][] m0 = am.getAmMatrix(hmsList, sTradeDates);

            cm = mEng.feval("corrcoef", (Object)m0);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return cm;
    }

    public void close() {
        try {
            if(mEng!=null)
                mEng.close();
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }


    //start a thread to fill cmMap
    public void startWorker(String stockCode, String startDate, 
            String startHMSList, String endHMSList, AmManager am) {
        String[] sTradeDates = new TradeDates(stockCode, startDate).getAllDates();
        String key0 = stockCode+startDate;

        mWThread = new Worker(sTradeDates, startHMSList, endHMSList, am, key0);
        mWThread.start();

        //wait until the cmMap.size()>=1
        while(cmMap.size()<1);
    }

    public class Worker extends Thread {

        //mEng&cmMap are outer members of CmManager
        private String[] sTradeDates;
        private String startHMSList;
        private String endHMSList;
        private AmManager am;
        private String key0;

        public Worker(String[] sTradeDates, String startHMSList, String endHMSList, 
                AmManager am, String key0) {
            this.sTradeDates = sTradeDates;
            this.startHMSList = startHMSList;
            this.endHMSList = endHMSList;
            this.am = am;
            this.key0 = key0;
        }

        private void fillCmMap() {
            CheckPoint0 ckpt = new CheckPoint0();
            int[] startIdxs = null;
            if(startHMSList!=null)
                startIdxs = ckpt.getIdxList(startHMSList);
            int[] endIdxs = null;
            if(endHMSList!=null)
                endIdxs = ckpt.getIdxList(endHMSList);

            int length = ckpt.getLength();
            Combinations c = new Combinations(length, 2);
            Comparator<int[]> iC = c.comparator();
            Iterator<int[]> itr = c.iterator();
            while(itr.hasNext()) {
                //wait if cmMap.size>=5
                synchronized(this) {
                    try {
                        while(cmMap.size()>=MAX_CORRMATRIX_BUFF_SIZE)
                            wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
                int[] e = itr.next();
                String hmsList = ckpt.getHMSList(e);
                if(startIdxs!=null && iC.compare(e, startIdxs)<0) {
                    continue;
                }
                if(endIdxs!=null && iC.compare(e, endIdxs)>=0) {
                    break;
                }

                //save <key,cm> to cmMap
                try {
                    double[][] m0 = am.getAmMatrix(hmsList, sTradeDates);
                    double[][] cm = mEng.feval("corrcoef", (Object)m0);
                    String key = key0+hmsList;
                    cmMap.put(key, cm);
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        public void run() {
            setPriority(Thread.MAX_PRIORITY);
            fillCmMap();
        }
    }
}
