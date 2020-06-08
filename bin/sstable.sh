#!/bin/bash

ssTableDir=${ssTableDir:-$rootDirCygdrive/data/ssTable}
intersectionDir=${intersectionDir:-$rootDirCygdrive/data/intersection}


function checkAllSSTable {
    local stockCode=$1
    local tradeDate=$2

    local i=
    for i in `java -jar $analyzetoolsJar getsstabname 2>/dev/null`
    do
        checkSSTable $stockCode $tradeDate $i
    done
}
function checkSSTable {
    local stockCode=$1
    local tradeDate=$2
    local tableName=$3

    java -jar $analyzetoolsJar checksstable $stockCode $tradeDate $tableName 2>/dev/null
}


function getHmsListDelta {
    local stockCode=$1
    local tradeDate=$2
    local hmsList=$3

    local start end
    read start end<<<`java -jar build/jar/analyzetools.jar getabs $stockCode $tradeDate $hmsList 2>/dev/null`

    echo $((end-start))
}
#dir=data/similarStack/600030/20160108_0.90_T1L
function makeIntersection {
    local dir=$1
    local sMatchExprComponent=$2
    local maxWaitThres=$3
    local maxCycle=${4:-180}
    local targetRate=${5:-1.100}

    local tradeDate=${sMatchExprComponent%:*}
    local hmsList=${sMatchExprComponent#*:}
    local fOut=$intersectionDir/${tradeDate}_${hmsList}_${maxWaitThres}.txt

    _ssGetIntersection $dir/${tradeDate}_${maxCycle}_${targetRate} $hmsList $maxWaitThres $dir/${tradeDate}_${maxCycle}_${targetRate}|tee $fOut

    #sort $fOut to $fTmp
    local fTmp=`mktemp`
    awk '$4!=hmsList{print $0}' hmsList=$hmsList $fOut >>$fTmp
    awk '$4==hmsList{print $0}' hmsList=$hmsList $fOut >>$fTmp
    mv $fTmp $fOut

    #manually remove those items not forming a large group 
}
function makeSSTable {
    local fIntersection=$1
    local maxWait=$2

    local fTmp=`mktemp`
    #remove invalid intersection items whose delta abs<n(240?)
    local line
    cat $fIntersection |while read line; 
    do 
        local a b c d matchedCnt cycle
        read a b c d matchedCnt cycle<<<`echo $line`; 

        [[ $cycle -gt maxWait ]] && continue;

        local delta1=`getHmsListDelta 600030 $a $b`; 
        local delta2=`getHmsListDelta 600030 $c $d`; 
        [[ $delta1 -lt 240 || $delta2 -lt 240 ]] && continue

        echo "$line" >>$fTmp
    done


    local fTable=${fIntersection##*/}
    local part1=${fTable%_*}
    local part2=${fTable##*_}
    part2=${part2%%.txt}
    fTable=$ssTableDir/${part1}_${maxWait}.txt
    
    echo "#TradeCount  stockCode  startDate  threshold  stDistance  tradeType  maxCycle  targetRate  matchExp" >$fTable
    local sPrefix="1000         600030      20090105   0.90       1           5         180       1.100       "

    awk '$6<=maxWait{print $0}' maxWait=$maxWait $fTmp|while read line;
    do
        local a b c d e
        read a b c d e<<<`echo $line`
        local sMatchExpr="$a:$b&$c:$d"
        local item=$sPrefix$sMatchExpr
        echo "$item" >>$fTable
    done

    rm -rf $fTmp
}
#dir=data/similarStack/600030/20160108_0.90_T1L
function ssGetMatchedCounts {
    local dir=$1
    local fTable=$2

    local fTmp=`mktemp`
    local i=
    for i in `cat $fTable |awk '{print $9}'`; 
    do 
        echo $i
        _baseGetSSCommonTradeDetails $dir "$i" 22; 
    done | tee $fTmp

    grep -v "\&" $fTmp|awk '{print $1,$2}'|sort|uniq|wc

    rm -rf $fTmp
}





function _checkSSTable {
    local stockCode=$1
    local tradeDate=$2
    local ssTableName=$3

    local fSSTable=$rootDirCygdrive/data/ssTable/$ssTableName.txt
    cat $fSSTable |grep -vE "^$|#" |awk '{print $9}'|sed "s/:/ /g"|while read line; 
    do 
        local matchedTradeDate hmsList 
        read matchedTradeDate hmsList<<<`echo $line`; 
        local amcorrel=`getAmCorrel $stockCode $tradeDate $matchedTradeDate $hmsList`; 
        local ret=`gt $amcorrel 0.90`; 
        [[ $ret == 1 ]] && echo $matchedTradeDate $hmsList $amcorrel; 
    done
}
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
function statsBasedOnOp {
    local dir=$1
    local hmsList=$2

    local stockCode=`ssGetStockCode $dir`
    local tradeDate=`ssGetTradeDate $dir`
    local fTmp=$TMP/${tradeDate}_${hmsList}.txt
    local line=
    cat $dir/$hmsList.txt | while read line; 
    do 
        local matchedDate b c d inPrice f g h i j
        read matchedDate b c d inPrice f g h i j <<<`echo $line`; 
        local nextDate=`getNextTradeDate $stockCode $matchedDate`
        local openP=`getOpenQuotationPrice 600030 $nextDate`; 
        local maxP=`getMaxPrice 600030 $nextDate|awk '{print $1}'`; 
        local closeP=`getCloseQuotationPrice 600030 $nextDate`; 

        local dk=`substract $openP $inPrice`; 
        local delta0=`substract $maxP $openP`; 
        local delta1=`substract $maxP $inPrice`; 
        local delta2=`substract $closeP $inPrice`; 
        printf "%s %s %8s %8s %8s %8s\n" $matchedDate $nextDate $dk $delta0 $delta1 $delta2; 
    done | tee $fTmp

}
function ssPrintBetween {
    local fTmp=$1
    local lowThres=$2
    local highThres=$3

    awk '$3>=low&&$3<high{print $0}' low=$lowThres high=$highThres $fTmp 
}
function ssGetMinDeltas {
    local fTmp=$1

    local cnt=
    local fTmp1=`mktemp`
    local lowThres=-0.10
    local highThres=0.10

    printf "openP<%.2f, reEnter in openP and exit with openP+minDelta:\n" $lowThres
    awk '$3<-0.10{print $0}' $fTmp >$fTmp1
    cnt=`cat $fTmp1|wc|awk '{print $1}'`
    minDelta=`cat $fTmp1|sort -rnk4,4|tail -n1|awk '{print $4}'`
    printf "%4d %.2f\n" $cnt $minDelta

    printf "%.2f=<openP<%.2f, exit with inPrice+minDelta:\n" $lowThres $highThres
    awk '$3>=-0.10&&$3<0.10{print $0}' $fTmp >$fTmp1
    cnt=`cat $fTmp1|wc|awk '{print $1}'`
    minDelta=`cat $fTmp1|sort -rnk5,5|tail -n1|awk '{print $5}'`
    printf "%4d %.2f\n" $cnt $minDelta

    printf "openP>%.2f, exit with inPrice+minDelta:\n" $highThres
    awk '$3>=0.10{print $0}' $fTmp >$fTmp1
    cnt=`cat $fTmp1|wc|awk '{print $1}'`
    minDelta=`cat $fTmp1|sort -rnk5,5|tail -n1|awk '{print $5}'`
    printf "%4d %.2f\n" $cnt $minDelta

    rm -rf $fTmp1
}


