package com.westsword.stocks.tools.helper;


import com.westsword.stocks.utils.*;

public class TradeSumLoader extends FileLoader {
    private String mText = "";
    private String mHMSList;


    private void reset(String hmsList) {
        mHMSList = hmsList;
        mText = "";
    }

    public boolean onLineRead(String line, int count) {
        if(line.matches("^ *#.*")||line.matches("^ *$"))
            return true;

        String[] fields=line.split(" +");
        if(!fields[8].equals(mHMSList)) {
            mText += line + "\n";
        } 

        return true;
    }
    public void load(String sTradeSumeFile, String hmsList, String out[]) {
        reset(hmsList);

        load(sTradeSumeFile);

        out[0] = mText;
    }
}
