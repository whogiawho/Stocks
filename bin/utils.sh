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


function isRealNumber {
    local sReal=$1

    local val=1
    [[ $sReal =~ ^[0-9]*.[0-9]+$ ]] && val=0

    return $val
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
    scriptLines=`find $rootDirCygdrive/bin/ $rootDirCygdrive/vbs/ -name *.bat -o -name *.sh -o -name *.vbs \
        |grep -v backup|grep -v test|grep -v obso|xargs wc|grep -v "$rootDirCygdrive/"|awk '{print $1}'`

    local idcLines=
    idcLines=`find $thsHackRootDirCygdrive/scripts/  -name *.idc \
        |grep -v backup | xargs wc|grep -v "$rootDirCygdrive/"|awk '{print $1}'`

    local pyLines=
    pyLines=`find $rootDirCygdrive/python/  -name *.py \
        |grep -v backup | xargs wc|grep -v "$rootDirCygdrive/"|awk '{print $1}'`

    local sFormat="%8s %8s %15s %8s %8s\n"
    printf "$sFormat" "Java" "C" "bash&bat&vbs" "idc" "python"
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
function getListMin {
    local fList=$1
    local colN=$2

    awk "{print \$$colN}" $fList|sort -n|head -n1
}
function getListMax {
    local fList=$1
    local colN=$2

    awk "{print \$$colN}" $fList|sort -n|tail -n1
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
    rm -rf $TMP/tmp* $TMP/sh-thd.* $TMP/*.TMP
    rm -rf $TMP/Diagnostics $TMP/PhotoCache $TMP/PowerQuery $TMP/VBE
    rm -rf $TMP/\{*
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
    local hmsList=$1
    local stockCode=$2
    local tradeDate=$3

    [[ -z $stockCode ]] && stockCode=600030
    [[ -z $tradeDate ]] && tradeDate=`date +%Y%m%d`
    local start end
    read start end<<<`java -jar $analyzetoolsJar getabs $stockCode $tradeDate $hmsList 2>/dev/null`

    echo $((end-start))
}

macrosJobId=
function startMacros {
    excel "$rootDir\\doc\\macros.xlsm" &
    macrosJobId=$!
    echo macrosJobId=$macrosJobId
}
function closeMacros {
    cscript "$rootDir\\vbs\\closeMacros.vbs"
}


function cecho(){
    RED="\033[0;31m"
    GREEN="\033[0;32m"
    YELLOW="\033[1;33m"
    # ... ADD MORE COLORS
    NC="\033[0m" # No Color

    printf "${!1}${2} ${NC}\n"
}
function beep {
    local cnt=$1

    local i
    for i in `seq $cnt`
    do
        echo $'\a'
        uSleep 300000
    done
}

#    note: dstdir&srcDir must be in one dir
#dstDir - dir where symlink files are
#srcDir - dir where source files are
function lnDirFiles {
    local srcDir=$1
    local dstDir=$2

    local i
    for i in `ls $srcDir`; 
    do 
        cmd /C "mklink $dstDir\\$i ..\\$srcDir\\$i"; 
    done
}
