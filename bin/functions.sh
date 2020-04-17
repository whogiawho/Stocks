#!/bin/bash

user=${user:-"whogi"}
rootDir=${rootDir:-"d:\\Stocks"}
rootDirCygdrive=${rootDirCygdrive:-"/cygdrive/d/Stocks"}
thsHackRootDirCygdrive=${thsHackRootDirCygdrive:-"$rootDirCygdrive/thsHack"}
dataRoot=${dataRoot:-"d:\\Stocks\\data"}
rawZuBiDataDir=${rawZuBiDataDir:-"$dataRoot\\rawTradeDetails"}
rawPankouDataDir=${rawPankouDataDir:-"$dataRoot\\rawPankou"}
dailyDir=${dailyDir:-"$dataRoot\\daily"}
thsRootDirCygdrive=${thsRootDirCygdrive:-"/cygdrive/e/HexinSoftware/Hexin"}
TYPE_BUY=0
TYPE_SELL=1
EXT_PAN=5
INT_PAN=1
UP=$EXT_PAN
DOWN=$INT_PAN

export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
cygwin32RootDir="E:\\cygwin"
cygwin64RootDir="E:\\cygwin64"
cygwinRootDir=$cygwin32RootDir
cygwinBitMode=`getCygwinBitMode`
echo cygwinBitMode=$cygwinBitMode
[[ $cygwinBitMode == 64 ]] && {
    HeapBase=10
    HeapSize=$(($HeapBase*1024))
    export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Xmx${HeapSize}m -Xms${HeapSize}m"
    cygwinRootDir=$cygwin64RootDir
}
cygwinTmpDir="$cygwinRootDir\\tmp"

. bin/settings.sh
. bin/time.sh
. bin/tradetime.sh
. bin/tradedate.sh
. bin/hexin.sh
. bin/rawdata.sh
. bin/utils.sh
. bin/pankou.sh
. bin/tradedetails.sh
. bin/similarstack.sh
. bin/amcorrel.sh
. bin/gcp.sh
. bin/sscommons.sh
. bin/ssn.sh


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

