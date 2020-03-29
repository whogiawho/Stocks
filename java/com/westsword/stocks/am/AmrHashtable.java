package com.westsword.stocks.am;


import java.util.*;

import com.westsword.stocks.base.Stock;
import com.westsword.stocks.base.time.Time;
import com.westsword.stocks.base.time.StockDates;
import com.westsword.stocks.base.time.AStockSdTime;

public class AmrHashtable {
    private TreeMap<AmrKey, TreeSet<AmRecord>> mOutTable4Long;
    private TreeMap<AmrKey, TreeSet<AmRecord>> mOutTable4Short;
    private TreeMap<String, TreeSet<Double>>   mTdPriceMap;    //including both upPrice&downPrice

    public AmrHashtable() {
        mOutTable4Long = new TreeMap<AmrKey, TreeSet<AmRecord>>(new AkComparator());
        mOutTable4Short = new TreeMap<AmrKey, TreeSet<AmRecord>>(new AkComparator());
        mTdPriceMap = new TreeMap<String, TreeSet<Double>>();
    }

    private TreeMap<AmrKey, TreeSet<AmRecord>> getOutTable(int tradeType) {
        return tradeType==Stock.TRADE_TYPE_LONG?mOutTable4Long:mOutTable4Short;
    }
    public TreeMap<String, TreeSet<Double>> getTdPriceMap() {
        return mTdPriceMap;
    }


