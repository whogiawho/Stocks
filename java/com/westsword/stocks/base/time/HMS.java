package com.westsword.stocks.base.time;

import com.westsword.stocks.Utils;

public class HMS {
    //hhmmss --> hh:mm:ss
    public static String formalize(String tradeTime) {
        if(tradeTime.contains(":"))
            return tradeTime;

        String sHMS;

        String hour = tradeTime.substring(0, 2);
        String minute = tradeTime.substring(2, 4);
        String second = tradeTime.substring(4, 6);

        sHMS = hour + ":" + minute + ":" + second;

        return sHMS;
    }
    //hh:mm:ss --> hhmmss
    public static String unformalize(String tradeTime) {
        return tradeTime.replace(":", "");
    }

    //convert "hh:mm:ss hh:mm:ss ... hh:mm:ss" to "hhmmss_hhmmss_..._hhmmss"
    public static String unformalizeList(String hmsList) {
        String sHMSList = hmsList;

        String regEx="[0-9]{6}_([0-9]{6}_){0,}[0-9]{6}";
        if(!hmsList.matches(regEx)) {
            sHMSList = "";
            String[] fields = hmsList.split(" +");
            for(int i=0; i<fields.length; i++) {
                String hms = unformalize(fields[i]);
                sHMSList += hms + "_";
            }
            sHMSList = sHMSList.substring(0, sHMSList.length()-1);
        }

        return sHMSList;
    }
    //convert hmsList of format hhmmss_hhmmss_... to "hh:mm:ss hh:mm:ss ..."
    public static String formalizeList(String hmsList) {
        String sHMSList = "";

        String regEx="[0-9]{6}_([0-9]{6}_){0,}[0-9]{6}";
        if(hmsList.matches(regEx)) {
            String[] fields = hmsList.split("_");
            for(int i=0; i<fields.length; i++) {
                String hms = formalize(fields[i]);
                sHMSList += hms + " ";
            }
            sHMSList = sHMSList.trim();
        }

        return sHMSList;
    }

}
