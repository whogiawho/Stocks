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


import java.io.*;

import com.westsword.stocks.base.Utils;

public class FileLoader implements ILoadFile {

    public FileLoader() {
    }

    public void load(String sFileName) {
        int count = 0;
        String line = "";
        try { 
            BufferedReader reader = new BufferedReader(new FileReader(sFileName)); 

            while ((line = reader.readLine()) != null) {
                boolean bCont = onLineRead(line, count);
                if(bCont)
                    count++;
                else
                    break;
            }

            reader.close();
        } catch(FileNotFoundException e) {
            System.err.format("%s: %s not found\n", 
                    Utils.getCallerName(getClass()), sFileName);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.format("%s: count=%d\n", 
                    Utils.getCallerName(getClass()), count);
            System.out.format("%s: %s\n", 
                    Utils.getCallerName(getClass()), line);
        }
    }

    public boolean onLineRead(String line, int count) {
        System.out.format("%s\n", line);
        return true;
    }
}
