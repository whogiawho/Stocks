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
 
 
package com.westsword.stocks.tools.helper.man;


import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.Task;
import com.westsword.stocks.base.TaskManager;

public class SSDatesManager extends TaskManager {

    public void run(SSDates ssd, AmManager am) {
        maxThreadsCheck();

        Thread t = new SSdTask(this, ssd, am);
        t.start();
    }

    public static class SSdTask extends Task {
        private SSDates mSSd;
        private AmManager am;

        public SSdTask(SSDatesManager m, SSDates ssd, AmManager am) {
            super(m);

            mSSd = ssd;
            this.am = am;
        }

        @Override
        public void runTask() {
            //run ssinstance
            if(mSSd!=null)
                mSSd.run(am);
        }
    }
}
