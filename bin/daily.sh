#!/bin/bash

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

    routinesAfterCloseQuotation $stockCode $tradeDate skipRA
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
    local skipRealtimeAnalyze=$3

    setupCfgFile $stockCode $tradeDate

    _realtimeAnalyze $stockCode $tradeDate y $skipRealtimeAnalyze

    #generate analysis.txt
    java -jar $analyzetoolsJar makeanalysistxt $stockCode $tradeDate

    makeSTDAmPricePng

    write2CheckAllTable $stockCode $tradeDate
}
#write today's checkAllSSTable output to fCheckAllTable
function write2CheckAllTable {
    local stockCode=$1
    local tradeDate=$2

    echo $tradeDate |tee -a $fCheckAllTable
    checkAllSSTable $stockCode $tradeDate |tee -a $fCheckAllTable
}

#convert rawPankou&rawTradeDetails, generate copylist and run realtimeAnalyze
function _realtimeAnalyze {
    local stockCode=$1
    local tradeDate=$2
    local bAppendLast=$3
    local skipRealtimeAnalyze=$4

    setupCfgFile $stockCode $tradeDate
    prepareRetrospective $stockCode $tradeDate $bAppendLast

    [[ -z $skipRealtimeAnalyze ]] && {
        realtimeAnalyze $stockCode $tradeDate &
        #wait until loading analysis.txt completes
        sleep $SLEEP_INTERVAL 
        $rootDirCygdrive/bin/autoImitate.bat
        #wait so that realtimeAnalyze can complete processing
        sleep $SLEEP_INTERVAL 

        killall java
        #only continue if java is killed
        checkProcKilled java
    }
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
function autoQuitQs {
    python -u $rootDir\\python\\killXiadan.py
}
function autoTrade {
    local stockCode=$1
    local tradeDate=$2

    #getLoginParms
    autoGetLoginParms
    local line=`checkHexinServer`
    [[ $? != 0 ]] && return
    local serverAddr=
    local serverPort=
    local serverType=
    IFS=: read serverAddr serverPort serverType <<<`echo $line`

    sleep $((SLEEP_INTERVAL/4))
    #login qs client
    autoLoginQs


    setupCfgFile $stockCode $tradeDate 

    #start cygwin32, and run getInstantData
    execGetInstantData $stockCode $tradeDate $serverAddr $serverPort $serverType $sEnv

    #prepareFiles $stockCode $tradeDate

    startMacros
    realtimeAnalyze $stockCode $tradeDate
    closeMacros
}


function execDailyGetJob {
    local tradeDate=$1
    local serverAddr=$2
    local serverPort=$3
    local serverType=$4
    local sEnv=$5

    local runGetInstantData=N
    local runDailyGetJob=$tradeDate:$serverAddr:$serverPort:$serverType

    export HOME=$cygwin32RootCygdrive/home/$user;      #32bit cygwin HOME
    local pathb=$PATH                                  #save PATH
    resetPATH; 
    wscript.exe "$cygwin32RootDir\invisible.vbs" "$cygwin32RootDir\_mintty.bat" $sEnv $runGetInstantData $runDailyGetJob; 
    export HOME=/home/$user;                           #restore HOME to 64bit cygwin
    export PATH=$pathb                                 #restore PATH
}
function execGetInstantData {
    local stockCode=$1
    local tradeDate=$2
    local serverAddr=$3
    local serverPort=$4
    local serverType=$5
    local sEnv=$6

    local runGetInstantData=$stockCode:$tradeDate:$serverAddr:$serverPort:$serverType

    export HOME=$cygwin32RootCygdrive/home/$user;      #32bit cygwin HOME
    local pathb=$PATH                                  #save PATH
    resetPATH; 
    wscript.exe "$cygwin32RootDir\invisible.vbs" "$cygwin32RootDir\_mintty.bat" $sEnv $runGetInstantData; 
    export HOME=/home/$user;                           #restore HOME to 64bit cygwin
    export PATH=$pathb                                 #restore PATH
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


function autoSubmitAbss {
    local stockCode=$1
    local tradeDate=$2
    local bNotRunAutoGetLoginParms=$3

    [[ -z $bNotRunAutoGetLoginParms ]] && {
        #getLoginParms
        autoGetLoginParms
    }

    autoLoginQs

    setupCfgFile $stockCode $tradeDate
    java -jar $analyzetoolsJar submitabs

    #quit
    autoQuitQs
}
function autoCheckAbss {
    local stockCode=$1
    local tradeDate=$2

    autoLoginQs

    setupCfgFile $stockCode $tradeDate
    java -jar $analyzetoolsJar checkabss

    #quit
    autoQuitQs
}
function autoDailyGetJob {
    local tradeDate=$1
    local bRunAutoGetLoginParms=$2

    [[ ! -z $bRunAutoGetLoginParms ]] && {
        #getLoginParms
        autoGetLoginParms
    }

    local line=`checkHexinServer`
    [[ $? != 0 ]] && return
    local serverAddr=
    local serverPort=
    local serverType=
    IFS=: read serverAddr serverPort serverType <<<`echo $line`
    echo serverAddr=$serverAddr serverPort=$serverPort serverType=$serverType IFS=$IFS

    execDailyGetJob $tradeDate $serverAddr $serverPort $serverType $sEnv
}





function prepareFiles {
    local currentDir="$dailyDir\\$stockCode\\$tradeDate"
    mkdir -p "$currentDir"
    #cp doc/instantWatch.xlsm 
    cp "$rootDir\\doc\\instantWatch.xlsm" "$currentDir"
    #cp prevTradeDate/analysis.txt
    local prevTradeDate=`getPrevTradeDate $stockCode $tradeDate`
    cp "$rootDir\\$stockCode\\$prevTradeDate\\analysis.txt" "$currentDir"
}
