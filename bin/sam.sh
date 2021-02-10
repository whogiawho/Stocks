#!/bin/bash

function makeNegTable {
    local fTxt=$1

    local a b c d
    awk '$15<1.5' $fTxt|while read a b c d; 
    do
        c=${c%:}
        echo "        list.add(new SAm(\"$a\", \"$b\", \"$c\"));"
    done 
}

function makePosTable {
    local fTxt=$1

    local a b c d
    awk '$15>=1.5' $fTxt|while read a b c d; 
    do
        c=${c%:}
        echo "        list.add(new SAm(\"$a\", \"$b\", \"$c\"));"
    done 
}

function getSubTxt {
    local fTxt=$1
    local start=$2
    local end=$3

    sort -nk4,4 $fTxt |awk "\$4>=$start&&\$4<$end"|sort -nk15,15
}

function makeSAm2Table {
    local fTxt=$1

    local fTmp=`mktemp`


    for i in 0_100 100_270 270_360 360_450 450_620 620_1300 
    do
        local start end
        start=${i%_*}
        end=${i#*_}
        getSubTxt $fTxt $start $end >$fTmp
        printf "negTable %8s %8s:\n" $start $end
        makeNegTable $fTmp
        printf "posTable %8s %8s:\n" $start $end
        makePosTable $fTmp
    done

    rm -rf $fTmp
}

function samtemplate {
    local targetSAmNO=$1

    local targetSAm=sam$targetSAmNO
    local analyzeDir="java/com/westsword/stocks/analyze"
    local modelSAmDir="$analyzeDir/sam4"
    local targetSAmDir="$analyzeDir/$targetSAm"

    local fTargetSAm=$targetSAmDir/SAm$targetSAmNO.java
    local fTargetSAmTask=$targetSAmDir/SAm${targetSAmNO}Task.java
    local fTargetSAmManager=$targetSAmDir/SAm${targetSAmNO}Manager.java

    cp -rf $modelSAmDir $targetSAmDir
    mv $targetSAmDir/SAm4.java $fTargetSAm
    mv $targetSAmDir/SAm4Task.java $fTargetSAmTask
    mv $targetSAmDir/SAm4Manager.java $fTargetSAmManager

    sed -i "s/SAm4/SAm$targetSAmNO/g" $fTargetSAm $fTargetSAmTask $fTargetSAmManager
    sed -i "s/sam4/sam$targetSAmNO/g" $fTargetSAm $fTargetSAmTask $fTargetSAmManager
}

function listSegment {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3

    local sAmders="$dataRoot/daily/$stockCode/$tradeDate/derivative/$hms.txt"
    local amders line
    amders=`awk '{print $2}' $sAmders`; 
    echo $amders|sed "s@#N/A@\n@g"|while read line; do 
        [[ -z $line ]] && continue; 
        local cnt=`echo $line|wc|awk '{print $2}'`; 
        printf "%8s %8s\n" $cnt `getAmLineType "$line"`
    done
}
function latestSegment {
    local stockCode=$1
    local tradeDate=$2


    local sAmders="$dataRoot\\daily\\$stockCode\\$tradeDate\\derivative"
    local sLatest=`ls $sAmders|sort|tail -n100|tac`

    local i
    for i in $sLatest
    do
        local cnt=`wc "$sAmders\\\\$i"|awk '{print $1}'`
        [[ $cnt == 1200 ]] && break;
    done

    sLatest=`echo $i|awk -F. '{print $1}'`
    echo sLatest=$sLatest
    listSegment $stockCode $tradeDate $sLatest
}
