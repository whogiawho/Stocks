package com.westsword.stocks.tools.helper;


import java.util.*;

import com.westsword.stocks.am.AmManager;
import com.westsword.stocks.base.Task;
import com.westsword.stocks.base.TaskManager;

public class SSgmsdrManager extends TaskManager {

    public void run(MinStdDevR msdr, 
            ArrayList<String> tradeDateList, String[] hms, AmManager am) {
        maxThreadsCheck();

        Thread t = new SSgmsdrTask(this, msdr, tradeDateList, hms, am);
        t.start();
    }

    public static class SSgmsdrTask extends Task {
        private MinStdDevR msdr;
        private ArrayList<String> tradeDateList;
        private String[] hms;
        private AmManager am;

        public SSgmsdrTask(SSgmsdrManager m, MinStdDevR msdr, 
                ArrayList<String> tradeDateList, String[] hms, AmManager am) {
            super(m);

            this.msdr = msdr;
            this.tradeDateList = tradeDateList;
            this.hms = hms;
            this.am = am;
        }

        @Override
        public void runTask() {
            //run ssinstance
            if(msdr!=null) {
                msdr.get(tradeDateList, hms, am);
                msdr.print();
            }
        }
    }
}
