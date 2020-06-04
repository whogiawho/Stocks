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


import com.westsword.stocks.base.Utils;

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


    public static String[] getHMSArray(String hmsList) {
        hmsList = formalizeList(hmsList);
        String[] fields = hmsList.split(" +");

        return fields;
    }

}
