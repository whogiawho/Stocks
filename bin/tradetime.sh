#!/bin/bash


CallAuctionStartTime=${CallAuctionStartTime:-"09:15:00"}
CallAuctionEndTime=${CallAuctionEndTime:-"09:25:00"}
CloseQuotationTime=${CloseQuotationTime:-"15:00:00"}
LastRawTradeDetailTime=${LastRawTradeDetailTime:-"15:01:00"}


#11:30
function getMidSuspensionTime {
    local hexTime=$1

    local tradeDate=`convertHex2Time $hexTime|awk '{print $1}'`
    local halfPoint=
    halfPoint=`date --date "$tradeDate 11:30:00" "+%s"`

    printf "%x" $halfPoint
}
#13:00
function getMidOpenQuotationTime {
    local hexTime=$1

    local tradeDate=`convertHex2Time $hexTime|awk '{print $1}'`
    local halfPoint=
    halfPoint=`date --date "$tradeDate 13:00:00" "+%s"`

    printf "%x" $halfPoint
}
#09:30
function getOpenQuotationTime {
    local hexTime=$1

    local tradeDate=`convertHex2Time $hexTime|awk '{print $1}'`
    local halfPoint=
    halfPoint=`date --date "$tradeDate 09:30:00" "+%s"`

    printf "%x" $halfPoint
}
#15:00
function getCloseQuotationTime {
    local hexTime=$1

    local tradeDate=`convertHex2Time $hexTime|awk '{print $1}'`
    local halfPoint=
    halfPoint=`date --date "$tradeDate $CloseQuotationTime " "+%s"`

    printf "%x" $halfPoint
}





#assumptions:
#    hexTime1 >= hexTime0 
#    both in one day
function getLogicalDeltaTime {
    local hexTime0=$1
    local hexTime1=$2

    local halfPoint=`getMidSuspensionTime $hexTime0`
    local halfPoint2=`getMidOpenQuotationTime $hexTime0`
    #echo $halfPoint $halfPoint2

    [[ 0x$hexTime0 -le 0x$halfPoint ]] &&  [[ 0x$hexTime1 -le 0x$halfPoint ]] && {
        echo $((0x$hexTime1-0x$hexTime0))
        return
    }
    [[ 0x$hexTime0 -ge 0x$halfPoint2 ]] &&  [[ 0x$hexTime1 -ge 0x$halfPoint2 ]] && {
        echo $((0x$hexTime1-0x$hexTime0))
        return
    }
    [[ 0x$hexTime0 -le 0x$halfPoint ]] &&  [[ 0x$hexTime1 -ge 0x$halfPoint2 ]] && {
        echo $((0x$hexTime1-0x$hexTime0-5400))
        return
    }
}

