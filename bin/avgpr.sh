#!/bin/bash

function makeAvgPrFromFile {
    local sInFile=$1
    local sOutPng=$2

    cscript.exe "$rootDir\\vbs\\makeAmDerivativePng.vbs" "$sInFile" "$sOutPng"
}

function makeTmpAvgPrPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional
    local bSaveTxt=$7                         #optional

    [[ -z $bwsd ]] && bwsd=1200
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    local avgprtxtDir="$dailyDir\\$stockCode\\$tradeDate\\avgprTxt"
    [[ ! -e "$avgprtxtDir" ]] && {
        mkdir -p "$avgprtxtDir"
    }

    local avgprName=${hms}_${bwsd}_${minDist}_${interval}
    local avgprTxt="$avgprtxtDir\\$avgprName.txt"
    makeAvgPrTxt $stockCode $tradeDate $hms $avgprTxt $bwsd $minDist $interval

    local sPngFile="$avgprtxtDir\\$avgprName.png"
    makeAvgPrFromFile "$avgprTxt" "$sPngFile"

    [[ -z $bSaveTxt ]] && rm -rf $avgprTxt
}
function makeAvgPrTxt {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local outTxt=$4
    local bwsd=$5                             #optional
    local minDist=$6                          #optional
    local interval=$7                         #optional 

    [[ -z $bwsd ]] && bwsd=1200
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    java -jar $analyzetoolsJar listavgpr -b$bwsd -m$minDist -i$interval $stockCode $tradeDate $hms \
        >"$outTxt" 2>/dev/null
}
function openTmpAvgPrPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional

    [[ -z $bwsd ]] && bwsd=1200
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    local avgprName=${hms}_${bwsd}_${minDist}_${interval}
    local avgprtxtDir="$dailyDir\\$stockCode\\$tradeDate\\avgprTxt"
    local avgprPng="$avgprtxtDir\\$avgprName.png"
    JPEGView.exe "$avgprPng" &
}


function makeAvgPrPngs {
    local stockCode=$1
    local tradeDate=$2

    local avgprDir="$dailyDir\\$stockCode\\$tradeDate\\avgpr"

    cscript.exe "$rootDir\\vbs\\makeAmDerivativePngs.vbs" "$avgprDir"
}


function avgprCorrel {
    local stockCode=$1
    local tradeDate0=$2
    local hms0=$3
    local tradeDate1=$4
    local hms1=$5
    local options=$6

    local correl=
    correl=`java -jar $analyzetoolsJar avgprcorrel $options $stockCode $tradeDate0 $hms0 $tradeDate1 $hms1 2>/dev/null`

    echo $correl
}
function avgprPredict {
    local stockCode=$1
    local tradeDate=$2
    local bwsd=$3
    local threshold=$4

    [[ -z $threshold ]] && threshold=0.9

    local fTmp=`mktemp`
    local hms=150000; 
    local startDate=`rgetYMDHMS $stockCode $bwsd|awk '{print $1}'`
    startDate=`getNextTradeDate $stockCode $startDate`
    local i=
    for i in `getTradeDateRange $stockCode $startDate $tradeDate`
    do
        local correl=`avgprCorrel $stockCode $tradeDate $hms $i $hms "-b$bwsd"`
        echo $i $hms $correl
    done | tee $fTmp

    _avgprPredict $stockCode $tradeDate $fTmp $threshold

    mv $fTmp $TMP/appb$bwsd.txt
}
function _avgprPredict {
    local stockCode=$1
    local tradeDate=$2
    local sInFile=$3
    local threshold=$4

    [[ -z $threshold ]] && threshold=0.9

    local a b c
    awk '$3>threshold' threshold=$threshold $sInFile |grep -v NaN|grep -v $tradeDate|while read a b c; 
    do 
        local line=`getLSProfit $stockCode $a $b -m1`; 
        local nd=`getNextTradeDate $stockCode $a`; 
        local cp=`getCloseQuotationPrice $stockCode $a`; 
        local op=`getOpenQuotationPrice $stockCode $nd`; 
        printf "%s %s %s %8s %8s %8s\n"  $a $b "$line" $cp $op `substract $op $cp`; 
    done|sort -nk7,7
}


function classifyAvgPr {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local fAvgPr=$4
    local thres=$5
    local options=$6


    local fTmp=`mktemp`
    local distThres=60
    local prevSd=0
    awk "\$4>$thres" $fAvgPr |sort -k2,3 | while read a b c d; 
    do 
        local sd=`getAbs $a $b $c`
        [[ $((sd-prevSd)) -ge $distThres ]] && {
            echo $a $b $c $d
        } 
        prevSd=$sd
    done | tee $fTmp

    local exHMS=2400
    cat $fTmp | while read a b c d;
    do
        local line=`getLSProfit $a $b $c "$options"`; 
        local correl=`avgprCorrel $stockCode $tradeDate $hms $b $c "-b$exHMS"`; 
        printf "%s %s %s %8s %8s %8s %8s\n" $a $b $c $line $d $correl; 
    done

    rm -rf $fTmp
}

function makeAvgPrFromDelta {
    local fDelta=$1        #in
    local avgprDir=$2      #out

    [[ ! -e $avgprDir ]] && mkdir $avgprDir

    local a b c d
    while read a b c d
    do
        [[ ! -e $avgprDir/$b.$c.txt ]] && {
            java -jar $analyzetoolsJar listavgpr $a $b $c 2>/dev/null >$avgprDir/$b.$c.txt
        }
    done <$fDelta
}

#$avgprDir.res - out
function makeAvgPrRes {
    local fDelta=$1        #in
    local avgprDir=$2      #in

    fDelta=`getWindowPathOfFile $fDelta`
    avgprDir=`getWindowPathOfFile $avgprDir`
    java -jar $analyzetoolsJar simavgprdelta -f"$fDelta" -d"$avgprDir" 2>/dev/null
}


function avgprDeltaShrink {
    local fDelta=$1        #in&out
    local avgprDir=$2      #in

    local a b c d e f
    while read a b c d e f
    do
        local cnt=`sort -uk2,2 $avgprDir/$b.$c.txt|wc|awk '{print $1}'`
        [[ $cnt -gt 1 ]] && printf "%s %s %s %8s %8s %8s\n" $a $b $c $d $e $f
    done <$fDelta
}
