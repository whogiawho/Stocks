package com.westsword.stocks.am;


import java.util.*;

public class ThreadRemoveAmc extends Thread {
    private ArrayList<String> tradeDateList;
    private String[] hms;

    public ThreadRemoveAmc(ArrayList<String> tradeDateList, String[] hms) {
        this.tradeDateList = tradeDateList;
        this.hms = hms;
    }

    public void run() {
        removeAmCorrels(tradeDateList, hms);
    }

    private void removeAmCorrels(ArrayList<String> tradeDateList, String[] hms) {
        for(int i=0; i<tradeDateList.size(); i++) {
            String tradeDate0 = tradeDateList.get(i);
            for(int j=0; j<tradeDateList.size(); j++) {
                String tradeDate1 = tradeDateList.get(j);
                AmcMap.removeAmCorrel(tradeDate0, tradeDate1, hms[0], hms[1]);
            }
        }
    }
}
