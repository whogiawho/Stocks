#!/bin/bash

function getWindowPathOfTmpFile {
    local tmpCygwinPath=$1

    echo $cygwinTmpDir\\`basename $tmpCygwinPath`
}
function getWindowPathOfFile {
    local fileCygdrive=$1


    local file=
    file=`basename $fileCygdrive`
    local dir=
    dir=`dirname $fileCygdrive`
    echo $dir|grep -q "\/cygdrive" && {
        dir=`echo $dir|sed 's@\/cygdrive@@g'`
        drive=${dir:1:1}
        dir=`echo $dir|sed "s@^\/$drive@$drive:@g"`
        dir=`echo $dir|sed 's@/@\\\@g'`
        echo $dir\\$file        
    } || {
        dir=`echo $dir|sed 's@/@\\\@g'`
        [[ $dir = '\' ]] && dir=
        echo "$cygwinRootDir\\$dir\\$file"
    }
}


function divide {
    local f0=$1
    local f1=$2
    local scale=$3

    [[ -z $scale ]] && scale=3

    local result=
    result=`echo "scale=$scale; $f0 / $f1"|bc`

    echo $result

}
function mul {
    local f0=$1
    local f1=$2

    local result=
    result=`echo "$f0 * $f1"|bc`

    echo $result
}
function add {
    local f0=$1
    local f1=$2

    local result=
    result=`echo "$f0 + $f1"|bc`

    echo $result
}
function substract {
    local f0=$1
    local f1=$2

    local result=
    result=`echo "$f0 - $f1"|bc`

    echo $result
}

function gt {
    local f0=$1
    local f1=$2

    local bCompare=
    bCompare=`echo "$f0 > $f1"|bc`

    echo $bCompare
}
function ge {
    local f0=$1
    local f1=$2

    local bCompare=
    bCompare=`echo "$f0 >= $f1"|bc`

    echo $bCompare
}
function lt {
    local f0=$1
    local f1=$2

    local bCompare=
    bCompare=`echo "$f0 < $f1"|bc`

    echo $bCompare
}
function le {
    local f0=$1
    local f1=$2

    local bCompare=
    bCompare=`echo "$f0 <= $f1"|bc`

    echo $bCompare
}


function getCodeStats {
    local javaLiines=
    javaLiines=`find $rootDirCygdrive/java $thsHackRootDirCygdrive/java -name *.java|xargs wc \
        |grep -v "$rootDirCygdrive/"|awk '{print $1}'`

    local cLines=
    cLines=`find $thsHackRootDirCygdrive/getRaw $thsHackRootDirCygdrive/uSleep -name *.c -o -name *.h|xargs wc \
        |grep -v "$rootDirCygdrive/"|awk '{print $1}'`

    local scriptLines=
    scriptLines=`find $rootDirCygdrive/bin/  -name *.bat -o -name *.sh \
        |grep -v backup|grep -v test|grep -v obso|xargs wc|grep -v "$rootDirCygdrive/"|awk '{print $1}'`

    local idcLines=
    idcLines=`find $thsHackRootDirCygdrive/scripts/  -name *.idc \
        |grep -v backup | xargs wc|grep -v "$rootDirCygdrive/"|awk '{print $1}'`

    local pyLines=
    pyLines=`find $rootDirCygdrive/python/  -name *.py \
        |grep -v backup | xargs wc|grep -v "$rootDirCygdrive/"|awk '{print $1}'`

    local sFormat="%8s %8s %12s %8s %8s\n"
    printf "$sFormat" "Java" "C" "bash&bat" "idc" "python"
    printf "$sFormat" $javaLiines $cLines $scriptLines $idcLines $pyLines
}

function killHexin {
    taskkill.exe  /S localhost /IM hexin.exe
    taskkill.exe  /S localhost /IM sjsj.exe 
    taskkill.exe  /S localhost /IM zdsj.exe 
}

function abs {
    local value=$1

    [[ $value -lt 0 ]] && value=$((value*-1))

    echo $value
}

function getListAvg {
    local fList=$1
    local colN=$2

    awk "BEGIN{count=0; sum=0}{sum+=\$$colN; count++;}END{avg=sum/count; printf \"%8.3f\",avg}" $fList
}
function getListSum {
    local fList=$1
    local colN=$2

    awk "BEGIN{count=0; sum=0}{sum+=\$$colN; count++;}END{printf \"%8.3f\",sum}" $fList
}
function getListStdDev {
    local fList=$1
    local colN=$2

    awk "{sum+=\$$colN; sumsq+=\$$colN*\$$colN} END {printf \"%8.3f\",sqrt(sumsq/NR-(sum/NR)**2)}" $fList
}

BuyStockServiceRate=${BuyStockServiceRate:-0.0003}
SellStockServiceRate=${SellStockServiceRate:-0.0003}
SellStockTaxRate=${SellStockTaxRate:-0.001}
function getTradeCost {
    local buyPrice=$1
    local sellPrice=$2

    local cost=
    local cost0=`mul $buyPrice $BuyStockServiceRate`
    local cost1=`mul $sellPrice $SellStockServiceRate`
    local cost2=`mul $sellPrice $SellStockTaxRate`
    cost=`add $cost0 $cost1`
    cost=`add $cost $cost2`

    echo $cost
}
function getNetProfit {
    local buyPrice=$1
    local sellPrice=$2

    local netProfit=
    local profit0=`substract $sellPrice $buyPrice`
    local profit1=`getTradeCost $buyPrice $sellPrice`
    netProfit=`substract $profit0 $profit1`

    echo $netProfit
}


function cleardmp {
    hexinRoot=$thsRootDirCygdrive
    set -x
    rm -rf $hexinRoot/*.dmp
    set +x
}
function cleartmp {
    set -x
    rm -rf $TMP/tmp* $TMP/sh-thd.*
    set +x
}

function getFilesWBaseRoot {
    local rootKey=$1          #something like "d:\\\\"

    grep -ri "$rootKey" python/ thsHack/ java/ bin/| \
        grep -v 匹配到二进制文件| \
        grep -v "txt:"| \
        awk -F: '{print $1}'|uniq
}


function checkProcKilled {
    local psKey=$1

    while [[ 1 ]] 
    do
        ps|grep -q $psKey && {
            sleep 1
        } || {
            break
        }
    done
}

function getHmsListDelta {
    local stockCode=$1
    local tradeDate=$2
    local hmsList=$3

    local start end
    read start end<<<`java -jar build/jar/analyzetools.jar getabs $stockCode $tradeDate $hmsList 2>/dev/null`

    echo $((end-start))
}

#If 3 parms, startHMS is of format HHMMSS_HHMMSS
#if 4 parms, startHMS&endHMS is of format HHMMSS
function viewTradeDateAmRange {
    local stockCode=$1
    local tradeDate=$2
    local startHMS=$3
    local endHMS=$4

    [[ -z $endHMS ]] && {
        endHMS=${startHMS#*_}
        startHMS=${startHMS%_*}
    }
    #convert startHMS to startHexTp
    local startHexTp=`convertTime2Hex $tradeDate $startHMS`
    #convert endHMS to endHexTp
    local endHexTp=`convertTime2Hex $tradeDate $endHMS`

    cscript.exe "$rootDir\\bin\\viewTradeDateRange.vbs" $stockCode $tradeDate $startHexTp $endHexTp $startHMS $endHMS
}

