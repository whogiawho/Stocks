#!/bin/bash


DefaultServerAddr=61.136.59.237
DefaultServerPort=8601
DefaultServerType=56000001


#rawPankou&rawTradeDetails realtime
function getInstantData {
    local stockCode=$1
    local tradeDate=$2
    local serverAddr=$3
    local serverPort=$4
    local serverType=$5

    while [[ 1 ]] 
    do
        java -jar $hexinProtocolJar command4 $serverAddr $serverPort $serverType $stockCode $tradeDate

        local hexTime=`currentHexTime`
        local closeQuotationTime=`getCloseQuotationTime $hexTime`
        [[ $hexTime > $closeQuotationTime ]] && break
    done
}

#rawPankou
function getRawPankou {
    local stockCode=$1
    local tradeDate=$2
    local serverAddr=$3
    local serverPort=$4
    local serverType=$5
    local bHMSCleared=$6
    local noCheck=$7

    local callAuctionTime=$CallAuctionEndTime
    local closeQuotationTime=$CloseQuotationTime
    [[ ! -z $bHMSCleared ]] && {
        callAuctionTime=
        closeQuotationTime=
    }

    local sFormat="getRawPankou: ($stockCode, $tradeDate) %20s\n"
    while [[ 1 ]]
    do
        _getRawPankou $stockCode $tradeDate $serverAddr $serverPort $serverType \
            $callAuctionTime $closeQuotationTime
        [[ ! -z $noCheck ]] && break
        checkRawPankou $stockCode $tradeDate && {
            printf "$sFormat" "ok!"
            break
        } || {
            printf "$sFormat" "exception! retrying ..."
        }
    done
}
function _getRawPankou {
    local stockCode=$1
    local tradeDate=$2
    local serverAddr=$3
    local serverPort=$4
    local serverType=$5
    local startHMS=$6
    local endHMS=$7

    serverAddr=${serverAddr:-$DefaultServerAddr}
    serverPort=${serverPort:-$DefaultServerPort}
    serverType=${serverType:-$DefaultServerType}

    #generate $dataRoot"\pankoumedium\$stockCode.$tradeDate.txt"
    java -jar $hexinProtocolJar command2 $serverAddr $serverPort $serverType \
        $stockCode $tradeDate $startHMS $endHMS

    local pankouDataDir="$rawPankouDataDir\\$stockCode\\$tradeDate\\pankou"
    [[ ! -e $pankouDataDir ]] && {
        mkdir -p $pankouDataDir
    }
    #convert medium to $stockCode\$tradeDate\pankou\$stockCode.$tradeDate.txt
    getrawpankou.exe $stockCode $tradeDate
}

#rawTradeDetails
function getRawTradeDetails {
    local stockCode=$1
    local tradeDate=$2
    local serverAddr=$3
    local serverPort=$4
    local serverType=$5
    local noCheck=$6

    local sFormat="getRawTradeDetails: ($stockCode, $tradeDate) %20s\n"
    while [[ 1 ]]
    do
        _getRawTradeDetails $stockCode $tradeDate $serverAddr $serverPort $serverType
        [[ ! -z $noCheck ]] && break
        checkRawTradeDetails $stockCode $tradeDate && {
            printf "$sFormat" "ok!"
            break
        } || {
            printf "$sFormat" "exception! retrying ..."
        }
    done
}
function _getRawTradeDetails {
    local stockCode=$1
    local tradeDate=$2
    local serverAddr=$3
    local serverPort=$4
    local serverType=$5

    serverAddr=${serverAddr:-$DefaultServerAddr}
    serverPort=${serverPort:-$DefaultServerPort}
    serverType=${serverType:-$DefaultServerType}
    local callAuctionTime=$CallAuctionEndTime
    local closeQuotationTime=$CloseQuotationTime

    #generate $dataRoot"\zubimingximedium\$stockCode.$tradeDate.txt"
    java -jar $hexinProtocolJar command1 $serverAddr $serverPort $serverType \
        $stockCode $tradeDate $callAuctionTime $closeQuotationTime

    local tradeDetailsDir="$rawZuBiDataDir\\$stockCode"
    [[ ! -e $tradeDetailsDir ]] && {
        mkdir -p $tradeDetailsDir
    }
    #convert medium to rawTradeDetails\$stockCode\$stockCode.$tradeDate.txt
    getrawtradedetails.exe $stockCode $tradeDate
}

function doDailyGetRawJob {
    local tradeDate=$1
    local serverAddr=$2
    local serverPort=$3
    local serverType=$4
    local noCheck=$5

    local stockList="600030 600036 601318 600196 510300 510900 512880"
    local stockCode=
    for stockCode in $stockList
    do
        getRaw $stockCode $tradeDate $serverAddr $serverPort $serverType $noCheck
    done

    [[ ! -z $noCheck ]] && return 

    echo 
    for stockCode in $stockList
    do
        local sFormat="doDailyGetRawJob: ($stockCode, $tradeDate) %20s %10s!\n"

        checkRawPankou $stockCode $tradeDate && {
            printf "$sFormat" "rawPankou" "ok"
        } || {
            printf "$sFormat" "rawPankou" "exception"
        }

        checkRawTradeDetails $stockCode $tradeDate && {
            printf "$sFormat" "rawTradedetails" "ok"
        } || {
            printf "$sFormat" "rawTradedetails" "exception"
        }
    done
}


function getRaw {
    local stockCode=$1
    local tradeDate=$2
    local serverAddr=$3
    local serverPort=$4
    local serverType=$5
    local noCheck=$6

    getRawPankou $stockCode $tradeDate $serverAddr $serverPort $serverType hmsCleared $noCheck
    getRawTradeDetails $stockCode $tradeDate $serverAddr $serverPort $serverType $noCheck
}


