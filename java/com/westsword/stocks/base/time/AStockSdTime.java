package com.westsword.stocks.base.time;


public class AStockSdTime extends SdTime {
    public AStockSdTime() {
        super();

        addRange("09:25:00", "09:25:00");
        addRange("09:30:00", "11:30:00");
        addRange("13:00:00", "15:00:00");
    }
}
