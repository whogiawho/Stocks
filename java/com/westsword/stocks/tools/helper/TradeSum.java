package com.westsword.stocks.tools.helper;


import com.westsword.stocks.base.Settings;

public class TradeSum {
    public String tradeDate;
    public int matchedCnt;
    public double winRate;
    public double avgNetRevenue;
    public double avgMaxRevenue;
    public double expRisk0;
    public double expRisk1;
    public String sMatchedTradeDates;
    public String hmsList;
    public double actualAvgNetRevenue;

    public TradeSum(String[] fields) {
        tradeDate = fields[0];
        matchedCnt = Integer.valueOf(fields[1]);
        winRate = Double.valueOf(fields[2].substring(0, fields[2].length()-1))/100;
        avgNetRevenue = Double.valueOf(fields[3]);
        avgMaxRevenue = Double.valueOf(fields[4]);
        expRisk0 = Double.valueOf(fields[5]);
        expRisk1 = Double.valueOf(fields[6]);
        sMatchedTradeDates = fields[7];
        hmsList = fields[8];
        actualAvgNetRevenue = Double.valueOf(fields[9]);

        boolean bSwitchOfRawData = Settings.getSwitch(Settings.SWITCH_OF_RAW_DATA);
        if(bSwitchOfRawData) {
            String sFormat = "%s %4d %8.0f%% %8.3f %8.3f %8.3f %8.3f %s %s %8.3f\n";
            System.out.format(sFormat, 
                    tradeDate, matchedCnt, winRate*100, avgNetRevenue, avgMaxRevenue, expRisk0, expRisk1, 
                    sMatchedTradeDates, hmsList, actualAvgNetRevenue);
        }
    }
}
