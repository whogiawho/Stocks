 /*
 Copyright (C) 1989-2020 Free Software Foundation, Inc.
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

import com.westsword.stocks.base.time.Time;


//those dates excluding:
//  Sat&Mon
public class WorkDates extends Dates {
    public WorkDates(String start, String end) {        //start&end must be of format YYYYMMDD
        Calendar cStart = Time.getCalendar(start);
        Calendar cEnd = Time.getCalendar(end);

        while(cStart.compareTo(cEnd)<=0) {
            int day = cStart.get(Calendar.DAY_OF_WEEK);
            if(day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
                add(Time.getTimeYMD(cStart, false));
            }
            cStart.add(Calendar.DATE, 1);
        }
    }
}
