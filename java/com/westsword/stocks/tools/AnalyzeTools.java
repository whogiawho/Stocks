package com.westsword.stocks.tools;

import java.util.*;


import com.westsword.stocks.tools.helper.*;

public class AnalyzeTools{

    private static void usage() {


        System.err.println("usage: java AnalyzeTools commands");
        System.err.println("       commands are listed below:");
        System.err.println("       [ getvalue | setvalue | decode |\n" +
                "         xxx | xxx | xxx ]");

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
        } else {
            usage();
        }
    }

}
