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
function doDailyTask {
    local stockCode=$1
    local tradeDate=$2

    autoTrade $stockCode $tradeDate

    continueIfRawPankouReady $stockCode $tradeDate
    continueIfRawTradeDetailsReady $stockCode $tradeDate

    routinesAfterCloseQuotation $stockCode $tradeDate
}
function continueIfRawTradeDetailsReady {
    local stockCode=$1
    local tradeDate=$2

    while [[ 1 ]]
    do
        checkRawTradeDetails $stockCode $tradeDate && {
            break
        } || sleep 10
    done
}
function continueIfRawPankouReady {
    local stockCode=$1
    local tradeDate=$2

    while [[ 1 ]]
    do
        checkRawPankou $stockCode $tradeDate && {
            break
        } || sleep 10
    done
}
function routinesAfterCloseQuotation {
    local stockCode=$1
    local tradeDate=$2

    setupCfgFile $stockCode $tradeDate

    _realtimeAnalyze $stockCode $tradeDate y
}
#convert rawPankou&rawTradeDetails, generate copylist and run realtimeAnalyze
function _realtimeAnalyze {
    local stockCode=$1
    local tradeDate=$2
    local bAppendLast=$3

    setupCfgFile $stockCode $tradeDate
    prepareRetrospective $stockCode $tradeDate $bAppendLast

    #generate analysis.txt
    java -jar $analyzetoolsJar makeanalysistxt $stockCode $tradeDate
}
#bAppendLast - fix pankou&&append last tradedetails if being set
function prepareRetrospective {
    local stockCode=$1
    local tradeDate=$2
    local bAppendLast=$3

    #local dataDir="$dailyDir\\$stockCode\\$tradeDate"
    #icacls $dataDir /grant $user:F

    setupCfgFile $stockCode $tradeDate
    printf "prepareRetrospective: %s\n" "fix&split raw pankou"
    [[ ! -z $bAppendLast ]] && {
        fixPankouTxt $stockCode $tradeDate
    }
    splitRawPankou $stockCode $tradeDate

    [[ ! -z $bAppendLast ]] && {
        printf "prepareRetrospective: %s\n" "append last raw tradedetail"
        appendLastRawTradeDetail $stockCode $tradeDate
    }
    printf "prepareRetrospective: %s\n" "split raw tradedetails"
    splitRawTradeDetails $stockCode $tradeDate

    printf "prepareRetrospective: %s\n" "get copy list"
    getCopyList $stockCode $tradeDate

    prepareAutoImitateBatch $stockCode $tradeDate
}
function prepareAutoImitateBatch {
    local stockCode=$1
    local tradeDate=$2

    setupCfgFile $stockCode $tradeDate
    local autoImitateBat="$rootDir\\bin\\autoImitate.bat"
    sed -i "s@..\\\\data\\\\copyList\\\\[0-9]\+.[0-9]\+.*@..\\\\data\\\\copyList\\\\$stockCode.$tradeDate.copylist.txt@g" $autoImitateBat

    #chmod a+rx "$autoImitateBat"                       //chmod does not work in bash function, replace it with icacls
    icacls $autoImitateBat /grant $user:RX
}
function getCopyList {
    local stockCode=$1
    local tradeDate=$2

    local emuPankouDir="$dailyDir\\$stockCode\\$tradeDate\\emuPankou"
    local emuTradeDetailsDir="$dailyDir\\$stockCode\\$tradeDate\\emuTradeDetails"
    local rtPankouDir="$dailyDir\\$stockCode\\$tradeDate\\rtPankou"
    local rtTradeDetailsDir="$dailyDir\\$stockCode\\$tradeDate\\tradeDetails"
    local copyList="$rootDir\\data\\copyList\\$stockCode.$tradeDate.copylist.txt"

    _getCopyList $emuPankouDir $emuTradeDetailsDir $rtPankouDir $rtTradeDetailsDir | tee $copyList
}
function _getCopyList {
    local dirPankou=$1
    local dirTradeDetails=$2
    local dirOutPankou=$3
    local dirOutTradeDetails=$4

    local tmp=`mktemp`

    mkdir -p $dirOutPankou
    mkdir -p $dirOutTradeDetails

    getSortedHexFileList $dirPankou $dirOutPankou              >$tmp
    getSortedHexFileList $dirTradeDetails $dirOutTradeDetails  >>$tmp

    sort -k1 $tmp

    rm -rf $tmp
}
function getSortedHexFileList {
    local hexDir=$1
    local outDir=$2

    local line=
    ls $hexDir | while read line 
    do
        local hexNumber=
        hexNumber=${line%.*}
        local ext=
        ext=${line#*.}
        #remove * from ext
        ext=${ext%\*}
        printf "%d %s.%s %s %s\n" 0x$hexNumber $hexNumber $ext $hexDir $outDir
    done
}


function autoGetLoginParms {
    python -u $rootDir\\python\\autoGetLoginParms.py
}
function autoLoginQs {
    python -u $rootDir\\python\\autologin.py
}
function execGetInstantData {
    local stockCode=$1
    local tradeDate=$2
    local serverAddr=$3
    local serverPort=$4
    local serverType=$5

    export HOME=$cygwin32RootCygdrive/home/$user;      #32bit cygwin HOME
    local pathb=$PATH                                  #save PATH
    resetPATH; 
    local sEnv=0
    local runGetInstantData=$stockCode:$tradeDate:$serverAddr:$serverPort:$serverType
    wscript.exe "$cygwin32RootDir\invisible.vbs" "$cygwin32RootDir\_mintty.bat" $sEnv $runGetInstantData; 
    export HOME=/home/$user;                           #restore HOME to 64bit cygwin
    export PATH=$pathb                                 #restore PATH
}
function autoTrade {
    local stockCode=$1
    local tradeDate=$2

    setupCfgFile $stockCode $tradeDate 

    #getLoginParms
    autoGetLoginParms

    #get server(Addr, Port, Type)
    local serverAddr=
    local serverPort=
    local serverType=
    local others=
    local line=`getHexinServerList |grep 56000001|head -n 1`
    [[ ! -z $line ]] && {
        read serverAddr serverPort serverType others <<<`echo $line`
        echo $serverAddr:$serverPort:$serverType
    } || {
        printf "autoTrade: %s\n" "there is no valid server!"
        return
    }

    #login qs client
    autoLoginQs

    #start cygwin32, and run getInstantData
    execGetInstantData $stockCode $tradeDate $serverAddr $serverPort $serverType

    realtimeAnalyze $stockCode $tradeDate
}
function realtimeAnalyze {
    local stockCode=$1
    local tradeDate=$2
    local noReset=$3

    local tradeDetailsDir="$dailyDir\\$stockCode\\$tradeDate\\tradeDetails"
    local pankouDir="$dailyDir\\$stockCode\\$tradeDate\\rtPankou"

    [[ -d "$root\\data\\raw\\$stockCode" ]] && {
        icacls "$dailyDir\\$stockCode" /grant $user:F
    }

    [[ -z $noReset ]] && {
        rm -rf $tradeDetailsDir $pankouDir
        mkdir -p $tradeDetailsDir $pankouDir
    }

    java -jar "$RealtimeJar" "$tradeDetailsDir" "$pankouDir" ""
}





function getReadableZuBiFile {
    local rawZuBiFile=$1                                   #in
    local outZuBiFile=$2                                   #in

    local tmp0=
    tmp0=`mktemp`
    tmp0=`getWindowPathOfTmpFile $tmp0`
    $JAVA -jar $topost0Jar $rawZuBiFile $tmp0 $outZuBiFile

    rm -rf $tmp0
}

