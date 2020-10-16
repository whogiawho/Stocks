#!/bin/bash

user=${user:-"whogi"}
rootDir=${rootDir:-"d:\\Stocks"}
rootDirCygdrive=${rootDirCygdrive:-"/cygdrive/d/Stocks"}
thsHackRootDirCygdrive=${thsHackRootDirCygdrive:-"$rootDirCygdrive/thsHack"}
dataRoot=${dataRoot:-"$rootDir\\data"}
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
SLEEP_INTERVAL=${SLEEP_INTERVAL:-60}

export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
cygwin32RootDir="e:\\cygwin"
cygwin64RootDir="e:\\cygwin64"
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

analyzetoolsJar="$rootDir\\build\\jar\\analyzetools.jar"
hexinProtocolJar="$rootDir\\build\\jar\\hexinprotocol.jar"
RealtimeJar="$rootDir\\build\\jar\\realtimeanalyze.jar"
topost0Jar="$rootDir\\build\\jar\\topost0.jar"
splitRawPankouJar="$rootDir\\build\\jar\\splitrawpankou.jar"
splitRawTradeDetailsJar="$rootDir\\build\\jar\\splitrawtradedetails.jar"

. bin/settings.sh
. bin/time.sh
. bin/sdtime.sh
. bin/tradetime.sh
. bin/tradedate.sh
. bin/hexin.sh
. bin/rawdata.sh
. bin/utils.sh
. bin/price.sh
. bin/pankou.sh
. bin/tradedetails.sh
. bin/amcorrel.sh
. bin/gcp.sh
. bin/similarstack.sh
. bin/sscommons.sh
. bin/ssn.sh
. bin/sstable.sh
. bin/daily.sh
. bin/thsqs.sh
. bin/session.sh
. bin/analysis.sh
. bin/qr.sh
. bin/amder.sh
. bin/perm.sh






function getReadableZuBiFile {
    local rawZuBiFile=$1                                   #in
    local outZuBiFile=$2                                   #in

    local tmp0=
    tmp0=`mktemp`
    tmp0=`getWindowPathOfTmpFile $tmp0`
    $JAVA -jar $topost0Jar $rawZuBiFile $tmp0 $outZuBiFile

    rm -rf $tmp0
}

