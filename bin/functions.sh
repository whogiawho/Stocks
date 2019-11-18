#!/bin/bash

user=${user:-"whogi"}
rootDir=${rootDir:-"F:\\Stocks"}
cygwinRootDir=${cygwinRootDir:-"/cygdrive/f/Stocks"}
thsHackRootDir=${thsHackRootDir:-"$cygwinRootDir/thsHack"}
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
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"


. bin/settings.sh
. bin/time.sh
. bin/tradetime.sh
. bin/tradedate.sh
. bin/hexin.sh
. bin/rawdata.sh
. bin/utils.sh
javaBitMode=`getJavaBitMode`
echo javaBitMode=$javaBitMode
[[ $javaBitMode == 64 ]] && {
    export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Xmx2048m -Xms1272m"
}
. bin/pankou.sh
. bin/tradedetails.sh


function setupCfgFile {
    local stockCode=$1
    local tradeDate=$2


    setValue stockCode $stockCode
    setValue currentDate $tradeDate

    local sHexTime=`convertTime2Hex $tradeDate $CloseQuotationTime`
    setValue "currentDateCloseQuotationTime" $sHexTime

    sHexTime=`convertTime2Hex $tradeDate $LastRawTradeDetailTime`
    setValue "currentDateLastRawTradeDetailTime" $sHexTime

    unix2dos "$rootDir\\settings.txt"
}

function getReadableZuBiFile {
    local rawZuBiFile=$1                                   #in
    local outZuBiFile=$2                                   #in

    local tmp0=
    tmp0=`mktemp`
    tmp0=`getWindowPathOfTmpFile $tmp0`
    local topost0Jar="$rootDir\\build\\jar\\topost0.jar"
    $JAVA -jar $topost0Jar $rawZuBiFile $tmp0 $outZuBiFile

    rm -rf $tmp0
}