    //merge t into this AmrHashtable
    public void merge(AmrHashtable t) {
        mergeOutTable(t, Stock.TRADE_TYPE_LONG);
        mergeOutTable(t, Stock.TRADE_TYPE_SHORT);

        mergeTdPriceMap(t);
    }
    private void mergeTdPriceMap(AmrHashtable t) {
        TreeMap<String, TreeSet<Double>> m = t.getTdPriceMap();
        TreeMap<String, TreeSet<Double>> current = getTdPriceMap();
        for(String k: m.keySet()) {
            TreeSet<Double> v = current.get(k);
            if(v!=null) {
                v.addAll(m.get(k));
            } else {
                current.put(k, m.get(k));
            }
        }
    }
    private void mergeOutTable(AmrHashtable t, int tradeType) {
        TreeMap<AmrKey, TreeSet<AmRecord>> m = t.getOutTable(tradeType);
        TreeMap<AmrKey, TreeSet<AmRecord>> current = getOutTable(tradeType);
        for(AmrKey k: m.keySet()) {
            TreeSet<AmRecord> v = current.get(k);
            if(v!=null) {
                v.addAll(m.get(k));
            } else {
                current.put(k, m.get(k));
            }
        }
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


    private double getMaxDeltaPriceBias(long inTime, double inPrice, String tradeDate, double price, 
            AmRecord outItem, double maxDeltaPriceBias, TreeMap<AmrKey, TreeSet<AmRecord>> table) {
        AmrKey k = new AmrKey(tradeDate, price);
        TreeSet<AmRecord> s = table.get(k);
        if(s!=null) {
            for(AmRecord r: s) {
                if(r.hexTimePoint>=inTime && r.hexTimePoint<=outItem.hexTimePoint) {
                    double delta = Math.abs(inPrice-price);
                    if(delta>maxDeltaPriceBias) {
                        maxDeltaPriceBias = delta;
                    }
                    break;
                }
            }
        }

        return maxDeltaPriceBias;
    }
    //maxDeltaPriceBias does not consider sTDistance, it always starts from inTime
    public double getMaxDeltaPriceBias(long inTime, int tradeType, 
            String nextTradeDateN, StockDates stockDates, 
            AmRecord outItem, AmManager am) {
        double maxDeltaPriceBias = 0;

        double inPrice = am.getInPrice(tradeType, inTime);
        TreeMap<AmrKey, TreeSet<AmRecord>> outTable = getOutTable(tradeType);

        //set outTradeDate&outItem
        String outTradeDate = nextTradeDateN;
        if(outItem!=null)
            outTradeDate = Time.getTimeYMD(outItem.hexTimePoint, false);
        if(outItem==null)
            outItem = am.getFloorItem(AStockSdTime.getCloseQuotationTime(nextTradeDateN));
        //set tradeDate
        String inTradeDate = Time.getTimeYMD(inTime, false);
        String tradeDate = inTradeDate;
        while(tradeDate!=null && tradeDate.compareTo(outTradeDate)<=0) {
            TreeSet<Double> e = mTdPriceMap.get(tradeDate);
            if(e!=null) {
                for(Double price: e) {
                    if(skipPrice(tradeType, price, inPrice)) {
                        maxDeltaPriceBias = getMaxDeltaPriceBias(inTime, inPrice, tradeDate, price, 
                                outItem, maxDeltaPriceBias, outTable);
                    } 
                }
            }

            tradeDate = stockDates.nextDate(tradeDate);
        }

        return maxDeltaPriceBias;
    }


    public double getMaxPosPrice(long inTime, int tradeType, 
            String nextTradeDateN, int sTDistance, StockDates stockDates) {
        double maxPosPrice = Double.POSITIVE_INFINITY;
        if(tradeType == Stock.TRADE_TYPE_LONG)
            maxPosPrice = Double.NEGATIVE_INFINITY;

        TreeMap<AmrKey, TreeSet<AmRecord>> outTable = getOutTable(tradeType);
        String outTradeDate = nextTradeDateN;
        String inTradeDate = Time.getTimeYMD(inTime, false);
        String tradeDate = inTradeDate;
        if(sTDistance!=0)
            tradeDate = stockDates.nextDate(tradeDate);
        while(tradeDate!=null && tradeDate.compareTo(outTradeDate)<=0) {
            TreeSet<Double> e = mTdPriceMap.get(tradeDate);
            if(e!=null) {
                for(Double price: e) {
                    maxPosPrice = getMaxPosPrice(inTime, tradeType, tradeDate, price, 
                            maxPosPrice, outTable);
                }
            }

            tradeDate = stockDates.nextDate(tradeDate);
        }

        return maxPosPrice;
    }
    private double getMaxPosPrice(long inTime, int tradeType, String tradeDate, double price, 
            double maxPosPrice, TreeMap<AmrKey, TreeSet<AmRecord>> table) {
        AmrKey k = new AmrKey(tradeDate, price);
        TreeSet<AmRecord> s = table.get(k);
        if(s!=null) {
            for(AmRecord r: s) {
                if(r.hexTimePoint>=inTime) {
                    if(tradeType == Stock.TRADE_TYPE_LONG) {
                        if(r.downPrice > maxPosPrice)
                            maxPosPrice = r.downPrice;
                    } else {
                        if(r.upPrice < maxPosPrice)
                            maxPosPrice = r.upPrice;
                    }
                    break;
                }
            }
        }

        return maxPosPrice;
    }


    //get the nearest AmRecord meeting below conditions:
    //  tradeDate<=nextTradeDateN 
    //  tp>=inTime depending on sTDistance
    //  upPrice|downPrice exceeds outPrice depending on tradeType
    public AmRecord getOutItem(long inTime, String nextTradeDateN, double outPrice,
            int sTDistance, int tradeType, StockDates stockDates) {
        AmRecord item = null;

        TreeMap<AmrKey, TreeSet<AmRecord>> outTable = getOutTable(tradeType);
        long[] out = new long[] {
            Long.MAX_VALUE,
        };
        //set tradeDate
        String inTradeDate = Time.getTimeYMD(inTime, false);
        String tradeDate = inTradeDate;
        if(sTDistance!=0)
            tradeDate = stockDates.nextDate(tradeDate);
        while(tradeDate!=null && tradeDate.compareTo(nextTradeDateN)<=0) {
            TreeSet<Double> e = mTdPriceMap.get(tradeDate);
            if(e!=null) {
                //loop tradeDate's all possible prices to get the nearest out AmRecord
                for(Double price: e) {
                    if(skipPrice(tradeType, price, outPrice))
                        continue;
    
                    item = searchOutAmRecord(item, outTable, tradeDate, price, inTime, out);
                }
            }
            if(item!=null)
                break;

            tradeDate = stockDates.nextDate(tradeDate);
        }

        return item;
    }
    private boolean skipPrice(int tradeType, double price, double outPrice) {
        return tradeType==Stock.TRADE_TYPE_LONG&&price<outPrice || 
            tradeType==Stock.TRADE_TYPE_SHORT&&price>outPrice;
    }
    private AmRecord searchOutAmRecord(AmRecord r0, TreeMap<AmrKey, TreeSet<AmRecord>> table, 
            String tradeDate, double price, long inTime, long[] out) {
        AmRecord item = r0;

        AmrKey k = new AmrKey(tradeDate, price);
        TreeSet<AmRecord> s = table.get(k);
        if(s!=null) {
            for(AmRecord r: s) {
                if(r.hexTimePoint>inTime && r.hexTimePoint<out[0]) {
                    out[0] = r.hexTimePoint;
                    item = r;
                    break;
                }
            }
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
