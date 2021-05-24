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
function getRel {
    local stockCode=$1
    local hmsList=$2

    java -jar $analyzetoolsJar getrel $stockCode $hmsList 2>/dev/null
}

function deltaHMS {
    local stockCode=$1
    local baseHMS=$2
    local delta=$3

    local rel=`getRel $stockCode $baseHMS`
    rel=$((rel+delta))
    rgetRel $rel
}

function deltaYMDHMS {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local delta=$4

    local abs=`getAbs $stockCode $tradeDate $hms`
    local nextAbs=$((abs+delta))

    local hexTp=`rgetAbs $stockCode $nextAbs`
    convertHex2Time $hexTp y
}

function rgetYMDHMS { 
    local stockCode=$1
    local sd=$2

    local hexTp=`rgetAbs $stockCode $sd`
    convertHex2Time $hexTp y|sed "s/,/ /g"
}
