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
