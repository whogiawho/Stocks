#!/bin/bash


function checkAllSSTable {
    local stockCode=$1
    local tradeDate=$2

    java -jar $analyzetoolsJar checksstable $stockCode $tradeDate h0 2>/dev/null
    java -jar $analyzetoolsJar checksstable $stockCode $tradeDate h1 2>/dev/null
    java -jar $analyzetoolsJar checksstable $stockCode $tradeDate h2 2>/dev/null
}

function checkSSTable {
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


