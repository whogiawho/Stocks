package com.westsword.stocks;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

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


    //append pankouYear to timeMissingYear, thus forming a time of format
    //    year-month-day hour:min:second
    //timeMissingYear[in]
    //    month-day hour:min:second
    public static long convertTime(String timeMissingYear, String pankouYear) {
        String[] list0 = timeMissingYear.split("-"); 
        String sMonth = list0[0]; 
        int month = new Integer(sMonth);
        String[] list1 = list0[1].split(" "); 
        String sDay = list1[0];
        int day = new Integer(sDay);
        String[] list2 = list1[1].split(":"); 
        String sHour = list2[0];
        int hour = new Integer(sHour);
        String sMinute = list2[1];
        int minute = new Integer(sMinute);
        String sSecond = list2[2];
        int second = new Integer(sSecond);

        //Year, Month, Day, Hour, Minute, Second
        Calendar cal = Calendar.getInstance();
        int year;
        if(pankouYear == null) {
            cal.setTimeInMillis(System.currentTimeMillis());
            year = cal.get(Calendar.YEAR);                             //get current year
        } else {
            year = new Integer(pankouYear);
        }
        cal.set(year, month-1, day, hour, minute, second);
               
        long millis = cal.getTimeInMillis()/1000;

        return millis;
    }





    public static String getCallerName(Class c) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String methodName = e.getMethodName();

        return c.getSimpleName() + "." + methodName;
    }













    public static int getIdx(String[] sArray, String s) {
        int idx = -1;

        for(int i=0; i<sArray.length; i++) {
            if(s.equals(sArray[i])) {
                idx = i;
                break;
            }
        }

        return idx;
    }

}
