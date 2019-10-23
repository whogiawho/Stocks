package com.westsword.stocks.base.time;

import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.base.time.Time;


//those dates excluding:
//  Sat&Mon
public class WorkDates extends Dates {
    public WorkDates(String start, String end) {        //start&end must be of format YYYYMMDD
        Calendar cStart = Utils.getCalendar(start);
        Calendar cEnd = Utils.getCalendar(end);

        while(cStart.compareTo(cEnd)<=0) {
            int day = cStart.get(Calendar.DAY_OF_WEEK);
            if(day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
                add(Time.getTimeYMD(cStart, false));
            }
            cStart.add(Calendar.DATE, 1);
        }
    }
}
