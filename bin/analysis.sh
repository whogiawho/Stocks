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



function makeContAmPricePngs {
    local stockCode=$1
    local tradeDate=$2

    [[ -z $tradeDate || -z $stockCode ]] && {
        echo "makeContAmPricePngs: invalid parms!"
        return
    }

    local ampricePngDir="$dailyDir/$stockCode/$tradeDate/ampricePng"
    mkdir -p $ampricePngDir
    local analysisTxt="$dailyDir/$stockCode/$tradeDate/analysis.txt"; 

    local count=`wc $analysisTxt|awk '{print $1}'`

    #replace it with parallel jobs
    local fTmp=`mktemp`
    local i=
    for i in `seq $count`
    do
        sed -n "1,${i}p" $analysisTxt >$fTmp
        local hms=`tail -n1 $fTmp|awk '{print $1}'`
        hms=`convertHex2Time $hms|awk '{print $2}'`
        hms=${hms//:/}
        local sHMSFile="$ampricePngDir\\$hms.txt"
        mv $fTmp $sHMSFile
        #run makeAmPricePng
        makeAmPricePng $sHMSFile $ampricePngDir

        rm -rf "$sHMSFile"
    done
    rm -rf $fTmp
}
function _makeContAmPricePngs {
    local analysisTxt=$1
    local ampricePngDir=$2
    local start=$3
    local end=$4

    local fTmp=`mktemp`
    local i=
    for i in `seq $start $end`
    do
        sed -n "1,${i}p" $analysisTxt >$fTmp
        local hms=`tail -n1 $fTmp|awk '{print $1}'`
        hms=`convertHex2Time $hms|awk '{print $2}'`
        hms=${hms//:/}
        local sHMSFile="$ampricePngDir\\$hms.txt"
        mv $fTmp $sHMSFile
        #run makeAmPricePng
        makeAmPricePng $sHMSFile $ampricePngDir

        rm -rf "$sHMSFile"
    done
    rm -rf $fTmp
}
function makeAmPricePng {
    local sAnalysisFile=$1
    local sPngDir=$2

    cscript.exe "$rootDir\\vbs\\makeAmPricePng.vbs" "$sAnalysisFile" "$sPngDir"
}
function makeAmPngs {
    local analysisTxtDir=$1

    cscript.exe "$rootDir\\vbs\\makeAmPngs.vbs" "$analysisTxtDir"
}
#analysisTxtDir - "d:\\Stocks\\data\\qr\\300_0.50_1"
function _makeAmPricePngs {
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
            cscript.exe "$rootDir\\vbs\\makeAmPricePng.vbs" "$j" "$dir1"
        done
    done
}

