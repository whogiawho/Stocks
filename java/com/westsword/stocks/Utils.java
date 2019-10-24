package com.westsword.stocks;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import com.westsword.stocks.base.time.Time;

import org.apache.commons.io.FileUtils;

public class Utils {
    public static boolean existFile(String path) {
        File f = new File(path);
        return f .exists();
    }
    //mkdir path if not exist
    public static void mkDir(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
    }
    public static void deleteFile(String path) {
        try{
            File file = new File(path);
            
            if(file.delete()){
                System.out.format("%s\n", file.getName() + " is deleted!");
            }else{
                System.out.format("Delete(%s) operation is failed.\n", path);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void append2File(String fileName, String line, boolean bAppend) {
        try {
            FileUtils.writeStringToFile(new File(fileName), line, StandardCharsets.UTF_8.name(), bAppend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void append2File(String fileName, String line) {
        append2File(fileName, line, true);
    }
    public static void resetDir(String sDir) {
        try {
            FileUtils.deleteDirectory(new File(sDir));
            //force creating dir
            FileUtils.forceMkdir(new File(sDir));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }





    public static String getCallerName(Class c) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String methodName = e.getMethodName();

        return c.getSimpleName() + "." + methodName;
    }















    public static Calendar getCalendar(String tradeDate) {
        tradeDate = Time.unformalizeYMD(tradeDate);
        int year = new Integer(tradeDate.substring(0, 4));
        int month = new Integer(tradeDate.substring(4, 6)) - 1;
        int date = new Integer(tradeDate.substring(6, 8));

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, date);

        return cal;
    }

}
