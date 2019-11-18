#!/bin/bash

#YYYY-MM-DD --> YYYYMMDD
function unformalizeTradeDate {
    local tradeDate=$1

    echo $tradeDate|grep -q "-" && {
        local year=${tradeDate:0:4}
        local month=${tradeDate:5:2}
        local day=${tradeDate:8:2}

        echo $year$month$day
    } || {
        echo $tradeDate
    }
}
#YYYYMMDD --> YYYY-MM-DD
function formalizeTradeDate {
    local tradeDate=$1

    echo $tradeDate|grep -q "-" && {
        echo $tradeDate
    } || {
        local year=${tradeDate:0:4}
        local month=${tradeDate:4:2}
        local day=${tradeDate:6:2}

        echo $year-$month-$day
    }
}



#consider future tradeDates
function calTradeDateRange {
    local stockCode=$1
    local start=$2
    local end=$3

    local bSuspension=
    local suspensionDates="$rootDir\\specialDates\\$stockCode.suspension.txt"
    [[ -f $suspensionDates ]] && {
        bSuspension=1
    }

    local i=
    for i in `listTradeDates $start $end`
    do
        [[ -z $bSuspension ]] && {
            echo $i
            continue
        } || {
            grep -q $i $suspensionDates || echo $i
        }
    done
}



function getTradeDateList {
    local stockCode=$1
    local bList=$2

    local stockDir="$dailyDir\\$stockCode"
    local tradeDateList=`ls $stockDir|sed "s@/@@g"`

    [[ ! -z $bList ]] && {
        echo $tradeDateList | sed "s/ /\n/g"
    } || {
        echo $tradeDateList
    }
}
function getTradeDateRange {
    local stockCode=$1
    local start=$2
    local end=$3

    local list=`getTradeDateList $stockCode` 
    echo $list|grep -q $start || {
        echo "startDate=$start does not exist!"
        return -1
    }
    echo $list|grep -q $end|| {
        echo "endDate=$end does not exist!"
        return -1
    }

    [[ $start == $end ]] && {
        echo $list|sed "s@ @\n@g"|sed -n "/$start/p"
    } || {
        echo $list|sed "s@ @\n@g"|sed -n "/$start/,/$end/p"
    }
}
function getTradeDateNO {
    local stockCode=$1

    local tmpFile=`mktemp`
    getTradeDateList $stockCode|sed "s/ /\n/g" >$tmpFile
    wc -l $tmpFile|awk '{print $1}'

    rm -rf $tmpFile
}
function getTradeDateDist {
    local stockCode=$1
    local startTradeDate=$2
    local endTradeDate=$3

    local NO=`getTradeDateRange $stockCode $startTradeDate $endTradeDate|wc -l`

    echo $((NO-1))
}
function getPrevTradeDate {
    local stockCode=$1
    local tradeDate=$2

    local prevTradeDate=
    local i=
    for i in `getTradeDateList $stockCode`
    do
        [[ $i != $tradeDate ]] && {
            prevTradeDate=$i
        } || {
            break
        }
    done

    echo $prevTradeDate
}
function getNextTradeDate {
    local stockCode=$1
    local tradeDate=$2

    local nextTradeDate=
    local i=
    for i in `getTradeDateList $stockCode|sed 's/ /\n/g'|tac`
    do
        [[ $i != $tradeDate ]] && {
            nextTradeDate=$i
        } || {
            break
        }
    done

    echo $nextTradeDate

}
function getNextTradeDateList {
    local stockCode=$1
    local tradeDateList=$2

    local nextTradeDateList=
    local i=
    for i in $tradeDateList
    do
        local next=`getNextTradeDate $stockCode $i`
        nextTradeDateList="$nextTradeDateList $next"
    done

    echo $nextTradeDateList
}
function getLatestTradeDate {
    local stockCode=$1
    local lastDate=$2
    local lookbackNO=$3

    local tmpFile=`mktemp`
    getTradeDateList $stockCode|sed "s/ /\n/g" >$tmpFile

    local lastDateLineNO=
    lastDateLineNO=`grep -n $lastDate $tmpFile|awk -F: '{print $1}'`
    [[ -z $lastDateLineNO ]] && {
        printf "%s\n" "$lastDate does not exist!"
        return 1
    }
    local firstDataLineNO=
    firstDataLineNO=$((lastDateLineNO-lookbackNO+1))
    [[ $firstDataLineNO -le 0 ]] && firstDataLineNO=1

    sed -n "${firstDataLineNO},${lastDateLineNO}p" $tmpFile

    rm -rf $tmpFile
}

