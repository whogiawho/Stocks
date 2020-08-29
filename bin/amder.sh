#!/bin/bash

function makeAvi {
    local stockCode=$1
    local tradeDate=$2

    local targetDir="$dataRoot\\amderAvi"
    [[ ! -e $targetDir ]] && {
        mkdir -p $targetDir
    }

    local curDir=$PWD
    local pngDir="$dailyDir\\$stockCode\\$tradeDate\\mergedPng"
    cd $pngDir
    mencoder mf://*.png -mf w=480:h=289:fps=1:type=png -ovc copy -oac copy -o "$targetDir\\$stockCode.$tradeDate.avi"
    cd $curDir
}
#merge pngs from amdertxtPng and derivativePng
function mergePng {
    local stockCode=$1
    local tradeDate=$2

    local dir0="$dailyDir\\$stockCode\\$tradeDate\\amdertxtPng"
    local dir1="$dailyDir\\$stockCode\\$tradeDate\\derivativePng"
    local outDir="$dailyDir\\$stockCode\\$tradeDate\\mergedPng"

    [[ ! -e $outDir ]] && {
        mkdir -p $outDir
    }

    local i=
    for i in `ls $dir0`
    do
        local hms=${i%.png}
        convert "$dir0\\$hms.png" "$dir1\\$hms.png" -append "$outDir\\$hms.png"
    done
}


function amderSearchHMS {
    local stockCode=$1
    local startSd=$2
    local endSd=$3
    local step=$4
    local naThreshold=$5
    local bwsd=$6
    local r2Threshold=$7


    [[ -z $startSd ]] && startSd=$((14405*2))
    [[ -z $endSd ]] && endSd=40319594
    [[ -z $step ]] && step=60
    [[ -z $naThreshold ]] && naThreshold=0.90
    [[ -z $bwsd ]] && bwsd=$((14405*2))
    [[ -z $r2Threshold ]] && r2Threshold=0.5

    local amDerTxt=`mktemp`
    local i=
    for i in `seq $startSd $step $endSd`
    do
        local sTp=`rgetAbs $stockCode $i`
        local str=`convertHex2Time $sTp y`
        local tradeDate=`echo $str|awk -F, '{print $1}'`
        local hms=`echo $str|awk -F, '{print $2}'`

        java -jar $analyzetoolsJar listamderivatives -b$bwsd -h$r2Threshold $stockCode $tradeDate $hms >"$amDerTxt" 2>/dev/null
        local sum=`wc $amDerTxt|awk '{print $1}'`
        local naCnt=`awk "\\$1<0.5{print \\$0}" $amDerTxt|wc|awk '{print $1}'`
        local rate=`divide $naCnt $sum`

        local bCmp=`ge $rate $naThreshold`
        [[ $bCmp == 1 ]] && {
            printf "%10d %8s %8s\n" $i $tradeDate $hms
        } || {
            printf "%10d\n" $i
        }
    done
}


