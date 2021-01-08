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


package com.westsword.stocks.analyze.sam;

import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.regression.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class SAm {
    public static int START_BASED = 0;
    public static int END_BASED = 1;

    public static double getCorrel(ArrayList<Double> l0, ArrayList<Double> l1, int type, 
            int length) {
        int l0Size = l0.size();
        int l1Size = l1.size();
        List<Double> m0=null, m1=null;
        if(type == END_BASED) {
            m0 = l0.subList(l0Size-length, l0Size);
            m1 = l1.subList(l1Size-length, l1Size);
        } else {
            m0 = l0.subList(0, length);
            m1 = l1.subList(0, length);
        }
        Double[] X = m0.toArray(new Double[0]);
        Double[] Y = m1.toArray(new Double[0]);
        double[] x = ArrayUtils.toPrimitive(X);
        double[] y = ArrayUtils.toPrimitive(Y);
   
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double correl = pc.correlation(x, y);
  
        return correl;
    }
    public static double getCorrel(ArrayList<Double> l0, ArrayList<Double> l1, int type) {
        int l0Size = l0.size();
        int l1Size = l1.size();
        int minSize = l0Size;
        if(minSize > l1Size)
            minSize = l1Size;
 
        return getCorrel(l0, l1, type, minSize);
    }

    public static boolean isAllGt0(ArrayList<Double> amderList) {
        boolean bAllGt0 = true;

        for(int i=0; i<amderList.size(); i++) {
            if(amderList.get(i)<0)
                return false;
        }

        return bAllGt0;
    }
    public static boolean isAllLt0(ArrayList<Double> amderList) {
        boolean bAllLt0 = true;

        for(int i=0; i<amderList.size(); i++) {
            if(amderList.get(i)>0)
                return false;
        }

        return bAllLt0;
    }

    public static double[] getSlopeR2wMin(ArrayList<Double> listR) {
        double min = Collections.min(listR); 
        int minIdx = listR.indexOf(min);
        ArrayList<Double> newListR = new ArrayList<Double>(listR.subList(0, minIdx+1));

        return getSlopeR2(newListR);
    }
    public static double[] getSlopeR2(ArrayList<Double> listR) {
        double[] vals = new double[2];

        SimpleRegression sr = new SimpleRegression();
        for(int i=0; i<listR.size(); i++) {
            sr.addData((double)i, listR.get(i));
        }
        vals[0] = sr.getSlope();
        vals[1] = sr.getRSquare();

        return vals;
    }

}

