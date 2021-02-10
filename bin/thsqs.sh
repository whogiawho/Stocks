#!/bin/bash

export PYTHONIOENCODING=utf8
export sEntrustTraded=(已成 已成)
export sBalanceAvaiRemains=(可用金额 可用金额)
export sEntrustStateKeys=(备注 委托状态)
export sEntrustPriceKeys=(成交均价 成交价格)

#0 - jyzq
#1 - zxzq
export qsIdx=0

function refresh {
    python -u $rootDir\\python\\refresh.py
}


function buy600030_1000 {
    local price=$1

    buy 600030 $price 1000
}
function sell600030_1000 {
    local price=$1

    sell 600030 $price 1000
}
function buy {
    local stockCode=$1
    local price=$2
    local amount=$3

    python -u $rootDir\\python\\buy.py $stockCode $price $amount

}
function sell {
    local stockCode=$1
    local price=$2
    local amount=$3

    python -u $rootDir\\python\\sell.py $stockCode $price $amount
}


function cancelEntrust {
    local entrustno=$1
    
    python -u $rootDir\\python\\cancelentrust.py $entrustno
}
function getEntrust {
    local eNO=$1
    local key=$2
    
    python -u $rootDir\\python\\today_entrusts.py $eNO $key
}
function getEntrustPrice {
    local eNO=$1
    
    #java -jar build/jar/analyzetools.jar getentrust $eNO ${sEntrustPriceKeys[$qsIdx]}
    python -u $rootDir\\python\\today_entrusts.py $eNO ${sEntrustPriceKeys[$qsIdx]}
}
function getEntrustState {
    local eNO=$1
    
    #java -jar build/jar/analyzetools.jar getentrust $eNO ${sEntrustStateKeys[$qsIdx]}
    python -u $rootDir\\python\\today_entrusts.py $eNO ${sEntrustStateKeys[$qsIdx]}
}
function getPosition {
    local stockCode=$1
    local key=$2

    python -u $rootDir\\python\\position.py $stockCode $key
}


function getBalance {
    local key=$1

    python -u $rootDir\\python\\balance.py $key
}


function makeRRP {
    local price=$1

    [[ -z $price ]] && price=1.000
    local rrpCode=131810

    local avaiRemains=`getBalance ${sBalanceAvaiRemains[$qsIdx]}`
    avaiRemains=`echo $avaiRemains|sed 's/\r//g'`
    local tradeFee=`mul $avaiRemains 0.00001`
    avaiRemains=`substract $avaiRemains $tradeFee`
    local amount=`divide $avaiRemains 1000 0`
    amount=`printf %0.f $amount`
    amount=$((amount*10))

    local entrustNO=
    entrustNO=`sell $rrpCode $price $amount`
    entrustNO=`echo $entrustNO|sed 's/\r//g'`
    echo entrustNO=$entrustNO

    local state=
    while [[ 1 ]] 
    do
        state=`getEntrustState $entrustNO`
        state=`echo $state|sed 's/\r//g'`
        [[ $state == ${sEntrustTraded[$qsIdx]} ]] && {
            break
        } || {
            echo state=$state
            sleep 1
        }
    done
}
