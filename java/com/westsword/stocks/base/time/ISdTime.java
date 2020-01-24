package com.westsword.stocks.base.time;

interface ISdTime {
    public int get(String hms, int interval); 
    public String rget(int relsdtime, int interval);
}

