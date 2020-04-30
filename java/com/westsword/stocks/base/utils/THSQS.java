package com.westsword.stocks.base.utils;

import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

import com.westsword.stocks.base.Utils;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.utils.StockPaths;

public class THSQS {
    public final static int EXEC_TIMEOUT_SECONDS = 60;
    public final static String[] sTradedDate = {"成交日期", "成交日期"};
    public final static String[] sTradedTime = {"成交时间", "成交时间"};
    public final static String[] sEntrustTraded = {"已成", "已成"};
    public final static String[] sBalanceAvaiRemains = {"可用金额", "可用金额"};
    public final static String[] sEntrustStateKeys = {"备注", "委托状态"};
    public final static String[] sEntrustPriceKeys = {"成交均价", "成交价格"};
    public final static String[] sPositionAvaiVols = {"可用余额", "可用股份"};

    public String getStringEntrustTraded() {
        return sEntrustTraded[qsIdx];
    }
    //0 - jyzq
    //1 - zxzq
    private int qsIdx = Settings.getQsIdx();

    public THSQS() {
        String sQSIdx = System.getenv("qsIdx");;
        if(sQSIdx != null)
            qsIdx = Integer.valueOf(sQSIdx);
        System.err.format("%s: qsIdx = %d\n", Utils.getCallerName(getClass()), qsIdx);
    }
    private int wait4ExitValue(Process proc) {
        int exitVal=-1;
        try {
            boolean bRet = proc.waitFor(EXEC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if(!bRet) { 
                Stream<ProcessHandle> sHandles = proc.descendants();
                Iterator<ProcessHandle> itr = sHandles.iterator();
                while(itr.hasNext()) {
                    ProcessHandle h = itr.next();
                    System.err.format("%s: timeOut happened! destroy pid=%d\n", 
                            Utils.getCallerName(getClass()), h.pid());
                    h.destroy();
                }
                System.err.format("%s: timeOut happened! destroy pid=%d\n", 
                        Utils.getCallerName(getClass()), proc.pid());
                proc.destroy();
            } else {
                exitVal = proc.exitValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exitVal;
    }
    public String execCommand(String sCommand, String[] outValues) {
        String sText = "";

        try {
            System.out.format("%s: %s\n", Utils.getCallerName(getClass()), sCommand);
            String[] cmd = {"cmd", "/C", sCommand}; 
            Process proc = Runtime.getRuntime().exec(cmd);

            int exitVal=wait4ExitValue(proc);
            outValues[0] = ""+exitVal;
            System.out.format("%s: Process exitValue: %d\n", 
                    Utils.getCallerName(getClass()), exitVal);

            //need to get the stdout
            InputStream stdin = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null)
                sText += line + "\n";
            System.out.format("%s: sText=%s\n", 
                    Utils.getCallerName(getClass()), sText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sText;
    }
    private String getCommandOutput(String sCommand) {
        String[] outValues = new String[1];
        String value = execCommand(sCommand, outValues);
        return value.equals("")?value:value.substring(0, value.length()-1);
    }


    private String getMarketSellCommand(String stockCode, double inPrice, int tradeVol) {
        String sCommand = "";
        sCommand += StockPaths.pythonCommand();
        sCommand += " ";
        sCommand += StockPaths.pythonSellPath();
        sCommand += " " + stockCode;
        sCommand += " " + String.format("%.2f", Utils.roundUp(inPrice));
        sCommand += " " + tradeVol;
        sCommand += " " + String.format("%.2f", Utils.roundUp(inPrice));

        return sCommand;
    }
    public String marketSell(String stockCode, double inPrice, int tradeVol) {
        String sCommand = getMarketSellCommand(stockCode, inPrice, tradeVol);

        //need to get the entrustno
        return execAndGetEntrustNO(sCommand);
    }
    private String getSellCommand(String stockCode, double inPrice, int tradeVol) {
        String sCommand = "";
        sCommand += StockPaths.pythonCommand();
        sCommand += " ";
        sCommand += StockPaths.pythonSellPath();
        sCommand += " " + stockCode;
        sCommand += " " + String.format("%.2f", Utils.roundUp(inPrice));
        sCommand += " " + tradeVol;

        return sCommand;
    }
    public String sell(String stockCode, double inPrice, int tradeVol) {
        String sCommand = getSellCommand(stockCode, inPrice, tradeVol);

        //need to get the entrustno
        return execAndGetEntrustNO(sCommand);
    }
    private String getBuyCommand(String stockCode, double inPrice, int tradeVol) {
        String sCommand = "";
        sCommand += StockPaths.pythonCommand();
        sCommand += " ";
        sCommand += StockPaths.pythonBuyPath();
        sCommand += " " + stockCode;
        sCommand += " " + String.format("%.2f", Utils.roundUp(inPrice));
        sCommand += " " + tradeVol;

        return sCommand;
    }
    public String buy(String stockCode, double inPrice, int tradeVol) {
        String sCommand = getBuyCommand(stockCode, inPrice, tradeVol);

        //need to get the entrustno
        return execAndGetEntrustNO(sCommand);
    }
    private String getMarketBuyCommand(String stockCode, double inPrice, int tradeVol) {
        String sCommand = "";
        sCommand += StockPaths.pythonCommand();
        sCommand += " ";
        sCommand += StockPaths.pythonBuyPath();
        sCommand += " " + stockCode;
        sCommand += " " + String.format("%.2f", Utils.roundUp(inPrice));
        sCommand += " " + tradeVol;
        sCommand += " " + String.format("%.2f", Utils.roundUp(inPrice));

        return sCommand;
    }
    public String marketBuy(String stockCode, double inPrice, int tradeVol) {
        String sCommand = getMarketBuyCommand(stockCode, inPrice, tradeVol);

        //need to get the entrustno
        return execAndGetEntrustNO(sCommand);
    }


    public String getRefreshCommand() {
        String sCommand = "";
        sCommand += StockPaths.pythonCommand();
        sCommand += " ";
        sCommand += StockPaths.pythonRefreshPath();

        return sCommand;
    }
    public void refresh() {
        String sCommand = getRefreshCommand();

        String[] outValues = new String[1];
        String value = execCommand(sCommand, outValues);
        return;
    }


    public String getPositionKeyCommand(String stockCode, String key) {
        String sCommand = "";
        sCommand += StockPaths.pythonCommand();
        sCommand += " ";
        sCommand += StockPaths.pythonGetPositionPath();
        sCommand += " " + stockCode;
        sCommand += " " + key;

        return sCommand;

    }
    public String queryPosition(String stockCode, String key) {
        String sCommand = getPositionKeyCommand(stockCode, key);

        //need to get the position's key value for stockCode
        return getCommandOutput(sCommand);
    }
    public Integer getStockAvaiVols(String stockCode) {
        String sPosition = queryPosition(stockCode, sPositionAvaiVols[qsIdx]);

        String sPattern = "[0-9]{1,}";
        if(!sPosition.matches(sPattern)) {
            sPosition = "0";
        }

        return Integer.valueOf(sPosition);
    }


    public String getBalanceKeyCommand(String key) {
        String sCommand = "";
        sCommand += StockPaths.pythonCommand();
        sCommand += " ";
        sCommand += StockPaths.pythonGetBalancePath();
        sCommand += " " + key;

        return sCommand;
    } 
    public String queryBalance(String key) {
        String sCommand = getBalanceKeyCommand(key);

        //need to get the entrust's key value
        return getCommandOutput(sCommand);
    }
    public Double getAvaiRemains() {
        return Double.valueOf(queryBalance(sBalanceAvaiRemains[qsIdx]));
    }


    public String getEntrustKeyCommand(String sEntrustNO, String key) {
        String sCommand = "";
        sCommand += StockPaths.pythonCommand();
        sCommand += " ";
        sCommand += StockPaths.pythonGetEntrustPath();
        sCommand += " " + sEntrustNO;
        sCommand += " " + key;

        return sCommand;
    } 
    public String queryEntrust(String sEntrustNO, String key) {
        String sCommand = getEntrustKeyCommand(sEntrustNO, key);

        //need to get the entrust's key value
        return getCommandOutput(sCommand);
    }
    public Double queryEntrustAvgPrice(String sEntrustNO) {
        //need to get the entrust price 
        String sRet = queryEntrust(sEntrustNO, sEntrustPriceKeys[qsIdx]);
        Double dRet = Double.NaN;
        if(!sRet.equals(""))
            dRet = Double.valueOf(sRet);

        return dRet;
    }
    public String queryEntrustState(String sEntrustNO) {
        //need to get the entrust state
        return queryEntrust(sEntrustNO, sEntrustStateKeys[qsIdx]);
    }
    //a loop to query an entrust, return 
    //  成交均价 with 合同编号==sEntrustNO && 备注==已成(jyzq)
    //  成交价格 with 委托编号==sEntrustNO && 委托状态==已成(zxzq)
    public Double wait4EntrustPrice(String sEntrustNO) {
        Double tradePrice = Double.NaN;
        while(true) {
            String state = queryEntrustState(sEntrustNO);
            if(state.equals(getStringEntrustTraded())) {
                tradePrice = queryEntrustAvgPrice(sEntrustNO);
                break;
            }
        }
        return tradePrice;
    }


    public String getTradeKeyCommand(String sEntrustNO, String key) {
        String sCommand = "";
        sCommand += StockPaths.pythonCommand();
        sCommand += " ";
        sCommand += StockPaths.pythonGetTradePath();
        sCommand += " " + sEntrustNO;
        sCommand += " " + key;

        return sCommand;
    }
    public String queryTrade(String sEntrustNO, String key) {
        String sCommand = getTradeKeyCommand(sEntrustNO, key);

        //need to get the trade's key value
        return getCommandOutput(sCommand);
    }
    public String queryTradedTime(String sEntrustNO) {
        //need to get the traded time
        String sRet = queryTrade(sEntrustNO, sTradedTime[qsIdx]);
        //only return the 1st one
        String[] fields = sRet.split("\n");
        sRet = fields[0];

        return sRet;
    }
    public String queryTradedDate(String sEntrustNO) {
        //need to get the traded date
        String sRet = queryTrade(sEntrustNO, sTradedDate[qsIdx]);
        //only return the 1st one
        String[] fields = sRet.split("\n");
        sRet = fields[0];

        return sRet;
    }

    //not in use now
    public Double queryTradedPrice(String sEntrustNO) {
        return Double.valueOf(queryTrade(sEntrustNO, sEntrustPriceKeys[qsIdx]));
    }
    //not in use now
    //a loop to query an entrust, return 
    //  成交均价 with 合同编号==sEntrustNO 
    //  成交价格 with 委托编号==sEntrustNO
    public double wait4TradedPrice(String sEntrustNO) {
        Double tradePrice = Double.NaN;
        while(true) {
            String sPrice = String.format("%.3f", queryTradedPrice(sEntrustNO));
            String sPattern = "[0-9]{1,}.[0-9]{1,}";
            if(sPrice.matches(sPattern)) {
                tradePrice = Double.valueOf(sPrice);
                break;
            }
        }
        return tradePrice;
    }

    private boolean matchEntrustNO(String sEntrustNO) {
        String sPattern = "[0-9]{1,}";
        return sEntrustNO.matches(sPattern);
    }
    private String execAndGetEntrustNO(String sCommand, boolean bThrowRTE) {
        while(true) {
            String sOutput = getCommandOutput(sCommand);
            if(!matchEntrustNO(sOutput)) {
                if(bThrowRTE) {
                    String msg = String.format("%s is not a valid entrust number!\n", sOutput);
                    throw new RuntimeException(msg);
                }
            } else {
                return sOutput;
            }
        }
    }
    private String execAndGetEntrustNO(String sCommand) {
        return execAndGetEntrustNO(sCommand, false);
    }
}

