#!/bin/bash

MinMatchedCount=${MinMatchedCount:-100}


#parms samples:
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
#maxwait=180              exclude those tradeLength>=maxwait
function getSSHMSListFullWin {
    local dir=$1
    local maxwait=$2

    local file=$dir.txt; 
    local i=
    for i in `sort -nk3,3 $file |grep "100\.\?[0-9]*%"|awk '{print $9}'`; #get those 100% winning hmsList
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

function baseGetSSCommon {
    local dir1=$1
    local hmsList1=$2
    local maxwait=$3
    local dir2=$4
    local hmsList2=$5

    #local a= last1= last2=
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

    echo $dir1 $hmsList1 $maxwait $dir2 $hmsList2 $countTotal $count0

    rm -rf $fTmp3
}

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
}




