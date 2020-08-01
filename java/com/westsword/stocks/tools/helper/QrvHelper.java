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
 
 
package com.westsword.stocks.tools.helper;


import java.util.*;
import org.apache.commons.cli.*;
import org.apache.commons.math3.util.Combinations;

import com.westsword.stocks.am.*;
import com.westsword.stocks.qr.*;
import com.westsword.stocks.base.*;
import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;

public class QrvHelper {
    public void shrink(String[] args) {
        CommandLine cmd = qrvGetCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length != 2) {
            shrinkUsage();
            return;
        }

        String stockCode = newArgs[0];
        String qrvFile = newArgs[1];
        double threshold = SSUtils.getThreshold(cmd, 0.7);
        int sdLength = getSdLength(cmd);

        AmManager am = new AmManager(stockCode);
        SdTime1 sdTime = new SdTime1(stockCode);
        QrvLoader l = new QrvLoader();
        ArrayList<Qrv> qrvList = new ArrayList<Qrv>();
        l.load(qrvFile, qrvList);

        ArrayList<Qrv> sQrvList = new ArrayList<Qrv>();
        getSqrvList(qrvList, am, sdTime, sdLength, threshold, 
                sQrvList);
        //System.out.format("sQrvList.size=%d\n", sQrvList.size());
        for(int i=0; i<sQrvList.size(); i++) {
            System.out.format("%s", sQrvList.get(i).toString());
        }

    }
    //out - sQrvList
    private void getSqrvList(ArrayList<Qrv> qrvList, AmManager am, SdTime1 sdTime, int sdLength, double threshold, 
            ArrayList<Qrv> sQrvList) {
        int ckptIntervalLen = Utils.getCkptIntervalSdLength();

        Qrv baseQrv = null;
        Qrv prevQrv = null;
        for(int i=0; i<qrvList.size(); i++) {
            Qrv qrv = qrvList.get(i);
            if(baseQrv == null) {
                baseQrv = qrv;
                prevQrv = qrv;
                sQrvList.add(qrv);
                //System.out.format("%s %s\n", qrv.getEndDate(), qrv.getEndHMS());
                continue;
            }
            int currentSdIdx = qrv.getEndSdt(sdTime);
            if(currentSdIdx - prevQrv.getEndSdt(sdTime) == ckptIntervalLen) {
                //getAmCorrel for (qrv, baseQrv)
                double amCorrel = qrv.getAmCorrel(baseQrv, sdLength, sdTime, am);
                if(amCorrel<threshold) {
                    baseQrv = qrv;
                    sQrvList.add(qrv);
                    //System.out.format("%s %s\n", qrv.getEndDate(), qrv.getEndHMS());
                } else {
                    //System.out.format("continued %s %s\n", qrv.getEndDate(), qrv.getEndHMS());
                }
            } else {
                baseQrv = qrv;
                sQrvList.add(qrv);
                //System.out.format("%s %s\n", qrv.getEndDate(), qrv.getEndHMS());
            }
            prevQrv = qrv;
        }
    }
    private void shrinkUsage() {
        System.err.println("usage: java AnalyzeTools shrinkqrv [-hl] stockCode qrvFile");
        System.err.println("       remove those items similar with prev item in qrvFile");
        System.err.println("       -h threshold ;  ");
        System.err.println("       -l sdLength;  sdLength of items in qrvFile");
        System.exit(-1);
    }
    private int getSdLength(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "l", Utils.getCkptIntervalSdLength()*4*60);
    }
    public static CommandLine qrvGetCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = new Options();
            options.addOption("h", true,  "a threshold value to get similar qrv");
            options.addOption("l", true,  "sdLength");

            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }





    public void getstats(String[] args) {
        CommandLine cmd = qrvGetCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length != 2) {
            getstatsUsage();
            return;
        }

        String stockCode = newArgs[0];
        String qrvFile = newArgs[1];
        int sdLength = getSdLength(cmd);

        QrvLoader l = new QrvLoader();
        ArrayList<Qrv> sQrvList = new ArrayList<Qrv>();
        l.load(qrvFile, sQrvList);

        AmManager am = new AmManager(stockCode);
        SdTime1 sdTime = new SdTime1(stockCode);
        CmManager cm = new CmManager();

        //loop to get sQrvList corrMatrix
        loop2Stats(sdLength, am, cm, sdTime, sQrvList);
        
        cm.close();
    }


    private void loop2Stats(int sdLength, AmManager am, CmManager cm, SdTime1 sdTime, ArrayList<Qrv> sQrvList) {
        int ckptIntSdLen = Utils.getCkptIntervalSdLength();
        Combinations c = new Combinations(sdLength/ckptIntSdLen+1, 2);
        Iterator<int[]> itr = c.iterator();
        while(itr.hasNext()) {
            int[] e = itr.next();

            double[][] amm = am.getAmMatrix(sQrvList, sdLength, e);
            double[][] cmm = cm.getCorrMatrix(amm);
            for(int i=0; i<cmm.length; i++) {
                int count = 0;
                double profitSum = 0.0;
                for(int j=0; j<cmm[i].length; j++) {
                    if(cmm[i][j]>=SSUtils.Default_Threshold) {
                        profitSum += sQrvList.get(j).getProfit();
                        count++;
                    }
                }
                if(profitSum>0&&count>=10&&profitSum/count>=0.25) {
                    //output profitSum, count, sQrvList.get(i), sdLength, e[]
                    sQrvList.get(i).print(sdLength, profitSum, count, e, sdTime);
                }
            }
        }
    }
    private void getstatsUsage() {
        System.err.println("usage: java AnalyzeTools getstats [-l] stockCode qrvFile");
        System.err.println("       get stats for qrvFile generated by shrinkqrv");
        System.err.println("       -l sdLength;  sdLength of items in qrvFile");
        System.exit(-1);
    }

}
