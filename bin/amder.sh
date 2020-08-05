#!/bin/bash

function tpAmDerStats {
    local fHMS=$1

    local i=0
    local start=-1
    local end=-1
    local slope=-1
    local line=
    while read line; do
        local a b
        read a b<<<`echo $line`; 
        local bCmp=`ge $a 0.9`; 
        [[ $start != -1 ]] && { 
            [[ $bCmp == 1 ]] && end=$((end+1)) || { 
                echo $fHMS,$start,$end,$slope; 
                start=-1; 
                end=-1; 
            } 
        } || { 
            [[ $bCmp == 1 ]] && { 
                start=$i; 
                end=$i; 
                slope=$b; 
            } 
        }; 
        i=$((i+1)); 
    done < $fHMS
    [[ $start != -1 ]] && {
        echo $fHMS,$start,$end,$slope; 
    }
}

function amDerStats {
    local stockCode=$1
    local tradeDate=$2

    local max=5
    local cnt=0
    local j=
    for j in $rootDirCygdrive/data/daily/$stockCode/$tradeDate/derivative/*
    do 
        #echo "tpAmDerStats $j"
        tpAmDerStats $j &
        cnt=$((cnt+1))
        echo cnt=$cnt
        [[ $cnt -ge $max ]] && {
            wait -n
            cnt=$((cnt-1))
        }
    done
}
