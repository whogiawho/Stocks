#!/bin/bash


function getAnalysis {
    local stockCode=$1
    local sDate=$2
    local sHMS=$3
    local eDate=$4
    local eHMS=$5
    local interval=$6                        #optional

    [[ -z $interval ]] && interval=1
    java -jar $analyzetoolsJar getanalysis -i$interval $stockCode ${sDate}_${sHMS} ${eDate}_${eHMS} 2>/dev/null
}

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
    local startDate=$2
    local endDate=$3
    local nDays=$4

    mkdir -p $TMP/$stockCode/$nDays
    local i=
    for i in `getTradeDateRange $stockCode $startDate $endDate`
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



function _makeAmPricePng {
    local sAnalysisFile=$1
    local sPngDir=$2

    cscript.exe "$rootDir\\vbs\\makeAmPricePngFromFile.vbs" "$sAnalysisFile" "$sPngDir"
}



#always start from 09:25:00
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
    local start=1
    while [[ $start -lt $count ]]
    do
        local end=$((start+1999))
        [[ $end -gt $count ]] && end=$count
        echo "_makeContAmPricePngs $analysisTxt $ampricePngDir $start $end"
        _makeContAmPricePngs $analysisTxt $ampricePngDir $start $end &

        start=$((start+2000))
    done
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
        #run _makeAmPricePng
        _makeAmPricePng $sHMSFile $ampricePngDir

        rm -rf "$sHMSFile"
    done
    rm -rf $fTmp
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
            cscript.exe "$rootDir\\vbs\\makeAmPricePngFromFile.vbs" "$j" "$dir1"
        done
    done
}


function getAm {
    local stockCode=$1
    local tradeDate=$2
    local hmsList=$3
    local endHMS=$4

    [[ ! -z $endHMS ]] && {
        hmsList=${hmsList}_$endHMS
    }

   java -jar $analyzetoolsJar getam $stockCode $tradeDate $hmsList 2>/dev/null
}
