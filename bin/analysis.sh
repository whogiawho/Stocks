#!/bin/bash


#If 3 parms, startHMS is of format HHMMSS_HHMMSS
#if 4 parms, startHMS&endHMS is of format HHMMSS
function viewTradeDateRange {
    local stockCode=$1
    local tradeDate=$2
    local startHMS=$3
    local endHMS=$4

    [[ -z $startHMS ]] && {
        startHMS=${tradeDate#*:}
        tradeDate=${tradeDate%:*}
    }
    [[ -z $endHMS ]] && {
        endHMS=${startHMS#*_}
        startHMS=${startHMS%_*}
    }
    #convert startHMS to startHexTp
    local startHexTp=`convertTime2Hex $tradeDate $startHMS`
    #convert endHMS to endHexTp
    local endHexTp=`convertTime2Hex $tradeDate $endHMS`

    cscript.exe "$rootDir\\vbs\\viewTradeDateRange.vbs" $stockCode $tradeDate $startHexTp $endHexTp $startHMS $endHMS
}



function makeSTDAmPricePng {
    cscript.exe "$rootDir\\vbs\\makeSTDAmPricePng.vbs"
}


#a Png file is made at the dir - dirname(sAnalysis), with ${basename(sAnalysis)%.txt}.png
function makeSTDAmPricePngFromFile {
    local sAnalysis=$1

    cscript.exe "$rootDir\\vbs\\makeSTDAmPricePngFromFile.vbs" "$sAnalysis"
}
function makeSTDAmPricePngs {
    local stockCode=$1
    local nDays=$2

    mkdir -p $TMP/$stockCode/$nDays
    local i=
    for i in `getTradeDateList $stockCode y |tac`
    do
        local fOut=$TMP/$stockCode/$nDays/$i.txt
        local startDate=`java -jar $analyzetoolsJar prevtradedate $stockCode $i $nDays 2>/dev/null`
        [[ ! -z $startDate ]] && {
            local j=
            for j in `getTradeDateRange $stockCode $startDate $i`; 
            do 
                cat "$dailyDir/$stockCode/$j/analysis.txt"; 
            done >$fOut

            makeSTDAmPricePngFromFile  "`getWindowPathOfFile $fOut`"

            rm -rf $fOut
        }
    done
}



function makeQrAmPricePngs {
    local analysisTxtDir=$1

    cscript.exe "$rootDir\\vbs\\makeQrAmPricePngs.vbs" "$analysisTxtDir"
}
function makeQrPngs {
    local analysisTxtDir=$1

    cscript.exe "$rootDir\\vbs\\makeQrPngs.vbs" "$analysisTxtDir"
}
#analysisTxtDir - "d:\\Stocks\\data\\qr\\300_0.50_1"
function _makeQrPngs {
    local analysisTxtDir=$1

    local dir0=`basename $analysisTxtDir`
    dir0="$qrGraphDir\\$dir0"

    local i
    for i in `ls $analysisTxtDir`
    do
        i=`basename $i`
        local dir1="$dir0\\$i"
        mkdir -p "$dir1"

        local j
        for j in `find $analysisTxtDir/$i -type f`
        do
            cscript.exe "$rootDir\\vbs\\makeQrAmPricePng.vbs" "$j" "$dir1"
        done
    done
}

