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

import com.westsword.stocks.base.time.*;
import com.westsword.stocks.base.utils.*;


public class PermSepHelper {
    public static void get(String args[]) {
        CommandLine cmd = getCommandLine(args);
        String[] newArgs = cmd.getArgs();
        if(newArgs.length!=3) {
            usage();
            return;
        }

        int bwsd = getBackwardSd(cmd);
        double maxProfitThres = getMaxProfit(cmd);
        int cycle = getCycle(cmd);
        System.out.format("bwsd=%d, maxProfitThres=%.3f, cycle=%d\n", bwsd, maxProfitThres, cycle);

        String stockCode = newArgs[0];
        String sAmDerSorted = newArgs[1];
        int permIdx = Integer.valueOf(newArgs[2]);

        //load AmPerm of permIdx into permList
        ArrayList<AmPerm> permList = new ArrayList<AmPerm>();
        TreeMap<Integer, AmPerm> apMap = new TreeMap<Integer, AmPerm>();
        AmPermLoader l = new AmPermLoader(stockCode);
        l.load(sAmDerSorted, permIdx, apMap, permList);
        System.out.format("permList.size=%d apMap.size=%d\n", permList.size(), apMap.size());


        //with apMap and permList, do seperation
        ArrayList<AmPerm> list0 = new ArrayList<AmPerm>();
        ArrayList<AmPerm> list1 = new ArrayList<AmPerm>();
        split(permList, maxProfitThres, cycle, list0, list1);
        System.out.format("list0.size=%d list1.size=%d\n", list0.size(), list1.size());

        TreeMap<Integer, Integer> idxCntMap0 = new TreeMap<Integer, Integer>(); //(permIdx, cnt)
        TreeSet<Integer> idxSet0 = new TreeSet<Integer>();
        getRelated(list0, apMap, bwsd, idxCntMap0, idxSet0);
        System.out.format("idxCntMap0.size=%d idxSet0.size=%d\n", idxCntMap0.size(), idxSet0.size());

        TreeMap<Integer, Integer> idxCntMap1 = new TreeMap<Integer, Integer>(); //(permIdx, cnt)
        TreeSet<Integer> idxSet1 = new TreeSet<Integer>();
        getRelated(list1, apMap, bwsd, idxCntMap1, idxSet1);
        System.out.format("idxCntMap1.size=%d idxSet1.size=%d\n", idxCntMap1.size(), idxSet1.size());

        TreeSet<Integer> cIdxSet1 = new TreeSet<Integer>(idxSet1);
        cIdxSet1.removeAll(idxSet0);
        int idxMaxCnt = -1;
        int maxCnt = -1;
        for(int idx: cIdxSet1) {
            int cnt = idxCntMap1.get(idx);
            if(cnt>maxCnt) {
                maxCnt = cnt;
                idxMaxCnt = idx;
            }
        }
        System.out.format("cIdxSet1.size=%d idxMaxCnt=%d maxCnt=%d maxRatio=%-8.3f\n", 
                cIdxSet1.size(), idxMaxCnt, maxCnt, (double)maxCnt/(double)list1.size());
    }
    private static void getRelated(ArrayList<AmPerm> list, TreeMap<Integer, AmPerm> apMap, int bwsd,
            TreeMap<Integer, Integer> idxCntMap0, TreeSet<Integer> idxSet) {
        for(int i=0; i<list.size(); i++) {
            AmPerm r = list.get(i);
            int startSd = r.sdt - bwsd;
            int endSd = r.sdt;

            TreeMap<Integer, Integer> idxCntMap1 = new TreeMap<Integer, Integer>(); //(permIdx, cnt)
            for(int sd=startSd; sd<endSd; sd++) {
                AmPerm r1 = apMap.get(sd);
                if(r1!=null) {
                    idxSet.add(r1.permIdx);
                    Integer currentCnt = idxCntMap1.get(r1.permIdx);
                    if(currentCnt==null) 
                        idxCntMap1.put(r1.permIdx, 1);
                }
            }
            for(int idx: idxCntMap1.keySet()) {
                Integer currentCnt = idxCntMap0.get(idx);
                if(currentCnt==null)
                    idxCntMap0.put(idx, 1);
                else
                    idxCntMap0.put(idx, currentCnt+1);
            }
        }
    }

