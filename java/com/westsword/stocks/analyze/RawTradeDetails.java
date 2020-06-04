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
 
 
package com.westsword.stocks.analyze;


import java.util.*;

import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.Time;

public class RawTradeDetails {
    public long time;
    public double price;
    public int count;
    public int type;

    public RawTradeDetails(long time, double price, int count, int type){
        this.time=time;
        this.price=price;
        this.count=count;
        this.type=type;

        Calendar c = Time.getCalendar();
        c.setTimeInMillis(time*1000);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        String strTime = year+"-"+month+"-"+day+" ";
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        strTime += hour+":"+minute+":"+second;

        boolean bSwitchOfRawData = Settings.getSwitch(Settings.SWITCH_OF_RAW_DATA);
        if(bSwitchOfRawData) {
            String str = String.format("%20s %20f %8d %8d", strTime, price, count, type);
            System.out.println(str);
        }
    }

}
