#!/bin/bash

function makeNegTable {
    local fTxt=$1

    local a b c d
    awk '$15<1.5' $fTxt|while read a b c d; 
    do
        c=${c%:}
        echo "        list.add(new SAm(\"$a\", \"$b\", \"$c\"));"
    done 
}

function makePosTable {
    local fTxt=$1

    local a b c d
    awk '$15>=1.5' $fTxt|while read a b c d; 
    do
        c=${c%:}
        echo "        list.add(new SAm(\"$a\", \"$b\", \"$c\"));"
    done 
}

function getSubTxt {
    local fTxt=$1
    local start=$2
    local end=$3

    sort -nk4,4 $fTxt |awk "\$4>=$start&&\$4<$end"|sort -nk15,15
}

function makeSAm2Table {
    local fTxt=$1

    local fTmp=`mktemp`


    for i in 0_100 100_270 270_360 360_450 450_620 620_1300 
    do
        local start end
        start=${i%_*}
        end=${i#*_}
        getSubTxt $fTxt $start $end >$fTmp
        printf "negTable %8s %8s:\n" $start $end
        makeNegTable $fTmp
        printf "posTable %8s %8s:\n" $start $end
        makePosTable $fTmp
    done

    rm -rf $fTmp
}
