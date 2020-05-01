#!/bin/bash

InvalidHMSList="092500_093000"
MinMatchedCount=${MinMatchedCount:-100}
#re100="100\.\?[0-9]*%"
re100="100\.0%"


#dir=data/similarStack/600030/20160108_0.90_T1L
function listSSTableStats {
    local dir=$1
    local ssTableName=$2
    local maxCycle=${3:-180}
    local targetRate=${4:-1.100}

    local fSSTable=$rootDirCygdrive/data/ssTable/$ssTableName.txt

    local line=
    while read line; 
    do 
        echo $line|grep -qE "\#|^$" && continue; 

        local a b c d e f g h i
        read a b c d e f g h i <<<`echo $line`;
        local tradeDate=${i%%:*}; 
        local hmsList=${i##*:};
        getInstanceStats $dir/${tradeDate}_${maxCycle}_${targetRate} $hmsList
    done<$fSSTable
}

#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function getInstanceStats {
    local dir=$1
    local hmsList=$2

    local fTradeDetails=$dir/${hmsList}.txt; 
    local tradeDate=`ssGetTradeDate $dir`

    local cnt=`wc $fTradeDetails|awk '{print $1}'`; 
    local netR=`tail -n 1 $fTradeDetails|awk '{print $8}'`;
    local maxWait=`sort -nk12,12 $fTradeDetails|tail -n 1|awk '{print $12}'`; 
    local maxHang=`sort -nk13,13 $fTradeDetails|tail -n 1|awk '{print $13}'`; 
    local minRisk0=`sort -nk10,10 $fTradeDetails|head -n 1|awk '{print $10}'`;
    local maxRisk1=`sort -nk11,11 $fTradeDetails|tail -n 1|awk '{print $11}'`;
    netR=`divide $netR $maxHang`;

    printf "%s %s %4d %8.3f %4d %4d %8.3f %8.3f\n" \
        $tradeDate $hmsList $cnt $netR $maxWait $maxHang $minRisk0 $maxRisk1
}

#dir=data/similarStack/600030/20160108_0.90_T1L
function getSSFullWin {
    local dir=$1

    local fTmp=`mktemp`
    echo "generating fullwin list file $fTmp ..."

    local loop=0
    local max=1
    local cnt=0
    local i=
    for i in `ls $dir/*.txt`
    do
        grep "$re100" $i >> $fTmp &
        cnt=$((cnt+1))
        [[ $cnt -ge $max ]] && {
            wait
            echo "loop=$loop finished!"
            cnt=0
            loop=$((loop+1))
        }
    done
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

    local max=1000
    local cnt=0
    local line=
    while read line; 
    do 
        local tradeDate b c d e f g h hmsList j
        read tradeDate b c d e f g h hmsList j <<<`echo $line`; 

        getInstanceStats $dir/${tradeDate}_${maxCycle}_${targetRate} $hmsList &
        cnt=$((cnt+1))
        [[ $cnt -ge $max ]] && {
            wait
            cnt=0
        }

    done <$fFullWin >$fTmp
}
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function getMinMaxProfitGe {
    local dir=$1
    local profitThres=$2

    local tradeDate=`ssGetTradeDate $dir`
    local i=
    for i in `ls $dir`
    do 
        local maxDelta=`sort -nk9,9 $dir/$i|head -n 1|awk '{print $9}'`
        local ret=`ge $maxDelta $profitThres`
        [[ $ret == 1 ]] && { 
            local cnt=`wc $dir/$i|awk '{print $1}'`
            local hmsList=${i%%.txt*}
            printf "%s %s %4d %8.3f\n" $tradeDate $hmsList $cnt $maxDelta
        } 
    done
}
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function getMaxCycleLe {
    local dir=$1
    local cycleThres=$2

    local tradeDate=`ssGetTradeDate $dir`
    local i=
    for i in `ls $dir`
    do 
        local maxCycle=`sort -nk12,12 $dir/$i|tail -n 1|awk '{print $12}'`
        local ret=`le $maxCycle $cycleThres`
        [[ $ret == 1 ]] && { 
            local cnt=`wc $dir/$i|awk '{print $1}'`
            local hmsList=${i%%.txt*}
            printf "%s %s %4d %8d\n" $tradeDate $hmsList $cnt $maxCycle
        } 
    done
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



#dir=data/similarStack/600030/20160108_0.90_T1L
function _getSSFullWin {
    local dir=$1

    local fTmp=`mktemp`
    echo "generating fullwin list file $fTmp ..."

    grep "$re100" $dir/*.txt|sort -nk2,2 > $fTmp
    sed -i "s@$dir.*:@@g" $fTmp
}
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


#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function getNetR {
    local dir=$1
    local hmsList=$2

    local fTradeSum=$dir.txt
    local netR=`grep $hmsList $fTradeSum|awk '{print $10}'`;

    echo $netR
}
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function ssGetStockCode {
    local dir=$1

    dir=`echo ${dir#*/}`
    dir=`echo ${dir#*/}`
    dir=`echo ${dir%%/*}`

    echo $dir
}
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function ssGetTradeDate {
    local dir=$1

    local tradeDate=${dir##*/}
    tradeDate=${tradeDate%%_*}

    echo $tradeDate
}
#sTradeDetails=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100/092500_093000.txt
function ssGetHMSList {
    local sTradeDetails=$1

    local hmsList=${sTradeDetails##*/}; 
    hmsList=${hmsList%%.txt};

    echo $hmsList
}
function ssGetStartHMS {
    local hmsList=$1

    local startHMS=${hmsList%%_*}

    echo $startHMS
}
function ssGetEndHMS {
    local hmsList=$1

    local endHMS=${hmsList##*_}

    echo $endHMS
}




#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
#cycleThres=180              those cycle==cycleThres&& cycle>cycleThres
function getSSTradeDates {
    local dir=$1
    local hmsList=$2
    local cycleThres=$3

    local tradeDate=`ssGetTradeDate $dir`
    local sTradeDetails=$dir/$hmsList.txt; 
    local fOutEq=$TMP/${tradeDate}_${hmsList}Eq$cycleThres.txt
    awk "\$12==cycleThres{print \$1}" cycleThres=$cycleThres $sTradeDetails > $fOutEq

    local fOutGt=$TMP/${tradeDate}_${hmsList}Gt$cycleThres.txt
    awk "\$12>cycleThres{print \$1}" cycleThres=$cycleThres $sTradeDetails > $fOutGt

    wc $fOutEq |awk '{print $1,$4}'
    wc $fOutGt |awk '{print $1,$4}'
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

