#!/bin/bash

function prevAACkpt {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4               #optional

    local iOption=
    [[ ! -z $interval ]] && iOption="-i $interval"

    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -jar $analyzetoolsJar nextaackpt -b $iOption $stockCode $tradeDate $hms 2>/dev/null
}
function _nextAACkpt {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4               #optional

    [[ -z $interval ]] && interval=1

    local nextHMS=`sed -n "/$hms/ {n;p}" "$dataRoot\\aackpti$interval.txt"`
    [[ -z $nextHMS ]] && {
        nextHMS=`head -n1 "$dataRoot\\aackpti$interval.txt"`
        tradeDate=`getNextTradeDate $stockCode $tradeDate`
    }

    echo $tradeDate $nextHMS
}
function nextAACkpt {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4               #optional

    local iOption=
    [[ ! -z $interval ]] && iOption="-i $interval"

    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -jar $analyzetoolsJar nextaackpt $iOption $stockCode $tradeDate $hms 2>/dev/null
}
function trackAACkpt {
    local stockCode=$1
    local tradeDate=$2
    local ckptInterval=$3                     #optional
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional 

    [[ -z $ckptInterval ]] && ckptInterval=60
    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    local avgamDir="$dailyDir\\$stockCode\\$tradeDate\\avgam"
    [[ ! -e $avgamDir ]] && mkdir -p $avgamDir
    local avgamPngDir="$dailyDir\\$stockCode\\$tradeDate\\avgamPng"
    [[ ! -e $avgamPngDir ]] && mkdir -p $avgamPngDir

    #make sure analysisTxt exists
    local analysisTxt="$dailyDir\\$stockCode\\$tradeDate\\analysis.txt"
    while [[ 1 ]]
    do
        [[ ! -f $analysisTxt ]] && { sleep 1; continue; } || break;
    done

    local startAACkpt=092500
    local prevAACkpt0=`prevAACkpt $stockCode $tradeDate $startAACkpt $ckptInterval`
    local currentAACkpt="$tradeDate $startAACkpt"
    local currentHMS=`echo $currentAACkpt|awk '{print $2}'`
    local aackptTp=`convertTime2Hex $currentAACkpt`
    while [[ 1 ]]
    do
        local line=`tail -n1 $analysisTxt`
        local currentTp=`echo $line|awk '{print $1}'`
        [[ ! -z $currentTp && 0x$currentTp -ge 0x$aackptTp ]] && {
            #local time0=`currentHexTime`

            local outTxt="$avgamDir\\$currentHMS.txt"
            #makeAvgAmTxt for aackptTp
            makeAvgAmTxt $stockCode $currentAACkpt $outTxt $bwsd $minDist $interval
            #make png
            local sPngFile="$avgamPngDir\\${currentHMS}.png"
            (cscript.exe "$rootDir\\vbs\\makeAmDerivativePng.vbs" "$outTxt" "$sPngFile" 2>/dev/null|grep -v Micro &)

            #get correl with prev aaCkpt
            local correl0=`avgamCorrel $stockCode $currentAACkpt $prevAACkpt0`
            correl0=`colorCorrel "$correl0" 0.75`
            [[ $? == 0 ]] && { 
                (beep 100 &)
            }
            local upPrice=`_getUpPrice $stockCode $currentAACkpt`
            printf "%s %8s %8s\n" "$currentAACkpt" "$correl0" "$upPrice"

            #set prev aaCkpt
            prevAACkpt0="$currentAACkpt"
            #set next aaCkpt
            currentAACkpt=`_nextAACkpt $stockCode $currentAACkpt $ckptInterval`
            echo $currentAACkpt|grep "150000" && break;
            currentHMS=`echo $currentAACkpt|awk '{print $2}'`
            aackptTp=`convertTime2Hex $currentAACkpt`
            :

            #local time1=`currentHexTime`
            #echo "time0=$time0 time1=$time1 delta=$((0x$time1-0x$time0))"
        } || {
            #sleep 1
            :
        }
    done
}

