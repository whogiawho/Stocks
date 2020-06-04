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


import com.westsword.stocks.base.Settings;

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
