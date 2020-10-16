#!/bin/bash

function _permStats {
    local fPerm=$1
    local i=$2
    local fOut=$3

    local fTmp=`mktemp`
    grep " $i$" $fPerm |sort -nk4,4 >$fTmp
    local permCount=`wc $fTmp|awk '{print $1}'`
    local minmaxProfit=`head -n1 $fTmp|awk '{print $4}'`
    printf "%10s %10s %8.3f\n" $i $permCount $minmaxProfit |tee -a $fOut

    rm -rf $fTmp
}
function permStats {
    local fPerm=$1
    local fPermIdx=$2
    local fOut=$3
    local start=$4           #36210
    local end=$5

    [[ ! -z $fOut ]] && rm $fOut

    local max=10
    local cnt=0
    local i=
    for i in `cat $fPermIdx`
    do
        [[ ! -z $start && $i -lt $start ]] && continue

        _permStats $fPerm $i $fOut &

        cnt=$((cnt+1))
        echo cnt=$cnt
        [[ $cnt -ge $max ]] && {
            wait -n
            cnt=$((cnt-1))
        }
    done
}

function getMatchedPermTp {
    local fPerm=$1
    local permIdx=$2

    local a b
    grep " $permIdx$" $fPerm |while read a b; do convertHex2Time $a; done|sort -uk1,1
}
