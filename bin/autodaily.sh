#!/bin/bash

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

    setupCfgFile $stockCode $tradeDate 

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

    #start cygwin32, and run getInstantData
    execGetInstantData $stockCode $tradeDate $serverAddr $serverPort $serverType $sEnv

    #prepareFiles $stockCode $tradeDate

    startMacros
    realtimeAnalyze $stockCode $tradeDate
    closeMacros
}

