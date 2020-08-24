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
 
 
package com.westsword.stocks.tools;

import java.util.Arrays;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.util.Calendar;

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.time.Time;
import com.westsword.stocks.base.utils.*;

public class ToPost0 extends FileLoader {
    public void write2File(BufferedWriter w, long time, int upCount, double upQ, int downCount, double downQ, double lastPrice) {
        String line2="";

        line2+=time+" ";

        String sUpCount=String.format("%6d", upCount);
        line2+=sUpCount+" ";

        String sUpQ=String.format("%14.3f", upQ);
        line2+=sUpQ+" ";

        String sDownCount=String.format("%6d", downCount);
        line2+=sDownCount+" ";

        String sDownQ=String.format("%14.3f", downQ);
        line2+=sDownQ+" ";

        String sLastPrice=String.format("%8.3f", lastPrice);
        line2+=sLastPrice+" ";

        try { 
            w.write(line2, 0, line2.length());
            w.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write2HumanFile(BufferedWriter w, long thisTime, double thisPrice, int count, int type) {
        thisTime = thisTime*1000;

        Calendar cal = Time.getCalendar();
        cal.setTimeInMillis(thisTime);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        String rawLine=""; 

        String sTime = String.format("%10x", thisTime/1000);
        rawLine+=sTime;

        String sReadableTime = String.format("%4d-%02d-%02d-%02d:%02d:%02d", year, month+1, date, hour, minute, second);
        rawLine+=" "+sReadableTime;

        String sThisPrice=String.format("%8.3f", thisPrice);
        rawLine+=" " +sThisPrice;

        String sCount=String.format("%6d", count);
        rawLine+=" " +sCount;

        String sType=String.format("%2d", type);
        rawLine+=" " +sType;

        try { 
            w.write(rawLine, 0, rawLine.length());
            w.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BufferedWriter mWriter = null;
    private BufferedWriter mHumanWriter = null;
    private long prevTime=0;
    private int prevUpCount=0, prevDownCount=0;
    private double prevUpQ=0.0, prevDownQ=0.0;
    private double prevPrice=0.0;
    public boolean onLineRead(String line, int count) {
        ConvertDD2Double cDD = new ConvertDD2Double();
        String[] fields=line.split(" +");
        //[0] time; [1] price; [2] count; [3] type
        long thisTime = Long.parseLong(fields[0], 16);
        double thisPrice = cDD.sub_48A0D0(fields[1]);

        int thisCount = Integer.parseInt(fields[2]);
        int thisType = Integer.parseInt(fields[3]);
        double thisQ = thisPrice*thisCount*Stock.SHOU_UNIT;

        if(mHumanWriter != null) {
            write2HumanFile(mHumanWriter, thisTime, thisPrice, thisCount, thisType);
        }

        if(prevTime!=thisTime){
            //skip write if prevTime == 0
            if(prevTime != 0){
                //write <prevTime, prevUpCount, prevUpQ, prevDownCount, prevDownQ>
                write2File(mWriter, prevTime, prevUpCount, prevUpQ, prevDownCount, prevDownQ, prevPrice);
            }

            //set this to prev
            prevTime=thisTime;
            //reset prevUpCount, prevDownCount, prevUpQ, prevDownQ
            prevUpCount=0;
            prevDownCount=0;
            prevUpQ=0.0;
            prevDownQ=0.0;
        }

        //total this to prev
        if(thisType==Stock.TRADE_TYPE_DOWN) {
            prevDownCount+=thisCount;
            prevDownQ+=thisQ;
        } else if(thisType==Stock.TRADE_TYPE_UP) {
            prevUpCount+=thisCount;
            prevUpQ+=thisQ;
        } else {
            System.err.println("unsupported type!"+ " " +"time: "+thisTime+";type: "+thisType);
        }
            
        prevPrice = thisPrice;

        return true;
    }
    public void process(String inFile, BufferedWriter writer, BufferedWriter humanWriter) {
        mWriter = writer;
        mHumanWriter = humanWriter;

        load(inFile);

        if(prevTime!=0){
            //write the last case
            write2File(writer, prevTime, prevUpCount, prevUpQ, prevDownCount, prevDownQ, prevPrice);
        }

    }

    public static void main(String args[]) throws Exception {
        if(args.length>=2){
            String inFile = args[0];
            String outFile = args[1];

            try { 
                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

                BufferedWriter humanWriter = null;
                if(args.length>=3) {
                    humanWriter = new BufferedWriter(new FileWriter(args[2]));
                }

                ToPost0 tp0 = new ToPost0();
                tp0.process(inFile, writer, humanWriter);

                writer.close();
                if(humanWriter != null)
                    humanWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
