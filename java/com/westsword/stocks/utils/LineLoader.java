package com.westsword.stocks.utils;

import java.util.*;

import com.westsword.stocks.utils.FileLoader;

public class LineLoader extends FileLoader {
    private ArrayList<String> mLines = null;

    public boolean onLineRead(String line, int count) {
        if(mLines != null)
            mLines.add(line);

        return true;
    }

    public void load(ArrayList<String> lines, String sFile) {
        mLines = lines;

        load(sFile);
    }
}
