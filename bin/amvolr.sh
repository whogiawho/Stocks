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

    [[ -z $interval ]] && interval=1
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

    [[ -z $interval ]] && interval=1
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

function amvolrCorrel {
    local stockCode=$1
    local tradeDate0=$2
    local hms0=$3
    local tradeDate1=$4
    local hms1=$5
    local bwsd=$6

    [[ -z $bwsd ]] && bwsd=1200

    local options="-b$bwsd"
    local correl=
    correl=`java -jar $analyzetoolsJar amvolrcorrel $options $stockCode $tradeDate0 $hms0 $tradeDate1 $hms1 2>/dev/null`

    echo $correl
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
function makeAmVolRStats {
    local fDelta=$1        #in
    local amvolrResDir=$2  #in
    local amvolrStats=$3   #out
    local options=$4       #in

    fDelta=`getWindowPathOfFile $fDelta`
    amvolrResDir=`getWindowPathOfFile $amvolrResDir`
    java -jar $analyzetoolsJar saadstats $options -f"$fDelta" -d"$amvolrResDir"  |tee $amvolrStats
}

function makeAmVolRm0m1m10Stats {
    local rawStats=$1      #in
    local amvolrResDir=$2  #in
    local tradeType=$3     #in

    [[ -z $tradeType ]] && tradeType=5

    amvolrResDir=`getWindowPathOfFile $amvolrResDir`
    local colStats colSS
    [[ $tradeType == $UP ]] && {
        colStats=7
        colSS=8
    } || {
        colStats=8
        colSS=9
    }

    local a b c d e f g h
    awk '$7>0.009&&$4>3' $rawStats |sort -nk$colStats,$colStats|while read a b c d e f g h; 
    do 
        minmaxm0=`java -jar $analyzetoolsJar saadstats -d"$amvolrResDir" -m0 -h0.90 $a,$b,$c 2>/dev/null| \
            sort -nk$colSS,$colSS|head -n1|awk "{print \\$colSS}" colSS=$colSS`; 
        minmaxm1=`java -jar $analyzetoolsJar saadstats -d"$amvolrResDir" -m1 -h0.90 $a,$b,$c 2>/dev/null| \
            sort -nk$colSS,$colSS|head -n1|awk "{print \\$colSS}" colSS=$colSS`; 
        printf "%s %s %s %4s %8s %8s %8s\n" $a $b $c $d "$g" $minmaxm0 $minmaxm1; 
    done
}
