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
function ssnMinSet {
    local dir=$1
    local fSet=$2
    local maxCycle=${3:-180}
    local targetRate=${4:-1.100}

    #sort fSet firstly
    local fTmp0=`mktemp`
    sort -rnk5,5 $fSet >$fTmp0

    local fTmp1=`mktemp`
    echo fTmp1=$fTmp1
    local fTmp2=`mktemp`
    echo fTmp2=$fTmp2
    local line=
    while read line
    do
        local a b c d e f
        read a b c d e f <<<`echo $line`
        local dir1=$dir/${a}_${maxCycle}_${targetRate}
        local dir2=$dir/${c}_${maxCycle}_${targetRate}

        baseGetSSCommonTradeDetails $dir1 $b 180 $dir2 $d|awk '{print $1}'>$fTmp2 

        local output=
        output=`comm -13 $fTmp1 $fTmp2`
        [[ ! -z $output ]] && {
            echo $output|sed "s/ /\n/g" >>$fTmp1

            #comm requires $fTmp1 to be sorted 
            sort $fTmp1 >$fTmp2
            mv $fTmp2 $fTmp1

            echo "$line"
        }
    done<$fTmp0

    rm -rf $fTmp0 $fTmp1 $fTmp2
}




function _ssnMinSet {
    local dir=$1
    local fSet=$2
    local maxCycle=${3:-180}
    local targetRate=${4:-1.100}

    local fTmpDir=`mktemp -d`
    
    declare -a commA
    local line=
    local idx=1 
    while read line
    do
        local a b c d e f
        read a b c d e f <<<`echo $line`
        local dir1=$dir/${a}_${maxCycle}_${targetRate}
        local dir2=$dir/${c}_${maxCycle}_${targetRate}

        local fTmp=`mktemp -p $fTmpDir`
        baseGetSSCommonTradeDetails $dir1 $b 180 $dir2 $d|awk '{print $1}'>$fTmp &

        commA[$idx]=$fTmp
        idx=$((idx+1))
    done<$fSet
    wait

    local fPair1=`mktemp -p $fTmpDir`
    echo fPair1=$fPair1
    local fPair2=
    while [[ true ]] 
    do
        local bFound=0
        local max=0
        local maxKey maxValue
        local key=
        for key in ${!commA[@]}
        do
            fPair2=${commA[$key]}
            local output=
            output=`comm -13 $fPair1 $fPair2`
            local count=`echo $output|wc|awk '{print $2}'`
            [[ $count -gt $max ]] && {
                max=$count
                maxKey=$key
                maxValue=$output
                bFound=1
            }
        done

        [[ $bFound == 0 ]] && break
        #echo "max=$max"
        sed -n "${maxKey}p" $fSet
        fPair2=${commA[$maxKey]}
        echo $maxValue|sed "s/ /\n/g" >>$fPair1
        sort $fPair1 > $fPair2
        mv $fPair2 $fPair1
        unset commA[$maxKey]
    done
}
