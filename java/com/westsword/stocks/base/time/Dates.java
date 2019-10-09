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
    //return null if workDate is the last
    public String nextDate(String workDate) {
        return mDatesSet.higher(workDate);
    }
    public String prevDate(String workDate) {
        return mDatesSet.lower(workDate);
    }
    //workData0&workDate1 of format: YYYYMMDD
    public int getDistance(String workDate0, String workDate1) {
        int dist = 0;

        boolean reversed = false;
        int result = workDate0.compareTo(workDate1);
        String startDate = workDate0;
        String endDate = workDate1;
        if(result > 0) {
            startDate = workDate1;
            endDate = workDate0;
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
    //dates between [startDate, endDate] should be returned
    public String[] getDateList(String startDate, String endDate) {
        NavigableSet<String> set0 = mDatesSet.subSet(startDate, true, endDate, true);
        return (String[])set0.toArray(new String[set0.size()]);
    }
    //dates between [firstDate, endDate] should be returned
    public String[] getDateList(String endDate) {
        return getDateListExclude(firstDate(), endDate);
    }
    //dates between [startDate, endDate) should be returned
    public String[] getDateListExclude(String startDate, String endDate) {
        NavigableSet<String> set0 = mDatesSet.subSet(startDate, true, endDate, false);
        return (String[])set0.toArray(new String[set0.size()]);
    }
    //exclude dates which are later than or equal to endDate 
    public String[] getDateListExclude(String endDate) {
        return getDateListExclude(firstDate(), endDate);
    }
    public TreeSet<String> lastNDates(int n) {
        return lastNDates(lastDate(), n);
    }
    public TreeSet<String> lastNDates(String endDate, int n) {
        TreeSet<String> set = new TreeSet<String>();
        if(n <= 0)
            return set;

        NavigableSet<String> set0 = mDatesSet.headSet(endDate, true);
        if(set0 == null)
            return set;

        Iterator<String> itr = set0.descendingIterator();
        int count=0;
        while(itr.hasNext()) {
            set.add(itr.next());
            count++;

            if(count >= n)
                break;
        }

        return set;
    }




    private void addDates(TreeSet<String> datesSet, String[] sDates) {
        for(int i=0; i<sDates.length; i++) {
            datesSet.add(sDates[i]);
        }
    }
}
