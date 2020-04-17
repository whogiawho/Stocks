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
