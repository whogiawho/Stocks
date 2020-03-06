package com.westsword.stocks.tools;

import java.util.*;


import com.westsword.stocks.tools.helper.*;

public class AnalyzeTools{

    private static void usage() {


        System.err.println("usage: java AnalyzeTools commands");
        System.err.println("       commands are listed below:");
        System.err.println("       [ getvalue | setvalue | make1mamcmap | makeanalysistxt |\n" +
                "         getamcorrel | getrel | getupprice | getdownprice | decode |\n" +
                "         ssgroupchar | ssgroupchars | makessdates |\n" +
                "         ssinstance | ssinstances | getfullss ]");

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
        } else if (sOption.equals("getrel")) {
            SdTime1Helper sth = new SdTime1Helper();
            sth.getrel(args);
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
        } else if (sOption.equals("makessdates")) {
            SSDatesHelper ssdh = new SSDatesHelper();
            ssdh.make(args);
        } else if (sOption.equals("getfullss")) {
            FullSSHelper fullssh = new FullSSHelper();
            fullssh.run(args);
        } else {
            usage();
        }
    }

}
