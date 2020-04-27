#!/bin/bash

function checkCloseSession {
    local closeDir=$rootDirCygdrive/data/sessions/close/*;

    local i=
    for i in `ls $closeDir`; 
    do 
        local stockCode=`grep stockCode $i|awk -F= '{print $2}'|dos2unix`; 
        local inTime=`grep inHexTimePoint $i|awk -F= '{print $2}'|dos2unix`; 
        local outTime=`grep actualOutHexTimePoint $i|awk -F= '{print $2}'|dos2unix`; 
        local inDate=`convertHex2Time $inTime|awk '{print $1}'|awk '{print $1}'|sed "s/-//g"`;  
        local outDate=`convertHex2Time $outTime|awk '{print $1}'|awk '{print $1}'|sed "s/-//g"`; 
        local inPrice=`grep actualInPrice $i|awk -F= '{print $2}'|dos2unix`
        local outPrice=`grep actualOutPrice $i|awk -F= '{print $2}'|dos2unix`
        local tradeType=`grep tradeType $i|awk -F= '{print $2}'|dos2unix`
        local profit=
        [[ $tradeType == 5 ]] && {
            profit=`substract $outPrice $inPrice`
        } || {
            profit=`substract $inPrice $outPrice`
        }

        printf "%s %12s %12s %4s %8s\n" $i $inDate $outDate `getTradeDateDist $stockCode $inDate $outDate` $profit; 
    done
}

function checkOpenSession {
    local openDir=$rootDirCygdrive/data/sessions/open/*;

    local i=
    for i in `ls $openDir`; 
    do 
        local stockCode=`grep stockCode $i|awk -F= '{print $2}'|dos2unix`; 
        local inTime=`grep inHexTimePoint $i|awk -F= '{print $2}'|dos2unix`; 
        local inDate=`convertHex2Time $inTime|awk '{print $1}'|awk '{print $1}'|sed "s/-//g"`;  
        local inPrice=`grep actualInPrice $i|awk -F= '{print $2}'|dos2unix`

        local outTime=`currentHexTime`
        local outDate=`convertHex2Time $outTime|awk '{print $1}'|awk '{print $1}'|sed "s/-//g"`; 

        local tradeType=`grep tradeType $i|awk -F= '{print $2}'|dos2unix`
        local profit=NaN

        printf "%s %12s %12s %4s %8s\n" $i $inDate $outDate `getTradeDateDist $stockCode $inDate $outDate` $profit; 
    done
}
