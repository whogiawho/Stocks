#!/bin/bash

function currentHexTime {
    local hexTime=`date +%s`
    hexTime=`printf "%x" $hexTime`
    echo $hexTime
}
function getToday {
    local today=`date +%F|sed "s/-//g"`
    echo $today
}
#HH:MM:SS --> HHMMSS
function unformalizeTradeTime {
    local tradeTime=$1

    local hour=${tradeTime:0:2}
    local minute=${tradeTime:3:2}
    local second=${tradeTime:6:2}

    echo $hour$minute$second
}
#HHMMSS --> HH:MM:SS
function formalizeTradeTime {
    local tradeTime=$1

    local hour=${tradeTime:0:2}
    local minute=${tradeTime:2:2}
    local second=${tradeTime:4:2}

    echo $hour:$minute:$second
}
#hhmmss_hhmmss_hhmmss --> hh:mm:ss hh:mm:ss hh:mm:ss
function formalizeHMSList {
    local hmsList=$1

    local str=
    local i=
    for i in `echo $hmsList|sed 's/_/ /g'`
    do
        local j=`formalizeTradeTime $i`
        str="$str$j "
    done

    echo $str
}
#hh:mm:ss hh:mm:ss hh:mm:ss --> hhmmss_hhmmss_hhmmss
function unformalizeHMSList {
    local hmsList=$1

    local str=
    local i=
    for i in $hmsList
    do
        local j=`unformalizeTradeTime $i`
        str="$str${j}_"
    done

    echo ${str%_}
}

function convertDec2Time {
    local dec=$1

    date -d @$dec "+%F %H:%M:%S"
}

function convertHex2Time {
    local hex=$1
    local dec=

    hex=0x$hex
    dec=`printf "%d" $hex`
    convertDec2Time $dec
}

#sTime format,   MM-DD hh:mm:ss
#year format,    YYYY
function convertTime {
    local sTime=$1                                         #in
    local year=$2                                          #in

    sTime="$year-$sTime"
    sTime=`date -d "$sTime" "+%s"`
    sTime=`printf "%x" $sTime`

    echo $sTime
}

function convertTime2Hex {
    local tradeDate=$1
    local tradeTime=$2

    tradeDate=`unformalizeTradeDate $tradeDate`
    tradeTime=${tradeTime//:/}
    local year=${tradeDate:0:4}
    local month=${tradeDate:4:2}
    local day=${tradeDate:6:2}
    local hour=${tradeTime:0:2}
    local minute=${tradeTime:2:2}
    local second=${tradeTime:4:2}

    local hexTime=`convertTime "$month-$day $hour:$minute:$second" $year`

    echo $hexTime
}
#[startDate, endDate]
function listTradeDates {
    local startDate=$1
    local endDate=$2

    local holidaysList="$rootDir\\specialDates\\holidays.txt"
    local distance=`dateDistance $startDate $endDate`
    [[ $distance -lt 0 ]] && {
        return
    }

    startDate=`formalizeTradeDate $startDate`
    local count=0
    while [[ 1 ]] 
    do
        local tradeDate=`date -d "$startDate +${count}day" +%Y%m%d`
        local weekDay=`date -d $tradeDate +%w`
        [[ $weekDay -ne 0 && $weekDay -ne 6 ]] && {
            grep -q $tradeDate $holidaysList || echo $tradeDate
        }
        count=$((count+1))

        [[ $tradeDate == $endDate ]] && break
    done
}



function getTimeHMS {
    local hexTime=$1

    local logTime=
    [[ ! -z $hexTime ]] && {
        logTime=`convertHex2Time $hexTime`
    } || {
        logTime=`date +%H:%M:%S`
    }
    logTime=${logTime#* }

    echo $logTime
}
function getDeltaHMS {
    local tradeDate=$1
    local baseTimeHMS=$2
    local delta=$3

    timePt=`date --date "$tradeDate $baseTimeHMS" "+%s"`
    timePt=$((timePt+delta))

    date --date @$timePt "+%H:%M:%S"
}
function last5WorkDate {
    local currentWeekDate=$1

    #local weekDay=`date -d "$currentWeekDate" +%w`
    #local weekNumber=`date -d "20160331" +%W`

    local i=
    for i in `seq 7`
    do
        local lastDay=
        lastDay=`date -d "$currentWeekDate -${i}day" +%Y%m%d`

        local lastWeekday=`date -d "$lastDay" +%w`
        [[ $lastWeekday -ne 0 && $lastWeekday -ne 6 ]] && echo $lastDay
    done
}

