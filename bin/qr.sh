#!/bin/bash

export qrDir=${qrDir:-"$dataRoot\\qr"}
export qrGraphDir=${qrGraphDir:-"$dataRoot\\qrGraph"}

#sQrFile - data/qr/300_0.50.txt
function makeAnalysisTxtFromQr {
    local stockCode=$1
    local sQrFile=$2
    local nDays=$3

    local sOutDir=`basename $sQrFile`
    sOutDir=${sOutDir%.*}
    sOutDir=${sOutDir}_${nDays}
    sOutDir=$TMP/$sOutDir

    local i=
    for i in `awk '{print $1}' $sQrFile`
    do
        local endDate=${i%_*}
        local endHMS=${i#*_}

        local startHMS=$endHMS
        local startDate=`java -jar $analyzetoolsJar prevtradedate $stockCode $endDate $nDays 2>/dev/null`

        [[ ! -z $startDate ]] && {
            mkdir -p $sOutDir/$endDate
            java -jar $analyzetoolsJar getanalysis $stockCode ${startDate}_${startHMS} ${endDate}_${endHMS} \
                >$sOutDir/$endDate/${i}_${nDays}.txt 2>/dev/null
            #echo $sOutDir/$endDate/${i}_${nDays}.txt ${startDate}_${startHMS} ${endDate}_${endHMS}
        }
    done

    echo $sOutDir
}

#analysisTxtDir - "d:\\Stocks\\data\\qr\\300_0.50_1"
function makeQrPng {
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
            cscript.exe "$rootDir\\bin\\makeQrPng.vbs" "$j" "$dir1"
        done
    done
}

function makeQrPngs {
    local analysisTxtDir=$1

    cscript.exe "$rootDir\\bin\\makeQrPngs.vbs" "$analysisTxtDir"
}


#qrMatchFile - data/qr/qrmaxmatch_900_0.50.txt
#qrgDir - 900_0.50_1
function getQrChar {
    local qrMatchFile=$1
    local qrgDir=$2
    local dstDir=$3

    mkdir -p $dstDir

    local nDays=${qrgDir##*_}
    local i
    for i in `cat $qrMatchFile |sed "s/ /\n/g"|grep ":"`; 
    do 
        local tradeDate=${i%:*}; 
        local hms=${i#*:}; 
        ln "$qrGraphDir\\$qrgDir\\$tradeDate\\${tradeDate}_${hms}_${nDays}.png" $dstDir; 
    done
}

#qrMatchFile - data/qr/qrmaxmatch_900_0.50.txt
#qrgDir - 900_0.50_1
function getQrGroupExtreme {
    local qrMatchFile=$1
    local qrgDir=$2

    local nDays=${qrgDir##*_}
    local i
    for i in `cat $qrMatchFile |sed "s/ /\n/g"|grep ":"`; 
    do 
        local tradeDate=${i%:*}; 
        local hms=${i#*:}; 
        local qrFile="$qrDir\\$qrgDir\\$tradeDate\\${tradeDate}_${hms}_${nDays}.txt"
        local min=`awk '{print $3}' $qrFile|sort -n|head -n1`
        local max=`awk '{print $3}' $qrFile|sort -n|tail -n1`
        local avg=`getListAvg $qrFile 3`
        local maxB=`substract $max $avg`
        local minB=`substract $min $avg`
        local delta=`substract $max $min`
        printf "%10s %10s %-10d %-10d %15.3f %15.3f %15.3f %15.3f\n" $tradeDate $hms $min $max $avg $minB $maxB $delta
    done
}

