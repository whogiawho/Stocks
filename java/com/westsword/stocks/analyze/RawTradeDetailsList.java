 /*
 Copyright (C) 2019-2050 WestSword, Inc.
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

import java.io.*;
import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.FileLoader;
import com.westsword.stocks.base.ConvertDD2Double;

public class RawTradeDetailsList extends FileLoader {
    private ConvertDD2Double cDD = new ConvertDD2Double();
    private ArrayList<RawTradeDetails> mRawTradeDetailsList = null;

    public RawTradeDetailsList(){
    }


    public boolean onLineRead(String line, int counter) {
                String[] fields=line.split(" +");
                //[0] time; [1] price; [2] count; [3] type
                long time = Long.parseLong(fields[0], 16);
                double price = cDD.sub_48A0D0(fields[1]);
                int count = Integer.valueOf(fields[2]);
                int type = Integer.valueOf(fields[3]);
                
                RawTradeDetails rawTradeDetails = new RawTradeDetails(time, price, count, type);
                if(mRawTradeDetailsList!=null)
                    mRawTradeDetailsList.add(rawTradeDetails);

                return true;
    }

    public void load(ArrayList<RawTradeDetails> rawDetailsList, String sRawTradeDetailsFile) {
        mRawTradeDetailsList = rawDetailsList;

        load(sRawTradeDetailsFile);
    }

    //sometimes idag hexin will quit because of exception
    //this method is for reloading previous raw tradedetails data
    public void loadPrevRawDetails(ArrayList<RawTradeDetails> rawDetailsList, String rawTradeDetailsDir) {
        mRawTradeDetailsList = rawDetailsList;

        File folder = new File(rawTradeDetailsDir);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if(listOfFiles[i].isFile()) {
                String sRawTradeDetailsFile = listOfFiles[i].getAbsolutePath();
                load(sRawTradeDetailsFile);
            } else if (listOfFiles[i].isDirectory()) {
                System.out.format("%s: %s\n", 
                        Utils.getCallerName(getClass()), "Directory " + listOfFiles[i].getAbsolutePath());
            }
        }

        //sort mRawTradeDetailsList
        Collections.sort(mRawTradeDetailsList, new Comparator<RawTradeDetails>() {
                @Override
                public int compare(RawTradeDetails s1, RawTradeDetails s2) {
                    return Long.valueOf(s1.time).compareTo(s2.time);
                }
        });
    }

}
