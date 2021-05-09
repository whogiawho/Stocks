#!/bin/bash

function makeAmVolRFromFile {
    local sInFile=$1
    local sOutPng=$2

    cscript.exe "$rootDir\\vbs\\makeAmDerivativePng.vbs" "$sInFile" "$sOutPng"
}

function makeTmpAmVolRPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4                         #optional
    local bwsd=$5                             #optional
    local bSaveTxt=$6                         #optional

    [[ -z $interval ]] && interval=60
    [[ -z $bwsd ]] && bwsd=1200

    local amvolrtxtDir="$dailyDir\\$stockCode\\$tradeDate\\amvolrTxt"
    [[ ! -e "$amvolrtxtDir" ]] && {
        mkdir -p "$amvolrtxtDir"
    }

    local amvolrTxt="$amvolrtxtDir\\${tradeDate}_${hms}_${bwsd}_amvolr.txt"
    makeAmVolRTxt $stockCode $tradeDate $hms $amvolrTxt $interval $bwsd 

    local sPngFile="$amvolrtxtDir\\${tradeDate}_${hms}_${bwsd}_amvolr.png"
    makeAmVolRFromFile "$amvolrTxt" "$sPngFile"

    [[ -z $bSaveTxt ]] && rm -rf $amvolrTxt
}
function makeAmVolRTxt {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local outTxt=$4
    local interval=$5                         #optional 
    local bwsd=$6                             #optional

    [[ -z $interval ]] && interval=60
    [[ -z $bwsd ]] && bwsd=1200

    java -jar $analyzetoolsJar listamvolr -b$bwsd -i${interval} $stockCode $tradeDate $hms \
        >"$outTxt" 2>/dev/null
}
function openTmpAmVolRPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional

    [[ -z $bwsd ]] && bwsd=1200

    local amvolrtxtDir="$dailyDir\\$stockCode\\$tradeDate\\amvolrTxt"
    JPEGView.exe "$amvolrtxtDir\\${tradeDate}_${hms}_${bwsd}_amvolr.png" &
}

