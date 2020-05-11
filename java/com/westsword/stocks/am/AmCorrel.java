package com.westsword.stocks.am;


import java.util.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;


public class AmCorrel {
    //get correl coefficient for 2 NavigableMaps, only considerig intersection
    public static double get(NavigableMap<Integer, AmRecord> map0, NavigableMap<Integer, AmRecord> map1) {
        double correl = 0;

        TreeSet<Integer> set = getCommonKeys(map0, map1);
        correl = get(map0, map1, set);
        //System.out.format("correl=%f\n", correl);

        return correl;
    }
    private static TreeSet<Integer> getCommonKeys(NavigableMap<Integer, AmRecord> map0, NavigableMap<Integer, AmRecord> map1) {
        //set0.retainAll will change set0, here we need to make sure map0.keySet() keeps unchanged
        //so a HashSet must be newed
        Set<Integer> set0 = new HashSet<Integer>(map0.keySet());
        Set<Integer> set1 = map1.keySet();

        set0.retainAll(set1);

        TreeSet<Integer> set = new TreeSet<Integer>();
        set.addAll(set0);

        return set;
    }
    private static double get(NavigableMap<Integer, AmRecord> map0, NavigableMap<Integer, AmRecord> map1, TreeSet<Integer> keySet) {
        double correl = Double.NaN;

        if(keySet.size() >= 2) {
            double[] x = new double[keySet.size()]; 
            double[] y = new double[keySet.size()];
            int i=0;
            for(Integer key: keySet) {
                x[i] = map0.get(key).am;
                y[i] = map1.get(key).am;
                i++;
            }
            correl = new PearsonsCorrelation().correlation(x, y);
        }

        return correl;
    }


}
