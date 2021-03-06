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
 
 
package com.westsword.stocks.tools;

import java.util.*;


import com.westsword.stocks.tools.helper.*;

public class AnalyzeTools{

    private static void usage() {


        System.err.println("usage: java AnalyzeTools commands");
        System.err.println("       commands are listed below:");
        System.err.println("       [ getvalue | setvalue | makeanalysistxt | priceamp |\n" +
                "         getamcorrel | getupprice | getdownprice | getam | decode |\n" +
                "         nexttradedate | prevtradedate | makepvtable | listamderivatives |\n" +
                "         getrel | rgetrel | getabs | rgetabs | getanalysis | stdprice |\n" +
                "         getlsprofit | listavgams | avgamdelta | simavgamdelta |saadstats |\n" +
                "         filecorrel | avgamcorrel | getaal2 |\n" +
                "         ssinstance | ssinstances | filterssi | sstrinstance | mminstance |\n" +
                "         getfullss | checksstable | getsstable | ssamhole | gettoppercent |\n" +
                "         searchsam[01345] | searchsam2 |\n" +
                "         getpermcoord | permstats | permsep | getamlinetype | \n" +
                "         getentrust | checkabss | submitabs | makerrp | nextaackpt |\n" +
                "         ssgroupchar | ssgroupchars | ssgroupverify | qrvgetstats |\n" +
                "         qualrange | qrmaxmatch | qrverify | qrsearchss | shrinkqrv |\n" +
                "         makessdates | ssdmaxmatchS | ssdmaxmatchA | make1mamcmap ]");

        System.exit(-1);
    }


