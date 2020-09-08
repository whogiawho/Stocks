#!/bin/bash

function getAbs {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3

    java -jar $analyzetoolsJar getabs $stockCode $tradeDate $hms 2>/dev/null
}

function rgetAbs {
    local stockCode=$1
    local sd=$2
    
    java -jar $analyzetoolsJar rgetabs $stockCode $sd 2>/dev/null
}

function rgetRel {
    local rel=$1
    local bComplex=$2

    local line=`java -jar $analyzetoolsJar rgetrel $rel 2>/dev/null`
    [[ -z $bComplex ]] && {
        echo $line|sed "s/://g"
    } || {
        echo $line
    }
}
