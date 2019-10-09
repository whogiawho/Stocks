package com.westsword.stocks.base.time;

import java.util.*;

import com.westsword.stocks.base.time.Time;

public class WorkDates extends Dates {
    public WorkDates(String start, String end) {
        Calendar cStart = getCalendar(start);
        Calendar cEnd = getCalendar(end);

        while(cStart.compareTo(cEnd)<=0) {
            int day = cStart.get(Calendar.DAY_OF_WEEK);
            if(day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
                add(Time.getTimeYMD(cStart, false));
            }
            cStart.add(Calendar.DATE, 1);
        }
    }

    private Calendar getCalendar(String tradeDate) {
        tradeDate = Time.unformalizeYMD(tradeDate);
        int year = new Integer(tradeDate.substring(0, 4));
        int month = new Integer(tradeDate.substring(4, 6)) - 1;
        int date = new Integer(tradeDate.substring(6, 8));

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, date);

        return cal;
    }
}
