package com.westsword.stocks.analyze;

import java.io.*;
import java.util.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.utils.FileLoader;

public class RawRTPankouList extends FileLoader {
    private ArrayList<RawRTPankou> mRawRTPankouList = null;

    public RawRTPankouList(){
    }

    public boolean onLineRead(String line, int count) {
        RawRTPankou rawRTPankou = new RawRTPankou(line);
        if(mRawRTPankouList != null)
            mRawRTPankouList.add(rawRTPankou);

        return true;
    }

    public void load(ArrayList<RawRTPankou> rawPankouList, String sRawRTPankouFile) {
        mRawRTPankouList = rawPankouList;

        load(sRawRTPankouFile);
    }

    public void loadPrevPankou(ArrayList<RawRTPankou> rawPankouList, String rtPankouDir) {
        mRawRTPankouList = rawPankouList;

        File folder = new File(rtPankouDir);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if(listOfFiles[i].isFile()) {
                String sRawRTPankouFile = listOfFiles[i].getAbsolutePath();
                load(sRawRTPankouFile);
            } else if (listOfFiles[i].isDirectory()) {
                System.out.format("%s: %s\n", 
                        "Utils.getCallerName(getClass())", "Directory " + listOfFiles[i].getAbsolutePath());
            }
        }

        //sort rawDetailsList
        Collections.sort(mRawRTPankouList, new Comparator<RawRTPankou>(){
                @Override
                public int compare(RawRTPankou s1, RawRTPankou s2) {
                    return Long.valueOf(s1.mSecondsFrom1970Time).compareTo(s2.mSecondsFrom1970Time);
                }
        });
    }

}
