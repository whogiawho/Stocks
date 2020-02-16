#!/bin/bash

#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
#maxwait=180
function getFullWinList {
    local dir=$1
    local maxwait=$2

    local file=$dir.txt; 
    local i=
    for i in `sort -nk3,3 $file |grep "100.*%"|awk '{print $9}'`;     #get those 100% winning hmsList
    do 
        local allg=0; 
        local j=
        for j in `awk '{print $12}' $dir/$i.txt`; 
        do 
            [[ $j -ge $maxwait ]] && { 
                allg=1; 
                break; 
            }; 
        done; 
        [[ $allg == 0 ]] && { 
            local c=`wc $dir/$i.txt|awk '{print $1}'`;                      #matchedTradeDates
            local m1=`awk '{print $12}' $dir/$i.txt|sort -nu|tail -n 1`;    #max tradeLength
            local m2=`awk '{print $13}' $dir/$i.txt|sort -nu|tail -n 1`;    #max currentHangCount
            printf "%s %4d %4d %4d\n" $i $c $m1 $m2; 
        }; 
    done
}