function makeAmDerivativePngs {
    local stockCode=$1
    local tradeDate=$2

    local derivativeDir="$dailyDir\\$stockCode\\$tradeDate\\derivative"

    cscript.exe "$rootDir\\vbs\\makeAmDerivativePngs.vbs" "$derivativeDir"
}
function makeAmDerivativePng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4
    local r2Threshold=$5

    [[ -z $bwsd ]] && bwsd=300
    [[ -z $r2Threshold ]] && r2Threshold=0.5

    local amderDir="$dailyDir\\$stockCode\\$tradeDate\\amderTxt"
    [[ ! -e "$amderDir" ]] && {
        mkdir -p "$amderDir"
    }

    amderGetAnalysis $stockCode $tradeDate $hms $bwsd

    local amDerTxt="$amderDir\\${hms}_${bwsd}_amder.txt"
    java -jar $analyzetoolsJar listamderivatives -b$bwsd -h$r2Threshold $stockCode $tradeDate $hms >"$amDerTxt"

    local sPngFile="$amderDir\\${hms}_${bwsd}_amder.png"
    cscript.exe "$rootDir\\vbs\\makeAmDerivativePng.vbs" "$amDerTxt" "$sPngFile"
}
function amderGetAnalysis {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4

    local amderDir="$dailyDir\\$stockCode\\$tradeDate\\amderTxt"
    [[ ! -e "$amderDir" ]] && {
        mkdir -p "$amderDir"
    }

    local hmsSd=`getAbs $stockCode $tradeDate $hms`
    local sSd=$((hmsSd-bwsd))
    local sTp=`rgetAbs $stockCode $sSd`
    local str=`convertHex2Time $sTp y`
    local sDate=`echo $str|awk -F, '{print $1}'`
    local sHMS=`echo $str|awk -F, '{print $2}'`
    local hmsTxt="$amderDir\\${hms}_${bwsd}_analysis.txt"
    getAnalysis $stockCode ${sDate} ${sHMS} ${tradeDate} ${hms} >"$hmsTxt"
}



function makeAmDerTxtPngs {
    local stockCode=$1
    local tradeDate=$2

    local amderTxtDir="$dailyDir\\$stockCode\\$tradeDate\\amderTxt"

    cscript.exe "$rootDir\\vbs\\makeAmDerTxtPngs.vbs" "$amderTxtDir"
}
function makeAmDerAnalysis {
    local stockCode=$1
    local tradeDate=$2
    local bwsd=$3

    local amderDir="$dailyDir\\$stockCode\\$tradeDate\\amderTxt"
    [[ ! -e "$amderDir" ]] && {
        mkdir -p "$amderDir"
    }

    #local startTp=`convertTime2Hex $tradeDate $CallAuctionEndTime`
    #local endTp=`convertTime2Hex $tradeDate $CloseQuotationTime`
    local startSd=`getAbs $stockCode $tradeDate $CallAuctionEndTime`
    local endSd=`getAbs $stockCode $tradeDate $CloseQuotationTime`
    local i=
    for i in `seq $startSd $endSd`
    do
        local tp=`rgetAbs $stockCode $i`
        local hms=`convertHex2Time $tp y|awk -F, '{print $2}'`

        local sSd=$((i-bwsd))
        local sTp=`rgetAbs $stockCode $sSd`
        local str=`convertHex2Time $sTp y`
        local sDate=`echo $str|awk -F, '{print $1}'`
        local sHMS=`echo $str|awk -F, '{print $2}'`

        getAnalysis $stockCode ${sDate} ${sHMS} ${tradeDate} ${hms} >"$amderDir\\$hms.txt"
    done
}


function tpAmDerStats {
    local fHMS=$1

    local i=0
    local start=-1
    local end=-1
    local slope=-1
    local line=
    while read line; do
        local a b
        read a b<<<`echo $line`; 
        local bCmp=`ge $a 0.9`; 
        [[ $start != -1 ]] && { 
            [[ $bCmp == 1 ]] && end=$((end+1)) || { 
                echo $fHMS,$start,$end,$slope; 
                start=-1; 
                end=-1; 
            } 
        } || { 
            [[ $bCmp == 1 ]] && { 
                start=$i; 
                end=$i; 
                slope=$b; 
            } 
        }; 
        i=$((i+1)); 
    done < $fHMS
    [[ $start != -1 ]] && {
        echo $fHMS,$start,$end,$slope; 
    }
}
function amDerStats {
    local stockCode=$1
    local tradeDate=$2

    local max=5
    local cnt=0
    local j=
    for j in $rootDirCygdrive/data/daily/$stockCode/$tradeDate/derivative/*
    do 
        #echo "tpAmDerStats $j"
        tpAmDerStats $j &
        cnt=$((cnt+1))
        echo cnt=$cnt
        [[ $cnt -ge $max ]] && {
            wait -n
            cnt=$((cnt-1))
        }
    done
}
