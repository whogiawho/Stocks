package com.westsword.stocks.tools.helper;

import com.westsword.stocks.Settings;

public class SettingsHelper {
    public static void getValue(String args[]) {
        if(args.length != 2) {
            usage();
            return;
        }

        String value = Settings.getString(args[1]);
        System.out.format("%s", value);
    }
    public static void setValue(String args[]) {
        if(args.length != 3) {
            usage();
            return;
        }

        Settings t = new Settings();
        t.setValue(args[1], args[2]);
    }



    private static void usage() {
        System.err.println("usage: java AnalyzeTools getvalue key");
        System.err.println("usage: java AnalyzeTools setvalue key value");
        System.exit(-1);
    }
}
