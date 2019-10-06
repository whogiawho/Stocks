#!/bin/bash

user=${user:-"whogi"}
rootDir=${rootDir:-"F:\\Stocks"}
cygwinRootDir=${cygwinRootDir:-"/cygdrive/f/Stocks"}
dataRoot=${dataRoot:-"F:\\Stocks\\data"}
rawZuBiDataDir=${rawZuBiDataDir:-"$dataRoot\\rawTradeDetails"}
rawPankouDataDir=${rawPankouDataDir:-"$dataRoot\\rawPankou"}
dailyDir=${dailyDir:-"$dataRoot\\daily"}
TYPE_BUY=0
TYPE_SELL=1
EXT_PAN=5
INT_PAN=1
UP=$EXT_PAN
DOWN=$INT_PAN
#JAVA="java -Xmx1272m -Xms1272m"
JAVA="java -Xmx2048m -Xms1272m"


. bin/time.sh
. bin/tradedate.sh
. bin/tradetime.sh
. bin/hexin.sh
. bin/rawdata.sh


