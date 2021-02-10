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

public class Offset100 extends Offset {
    public Offset100() {
        super();
        _getNegativeSAms(getNegativeSAms());
        _getPositiveSAms(getPositiveSAms());
        makeThresMap();
    }

    private void _getNegativeSAms(ArrayList<SAm> list) {
        list.add(new SAm("600030", "20150505", "103457"));
        list.add(new SAm("600030", "20150505", "103557"));
        list.add(new SAm("600030", "20120913", "111957"));

        list.add(new SAm("600030", "20181207", "144956"));
        list.add(new SAm("600030", "20181207", "145056"));
        list.add(new SAm("600030", "20181207", "145156"));
        list.add(new SAm("600030", "20181207", "145356"));
        list.add(new SAm("600030", "20181207", "145256"));
        list.add(new SAm("600030", "20180718", "104657"));
        list.add(new SAm("600030", "20180718", "104757"));
        list.add(new SAm("600030", "20180718", "104857"));
        list.add(new SAm("600030", "20180718", "104957"));
        list.add(new SAm("600030", "20180718", "104557"));

    }
    private void _getPositiveSAms(ArrayList<SAm> list) {
        list.add(new SAm("600030", "20090616", "092500"));
        list.add(new SAm("600030", "20090616", "093057"));
        list.add(new SAm("600030", "20090615", "144756"));
        list.add(new SAm("600030", "20090616", "093157"));
        list.add(new SAm("600030", "20090615", "144856"));
        list.add(new SAm("600030", "20090616", "093257"));
        list.add(new SAm("600030", "20090615", "144956"));
        list.add(new SAm("600030", "20090616", "093357"));
        list.add(new SAm("600030", "20090615", "145056"));
        list.add(new SAm("600030", "20090616", "093457"));
        list.add(new SAm("600030", "20090615", "145156"));
        list.add(new SAm("600030", "20090616", "093557"));
        list.add(new SAm("600030", "20090615", "145256"));
        list.add(new SAm("600030", "20090616", "093657"));
        list.add(new SAm("600030", "20090615", "145356"));
        list.add(new SAm("600030", "20090615", "145456"));
        list.add(new SAm("600030", "20090615", "145556"));
        list.add(new SAm("600030", "20090615", "145656"));
        list.add(new SAm("600030", "20090615", "145756"));
        list.add(new SAm("600030", "20090615", "145856"));
        list.add(new SAm("600030", "20090615", "145956"));
        list.add(new SAm("600030", "20091110", "101757"));
        list.add(new SAm("600030", "20091110", "101857"));
        list.add(new SAm("600030", "20091110", "101957"));
        list.add(new SAm("600030", "20091110", "102057"));
        list.add(new SAm("600030", "20091110", "102157"));
        list.add(new SAm("600030", "20091110", "102257"));
        list.add(new SAm("600030", "20091110", "102357"));
        list.add(new SAm("600030", "20091110", "102457"));
        list.add(new SAm("600030", "20091110", "102557"));
        list.add(new SAm("600030", "20091110", "102657"));
        list.add(new SAm("600030", "20091110", "102757"));
        list.add(new SAm("600030", "20091110", "102857"));
        list.add(new SAm("600030", "20091110", "102957"));
        list.add(new SAm("600030", "20091110", "103057"));
        list.add(new SAm("600030", "20091110", "103157"));
        list.add(new SAm("600030", "20091110", "103257"));
        list.add(new SAm("600030", "20091110", "103357"));
        list.add(new SAm("600030", "20091110", "103457"));
        list.add(new SAm("600030", "20091110", "103557"));
        list.add(new SAm("600030", "20091110", "103657"));
        list.add(new SAm("600030", "20091110", "103757"));
        list.add(new SAm("600030", "20091110", "103857"));
        list.add(new SAm("600030", "20091110", "103957"));
        list.add(new SAm("600030", "20091110", "104057"));
        list.add(new SAm("600030", "20091110", "104157"));
        list.add(new SAm("600030", "20091110", "104257"));
        list.add(new SAm("600030", "20091110", "104357"));
        list.add(new SAm("600030", "20091110", "104457"));
        list.add(new SAm("600030", "20091110", "104557"));
        list.add(new SAm("600030", "20091110", "104657"));
        list.add(new SAm("600030", "20091110", "104757"));
        list.add(new SAm("600030", "20091110", "104857"));
        list.add(new SAm("600030", "20091110", "104957"));
        list.add(new SAm("600030", "20091110", "105057"));
        list.add(new SAm("600030", "20091110", "105157"));
        list.add(new SAm("600030", "20091110", "105257"));
        list.add(new SAm("600030", "20091110", "105357"));
        list.add(new SAm("600030", "20091110", "105457"));
        list.add(new SAm("600030", "20091110", "105557"));
        list.add(new SAm("600030", "20120116", "131156"));
        list.add(new SAm("600030", "20120116", "131456"));
        list.add(new SAm("600030", "20120116", "144756"));
        list.add(new SAm("600030", "20120116", "144856"));
        list.add(new SAm("600030", "20120907", "100457"));
        list.add(new SAm("600030", "20120907", "100557"));
        list.add(new SAm("600030", "20120907", "100657"));
        list.add(new SAm("600030", "20120907", "100857"));
        list.add(new SAm("600030", "20120907", "100957"));
        list.add(new SAm("600030", "20140107", "095957"));
        list.add(new SAm("600030", "20140107", "100057"));
        list.add(new SAm("600030", "20140107", "100157"));
        list.add(new SAm("600030", "20140107", "100257"));
        list.add(new SAm("600030", "20140107", "100357"));
        list.add(new SAm("600030", "20140107", "100457"));
        list.add(new SAm("600030", "20150327", "104157"));
        list.add(new SAm("600030", "20150327", "104257"));
        list.add(new SAm("600030", "20150327", "104357"));
        list.add(new SAm("600030", "20150327", "104457"));
        list.add(new SAm("600030", "20150327", "104557"));
        list.add(new SAm("600030", "20150327", "104657"));
        list.add(new SAm("600030", "20150327", "104757"));
        list.add(new SAm("600030", "20150327", "104857"));
        list.add(new SAm("600030", "20150327", "104957"));
        list.add(new SAm("600030", "20150327", "105057"));
        list.add(new SAm("600030", "20150327", "105157"));
        list.add(new SAm("600030", "20150327", "105257"));
        list.add(new SAm("600030", "20150327", "105357"));
        list.add(new SAm("600030", "20150327", "111057"));
        list.add(new SAm("600030", "20150327", "111157"));
        list.add(new SAm("600030", "20150327", "111257"));
        list.add(new SAm("600030", "20150327", "111357"));
        list.add(new SAm("600030", "20150327", "111457"));
        list.add(new SAm("600030", "20150327", "111557"));
        list.add(new SAm("600030", "20150327", "111657"));
        list.add(new SAm("600030", "20150327", "111757"));
        list.add(new SAm("600030", "20150327", "111857"));
        list.add(new SAm("600030", "20150327", "111957"));
        list.add(new SAm("600030", "20150327", "112057"));
        list.add(new SAm("600030", "20150327", "112157"));
        list.add(new SAm("600030", "20150327", "112257"));
        list.add(new SAm("600030", "20150327", "112357"));
        list.add(new SAm("600030", "20150327", "112457"));
        list.add(new SAm("600030", "20150327", "112557"));
        list.add(new SAm("600030", "20150327", "112657"));
        list.add(new SAm("600030", "20150327", "112757"));
        list.add(new SAm("600030", "20150327", "112857"));
        list.add(new SAm("600030", "20150327", "112957"));
        list.add(new SAm("600030", "20150327", "130056"));
        list.add(new SAm("600030", "20150327", "130956"));
        list.add(new SAm("600030", "20150327", "142256"));
        list.add(new SAm("600030", "20150327", "142356"));
        list.add(new SAm("600030", "20150327", "142456"));
        list.add(new SAm("600030", "20150327", "142556"));
        list.add(new SAm("600030", "20150327", "142656"));
        list.add(new SAm("600030", "20150327", "142756"));
        list.add(new SAm("600030", "20150327", "142856"));
        list.add(new SAm("600030", "20150327", "142956"));
        list.add(new SAm("600030", "20150327", "143056"));
        list.add(new SAm("600030", "20150327", "143156"));
        list.add(new SAm("600030", "20150327", "143256"));
        list.add(new SAm("600030", "20150327", "143356"));
        list.add(new SAm("600030", "20150327", "143456"));
        list.add(new SAm("600030", "20150327", "143556"));
        list.add(new SAm("600030", "20150327", "143656"));
        list.add(new SAm("600030", "20150327", "143756"));
        list.add(new SAm("600030", "20150327", "143856"));
        list.add(new SAm("600030", "20150327", "143956"));
        list.add(new SAm("600030", "20150327", "144056"));
        list.add(new SAm("600030", "20150327", "144156"));
        list.add(new SAm("600030", "20150327", "144256"));
        list.add(new SAm("600030", "20150327", "144356"));
        list.add(new SAm("600030", "20150327", "144456"));
        list.add(new SAm("600030", "20150327", "144556"));
        list.add(new SAm("600030", "20150327", "144656"));
        list.add(new SAm("600030", "20150327", "144756"));
        list.add(new SAm("600030", "20150327", "144856"));
        list.add(new SAm("600030", "20150327", "144956"));
        list.add(new SAm("600030", "20170216", "132756"));
        list.add(new SAm("600030", "20170216", "132856"));
        list.add(new SAm("600030", "20170216", "132956"));
        list.add(new SAm("600030", "20170216", "133056"));
        list.add(new SAm("600030", "20170216", "133156"));
        list.add(new SAm("600030", "20170216", "133256"));
        list.add(new SAm("600030", "20170216", "133356"));
        list.add(new SAm("600030", "20170216", "133456"));
        list.add(new SAm("600030", "20170216", "133556"));
        list.add(new SAm("600030", "20170216", "133656"));
        list.add(new SAm("600030", "20170216", "133756"));
        list.add(new SAm("600030", "20170216", "133856"));
        list.add(new SAm("600030", "20180427", "144456"));
        list.add(new SAm("600030", "20180427", "144556"));
        list.add(new SAm("600030", "20180427", "144656"));
        list.add(new SAm("600030", "20180427", "144756"));
        list.add(new SAm("600030", "20180427", "144856"));
        list.add(new SAm("600030", "20180427", "144956"));
        list.add(new SAm("600030", "20180427", "145056"));
        list.add(new SAm("600030", "20180427", "145156"));
        list.add(new SAm("600030", "20180427", "145256"));
        list.add(new SAm("600030", "20180427", "145356"));
        list.add(new SAm("600030", "20180427", "145456"));
        list.add(new SAm("600030", "20180427", "145556"));
        list.add(new SAm("600030", "20180427", "145656"));
        list.add(new SAm("600030", "20180427", "145756"));
        list.add(new SAm("600030", "20190315", "135656"));
        list.add(new SAm("600030", "20190315", "135756"));
        list.add(new SAm("600030", "20190315", "135856"));
        list.add(new SAm("600030", "20190315", "135956"));
        list.add(new SAm("600030", "20190315", "140056"));
        list.add(new SAm("600030", "20190315", "140156"));
        list.add(new SAm("600030", "20190315", "140256"));
        list.add(new SAm("600030", "20190315", "140356"));
        list.add(new SAm("600030", "20190315", "140456"));
        list.add(new SAm("600030", "20190315", "140556"));
        list.add(new SAm("600030", "20190816", "102357"));
        list.add(new SAm("600030", "20190816", "102457"));
        list.add(new SAm("600030", "20190816", "102557"));
        list.add(new SAm("600030", "20190816", "102657"));
        list.add(new SAm("600030", "20190816", "102757"));
        list.add(new SAm("600030", "20190816", "102857"));
    }
}
