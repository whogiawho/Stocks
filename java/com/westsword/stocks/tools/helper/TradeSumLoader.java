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
 
 
package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.base.utils.*;

public class TradeSumLoader extends FileLoader {
    private String mText = null;
    private String mHMSList = null;

    private ArrayList<TradeSum> mList = null;


    private void setVars(String hmsList, String sText) {
        mHMSList = hmsList;
        mText = sText;
    }

    public boolean onLineRead(String line, int count) {
        if(line.matches("^ *#.*")||line.matches("^ *$"))
            return true;

        String[] fields=line.split(" +");
        if(mHMSList!=null&&mText!=null) {
            if(!fields[8].equals(mHMSList)) {
                mText += line + "\n";
            } 
        }
        if(mList != null) {
            TradeSum r = new TradeSum(fields);
            mList.add(r);
        }

        return true;
    }

    public void load(String sTradeSumeFile, String hmsList, String out[]) {
        setVars(hmsList, "");
        load(sTradeSumeFile);
        setVars(null, null);
        
        out[0] = mText;
    }

    public void load(String sTradeSumeFile, ArrayList<TradeSum> list) {
        mList = list;
        load(sTradeSumeFile);
        mList = null;
    }
}
