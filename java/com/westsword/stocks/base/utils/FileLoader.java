package com.westsword.stocks.base.utils;


import java.io.*;

import com.westsword.stocks.base.Utils;

public class FileLoader implements ILoadFile {

    public FileLoader() {
    }

    public void load(String sFileName) {
        int count = 0;
        String line = "";
        try { 
            BufferedReader reader = new BufferedReader(new FileReader(sFileName)); 

            while ((line = reader.readLine()) != null) {
                boolean bCont = onLineRead(line, count);
                if(bCont)
                    count++;
                else
                    break;
            }

            reader.close();
        } catch(FileNotFoundException e) {
            System.out.format("%s: %s not found\n", 
                    Utils.getCallerName(getClass()), sFileName);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.format("%s: count=%d\n", 
                    Utils.getCallerName(getClass()), count);
            System.out.format("%s: %s\n", 
                    Utils.getCallerName(getClass()), line);
        }
    }

    public boolean onLineRead(String line, int count) {
        System.out.format("%s\n", line);
        return true;
    }
}
