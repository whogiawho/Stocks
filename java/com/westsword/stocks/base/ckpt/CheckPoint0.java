package com.westsword.stocks.base.ckpt;


import java.util.*;

import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.ckpt.*;
import com.westsword.stocks.base.Settings;

public class CheckPoint0 extends CheckPoint {
    //interval - in seconds
    public CheckPoint0(int interval, String endHMS) {
        super();

        String currentDate = Time.currentDate();

        //add 09:25:00
        add(AStockSdTime.getCallAuctionEndTime0());

        //add 09:30:00, 11:30:00
        long startTp = Time.getSpecificTime(currentDate, AStockSdTime.getOpenQuotationTime());
        long endTp = Time.getSpecificTime(currentDate, AStockSdTime.getMidSuspensionTime());
        add(startTp, endTp, interval);

        //add 13:00:00, endHMS
        startTp = Time.getSpecificTime(currentDate, AStockSdTime.getMidOpenQuotationTime());
        endTp = Time.getSpecificTime(currentDate, endHMS);
        add(startTp, endTp, interval);
    }

    public CheckPoint0() {
        this(Settings.getCkptInterval(), "14:56:00");
    }
}
