 /*
 Copyright (C) 1989-2020 Free Software Foundation, Inc.
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
 
 
package com.westsword.stocks.tools.helper.man;


import java.util.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.Task;
import com.westsword.stocks.base.TaskManager;
import com.westsword.stocks.tools.helper.MinStdDevR;

public class SSgmsdrManager extends TaskManager {

    public void run(MinStdDevR msdr, 
            ArrayList<String> tradeDateList, String[] hms, AmManager am) {
        maxThreadsCheck();

        Thread t = new SSgmsdrTask(this, msdr, tradeDateList, hms, am);
        t.start();
    }
    public void run(MinStdDevR msdr, 
            ArrayList<String> tradeDateList, String hmsList, AmManager am) {
        maxThreadsCheck();

        Thread t = new SSgmsdrTask(this, msdr, tradeDateList, hmsList, am);
        t.start();
    }

    public static class SSgmsdrTask extends Task {
        private MinStdDevR msdr;
        private ArrayList<String> tradeDateList;
        private AmManager am;

        private String[] hms;
        private String hmsList;

        public SSgmsdrTask(SSgmsdrManager m, MinStdDevR msdr, 
                ArrayList<String> tradeDateList, String[] hms, AmManager am) {
            super(m);

            this.msdr = msdr;
            this.tradeDateList = tradeDateList;
            this.hms = hms;
            this.hmsList = null;
            this.am = am;
        }
        public SSgmsdrTask(SSgmsdrManager m, MinStdDevR msdr, 
                ArrayList<String> tradeDateList, String hmsList, AmManager am) {
            super(m);

            this.msdr = msdr;
            this.tradeDateList = tradeDateList;
            this.hmsList = hmsList;
            this.hms = null;
            this.am = am;
        }

        @Override
        public void runTask() {
            //run MinStdDevR
            if(msdr!=null) {
                if(hms!=null)
                    msdr.get(tradeDateList, hms, am);
                else
                    msdr.get(tradeDateList, hmsList, am);

                msdr.print();
            }
        }
    }
}
