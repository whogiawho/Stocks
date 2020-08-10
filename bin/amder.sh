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


function makeAmDerivativePngs {
    local stockCode=$1
    local tradeDate=$2

    local derivativeDir="$dailyDir\\$stockCode\\$tradeDate\\derivative"

    cscript.exe "$rootDir\\vbs\\makeAmDerivativePngs.vbs" "$derivativeDir"
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

        local ssd=$((i-bwsd))
        local stp=`rgetAbs $stockCode $ssd`
        local str=`convertHex2Time $stp y`
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