    public static void main(String args[]) throws Exception {
        String sOption = null;
        if(args.length == 0) {
            usage();
        }
        sOption = args[0];

        if(sOption == null) {
            usage();
        } else if(sOption.equals("getvalue")) {
            SettingsHelper.getValue(args);
        } else if(sOption.equals("setvalue")) {
            SettingsHelper.setValue(args);
        } else if (sOption.equals("decode")) {
            CoderHelper.decode(args);
        } else if (sOption.equals("makeanalysistxt")) {
            AnalysisHelper.makeTxt(args);
        } else if (sOption.equals("priceamp")) {
            PriceHelper.amplitude(args);
        } else if (sOption.equals("make1mamcmap")) {
            Amc1mMapHelper amcmh = new Amc1mMapHelper();
            amcmh.make(args);
        } else if(sOption.equals("getupprice")) {
            AnalysisTxtHelper.getPrice(args);
        } else if(sOption.equals("getdownprice")) {
            AnalysisTxtHelper.getPrice(args);
        } else if (sOption.equals("getamcorrel")) {
            AmCorrelHelper ach = new AmCorrelHelper();
            ach.getAmCorrel(args);
        } else if (sOption.equals("filecorrel")) {
            FileCorrelHelper fch = new FileCorrelHelper();
            fch.getCorrel(args);
        } else if (sOption.equals("avgamcorrel")) {
            AvgAmCorrelHelper aach = new AvgAmCorrelHelper();
            aach.getCorrel(args);
        } else if (sOption.equals("getaal2")) {
            AAL2Helper aal2h = new AAL2Helper();
            aal2h.get(args);
        } else if (sOption.equals("listamderivatives")) {
            AmDerivativeHelper amdh = new AmDerivativeHelper();
            amdh.list(args);
        } else if (sOption.equals("listavgams")) {
            AvgAmHelper aah = new AvgAmHelper();
            aah.list(args);
        } else if (sOption.equals("avgamdelta")) {
            AvgAmDeltaHelper aadh = new AvgAmDeltaHelper();
            aadh.get(args);
        } else if (sOption.equals("simavgamdelta")) {
            SimAvgAmDeltaHelper saadh = new SimAvgAmDeltaHelper();
            saadh.get(args);
        } else if (sOption.equals("saadstats")) {
            SaadStatsHelper ssh = new SaadStatsHelper();
            ssh.get(args);
        } else if (sOption.equals("getam")) {
            AmHelper amh = new AmHelper();
            amh.getAm(args);
        } else if (sOption.equals("getpermcoord")) {
            PermHelper ph = new PermHelper();
            ph.getPermIdx(args);
        } else if (sOption.equals("getrel")) {
            SdTime1Helper sth = new SdTime1Helper();
            sth.getRel(args);
        } else if (sOption.equals("rgetrel")) {
            SdTime1Helper sth = new SdTime1Helper();
            sth.rgetRel(args);
        } else if (sOption.equals("getabs")) {
            SdTime1Helper sth = new SdTime1Helper();
            sth.getAbs(args);
        } else if (sOption.equals("rgetabs")) {
            SdTime1Helper sth = new SdTime1Helper();
            sth.rgetAbs(args);
        } else if (sOption.equals("getanalysis")) {
            AnalysisTxtHelper.getRange(args);
        } else if (sOption.equals("stdprice")) {
            AmHelper.stdprice(args);
        } else if (sOption.equals("nexttradedate")) {
            TradeDatesHelper.nextTradeDate(args);
        } else if (sOption.equals("prevtradedate")) {
            TradeDatesHelper.prevTradeDate(args);
        } else if (sOption.equals("ssinstance")) {
            SSInstanceHelper ssih = new SSInstanceHelper();
            ssih.run(args);
        } else if (sOption.equals("ssinstances")) {
            SSInstancesHelper ssih = new SSInstancesHelper();
            ssih.run(args);
        } else if (sOption.equals("filterssi")) {
            SSIFilterHelper ssif = new SSIFilterHelper();
            ssif.run(args);
        } else if (sOption.equals("sstrinstance")) {
            SSTRInstanceHelper sstrih = new SSTRInstanceHelper();
            sstrih.run(args);
        } else if (sOption.equals("ssgroupchar")) {
            SSGroupHelper ssgh = new SSGroupHelper();
            ssgh.listChar(args);
        } else if (sOption.equals("ssgroupchars")) {
            SSGroupHelper ssgh = new SSGroupHelper();
            ssgh.listChars(args);
        } else if (sOption.equals("ssgroupverify")) {
            SSGroupHelper ssgh = new SSGroupHelper();
            ssgh.verify(args);
        } else if (sOption.equals("mminstance")) {
            MMInstanceHelper mmih = new MMInstanceHelper();
            mmih.run(args);
        } else if (sOption.equals("qualrange")) {
            QualRangeHelper qrh = new QualRangeHelper();
            qrh.findQualified(args);
        } else if (sOption.equals("qrmaxmatch")) {
            QRMMHelper qrmmh = new QRMMHelper();
            qrmmh.maxmatch(args);
        } else if (sOption.equals("qrverify")) {
            QrVerifier qrv = new QrVerifier();
            qrv.verify(args);
        } else if (sOption.equals("qrsearchss")) {
            QualRangeHelper qrh = new QualRangeHelper();
            qrh.searchSS(args);
        } else if (sOption.equals("shrinkqrv")) {
            QrvHelper qrvh = new QrvHelper();
            qrvh.shrink(args);
        } else if (sOption.equals("qrvgetstats")) {
            QrvHelper qrvh = new QrvHelper();
            qrvh.getstats(args);
        } else if (sOption.equals("makessdates")) {
            SSDatesHelper ssdh = new SSDatesHelper();
            ssdh.make(args);
        } else if (sOption.equals("ssdmaxmatchS")) {
            SSDatesHelper ssdh = new SSDatesHelper();
            ssdh.maxMatchSingle(args);
        } else if (sOption.equals("ssdmaxmatchA")) {
            SSDatesHelper ssdh = new SSDatesHelper();
            ssdh.maxMatchAll(args);
        } else if (sOption.equals("getfullss")) {
            FullSSHelper fullssh = new FullSSHelper();
            fullssh.run(args);
        } else if (sOption.equals("checksstable")) {
            SSTableHelper ssth = new SSTableHelper();
            ssth.checkSSTable(args);
        } else if (sOption.equals("getsstable")) {
            SSTableHelper ssth = new SSTableHelper();
            ssth.getSSTableName(args);
        } else if (sOption.equals("ssamhole")) {
            AmHoleHelper.search(args);
        } else if (sOption.equals("searchsam0")) {
            SAm0Helper sam0h = new SAm0Helper();
            sam0h.search(args);
        } else if (sOption.equals("searchsam1")) {
            SAm1Helper sam1h = new SAm1Helper();
            sam1h.search(args);
        } else if (sOption.equals("searchsam3")) {
            SAm3Helper sam3h = new SAm3Helper();
            sam3h.search(args);
        } else if (sOption.equals("searchsam4")) {
            SAm4Helper sam4h = new SAm4Helper();
            sam4h.search(args);
        } else if (sOption.equals("searchsam5")) {
            SAm5Helper sam5h = new SAm5Helper();
            sam5h.search(args);
        } else if (sOption.equals("searchsam2")) {
            SAm2Helper sam2h = new SAm2Helper();
            sam2h.search(args);
        } else if (sOption.equals("getlsprofit")) {
            LSProfitHelper.get(args);
        } else if (sOption.equals("permstats")) {
            PermStatsHelper.get(args);
        } else if (sOption.equals("permsep")) {
            PermStatsHelper.get(args);
        } else if (sOption.equals("getamlinetype")) {
            AmLineHelper.getType(args);
        } else if(sOption.equals("checkabss")) {
            THSQSHelper.checkAbss(args);
        } else if(sOption.equals("submitabs")) {
            THSQSHelper.submitAbs(args);
        } else if(sOption.equals("makerrp")) {
            THSQSHelper.makeRRP(args);
        } else if(sOption.equals("nextaackpt")) {
            AACkptHelper.next(args);
        } else if(sOption.equals("makepvtable")) {
            PVTableHelper.make(args);
        } else if(sOption.equals("gettoppercent")) {
            TopPercentHelper.get(args);
        } else if(sOption.equals("getentrust")) {
            THSQSHelper.getEntrust(args);
        } else {
            usage();
        }
    }

}
