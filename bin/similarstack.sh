#!/bin/bash

MinMatchedCount=${MinMatchedCount:-100}
re100="100\.\?[0-9]*%"


#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function _getInstanceStats {
    local stockCode=$1
    local startDate=$2
    local threshold=$3
    local sTDistance=$4
    local tradeType=$5
    local tradeDate=$6
    local hmsList=$7
    local maxCycle=$8
    local targetRate=$9

    local dir=`makeTradeDateDir $stockCode $startDate $threshold $sTDistance $tradeType \
        $tradeDate $maxCycle $targetRate`

    getInstanceStats $dir $hmsList
}
function getInstanceStats {
    local dir=$1
    local hmsList=$2

    local fTradeDetails=$dir/${hmsList}.txt; 

    local cnt=`wc $fTradeDetails|awk '{print $1}'`; 
    local netR=`tail -n 1 $fTradeDetails|awk '{print $8}'`;
    local maxWait=`sort -nk12,12 $fTradeDetails|tail -n 1|awk '{print $12}'`; 
    local maxHang=`sort -nk13,13 $fTradeDetails|tail -n 1|awk '{print $13}'`; 
    netR=`divide $netR $maxHang`;

    printf "%4d %8.3f %4d %4d\n" $cnt $netR $maxWait $maxHang
}
function getNetR {
    local dir=$1
    local hmsList=$2

    local fTradeSum=$dir.txt
    local netR=`grep $hmsList $fTradeSum|awk '{print $10}'`;

    echo $netR
}


