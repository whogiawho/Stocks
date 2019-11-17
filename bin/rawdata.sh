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

    local hexinProtocolJar="$rootDir\\build\\jar\\hexinprotocol.jar"
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
    local startHMS=$6
    local endHMS=$7

    serverAddr=${serverAddr:-$DefaultServerAddr}
    serverPort=${serverPort:-$DefaultServerPort}
    serverType=${serverType:-$DefaultServerType}
    local hexinProtocolJar="$rootDir\\build\\jar\\hexinprotocol.jar"

    #generate "d:\stocks\data\pankoumedium\$stockCode.$tradeDate.txt"
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

    serverAddr=${serverAddr:-$DefaultServerAddr}
    serverPort=${serverPort:-$DefaultServerPort}
    serverType=${serverType:-$DefaultServerType}
    local hexinProtocolJar="$rootDir\\build\\jar\\hexinprotocol.jar"
    local callAuctionTime=$CallAuctionEndTime
    local closeQuotationTime=$CloseQuotationTime

    #generate "d:\stocks\data\zubimingximedium\$stockCode.$tradeDate.txt"
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

    local stockList="600030 600036 601318 510300 510900"
    local stockCode=
    for stockCode in $stockList
    do
        getRaw $stockCode $tradeDate $serverAddr $serverPort $serverType
    done
}


function getRaw {
    local stockCode=$1
    local tradeDate=$2
    local serverAddr=$3
    local serverPort=$4
    local serverType=$5

    getRawPankou $stockCode $tradeDate $serverAddr $serverPort $serverType
    getRawTradeDetails $stockCode $tradeDate $serverAddr $serverPort $serverType
}


