#!/bin/bash


#depend on settings.txt
function splitRawTradeDetails {
    local stockCode=$1
    local tradeDate=$2

    local tradeDetailsFile="$rawZuBiDataDir\\$stockCode\\$stockCode.$tradeDate.txt"
    local emuTradeDetailsDir="$dailyDir\\$stockCode\\$tradeDate\\emuTradeDetails"
    rm -rf "$emuTradeDetailsDir"
    [[ ! -d "$emuTradeDetailsDir" ]] && {
        mkdir -p "$emuTradeDetailsDir"
    }
    java -jar "$splitRawTradeDetailsJar" "$tradeDetailsFile" "$emuTradeDetailsDir" 2>/dev/null
}

function appendLastRawTradeDetail {
    local stockCode=$1
    local tradeDate=$2

    local tradeDetailsFile="$rawZuBiDataDir\\$stockCode\\$stockCode.$tradeDate.txt"

    local encTradeTime=`convertTime2Hex $tradeDate $LastRawTradeDetailTime|tr a-z A-Z`
    local tradePrice=`tail -n 1 $tradeDetailsFile|awk '{print $2}'`
    local tradeNO=0
    local tradeType=1

    grep -q $encTradeTime $tradeDetailsFile || {
        echo -e "$encTradeTime $tradePrice $tradeNO $tradeType\r" >>$tradeDetailsFile
    }
}

#1. must end with 14:59:xx||15:0x:xx
#2. must begin with 09:2x:xx
function checkRawTradeDetails {
    local stockCode=$1
    local tradeDate=$2

    local rawZuBiFile="$rawZuBiDataDir\\$stockCode\\$stockCode.$tradeDate.txt"

    local lastTradeTime=
    lastTradeTime=`tail -n 1 $rawZuBiFile|awk '{print $1}'`
    lastTradeTime=`convertHex2Time $lastTradeTime|awk '{print $2}'`
    lastTradeTime=${lastTradeTime:0:5}
    [[ $lastTradeTime =~ "14:59" || $lastTradeTime =~ "15:0" ]] && {
        #now check firstTradeTime, must start with timeIdx=0
        local firstTradeTime=
        firstTradeTime=`head -n 1 $rawZuBiFile|awk '{print $1}'`
        firstTradeTime=`convertHex2Time $firstTradeTime|awk '{print $2}'`
        firstTradeTime=${firstTradeTime:0:4}
        [[ $firstTradeTime =~ "09:2" ]] && {
            return 0
        } || {
            return 1
        }
    } || {
        return 1
    }
}




