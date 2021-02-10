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


package com.westsword.stocks.analyze.sam2;

import java.util.*;

import com.westsword.stocks.analyze.sam.*;

public class Offset620 extends Offset {
    public Offset620() {
        super();
        _getNegativeSAms(getNegativeSAms());
        _getPositiveSAms(getPositiveSAms());
        makeThresMap();
    }

    private void _getNegativeSAms(ArrayList<SAm> list) {
    }
    private void _getPositiveSAms(ArrayList<SAm> list) {
        list.add(new SAm("600030", "20120521", "133756"));
        list.add(new SAm("600030", "20120521", "133856"));
        list.add(new SAm("600030", "20120521", "133956"));
        list.add(new SAm("600030", "20120521", "134056"));
        list.add(new SAm("600030", "20120521", "134156"));
        list.add(new SAm("600030", "20120521", "134256"));
        list.add(new SAm("600030", "20120521", "134356"));
        list.add(new SAm("600030", "20130826", "095157"));
        list.add(new SAm("600030", "20130826", "095257"));

        list.add(new SAm("600030", "20190123", "094957"));
        list.add(new SAm("600030", "20190123", "095057"));
        list.add(new SAm("600030", "20190123", "095257"));

    }
}
