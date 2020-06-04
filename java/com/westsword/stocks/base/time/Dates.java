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


    //bCurrent=true:  consider currentDate as one of threshold for the last
    //bCurrent=false: does not consider currentDate as one of threshold for the last
    //bNullReturned=true:  null will be returned if distance is < n
    //bNullReturned=false: null will never be returned
    public String nextDate(String tradeDate, int n, boolean bNullReturned, boolean bCurrent) {
        String last = lastDate();
        if(bCurrent) {     
            String currentDate = Time.currentDate();
            //the possibility of tradeDate being last should also be considered
            if(tradeDate.compareTo(currentDate)>0)
                currentDate = tradeDate;
            currentDate = floor(currentDate);
            if(currentDate.compareTo(last)<0)
                last = currentDate;
        }

        String next = tradeDate;
        for(int i=0; i<n; i++) {
            if(next.equals(last)) {
                if(bNullReturned)
                    next=null;
                break;
            }
            next = nextDate(next);
        }
        return next;
    }
    public String nextDate(String tradeDate, int n, boolean bNullReturned) {
        return nextDate(tradeDate, n, bNullReturned, false);
    }
    //return the date whose distance is n from tradeDate if possible
    //when the distance is <n, the last tradedate is returned
    //note: null will never be returned
    public String nextDate(String tradeDate, int n) {
        return nextDate(tradeDate, n, false);
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
    public String nextDate(String date) {    //>
        return mDatesSet.higher(date);
    }
    public String prevDate(String date) {    //<
        return mDatesSet.lower(date);
    }
    public String floor(String date) {       //<=
        return mDatesSet.floor(date);
    }
    public String ceiling(String date) {     //>=
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
    public int getIndex(String tradeDate) {
        return getDistance(firstDate(), tradeDate);
    }
    public String getDate(int idx) {
        return getAllDates()[idx];
    }




    private void addDates(TreeSet<String> datesSet, String[] sDates) {
        for(int i=0; i<sDates.length; i++) {
            datesSet.add(sDates[i]);
        }
    }


    public static String nextYearLastDate(String tradeDate) {
        String y = tradeDate.substring(0, 4);
        int iy = Integer.valueOf(y);
        iy++;
        return "" + iy + "1231";
    }
}
