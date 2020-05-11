package com.westsword.stocks.tools;

import java.util.*;


import com.westsword.stocks.tools.helper.*;

public class AnalyzeTools{

    private static void usage() {


        System.err.println("usage: java AnalyzeTools commands");
        System.err.println("       commands are listed below:");
        System.err.println("       [ getvalue | setvalue | makeanalysistxt | priceamp |\n" +
                "         getamcorrel | getupprice | getdownprice | getam | decode |\n" +
                "         getrel | getabs | rgetabs |\n" +
                "         ssinstance | ssinstances | getfullss | checksstable |\n" +
                "         getentrust | checkabss | submitabs | makerrp |\n" +
                "         ssgroupchar | ssgroupchars | ssgroupverify |\n" +
                "         mminstance | qualrange |\n" +
                "         makessdates | maxmatchsingle | maxmatchall | make1mamcmap ]");

        System.exit(-1);
    }


    public static void main(String args[]) throws Exception {
        String sOption = null;
        if(args.length == 0) {
            usage();
        }
        sOption = args[0];

        if(sOption == null) {
            usage();
        } else if(sOption.equals("getvalue")) {
            SettingsHelper.getValue(args);
        } else if(sOption.equals("setvalue")) {
            SettingsHelper.setValue(args);
        } else if (sOption.equals("decode")) {
            CoderHelper.decode(args);
        } else if (sOption.equals("makeanalysistxt")) {
            AnalysisHelper.makeTxt(args);
        } else if (sOption.equals("priceamp")) {
            PriceHelper.amplitude(args);
        } else if (sOption.equals("make1mamcmap")) {
            Amc1mMapHelper amcmh = new Amc1mMapHelper();
            amcmh.make(args);
        } else if(sOption.equals("getupprice")) {
            AnalysisTxtHelper.getPrice(args);
        } else if(sOption.equals("getdownprice")) {
            AnalysisTxtHelper.getPrice(args);
        } else if (sOption.equals("getamcorrel")) {
            AmCorrelHelper ach = new AmCorrelHelper();
            ach.getAmCorrel(args);
        } else if (sOption.equals("getam")) {
            AmHelper amh = new AmHelper();
            amh.getAm(args);
        } else if (sOption.equals("getrel")) {
            SdTime1Helper sth = new SdTime1Helper();
            sth.getRel(args);
        } else if (sOption.equals("getabs")) {
            SdTime1Helper sth = new SdTime1Helper();
            sth.getAbs(args);
        } else if (sOption.equals("rgetabs")) {
            SdTime1Helper sth = new SdTime1Helper();
            sth.rgetAbs(args);
        } else if (sOption.equals("ssinstance")) {
            SSInstanceHelper ssih = new SSInstanceHelper();
            ssih.run(args);
        } else if (sOption.equals("ssinstances")) {
            SSInstancesHelper ssih = new SSInstancesHelper();
            ssih.run(args);
        } else if (sOption.equals("ssgroupchar")) {
            SSGroupHelper ssgh = new SSGroupHelper();
            ssgh.listChar(args);
        } else if (sOption.equals("ssgroupchars")) {
            SSGroupHelper ssgh = new SSGroupHelper();
            ssgh.listChars(args);
        } else if (sOption.equals("ssgroupverify")) {
            SSGroupHelper ssgh = new SSGroupHelper();
            ssgh.verify(args);
        } else if (sOption.equals("mminstance")) {
            MMInstanceHelper mmih = new MMInstanceHelper();
            mmih.run(args);
        } else if (sOption.equals("qualrange")) {
            RangeHelper rh = new RangeHelper();
            rh.findQualified(args);
        } else if (sOption.equals("makessdates")) {
            SSDatesHelper ssdh = new SSDatesHelper();
            ssdh.make(args);
        } else if (sOption.equals("maxmatchsingle")) {
            SSDatesHelper ssdh = new SSDatesHelper();
            ssdh.maxMatchSingle(args);
        } else if (sOption.equals("maxmatchall")) {
            SSDatesHelper ssdh = new SSDatesHelper();
            ssdh.maxMatchAll(args);
        } else if (sOption.equals("getfullss")) {
            FullSSHelper fullssh = new FullSSHelper();
            fullssh.run(args);
        } else if (sOption.equals("checksstable")) {
            SSTableHelper ssth = new SSTableHelper();
            ssth.checkSSTable(args);
        } else if(sOption.equals("checkabss")) {
            THSQSHelper.checkAbsS(args);
        } else if(sOption.equals("submitabs")) {
            THSQSHelper.submitAbs(args);
        } else if(sOption.equals("makerrp")) {
            THSQSHelper.makeRRP(args);
        } else if(sOption.equals("getentrust")) {
            THSQSHelper.getEntrust(args);
        } else {
            usage();
        }
    }

}
