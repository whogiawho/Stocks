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
 
 
package com.westsword.stocks.base;


import java.io.*;
import java.util.*;
import java.math.*;
import java.text.*;
import java.nio.charset.*;
import java.util.stream.*;
import java.util.concurrent.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.fitting.*;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.*;

public class Utils {
    public final static int EXEC_TIMEOUT_SECONDS = 60;
    public final static Random r = new Random();


    public static boolean guardedCopy(String sSrcFile, String sDstFile, int maxTry) {
        boolean bCopied = false;
        int i = 0;
        while(!bCopied && i<maxTry) {
            bCopied = copyFile(sSrcFile, sDstFile);
            i++;
            if(!bCopied) {
                System.err.format("%s: again try i=%d! %s\n", 
                        "Utils.guardedCopy", i, sSrcFile);
            }
        }

        return bCopied;
    }
    public static boolean copyFile(String sSrcFile, String sDstFile) {
        boolean bCopied = false;
        try {
            FileUtils.copyFile(new File(sSrcFile), new File(sDstFile));
            bCopied = true;
        } catch(Exception e){
            e.printStackTrace();
        }
        return bCopied;
    }
    public static boolean isFile(String path) {
        File f = new File(path);
        return f.isFile();
    }
    //mkdir path if not exist
    public static void mkDir(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
    }
    public static void deleteFile(String path) {
        boolean bSwitchOfRawData = Settings.getSwitch(Settings.SWITCH_OF_RAW_DATA);

        try{
            File file = new File(path);
            
            if(file.delete()){
                if(bSwitchOfRawData)
                    System.out.format("%s\n", file.getName() + " is deleted!");
            }else{
                if(bSwitchOfRawData)
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
    public static String[] getSubNames(String sDir) {
        File fDir = new File(sDir);
        String[] sNames = fDir.list();
        //System.out.format("size=%d\n", sNames.length);
	    TreeSet<String> trSet = new TreeSet<String>(Arrays.asList(sNames));
	    sNames = trSet.toArray(new String[0]);

        return sNames;
    }






    public static String getCallerName(Class c) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String methodName = e.getMethodName();

        return c.getSimpleName() + "." + methodName;
    }












    public static double roundDouble(double inD, String sFormat, RoundingMode mode) {
        if(sFormat==null) {
            sFormat = Settings.getPriceDecimalFormat();
        }
        DecimalFormat df = new DecimalFormat(sFormat);
        df.setRoundingMode(mode);

        //adjust inD to specified Decimal format
        inD = Double.valueOf(df.format(inD));

        return inD;
    }

    public static double roundUp(double inD) {
        return roundUp(inD, null);
    }
    public static double roundUp(double inD, String sFormat) {
        return roundDouble(inD, sFormat, RoundingMode.CEILING);
    }
    public static double roundDown(double inD) {
        return roundDown(inD, null);
    }
    public static double roundDown(double inD, String sFormat) {
        return roundDouble(inD, sFormat, RoundingMode.FLOOR);
    }


    public static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }
    public static boolean isLinux()
    {
        return  System.getProperty("os.name").startsWith("Linux");
    }
    public static String getSeperator() {
        return Utils.isWindows()? "\\":"/";
    }


    public static String getAmcKey(String tradeDate0, String tradeDate1, String startHMS, String endHMS) {
        String sHMSPair = startHMS + "," + endHMS;
        String key = tradeDate0 + ",";
        key += tradeDate1 + ",";
        key += sHMSPair;

        return key;
    }


    public static double getOutPrice(double inPrice, double targetProfit, int tradeType) {
        double outPrice;
        if(tradeType == Stock.TRADE_TYPE_LONG)
            outPrice = inPrice + targetProfit;
        else
            outPrice = inPrice - targetProfit;

        return outPrice;
    }



    public static int getIdx(String[] strings, String s) {
        int idx = -1;
        for(int i=0; i<strings.length; i++) {
            if(s.equals(strings[i]))
                return i;
        }
        return idx;
    }



    public static boolean isMarketOff() {
        return isOfflineRun();
    }
    public static boolean isOfflineRun() {
        String tradeDate = Settings.getTradeDate();
        return isOfflineRun(tradeDate);
    }
    public static boolean isOfflineRun(String tradeDate) {
        boolean bOffline = false;

        long closeTp4RunDate = Time.getSpecificTime(tradeDate, AStockSdTime.getCloseQuotationTime());
        //offline run
        long currentTp = System.currentTimeMillis()/1000;
        if(currentTp >= closeTp4RunDate)
            bOffline = true;

        return bOffline;
    }
    //(14:56:00, 15:30:00)
    public static boolean isRRPTime(long currentTp) {
        long rrpStartTime = AStockSdTime.getRrpStartTime(currentTp);
        if(currentTp<rrpStartTime) {
            return false;
        }

        long rrpEndTime = AStockSdTime.getRrpEndTime(currentTp);
        if(currentTp>rrpEndTime) {
            return false;
        }

        return true;
    }


    public static int getCkptIntervalSdLength() {
        int ckptInterval = Settings.getCkptInterval();
        int sdInterval = Settings.getSdInterval();

        int sdLength = (int)roundUp((double)ckptInterval/sdInterval, "#");

        return sdLength;
    }


    public static long[] getFactorials(int n) {
        long[] factorials = new long[n];
        for(int i=0; i<n; i++) {
            factorials[i] = CombinatoricsUtils.factorial(i);
        }

        return factorials;
    }
    public static long[] getFactorials(int[] idxs) {
        int n=idxs.length;
        return getFactorials(n);
    }


    public static int wait4ExitValue(Process proc) {
        int exitVal=-1;
        try {
            boolean bRet = proc.waitFor(EXEC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if(!bRet) { 
                Stream<ProcessHandle> sHandles = proc.descendants();
                Iterator<ProcessHandle> itr = sHandles.iterator();
                while(itr.hasNext()) {
                    ProcessHandle h = itr.next();
                    System.err.format("Utils.wait4ExitValue(): timeOut happened! destroy pid=%d\n", 
                            h.pid());
                    h.destroy();
                }
                System.err.format("Utils.wait4ExitValue(): timeOut happened! destroy pid=%d\n", 
                        proc.pid());
                proc.destroy();
            } else {
                exitVal = proc.exitValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exitVal;
    }
    public static boolean wait4Alive(Process proc) {
        boolean bAmRateViewerStarted = false;
        int i=0;
        while(!proc.isAlive()&&i<10) {
            System.err.format("AmRateViewer._start(): waiting amrateviewer alive!\n");
            i++;
        }
        if(i<10)
             bAmRateViewerStarted = true;

        return bAmRateViewerStarted;
    }

    //random sleep [0, 1) second
    public static void randSleep() {
        long ts = (long)(r.nextDouble()*1000);
        Utils.sleep(ts);
    }
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static double getPolynomialR2(List<WeightedObservedPoint> obl, double[] coeff) {
        double[] x0 = new double[obl.size()];
        for(int i=0; i<obl.size(); i++)
           x0[i] = obl.get(i).getY(); 

        final PolynomialFunction fitted = new PolynomialFunction(coeff);
        double[] x1 = new double[obl.size()];
        for(int i=0; i<obl.size(); i++)
            x1[i] = fitted.value(obl.get(i).getX());

        double r = new PearsonsCorrelation().correlation(x0, x1);
        double r2 = r*r; 

        return r2;
    }

    public static String toPN0String(double[] coeff) {
        String s01 = "";
        for(int i=0; i<coeff.length; i++) {
            s01 += coeff[i]>0?"+":coeff[i]<0?"-":"0";
        }
        return s01;
    }
    public static String toSneString(double[] coeff) {
        String s01 = "";
        for(int i=0; i<coeff.length; i++) {
            s01 += String.format("%.1e,", coeff[i]);
        }
        return s01;
    }

    //[start, end)
    public static double[] toDoubleArray(List<String> sList, int start, int end) {
        double[] array = new double[sList.size()];

        for(int i=start; i<end; i++) {
            array[i] = Double.valueOf(sList.get(i)); 
        }

        return array;
    }
}
