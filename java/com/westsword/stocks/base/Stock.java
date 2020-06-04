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
 
 
package com.westsword.stocks.base;

public class Stock {
    public final static int TRADE_TYPE_INVALID = 0;

    public final static int TRADE_TYPE_UP = 5;
    public final static int TRADE_TYPE_DOWN = 1;
    public final static int TRADE_TYPE_EXTERNAL = 5;
    public final static int TRADE_TYPE_INTERNAL = 1;
    public final static int TRADE_TYPE_BUY = 5;
    public final static int TRADE_TYPE_SELL = 1;
    public final static int TRADE_TYPE_LONG = 5;
    public final static int TRADE_TYPE_SHORT = 1;

    public final static int LONG_POSITION = 5;
    public final static int SHORT_POSITION = 1;

    public final static int ENTER_TRADE = 0;
    public final static int QUIT_TRADE = 1;

    public final static int SHOU_UNIT = 100;

    public static String getSymbol(int tradeType) {
        String sSymbol = "+";

        if(tradeType == TRADE_TYPE_SHORT)
            sSymbol = "-";

        return sSymbol;
    }
}
