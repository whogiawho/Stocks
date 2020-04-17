#!/bin/bash

#dir=data/similarStack/600030/20160108_0.90_T1L
#N - the min matched count of the <tradeDate, hmsList> whose cycles are all <=maxWait
function makeMatchCntGeNList {
    local dir=$1
    local N=$2
    local maxWait=$3

    #reset outDir
    local outDir=/tmp/maxWait${N}_${maxWait}
    rm -rf $outDir
    mkdir -p $outDir

    local loop=0
    local max=100
    local cnt=0
    local i=
    for i in `find $dir -mindepth 1 -maxdepth 1 -type d`
    do
        _makeMatchCntGeNList $i $N $maxWait $outDir&

        cnt=$((cnt+1))
        [[ $cnt -ge $max ]] && {
            wait
            echo "loop=$loop finished!"
            cnt=0
            loop=$((loop+1))
        }
    done
}
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
#N - the min matched count of the <tradeDate, hmsList> whose cycles are all <=maxWait
function _makeMatchCntGeNList {
    local dir=$1
    local N=$2
    local maxWait=$3
    local outDir=$4

    local outFile=${dir##*/}.txt
    outFile=$outDir/$outFile

    local sTradeSum=$dir.txt
    local fTmp=`mktemp`

    awk "\$2>=N{print \$0}" N=$N $sTradeSum >$fTmp
    local line=
    while read line
    do
        local a b c d e f g matchedTradeDates hmsList j
        read a b c d e f g matchedTradeDates hmsList j<<<`echo $line`
        local maxWaitList=`getMatchTradeDatesCycleLeN $dir $hmsList $maxWait`

        #skip this check as u wish
        local cnt=`echo $maxWaitList|wc|awk '{print $2}'`
        [[ $cnt -lt $N ]] && continue

        echo "$dir/$hmsList" >>$outFile
    done<$fTmp

    rm -rf $fTmp
}
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function getMatchTradeDatesCycleLeN {
    local dir=$1
    local hmsList=$2
    local maxWait=$3

    local sTradeDetails=$dir/$hmsList.txt

    awk "\$12<=maxWait{print \$1}" maxWait=$maxWait $sTradeDetails
}





