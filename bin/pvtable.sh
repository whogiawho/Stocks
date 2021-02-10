#!/bin/bash

function getTopPercent {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local daysBackward=$4

    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -jar $analyzetoolsJar gettoppercent \
        -b${daysBackward} -e${hms} -h${hms} $stockCode $tradeDate 2>/dev/null|awk '{print $1}'
}

#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function listSSTradeDetailsPercent {
    local dir=$1
    local hmsList=$2

    local inHMS=${hmsList#*_}
    local stockCode=`ssGetStockCode $dir`
    local tradeDate=`ssGetTradeDate $dir`
    local sTxt=$dir/$hmsList.txt
    local a b c d e f g h i j k l m
    cat $sTxt|sort -nk12,12|while read a b c d e f g h i j k l m; do 
        p0=`getTopPercent $stockCode $a $inHMS 0`; 
        p1=`getTopPercent $stockCode $a $inHMS 1`; 
        p2=`getTopPercent $stockCode $a $inHMS 2`; 
        p3=`getTopPercent $stockCode $a $inHMS 3`; 
        p4=`getTopPercent $stockCode $a $inHMS 4`; 
        p5=`getTopPercent $stockCode $a $inHMS 5`; 
        printf "%8s %s %4s %8s %8s %8s %8s %8s %8s\n" $a $hmsList $l $p0 $p1 $p2 $p3 $p4 $p5; 
    done;
}
