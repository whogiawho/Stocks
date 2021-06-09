#!/bin/bash

function getLSProfit {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local option=$4

    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -jar $analyzetoolsJar getlsprofit $option $stockCode $tradeDate $hms 2>/dev/null
}

function getOpenQuotationPrice {
    local stockCode=$1
    local tradeDate=$2

    getUpPrice $stockCode $tradeDate $CallAuctionEndTime
}
function getCloseQuotationPrice {
    local stockCode=$1
    local tradeDate=$2

    getUpPrice $stockCode $tradeDate $CloseQuotationTime
}
function getExtremePrice {
    local stockCode=$1
    local tradeDateList=$2

    local line=
    line=`getMaxPrice $stockCode $tradeDateList`
    echo $line

    line=`getMinPrice $stockCode $tradeDateList`
    echo $line
}
function getMaxCol {
    local stockCode=$1
    local tradeDateList=$2
    local col=$3

    local max=-1000000000000000000000000000
    local maxTime=
    local i=
    for i in $tradeDateList
    do
        local analysisTxt="$dailyDir\\$stockCode\\$i\\analysis.txt"
        local dailyMax=`awk '{print \$col}' col=$col $analysisTxt|sort -n|tail -n 1`
        local dailyMaxTime=`cat $analysisTxt|sort -nk$col,$col|tail -n 1|awk '{print $1}'`
        local rCmp=`ge $dailyMax $max`
        [[ $rCmp == 1 ]] && {
            max=$dailyMax
            maxTime=${i}_`getTimeHMS $dailyMaxTime`
        }
    done

    echo $max $maxTime
}
function getMinCol {
    local stockCode=$1
    local tradeDateList=$2
    local col=$3

    local min=1000000000000000000000000000
    local minTime=
    local i=
    for i in $tradeDateList
    do
        local analysisTxt="$dailyDir\\$stockCode\\$i\\analysis.txt"
        local dailyMin=`awk '{print \$col}' col=$col $analysisTxt|sort -rn|tail -n 1`
        local dailyMinTime=`cat $analysisTxt|sort -rnk$col,$col|tail -n 1|awk '{print $1}'`
        local rCmp=`le $dailyMin $min`
        [[ $rCmp == 1 ]] && {
            min=$dailyMin
            minTime=${i}_`getTimeHMS $dailyMinTime`
        }
    done

    echo $min $minTime
}

function getMaxPrice {
    local stockCode=$1
    local tradeDateList=$2

    local maxPrice=0.0 
    local maxPriceTime=
    local i=
    for i in $tradeDateList
    do
        local analysisTxt="$dailyDir\\$stockCode\\$i\\analysis.txt"
        local price=`awk '{print $4}' $analysisTxt|sort -n|tail -n 1`
        local priceTime=`cat $analysisTxt|sort -nk4,4|tail -n 1|awk '{print $1}'`
        local rCmp=`ge $price $maxPrice`
        [[ $rCmp == 1 ]] && {
            maxPrice=$price
            maxPriceTime=${i}_`getTimeHMS $priceTime`
        }
    done

    echo $maxPrice $maxPriceTime
}
function getMinPrice {
    local stockCode=$1
    local tradeDateList=$2

    local minPrice=100000
    local minPriceTime=
    local i=
    for i in $tradeDateList
    do
        local analysisTxt="$dailyDir\\$stockCode\\$i\\analysis.txt"
        local price=`awk '{print $5}' $analysisTxt|sort -rn|tail -n 1`
        local priceTime=`cat $analysisTxt|sort -rnk5,5|tail -n 1|awk '{print $1}'`
        local rCmp=`le $price $minPrice`
        [[ $rCmp == 1 ]] && {
            minPrice=$price
            minPriceTime=${i}_`getTimeHMS $priceTime`
        }
    done

    echo $minPrice $minPriceTime
}

function getInPrice {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local tradeType=$4
    
    #support unformal hms
    echo $hms|grep -q : || {
        hms=`formalizeHMSList $hms`
    }

    local idxPrice=
    [[ $tradeType == 5 ]] && {
        java -jar $analyzetoolsJar getupprice $stockCode $tradeDate $hms 2>/dev/null
    } || {
        java -jar $analyzetoolsJar getdownprice $stockCode $tradeDate $hms 2>/dev/null
    }
}
function _getUpPrice {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3

    [[ -z $hms ]] && {
        hms=${tradeDate#*,}
        tradeDate=${tradeDate%,*}
    }

    local analysisTxt="$dailyDir\\$stockCode\\$tradeDate\\analysis.txt"
    local hexTp=`convertTime2Hex $tradeDate $hms`
    grep $hexTp $analysisTxt|awk '{print$4}'
}
function getUpPrice {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
 
    [[ -z $hms ]] && {
        hms=${tradeDate#*,}
        tradeDate=${tradeDate%,*}
    }
    getInPrice $stockCode $tradeDate $hms 5
}
function getDownPrice {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
 
    getInPrice $stockCode $tradeDate $hms 1
}
function getMaxPriceBetween {
    local stockCode=$1
    local tradeDate=$2
    local startHMS=$3
    local endHMS=$4
    local tradeType=$5

    local analysisTxt="$dailyDir\\$stockCode\\$tradeDate\\analysis.txt"
    local idxPrice=
    [[ $tradeType == 5 ]] && {
        idxPrice=4
    } || {
        idxPrice=5
    }

    startHMS=0x`convertTime2Hex $tradeDate $startHMS`
    endHMS=0x`convertTime2Hex $tradeDate $endHMS`

    awk "strtonum(\"0x\"\$1)>=$startHMS&&strtonum(\"0x\"\$1)<=$endHMS{print \$$idxPrice}" $analysisTxt|sort -n|tail -n 1
}
function getMinPriceBetween {
    local stockCode=$1
    local tradeDate=$2
    local startHMS=$3
    local endHMS=$4
    local tradeType=$5

    local analysisTxt="$dailyDir\\$stockCode\\$tradeDate\\analysis.txt"
    local idxPrice=
    [[ $tradeType == 5 ]] && {
        idxPrice=4
    } || {
        idxPrice=5
    }

    startHMS=0x`convertTime2Hex $tradeDate $startHMS`
    endHMS=0x`convertTime2Hex $tradeDate $endHMS`

    awk "strtonum(\"0x\"\$1)>=$startHMS&&strtonum(\"0x\"\$1)<=$endHMS{print \$$idxPrice}" $analysisTxt|sort -n|head -n 1
}
function getExtremePriceBetween {
    local stockCode=$1
    local tradeDate=$2
    local startHMS=$3
    local endHMS=$4

    local max=`getMaxPriceBetween $stockCode $tradeDate $startHMS $endHMS 5`
    local min=`getMinPriceBetween $stockCode $tradeDate $startHMS $endHMS 1`

    printf "%8s %8s\n" $max $min
}


function getPrevCloseQuotationHexPrice {
    local stockCode=$1
    local tradeDate=$2

    local prevDate=`getPrevTradeDate $stockCode $tradeDate`
    
    tail -n 1 "$rawZuBiDataDir\\$stockCode\\$stockCode.$prevDate.txt"|awk '{print $2}'
}


