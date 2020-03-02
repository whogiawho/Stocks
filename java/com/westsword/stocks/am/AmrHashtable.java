package com.westsword.stocks.am;


import java.util.*;

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.time.Time;

public class AmrHashtable {
    private TreeMap<AmrKey, TreeSet<AmRecord>> mOutTable4Long;
    private TreeMap<AmrKey, TreeSet<AmRecord>> mOutTable4Short;

    public AmrHashtable() {
        mOutTable4Long = new TreeMap<AmrKey, TreeSet<AmRecord>>(new AkComparator());
        mOutTable4Short = new TreeMap<AmrKey, TreeSet<AmRecord>>(new AkComparator());
    }

    public void put(AmRecord r, int tradeType) {
        String tradeDate = Time.getTimeYMD(r.hexTimePoint, false);
        put(tradeDate, r, tradeType);
    }

    public void put(String tradeDate, AmRecord r, int tradeType) {
        double price = 0.0;
        AmrKey k = null;
        TreeMap<AmrKey, TreeSet<AmRecord>> outTable = null;

        if(tradeType == Stock.TRADE_TYPE_LONG) {
            price = r.downPrice;
            outTable = mOutTable4Long;
        } else {
            price = r.upPrice;
            outTable = mOutTable4Short;
        }

        k = new AmrKey(tradeDate, price);
        TreeSet<AmRecord> v = outTable.get(k);
        if(v==null) {
            v = new TreeSet<AmRecord>(new Comparator<AmRecord>() {
                @Override
                public int compare(AmRecord r0, AmRecord r1) {
                    return Long.compare(r0.hexTimePoint, r1.hexTimePoint);
                }
            });
            outTable.put(k, v);
        }
        v.add(r);
    }


    public static class AmrKey {
        public String tradeDate;
        public double price;

        public AmrKey(String tradeDate, double price) {
            this.tradeDate = tradeDate;
            this.price = price;
        }
    }

    public static class AkComparator implements Comparator<AmrKey> {
        @Override
        public int compare(AmrKey k0, AmrKey k1) {
            if(k0.price != k1.price) {
                return Double.compare(k0.price, k1.price);
            } else {
                return k0.tradeDate.compareTo(k1.tradeDate);
            }
        }
    }
}
