package com.westsword.stocks.base.time;

import java.util.*;

public class Dates {
    private TreeSet<String> mDatesSet = new TreeSet<String>();

    public Dates() {
    }
    public Dates(ArrayList<String> datesList) {
        addAll(datesList);
    }
    public Dates(String[] sDates) {
        addDates(mDatesSet, sDates);
    }


    public boolean contains(String date) {
        return mDatesSet.contains(date);
    }
    public void add(String date) {
        mDatesSet.add(date);
    }
    public void addAll(Collection<String> c) {
        mDatesSet.addAll(c);
    }
    public void removeAll(Collection<String> c) {
        mDatesSet.removeAll(c);
    }
    public int size() {
        return mDatesSet.size();
    }
    public String[] getAllDates() {
        return (String[])mDatesSet.toArray(new String[mDatesSet.size()]);
    }
    public String firstDate() {
        return mDatesSet.first();
    }
    public String lastDate() {
        return mDatesSet.last();
    }
    //return the date whose distance is n from tradeDate if possible
    //when the distance is <n, the last tradedate is returned
    //note: null will never be returned
    public String nextDate(String tradeDate, int n) {
        String last = lastDate();

        String next = tradeDate;
        for(int i=0; i<n; i++) {
            if(next.equals(last))
                break;
            next = nextDate(next);
        }
        return next;
    }

    //asuumption for these methods:
    //  1. date0&date1 of format: YYYYMMDD; 
    //  2. both are valid members of mDatesSet
    //     otherwise unknown result may occur
    //methods:
    //  nextDate
    //  prevDate
    //  getDistance
    
    //return null if date is the last
    public String nextDate(String date) {
        return mDatesSet.higher(date);
    }
    public String prevDate(String date) {
        return mDatesSet.lower(date);
    }
    public String floor(String date) {
        return mDatesSet.floor(date);
    }
    public String ceiling(String date) {
        return mDatesSet.ceiling(date);
    }
    public int getDistance(String date0, String date1) {
        int dist = 0;

        boolean reversed = false;
        int result = date0.compareTo(date1);
        String startDate = date0;
        String endDate = date1;
        if(result > 0) {
            startDate = date1;
            endDate = date0;
            reversed = true;
            //System.out.format("%s: reversed is true!\n", Utils.getCallerName(getClass()));
        }
        while(!startDate.equals(endDate)) {
            startDate = nextDate(startDate);
            dist++;
        }
        if(reversed)
            dist=-dist;

        return dist;
    }




    private void addDates(TreeSet<String> datesSet, String[] sDates) {
        for(int i=0; i<sDates.length; i++) {
            datesSet.add(sDates[i]);
        }
    }

}