    private static void split(ArrayList<AmPerm> permList, double maxProfitThres, int cycle,
            ArrayList<AmPerm> list0, ArrayList<AmPerm> list1) {
        for(int i=0; i<permList.size(); i++) {
            AmPerm r = permList.get(i);
            if(cycle==0) {
                if(r.maxProfit0<=maxProfitThres)
                    list0.add(r);
                else
                    list1.add(r);
            } else {
                if(r.maxProfit1<=maxProfitThres)
                    list0.add(r);
                else
                    list1.add(r);
            }
        }
    }
    public static int getBackwardSd(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "b", 300);
    }
    public static double getMaxProfit(CommandLine cmd) {
        return CmdLineUtils.getDouble(cmd, "m", 0.09);
    }
    public static int getCycle(CommandLine cmd) {
        return CmdLineUtils.getInteger(cmd, "c", 1);
    }





    public static class AmPerm {
        public long hexTp;
        public int sdt;
        public double maxProfit0;
        public double maxProfit1;
        public int permIdx;

        public AmPerm(int sdt, long hexTp, double maxProfit0, double maxProfit1, int permIdx) {
            this.sdt = sdt;
            this.hexTp = hexTp;
            this.maxProfit0 = maxProfit0;
            this.maxProfit1 = maxProfit1;
            this.permIdx = permIdx;
        }
    }
    public static class AmPermLoader extends FileLoader {

        private int mPermIdx;
        private TreeMap<Integer, AmPerm> mApMap = null;
        private ArrayList<AmPerm> mPermList = null;

        public AmPermLoader(String stockCode) {
        }
        public boolean onLineRead(String line, int counter) {
            //System.out.format("counter=%d\n", counter);

            String[] fields=line.split(" +");
            long hexTp = Long.parseLong(fields[0], 16);
            int sdt = Integer.valueOf(fields[1]);
            double maxProfit0 = Double.valueOf(fields[3]);
            double maxProfit1 = Double.valueOf(fields[4]);
            int permIdx = Integer.valueOf(fields[15]);
            AmPerm r = new AmPerm(sdt, hexTp, maxProfit0, maxProfit1, permIdx);

            if(mApMap!=null)
                mApMap.put(sdt, r);
            if(mPermList!=null&&permIdx==mPermIdx)
                mPermList.add(r);

            return true;
        }
        public void load(String sAmDerSorted,
                int permIdx, TreeMap<Integer, AmPerm> apMap, ArrayList<AmPerm> permList) {
            mPermIdx = permIdx;
            mApMap = apMap;
            mPermList = permList;

            load(sAmDerSorted);
        }
    }



    private static void usage() {
        System.err.println("usage: java AnalyzeTools permsep [-bcm] stockCode sAmDerSorted permIdx");
        System.err.println("       loop permIdx's items, list those previous to them which can differentiate maxProfitThres above from belowequal");
        System.err.println("       -b sdbw            ; at most sdbw shall be looked backward; default 300");
        System.err.println("       -c cycle           ; 0|1; default 1");
        System.err.println("       -m maxProfitThres  ; a threshold above; default 0.09");
        System.exit(-1);
    }
    public static CommandLine getCommandLine(String[] args) {
        CommandLine cmd = null;
        try {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Options options = getOptions();

            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, newArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cmd;
    }
    public static Options getOptions() {
        Options options = new Options();
        options.addOption("b", true,  "at most sdtime shall be looked backward");
        options.addOption("c", true,  "cycle(0|1)");
        options.addOption("m", true,  "maxProfit threshold");

        return options;
    }






    private static void testTreeSet() {
        AmPerm ap0 = new AmPerm(0, 0, 0.1, 0.2, 0);
        AmPerm ap1 = new AmPerm(0, 0, 0.2, 0.1, 0);
        AmPerm ap2 = new AmPerm(0, 0, 0.3, 0.1, 0);

        TreeSet<AmPerm> apSet = new TreeSet<AmPerm>(new Comparator<AmPerm>() {
            @Override
            public int compare(AmPerm e0, AmPerm e1) {
                return Double.compare(e0.maxProfit0, e1.maxProfit0);
            }
        });
        apSet.add(ap0);
        apSet.add(ap1);
        apSet.add(ap2);

        System.out.format("size=%d\n", apSet.size());
    }
    private static void _get(ArrayList<AmPerm> list0, ArrayList<AmPerm> list1, TreeMap<Integer, AmPerm> apMap, int bwsd) {
        //testTreeSet();

        TreeMap<Integer, TreeSet<Integer>> m0 = getTpMap(list0, bwsd, apMap);
        TreeMap<Integer, TreeSet<Integer>> m1 = getTpMap(list1, bwsd, apMap);
        System.out.format("m0.size=%d m1.size=%d\n", m0.size(), m1.size());
        TreeSet<Integer> idxSet0 = getAllPermIdxSet(m0);
        TreeSet<Integer> idxSet1 = getAllPermIdxSet(m1);
        System.out.format("idxSet0.size=%d idxSet1.size=%d\n", idxSet0.size(), idxSet1.size());
        TreeSet<Integer> cIdxSet0 = new TreeSet<Integer>(idxSet0);
        TreeSet<Integer> cIdxSet1 = new TreeSet<Integer>(idxSet1);
        cIdxSet0.removeAll(idxSet1);       //uniq permIdx set for list0
        cIdxSet1.removeAll(idxSet0);       //uniq permIdx set for list1
        System.out.format("cIdxSet0.size=%d cIdxSet1.size=%d\n", cIdxSet0.size(), cIdxSet1.size());

        listMaxPermIdx(cIdxSet1, m1);
    }
    private static void listMaxPermIdx(TreeSet<Integer> idxSet, TreeMap<Integer, TreeSet<Integer>> m) {
        int idxMaxR = -1;
        double maxRatio = -1;
        for(Integer i: idxSet) {
            double ratio = getCountRatio(i, m);
            if(ratio>maxRatio) {
                maxRatio = ratio;
                idxMaxR = i;
            }
        }
        System.out.format("idxMaxR=%d maxRatio=%-8.3f\n", idxMaxR, maxRatio);
    }
    private static double getCountRatio(int i, TreeMap<Integer, TreeSet<Integer>> m) {
        int size = m.size();

        int count = 0;
        for(Integer j: m.keySet()) {         //loop m(tp, permIdxSet)
            if(m.get(j).contains(i))
                count++;
        }
        return (double)count/(double)size;
    }
    private static TreeSet<Integer> getAllPermIdxSet(TreeMap<Integer, TreeSet<Integer>> m) {
        TreeSet<Integer> idxSet = new TreeSet<Integer>();

        for(Integer i: m.keySet()) {
            TreeSet<Integer> s = m.get(i);
            idxSet.addAll(s);
        }

        return idxSet;
    }
    //<tp, permIdxSet>
    private static TreeMap<Integer, TreeSet<Integer>> getTpMap(ArrayList<AmPerm> list, int bwsd, 
            TreeMap<Integer, AmPerm> apMap) {
        TreeMap<Integer, TreeSet<Integer>> map = new TreeMap<Integer, TreeSet<Integer>>();

        for(int i=0; i<list.size(); i++) {
            AmPerm r = list.get(i);
            int startSd = r.sdt - bwsd;
            int endSd = r.sdt;
            TreeSet<Integer> idxSet = new TreeSet<Integer>();
            for(int j=startSd; j<endSd; j++) {
                idxSet.add(apMap.get(j).permIdx);
            }
            map.put(r.sdt, idxSet);
        }

        return map;
    }
    public static TreeSet<String> getTradeDates(String stockCode, int bwsd, ArrayList<AmPerm> permList) {
        TreeSet<String> tradeDateSet = new TreeSet<String>();

        SdTime1 sdTime = new SdTime1(stockCode);
        for(int i=0; i<permList.size(); i++) {
            AmPerm r = permList.get(i);
            int startSdt = r.sdt - bwsd;
            long startTp = sdTime.rgetAbs(startSdt);
            long endTp = r.hexTp;
            String startDate = Time.getTimeYMD(startTp, false);
            String endDate = Time.getTimeYMD(endTp, false);
            //System.out.format("startDate=%s endDate=%s\n", startDate, endDate);
            String[] sTradeDates = TradeDates.getTradeDateList(stockCode, startDate, endDate);
            tradeDateSet.addAll(Arrays.asList(sTradeDates));
        }
        return tradeDateSet;
    }
    public static void load(String stockCode, String sDir, TreeSet<String> tradeDateSet, 
            TreeMap<Integer, AmPerm> apMap) {
        AmPermLoader l = new AmPermLoader(stockCode);
        for(String tradeDate: tradeDateSet) {
            String sFile = sDir + "\\" + tradeDate + ".txt";
            l.load(sFile, -1, apMap, null);
            //System.out.format("apMap.size=%d\n", apMap.size());
            //System.out.format("tradeDate=%s\n", tradeDate);
        }
    }

}

