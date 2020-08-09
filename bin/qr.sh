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
            getAnalysis $stockCode ${startDate} ${startHMS} ${endDate} ${endHMS} \
                >$sOutDir/$endDate/${i}_${nDays}.txt 2>/dev/null
            #echo $sOutDir/$endDate/${i}_${nDays}.txt ${startDate}_${startHMS} ${endDate}_${endHMS}
        }
    done

    echo $sOutDir
}



#qrMatchFile - data/qr/qrmaxmatch_900_0.50.txt
#qrgDir - 900_0.50_1
function groupQrPngs {
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
function groupQrs {
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
        ln "$qrDir\\$qrgDir\\$tradeDate\\${tradeDate}_${hms}_${nDays}.txt" $dstDir; 
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
        printf "%10s %10s %-10d %-10d %15.3f %15.3f %15.3f %15.3f\n" \
            $tradeDate $hms $min $max $avg $minB $maxB $delta
    done
}


#qrEndYMDHMS - 20090722,142400
function convertSdTime2YMDHMS {
    local stockCode=$1
    local qrEndYMDHMS=$2
    local startSd=$3
    local endSd=$4

    local qrEndDate=${qrEndYMDHMS%,*}
    local qrEndHMS=${qrEndYMDHMS#*,}
    local startYMDHMS=`getPrevTradeDate $stockCode $qrEndDate`,$qrEndHMS

    local startHexTp=`java -jar $analyzetoolsJar rgetabs $stockCode $startSd $startYMDHMS 2>/dev/null`
    local endHexTp=`java -jar $analyzetoolsJar rgetabs $stockCode $endSd $startYMDHMS 2>/dev/null`

    convertHex2Time $startHexTp y
    convertHex2Time $endHexTp y
}








