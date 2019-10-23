package com.westsword.stocks.base.time;

import java.util.*;

import com.westsword.stocks.Utils;
import com.westsword.stocks.base.time.Time;


public class NatureDates extends Dates {
    public NatureDates (String start, String end) {        //start&end must be of format YYYYMMDD
        Calendar cStart = Utils.getCalendar(start);
        Calendar cEnd = Utils.getCalendar(end);

        while(cStart.compareTo(cEnd)<=0) {
            add(Time.getTimeYMD(cStart, false));
            cStart.add(Calendar.DATE, 1);
        }
    }
}
