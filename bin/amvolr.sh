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
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional
    local bSaveTxt=$7                         #optional

    [[ -z $bwsd ]] && bwsd=1200
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    local amvolrtxtDir="$dailyDir\\$stockCode\\$tradeDate\\amvolrTxt"
    [[ ! -e "$amvolrtxtDir" ]] && {
        mkdir -p "$amvolrtxtDir"
    }

    local amvolrName=${hms}_${bwsd}_${minDist}_${interval}
    local amvolrTxt="$amvolrtxtDir\\$amvolrName.txt"
    makeAmVolRTxt $stockCode $tradeDate $hms $amvolrTxt $bwsd $minDist $interval 

    local sPngFile="$amvolrtxtDir\\$amvolrName.png"
    makeAmVolRFromFile "$amvolrTxt" "$sPngFile"

    [[ -z $bSaveTxt ]] && rm -rf $amvolrTxt
}
function makeAmVolRTxt {
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

    java -jar $analyzetoolsJar listamvolr -b$bwsd -m$minDist -i$interval $stockCode $tradeDate $hms \
        >"$outTxt" 2>/dev/null
}
function openTmpAmVolRPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional

    [[ -z $bwsd ]] && bwsd=1200
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    local amvolrName=${hms}_${bwsd}_${minDist}_${interval}
    local amvolrtxtDir="$dailyDir\\$stockCode\\$tradeDate\\amvolrTxt"
    local amvolrPng="$amvolrtxtDir\\$amvolrName.png"
    JPEGView.exe "$amvolrPng" &
}
function amvolrCorrel {
    local stockCode=$1
    local tradeDate0=$2
    local hms0=$3
    local tradeDate1=$4
    local hms1=$5
    local options=$6

    local correl=
    correl=`java -jar $analyzetoolsJar amvolrcorrel $options $stockCode $tradeDate0 $hms0 $tradeDate1 $hms1 2>/dev/null`

    echo $correl
}


function getMaxCorrel {
    local stockCode=$1
    local tradeDate0=$2
    local hms0=$3
    local tradeDate1=$4
    local hms1=$5

    local fTmp=`mktemp`
    local i=
    local correl=
    for i in `seq 10 10 10800`; 
    do 
        correl=`java -jar $analyzetoolsJar amvolrcorrel -b$i $stockCode $tradeDate0 $hms0 $tradeDate1 $hms1 2>/dev/null` 
        printf "%8s %8s\n" $i $correl; 
    done > $fTmp

    local line=`cat $fTmp|grep -v NaN|sort -nk2,2|tail -n1`
    printf "%s %s %s %s %s\n" $tradeDate0 $hms0 $tradeDate1 $hms1 "$line"

    rm -rf $fTmp
}


function addAmVolR {
    local fList=$1
    local sDir=$2


}



function _makeAmVolRRes {
    local fDelta=$1        #in
    local amvolrDir=$2     #in

    fDelta=`getWindowPathOfFile $fDelta`
    amvolrDir=`getWindowPathOfFile $amvolrDir`
    local a b c d
    local max=10
    local cnt=0
    local i=
    while read a b c d
    do
        JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -jar $analyzetoolsJar simamvolrdelta \
            -f"$fDelta" -d"$amvolrDir" $a,$b,$c \
            >"$amvolrDir.res\\$b.$c.correl" 2>/dev/null &
        cnt=$((cnt+1))
        [[ $cnt -ge $max ]] && {
            wait
            cnt=0
        }
    done <$fDelta
}

function makeAmVolRFromDelta {
    local stockCode=$1     #in
    local fDelta=$2        #in
    local amvolrDir=$3     #out
    local options=$4       #in

    [[ ! -e $amvolrDir ]] && mkdir $amvolrDir

    fDelta=`getWindowPathOfFile $fDelta`
    amvolrDir=`getWindowPathOfFile $amvolrDir`
    java -jar $analyzetoolsJar listamvolr $options -f"$fDelta" -d"$amvolrDir" $stockCode 
}
#$amvolrDir.res - out
function makeAmVolRRes {
    local fDelta=$1        #in
    local amvolrDir=$2     #in

    fDelta=`getWindowPathOfFile $fDelta`
    amvolrDir=`getWindowPathOfFile $amvolrDir`
    java -jar $analyzetoolsJar simamvolrdelta -f"$fDelta" -d"$amvolrDir" 2>/dev/null
}

