#!/bin/bash

#dir[12]=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
#only considering those hmsList with matchedTradeDates>=100
function getSSCommon {
    local dir1=$1
    local maxWait=$2
    local dir2=$3

    local file1=$dir1.txt; 
    local list1=`awk "\\$2>=$MinMatchedCount{print \\$9}" $file1`

    local file2=$dir2.txt; 
    local list2=`awk "\\$2>=$MinMatchedCount{print \\$9}" $file2`

    local i=
    local j=
    for i in $list1
    do
        for j in $list2
        do
            baseGetSSCommon $dir1 $i $maxWait $dir2 $j
        done
    done
}
#dir[12]=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function baseGetSSCommon {
    local dir1=$1
    local hmsList1=$2
    local maxWait=$3              #apply to <dir1, hmsList1>
    local dir2=$4
    local hmsList2=$5

    #local a last1 last2
    #IFS=_ read a last1 <<<`echo $hmsList1`
    #IFS=_ read a last2 <<<`echo $hmsList1`
    #[[ $last1 > $last2 ]] && return

    [[ $dir1 == $dir2 && $hmsList1 == $hmsList2 ]] && return

    local file1=$dir1/$hmsList1.txt
    local file2=$dir2/$hmsList2.txt

    local fTmp3=`mktemp`
    comm -12 <(awk "{print \$1}" $file1) <(awk "{print \$1}" $file2) >$fTmp3
    sed -i "s/^/\^/g" $fTmp3

    local countTotal=`wc $fTmp3|awk '{print $1}'`
    local count0=`grep -f $fTmp3 $file1|awk "\\$12<=$maxWait{print \\$0}"|wc|awk '{print $1}'`
    local r=`echo "scale=2; $count0/$countTotal"|bc`

    printf "%s %s %s %s %s %8s %8s %8.3f\n" $dir1 $hmsList1 $maxWait $dir2 $hmsList2 $countTotal $count0 $r

    rm -rf $fTmp3
}
#dir[12]=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function baseGetSSCommonTradeDetails {
    local dir1=$1
    local hmsList1=$2
    local maxWait=$3              #apply to <dir1, hmsList1>
    local dir2=$4
    local hmsList2=$5

    [[ $dir1 == $dir2 && $hmsList1 == $hmsList2 ]] && return

    local file1=$dir1/$hmsList1.txt
    local file2=$dir2/$hmsList2.txt

    local fTmp3=`mktemp`
    comm -12 <(awk "{print \$1}" $file1) <(awk "{print \$1}" $file2) >$fTmp3
    sed -i "s/^/\^/g" $fTmp3

    grep -f $fTmp3 $file1|awk "\$12<=$maxWait{print \$0}"

    rm -rf $fTmp3
}
#dir=data/similarStack/600030/20160108_0.90_T1L
#matchExpr="20150410:144400_145500&20150410:112600_131500"
function _baseGetSSCommonTradeDetails {
    local dir=$1
    local matchExpr=$2
    local maxWait=$3
    local maxCycle=${4:-180}
    local targetRate=${5:-1.100}

    local exprPart1=${matchExpr%&*}
    local exprPart2=${matchExpr#*&}
    local tradeDate1=${exprPart1%:*}
    local tradeDate2=${exprPart2%:*}
    local hmsList1=${exprPart1#*:}
    local hmsList2=${exprPart2#*:}
    local dir1=$dir/${tradeDate1}_${maxCycle}_${targetRate}
    local dir2=$dir/${tradeDate2}_${maxCycle}_${targetRate}

    #echo $dir1 $hmsList1 $dir2 $hmsList2
    baseGetSSCommonTradeDetails $dir1 $hmsList1 $maxWait $dir2 $hmsList2
}




#dir=data/similarStack/600030/20160108_0.90_T1L
function ssGetIntersection {
    local dir=$1
    local tradeDates=$2
    local maxCycle=${3:-180}
    local targetRate=${4:-1.100}

    local i=
    local dirList=
    [[ -z $tradeDates ]] && {
        for i in `find $dir -mindepth 1 -maxdepth 1 -type d`
        do
            dirList="$dirList $i"
        done
    } || {
        for i in $tradeDates
        do
            dirList="$dirList $dir/${i}_${maxCycle}_${targetRate}" 
        done
    }

    local maxWaitThres=5
    local max=49
    local cnt=0
    for i in $dirList
    do
        local sTradeSum="$i.txt"
        local hmsList=`sort -nk2,2 $sTradeSum|grep -v $InvalidHMSList|tail -n 1|awk '{print $9}'`
        _ssGetIntersection $i $hmsList $maxWaitThres $i &
        cnt=$((cnt+1))
        [[ $cnt -ge $max ]] && {
            wait
            cnt=0
        }
    done
}
#dir[12]=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function _ssGetIntersection {
    local dir1=$1
    local hmsList1=$2
    local maxWaitThres=$3              #apply to <dir1, hmsList1>
    local dir2=$4

    [[ $hmsList1 == $InvalidHMSList ]] && return

    local tradeDate1=`ssGetTradeDate $dir1`
    local tradeDate2=`ssGetTradeDate $dir2`
    local endHMS1=`ssGetEndHMS $hmsList1`
    local fTmp=`mktemp`; 
    local i=
    for i in `ls $dir2/*.txt`; do 
        i=`ssGetHMSList $i`
        [[ $i == $InvalidHMSList ]] && continue

        #whose endHMS is later? i or hmsList1 
        local endHMS2=`ssGetEndHMS $i`
        [[ $endHMS2 > $endHMS1 ]] && {
            baseGetSSCommonTradeDetails $dir2 $i 180 $dir1 $hmsList1>$fTmp; 
        } || {
            baseGetSSCommonTradeDetails $dir1 $hmsList1 180 $dir2 $i>$fTmp; 
        }

        local cnt=`wc $fTmp|awk '{print $1}'`; 
        local maxCycle=`sort -nk12,12 $fTmp|tail -n1|awk '{print $12}'`; 
        local minProfit=`sort -rnk7,7 $fTmp|tail -n1|awk '{print $7}'`
        #echo $minProfit 
        local bmpgt0=0
        [[ ! -z $minProfit ]] && bmpgt0=`gt $minProfit 0` 

        [[ $cnt != 0 && $maxCycle -le $maxWaitThres && $bmpgt0 == 1 ]] && {
            [[ $endHMS2 > $endHMS1 ]] && {
                printf "%s %s %s %s %4d %4d\n" $tradeDate2 $i $tradeDate1 $hmsList1 $cnt $maxCycle; 
            } || {
                printf "%s %s %s %s %4d %4d\n" $tradeDate1 $hmsList1 $tradeDate2 $i $cnt $maxCycle; 
            } 
        }
    done

    rm -rf $fTmp
}




#dir=data/similarStack/600030/20160108_0.90_T1L
#fSet - output by _ssGetIntersection
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
#dir=data/similarStack/600030/20160108_0.90_T1L
#fSet - output by _ssGetIntersection
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
