package com.westsword.stocks.am;


import java.util.*;

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.time.Time;
import com.westsword.stocks.base.time.StockDates;

public class AmrHashtable {
    private TreeMap<AmrKey, TreeSet<AmRecord>> mOutTable4Long;
    private TreeMap<AmrKey, TreeSet<AmRecord>> mOutTable4Short;
    private TreeMap<String, TreeSet<Double>>   mTdPriceMap;

    public AmrHashtable() {
        mOutTable4Long = new TreeMap<AmrKey, TreeSet<AmRecord>>(new AkComparator());
        mOutTable4Short = new TreeMap<AmrKey, TreeSet<AmRecord>>(new AkComparator());
        mTdPriceMap = new TreeMap<String, TreeSet<Double>>();
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

        TreeSet<Double> e = mTdPriceMap.get(tradeDate);
        if(e == null) {
            e = new TreeSet<Double>();
            mTdPriceMap.put(tradeDate, e);
        }
        e.add(price);
    }

    //get the nearest AmRecord>=k meeting below conditions:
    //  tradeDate<=nextTradeDateN 
    //  tp>=inTime depending on sTDistance
    public AmRecord getOutItem(long inTime, int tradeType, AmrKey k,
            String nextTradeDateN, int sTDistance, StockDates stockDates) {
        AmRecord item = null;

        String inTradeDate = Time.getTimeYMD(inTime, false);
        TreeMap<AmrKey, TreeSet<AmRecord>> outTable;
        if(tradeType == Stock.TRADE_TYPE_LONG) {
            outTable = mOutTable4Long;
        } else {
            outTable = mOutTable4Short;
        }

        String tradeDate = inTradeDate;
        if(sTDistance!=0)
            tradeDate = stockDates.nextDate(tradeDate);

        long nearestTime = Long.MAX_VALUE;
        while(tradeDate!=null && tradeDate.compareTo(nextTradeDateN)<=0) {
            TreeSet<Double> e = mTdPriceMap.get(tradeDate);
            if(e!=null) {
                for(Double price: e) {
                    if(price<k.price)
                        continue;
    
                    AmrKey k0 = new AmrKey(tradeDate, price);
                    TreeSet<AmRecord> s = outTable.get(k0);
                    if(s!=null) {
                        for(AmRecord r: s) {
                            if(r.hexTimePoint>inTime && r.hexTimePoint<nearestTime) {
                                nearestTime = r.hexTimePoint;
                                item = r;
                                break;
                            }
                        }
                    }
                }
            }
            if(item!=null)
                break;

            tradeDate = stockDates.nextDate(tradeDate);
        }

        return item;
    }

    public static class AmrKey {
        public String tradeDate;
        public double price;

        public AmrKey(long tp, double price) {
            this(Time.getTimeYMD(tp, false), price);
        }
        public AmrKey(String tradeDate, double price) {
            this.tradeDate = tradeDate;
            this.price = price;
        }

        public int compareTo(AmrKey k) {
            if(price != k.price) {
                return Double.compare(price, k.price);
            } else {
                return tradeDate.compareTo(k.tradeDate);
            }
        }
    }

    public static class AkComparator implements Comparator<AmrKey> {
        @Override
        public int compare(AmrKey k0, AmrKey k1) {
            return k0.compareTo(k1);
        }
    }
}
