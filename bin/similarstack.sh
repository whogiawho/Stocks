#!/bin/bash

InvalidHMSList="092500_093000"
MinMatchedCount=${MinMatchedCount:-100}
#re100="100\.\?[0-9]*%"
re100="100\.0%"


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
        local cycle=`sort -nk12,12 $fTmp|tail -n1|awk '{print $12}'`; 

        [[ $cnt != 0 && $cycle -le $maxWaitThres ]] && {
            [[ $endHMS2 > $endHMS1 ]] && {
                printf "%s %s %s %s %4d %4d\n" $tradeDate2 $i $tradeDate1 $hmsList1 $cnt $cycle; 
            } || {
                printf "%s %s %s %s %4d %4d\n" $tradeDate1 $hmsList1 $tradeDate2 $i $cnt $cycle; 
            } 
        }
    done

    rm -rf $fTmp
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


function getNetR {
    local dir=$1
    local hmsList=$2

    local fTradeSum=$dir.txt
    local netR=`grep $hmsList $fTradeSum|awk '{print $10}'`;

    echo $netR
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




#parms samples:
#dir=data/similarStack/600030/20160108_0.90_T1L/20160111_180_1.100
#maxWait=180              exclude those tradeLength>maxWait
function getSSHMSListFullWin {
    local dir=$1
    local maxWait=$2

    local file=$dir.txt; 
    local i=
    for i in `sort -nk3,3 $file |grep "$re100"|awk '{print $9}'`; #get those 100% winning hmsList
    do 
        local allg=0; 
        local maxPeriod=`awk '{print $12}' $dir/$i.txt|sort -n|tail -n 1`

        [[ $maxPeriod -gt $maxWait ]] && { 
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
#dir=data/similarStack/600030/20160108_0.90_T1L
#maxWait            exclude those tradeLength>maxWait
function getSSHMSList {
    local dir=$1
    local maxWait=$2

    local i=
    for i in `find $dir -mindepth 1 -maxdepth 1 -type d`
    do
        echo $i
        getSSHMSListFullWin $i $maxWait
    done
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

