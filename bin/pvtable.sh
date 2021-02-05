#!/bin/bash

function getTopPercent {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local daysBackward=$4

    java -jar $analyzetoolsJar gettoppercent \
        -b${daysBackward} -e${hms} -h${hms} $stockCode $tradeDate 2>/dev/null|awk '{print $1}'
}


