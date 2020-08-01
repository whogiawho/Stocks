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


package com.westsword.stocks.base.utils;
 
import org.apache.commons.cli.*;

public class CmdLineUtils {
    public static String getString(CommandLine cmd, String sSwitch, String sDefault) {
        String string = sDefault;
        if(cmd!=null && cmd.hasOption(sSwitch))
            string = cmd.getOptionValue(sSwitch);

        return string;
    }
    public static int getInteger(CommandLine cmd, String sSwitch, int sDefault) {
        int i = sDefault;
        if(cmd!=null && cmd.hasOption(sSwitch))
            i = Integer.valueOf(cmd.getOptionValue(sSwitch));

        return i;
    }
    public static double getDouble(CommandLine cmd, String sSwitch, double sDefault) {
        double d = sDefault;
        if(cmd!=null && cmd.hasOption(sSwitch))
            d = Double.valueOf(cmd.getOptionValue(sSwitch));

        return d;
    }
    public static boolean getBoolean(CommandLine cmd, String sSwitch, boolean sDefault) {
        boolean bSwitch = sDefault;
        if(cmd!=null && cmd.hasOption(sSwitch))
            bSwitch = !sDefault;

        return bSwitch;
    }
}
