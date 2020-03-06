package com.westsword.stocks.tools.helper;


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
