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


package com.westsword.stocks.analyze.sam2;

import java.util.*;

import com.westsword.stocks.am.*;
import com.westsword.stocks.analyze.sam.*;

public class Offset {
    private ArrayList<SAm> negativeSAmList;
    private ArrayList<SAm> positiveSAmList;
    private TreeMap<SAm, Double> posThresMap;
    private TreeMap<SAm, Double> negThresMap;

    public Offset() {
        negativeSAmList = new ArrayList<SAm>();
        positiveSAmList = new ArrayList<SAm>();
        negThresMap = new TreeMap<SAm, Double>();
        posThresMap = new TreeMap<SAm, Double>();
    }
    public ArrayList<SAm> getNegativeSAms() {
        return negativeSAmList;
    }
    public ArrayList<SAm> getPositiveSAms() {
        return positiveSAmList;
    }
    public double getPosThreshold(SAm sam) {
        return posThresMap.get(sam);
    }
    public double getNegThreshold(SAm sam) {
        return negThresMap.get(sam);
    }



    public boolean negFilterByCorrel(Segment maxS, SAm dstSAm) {
        boolean bFiltered = false;

        ArrayList<SAm> negativeSAmList = getNegativeSAms();
        //System.out.format("negativeSAmList.size()=%d\n", negativeSAmList.size());
        for(int i=0; i<negativeSAmList.size(); i++) {
            SAm srcSAm = negativeSAmList.get(i);
            Segment maxS0 = srcSAm.getSegmentOfMaxLen();
            double correl = SAm.getCorrel(maxS.eList, maxS0.eList, SAm.START_BASED);
            double threshold = getNegThreshold(srcSAm);

            if(correl>threshold) {
                //System.out.format("%s: %s %8.3f\n", dstSAm, srcSAm, correl);
                bFiltered = true;
                break;
            }
        }

        return bFiltered;
    }
    public boolean posFilterByCorrel(Segment maxS, SAm dstSAm) {
        boolean bFiltered = true;

        ArrayList<SAm> positiveSAmList = getPositiveSAms();
        //System.out.format("positiveSAmList.size()=%d\n", positiveSAmList.size());
        for(int i=0; i<positiveSAmList.size(); i++) {
            SAm srcSAm = positiveSAmList.get(i);
            Segment maxS0 = srcSAm.getSegmentOfMaxLen();
            double correl = SAm.getCorrel(maxS.eList, maxS0.eList, SAm.START_BASED);
            double threshold = getPosThreshold(srcSAm);

            if(correl>threshold) {
                //System.out.format("%s: %s %8.3f\n", dstSAm, srcSAm, correl);
                bFiltered = false;
                break;
            }
        }

        return bFiltered;
    }
    public boolean filterByCorrel(Segment maxS, SAm dstSAm, boolean bSkipNeg) {
        boolean bFiltered = false;

        if(!bSkipNeg) {
            bFiltered = negFilterByCorrel(maxS, dstSAm);
            if(bFiltered)
                return bFiltered;    //return if negFilter==true
        }

        bFiltered = posFilterByCorrel(maxS, dstSAm);

        return bFiltered;
    }
    public void makeThresMap() {
        ArrayList<SAm> negLists = getNegativeSAms();
        ArrayList<SAm> posLists = getPositiveSAms();
        double[][] m = getInterCorrelMatrix(negLists, posLists);
        setNegThresMap(negLists, posLists, m);
        setPosThresMap(negLists, posLists, m);
    }
    private double[][] getInterCorrelMatrix(ArrayList<SAm> negLists, ArrayList<SAm> posLists) {
        double[][] m = new double[negLists.size()][posLists.size()];
        for(int i=0; i<negLists.size(); i++) {
            SAm negL = negLists.get(i);
            Segment maxS0 = negL.getSegmentOfMaxLen();

            for(int j=0; j<posLists.size(); j++) {
                SAm posL = posLists.get(j);
                Segment maxS1 = posL.getSegmentOfMaxLen();

                double correl = SAm.getCorrel(maxS0.eList, maxS1.eList, SAm.START_BASED);
                m[i][j] = correl;
            }
        }

        return m;
    }
    private void setNegThresMap(ArrayList<SAm> negLists, ArrayList<SAm> posLists, 
            double[][] m) {
        for(int i=0; i<negLists.size(); i++) {
            double maxCorrel = Double.NEGATIVE_INFINITY;
            for(int j=0; j<posLists.size(); j++) {
                if(m[i][j] > maxCorrel)
                    maxCorrel = m[i][j];
            }
            negThresMap.put(negLists.get(i), maxCorrel);
            //System.out.format("neg %s: %8.3f\n", negLists.get(i), maxCorrel);
        }
    }
    private void setPosThresMap(ArrayList<SAm> negLists, ArrayList<SAm> posLists, 
            double[][] m) {
        for(int i=0; i<posLists.size(); i++) {
            double maxCorrel = Double.NEGATIVE_INFINITY;
            for(int j=0; j<negLists.size(); j++) {
                if(m[j][i] > maxCorrel)
                    maxCorrel = m[j][i];
            }
            posThresMap.put(posLists.get(i), maxCorrel);
            //System.out.format("pos %s: %8.3f\n", posLists.get(i), maxCorrel);
        }
    }





    public boolean filterBySlopeAndInTime(double slope, SAm dstSAm) {
        boolean bFiltered = false;

        return bFiltered;
    }
}