#dir=data/similarStack/600030/20160108_0.90_T1L
function getSSFullWin {
    local dir=$1

    local fTmp=`mktemp`
    echo "generating fullwin list file $fTmp ..."

    grep "$re100" $dir/*.txt|sort -nk2,2 > $fTmp
    sed -i "s@$dir.*:@@g" $fTmp
}
#dir=data/similarStack/600030/20160108_0.90_T1L
#fFullWin=`getSSFullWin`
function getSSFullWinStats {
    local dir=$1
    local fFullWin=$2
    local maxCycle=${3:-180}
    local targetRate=${4:-1.100}

    local fTmp=`mktemp`
    echo "generating fullwin list stats file $fTmp ..."

    local line=
    while read line; 
    do 
        local tradeDate b c d e f g h hmsList j
        read tradeDate b c d e f g h hmsList j <<<`echo $line`; 

        local stats=`getInstanceStats $dir/${tradeDate}_${maxCycle}_${targetRate} $hmsList`

        printf "%s %s %s\n" $tradeDate $hmsList "$stats"
    done <$fFullWin >$fTmp
}

#parms samples:
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
#maxwait=180              exclude those tradeLength>=maxwait
function getSSHMSListFullWin {
    local dir=$1
    local maxwait=$2

    local file=$dir.txt; 
    local i=
    for i in `sort -nk3,3 $file |grep "$re100"|awk '{print $9}'`; #get those 100% winning hmsList
    do 
        local allg=0; 
        local maxPeriod=`awk '{print $12}' $dir/$i.txt|sort -n|tail -n 1`

        [[ $maxPeriod -ge $maxwait ]] && { 
            allg=1; 
        }
        [[ $allg == 0 ]] && { 
            local c=`wc $dir/$i.txt|awk '{print $1}'`;                      #matchedTradeDates
            local m1=`awk '{print $12}' $dir/$i.txt|sort -nu|tail -n 1`;    #max tradeLength
            local m2=`awk '{print $13}' $dir/$i.txt|sort -nu|tail -n 1`;    #max currentHangCount
            printf "%s %4d %4d %4d\n" $i $c $m1 $m2; 
        }; 
    done
}
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
#maxwait            include those tradeLength<=maxwait
function getSSHMSList {
    local dir=$1
    local maxwait=$2

    local file=$dir.txt; 
    local i=
    for i in `awk '{print $9}' $file`; 
    do 
        local count=`awk "\\$12<=$maxwait{print \\$1}" $dir/$i.txt|wc|awk '{print $1}'`;
        echo $i $count; 
    done
}


#dir[12]=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
#only considering those hmsList with matchedTradeDates>=100
function getSSCommon {
    local dir1=$1
    local maxwait=$2
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
            baseGetSSCommon $dir1 $i $maxwait $dir2 $j
        done
    done
}
#dir[12]=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function baseGetSSCommon {
    local dir1=$1
    local hmsList1=$2
    local maxwait=$3
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
    local count0=`grep -f $fTmp3 $file1|awk "\\$12<=$maxwait{print \\$0}"|wc|awk '{print $1}'`
    local r=`echo "scale=2; $count0/$countTotal"|bc`

    printf "%s %s %s %s %s %8s %8s %8.3f\n" $dir1 $hmsList1 $maxwait $dir2 $hmsList2 $countTotal $count0 $r

    rm -rf $fTmp3
}
#dir[12]=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function baseGetSSCommonTradeDetails {
    local dir1=$1
    local hmsList1=$2
    local maxwait=$3
    local dir2=$4
    local hmsList2=$5

    [[ $dir1 == $dir2 && $hmsList1 == $hmsList2 ]] && return

    local file1=$dir1/$hmsList1.txt
    local file2=$dir2/$hmsList2.txt

    local fTmp3=`mktemp`
    comm -12 <(awk "{print \$1}" $file1) <(awk "{print \$1}" $file2) >$fTmp3
    sed -i "s/^/\^/g" $fTmp3

    grep -f $fTmp3 $file1|awk "\$12<=$maxwait{print \$0}"

    rm -rf $fTmp3
}



function makeStartDateDir {
    local stockCode=$1
    local startDate=$2
    local threshold=$3
    local sTDistance=$4
    local tradeType=$5

    threshold=`printf "%.2f" $threshold`
    local sym
    [[ $tradeType == 5 ]] && sym=L || sym=S
    echo data/similarStack/$stockCode/${startDate}_${threshold}_T${sTDistance}${sym}
}
function makeTradeDateDir {
    local stockCode=$1
    local startDate=$2
    local threshold=$3
    local sTDistance=$4
    local tradeType=$5
    local tradeDate=$6
    local maxCycle=$7
    local targetRate=$8

    local s1st=`makeStartDateDir $stockCode $startDate $threshold $sTDistance $tradeType`
    targetRate=`printf "%.3f" $targetRate`
    echo $s1st/${tradeDate}_${maxCycle}_${targetRate}
}


function verifyGroup {
    local stockCode=$1
    local hmsList=$2
    local fList0=$1
    local fList1=$2
    local fList2=$3

    local fTmp0=`mktemp`
    local fTmp1=`mktemp`

    local i=
    for i in `cat $fList0`
    do
        local j=
        for j in `cat $fList1`
        do
            [[ $j != $i ]] && {
                getAmCorrel $stockCode $i $j $hmsList >>$fTmp0
            }
        done

        for j in `cat $fList2`
        do
            [[ $j != $i ]] && {
                getAmCorrel $stockCode $i $j $hmsList >>$fTmp1
            }
        done

        local avg0=`getListAvg $fTmp0 1`
        local avg1=`getListAvg $fTmp1 1`
        local stddev0=`getListStdDev $fTmp0 1`
        local stddev1=`getListStdDev $fTmp1 1`

        local ingroupT=                                     //theory
        local r=`ge $avg0 $avg1`
        [[ $r == 1 ]] && ingroupT=1 || ingroupT=2

        local ingroupF=                                     //fact
        grep -q $i $fList1 && ingroupF=1 || ingroupF=2 

        printf "%s %8s %8s %4s %4s %8s %8s\n" $i $avg0 $avg1 $ingroupT $ingroupF $stddev0 $stddev1

        rm -rf $fTmp0 $fTmp1
    done
}
