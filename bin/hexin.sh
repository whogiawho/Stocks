#!/bin/bash

function getHexinServerList {
    local loginParmsDir="$dataRoot\\loginParms"

    local fTmp1=`mktemp`
    ls -lt $loginParmsDir|grep txt >$fTmp1

    local latestDate=`cat $fTmp1|tail -n +2|head -n 1|awk '{printf "%s *%s\n",$6,$7}'`

    local fTmp2=`mktemp`
    cat $fTmp1|grep "$latestDate"|awk '{print $9}'|sed 's/.txt//g'|sed 's/_/ /g' >$fTmp2

    printf "%15s %10s %10s %8s %8s\n" "serverAddr" "serverPort" "serverType" "part401" "part081"
    local line=
    while read line
    do
        local serverAddr=
        local serverPort=
        local serverType=
        read serverAddr serverPort serverType<<< `echo $line`
        local sPart401File=`getPart401File $serverAddr $serverPort $serverType`
        local sPart081File=`getPart081File $serverAddr $serverPort $serverType`
        local out=`printf "%15s %10s %10s" $serverAddr $serverPort $serverType`
        local sPart401=" "
        [[ -f "$sPart401File" ]] && {
            sPart401=X
        }
        out=`printf "%s %8s" "$out" "$sPart401"`
        local sPart081=" "
        [[ -f "$sPart081File" ]] && {
            sPart081=X
        }
        out=`printf "%s %8s" "$out" "$sPart081"`
        printf "%s\n" "$out"
    done < $fTmp2 |sort -rk4,4

    rm -rf $fTmp1 $fTmp2
}

function getPart401File {
    local serverAddr=$1
    local serverPort=$2
    local serverType=$3

    local loginParmsDir="$dataRoot\\loginParms"
    local sPart401File="$loginParmsDir\\${serverAddr}_${serverPort}_$serverType.part401"

    echo $sPart401File
}
function getPart081File {
    local serverAddr=$1
    local serverPort=$2
    local serverType=$3

    local loginParmsDir="$dataRoot\\loginParms"
    local sPart081File="$loginParmsDir\\${serverAddr}_${serverPort}_$serverType.part081"

    echo $sPart081File
}

function checkHexinServer {
    #get server(Addr, Port, Type)
    local serverAddr=
    local serverPort=
    local serverType=
    local others=
    local line=`getHexinServerList |grep -E "56000001|56000000" |head -n 1`
    [[ ! -z $line ]] && {
        read serverAddr serverPort serverType others <<<`echo $line`
        echo $serverAddr:$serverPort:$serverType
        return 0
    } || {
        printf "checkHexinServer: %s\n" "there is no valid server!"
        return 1
    }
}

