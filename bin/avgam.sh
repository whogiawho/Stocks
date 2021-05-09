#!/bin/bash

fAvgAmAllTable=${fAvgAmAllTable:-~/analysis/avgamAllTable.txt}

function checkAvgAmTable {
    local stockCode=$1
    local tradeDate=$2

    local sNull="\"\""
    local fTmp0=`mktemp`
    local fTmp1=`mktemp`
    java -jar $analyzetoolsJar avgamdelta -s$tradeDate -e$tradeDate $stockCode 2>/dev/null|awk '$4<0.85' >$fTmp0

    local dirAvgAmTable="data/avgamTable"
    local a b c d
    cat $fTmp0|while read a b c d
    do
        local aatFile
        for aatFile in `ls $dirAvgAmTable`
        do
            cat $dirAvgAmTable/$aatFile|grep -v "stockCode"|grep -v "^$" >$fTmp1

            local e f g sHMS eHMS tradeType k l maxCycle n scThres TSwitch
            cat $fTmp1 | while read e f g sHMS eHMS tradeType k l maxCycle n scThres TSwitch; do
                [[ $sHMS != $sNull  && $c < $sHMS ]] && continue;
                [[ $eHMS != $sNull  && $c > $eHMS ]] && continue;

                local correl=`java -jar $analyzetoolsJar avgamcorrel $stockCode $b $c $f $g 2>/dev/null`
                local bCmp=`ge $correl $scThres`
                [[ $bCmp == 1 ]] && {
                    local inPrice=`getInPrice $stockCode $b $c $tradeType`
                    local tName=`echo $aatFile|sed "s/.txt//g"`
                    local sFormat="%-10s %s %s %s %8s | %s %s %8s %4s %8s %4s %8s\n"
                    printf "$sFormat" $tName $stockCode $b $c $scThres $f $g $correl $tradeType $inPrice $maxCycle $l
                }
            done
        done
    done

    rm -rf $fTmp0 $fTmp1
}
function avgamPredictByLast20m {
    local stockCode=$1
    local tradeDate=$2
    local threshold=$3

    [[ -z $threshold ]] && threshold=0.9

    local fTmp=`mktemp`
    local hms=145956; 
    local i
    for i in `getTradeDateList $stockCode`; 
    do 
        local correl=`avgamCorrel $stockCode $tradeDate $hms $i $hms`; 
        echo $i $hms $correl; 
    done|tee $fTmp

    _avgamPredictByLast20m $stockCode $tradeDate $fTmp $threshold

    mv $fTmp $TMP/aapbl20m.txt
}
function _avgamPredictByLast20m {
    local stockCode=$1
    local tradeDate=$2
    local sInFile=$3
    local threshold=$4

    [[ -z $threshold ]] && threshold=0.9

    local a b c
    awk '$3>threshold' threshold=$threshold $sInFile |grep -v NaN|grep -v $tradeDate|while read a b c; 
    do 
        local line=`getLSProfit $stockCode $a $b m1`; 
        local nd=`getNextTradeDate $stockCode $a`; 
        local correl=`avgamCorrel $stockCode $nd 092500 $a 150000`; 
        local cp=`getCloseQuotationPrice $stockCode $a`; 
        local op=`getOpenQuotationPrice $stockCode $nd`; 
        printf "%s %s %8s %s %8s %8s %8s\n"  $a $b $correl "$line" $cp $op `substract $op $cp`; 
    done|sort -nk8,8
}

function prevAACkpt {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4               #optional

    local iOption=
    [[ ! -z $interval ]] && iOption="-i $interval"

    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -jar $analyzetoolsJar nextaackpt -b $iOption $stockCode $tradeDate $hms 2>/dev/null
}
function _nextAACkpt {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4               #optional

    local nextHMS=`sed -n "/$hms/ {n;p}" "$dataRoot\\aackpti$interval.txt"`
    [[ -z $nextHMS ]] && {
        nextHMS=`head -n1 "$dataRoot\\aackpti$interval.txt"`
        tradeDate=`getNextTradeDate $stockCode $tradeDate`
    }

    echo $tradeDate $nextHMS
}
function nextAACkpt {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4               #optional

    local iOption=
    [[ ! -z $interval ]] && iOption="-i $interval"

    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -jar $analyzetoolsJar nextaackpt $iOption $stockCode $tradeDate $hms 2>/dev/null
}
function fileCorrel {
    local sFile0=$1
    local sFile1=$2
    local idxCol=$3
    local sdbw=$4    #optional

    local sdbwOption=
    [[ ! -z $sdbw ]] && sdbwOption="-b $sdbw"

    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -jar $analyzetoolsJar filecorrel $sdbwOption $sFile0 $sFile1 $idxCol 2>/dev/null; 
}
function avgamCorrel {
    local stockCode=$1
    local tradeDate0=$2
    local hms0=$3
    local tradeDate1=$4
    local hms1=$5

    local sFile0="$dailyDir\\$stockCode\\$tradeDate0\\avgam\\$hms0.txt"
    local sFile1="$dailyDir\\$stockCode\\$tradeDate1\\avgam\\$hms1.txt"
    local correl=`fileCorrel $sFile0 $sFile1 1`; 

    echo $correl
}

function makeTmpAvgAmPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4                         #optional
    local bwsd=$5                             #optional
    local minDist=$6                          #optional
    local bSaveTxt=$7                         #optional

    [[ -z $interval ]] && interval=60
    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60

    local avgamtxtDir="$dailyDir\\$stockCode\\$tradeDate\\avgamTxt"
    [[ ! -e "$avgamtxtDir" ]] && {
        mkdir -p "$avgamtxtDir"
    }

    local avgamTxt="$avgamtxtDir\\${tradeDate}_${hms}_${bwsd}_avgam.txt"
    makeAvgAmTxt $stockCode $tradeDate $hms $avgamTxt $interval $bwsd $minDist

    local sPngFile="$avgamtxtDir\\${tradeDate}_${hms}_${bwsd}_avgam.png"
    makeAvgAmPngFromFile "$avgamTxt" "$sPngFile"

    [[ -z $bSaveTxt ]] && rm -rf $avgamTxt
}
function openTmpAvgAmPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional

    [[ -z $bwsd ]] && bwsd=1170

    local avgamtxtDir="$dailyDir\\$stockCode\\$tradeDate\\avgamTxt"
    JPEGView.exe "$avgamtxtDir\\${tradeDate}_${hms}_${bwsd}_avgam.png" &
}

function openAvgAmPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3

    [[ -z $hms ]] && {
        local sep=_
        [[ $tradeDate =~ , ]] && sep=,

        hms=${tradeDate#*$sep}
        tradeDate=${tradeDate%$sep*}
    }

    local avgamPngDir="$dailyDir\\$stockCode\\$tradeDate\\avgamPng"
    JPEGView.exe "$avgamPngDir\\$hms.png" &
}
function viewAvgAmRange {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$5                             #optional

    [[ -z $bwsd ]] && bwsd=1170

    local startHMS=`deltaHMS $stockCode $hms -$bwsd`

    viewTradeDateRange $stockCode $tradeDate $startHMS $hms
}


function makeAvgAmPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4                         #optional
    local bwsd=$5                             #optional
    local minDist=$6                          #optional

    [[ -z $interval ]] && interval=60
    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60

    makeAvgAm $stockCode $tradeDate $hms $interval $bwsd $minDist

    local avgamPngDir="$dailyDir\\$stockCode\\$tradeDate\\avgamPng"
    local sPngFile="$avgamPngDir\\$hms.png"
    local avgamDir="$dailyDir\\$stockCode\\$tradeDate\\avgam"
    local inTxt="$avgamDir\\$hms.txt"
    makeAvgAmPngFromFile "$inTxt" "$sPngFile"
}
function makeAvgAm {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4                         #optional 
    local bwsd=$5                             #optional
    local minDist=$6                          #optional

    [[ -z $interval ]] && interval=60
    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60

    local avgamDir="$dailyDir\\$stockCode\\$tradeDate\\avgam"
    local outTxt="$avgamDir\\$hms.txt"
    java -jar $analyzetoolsJar listavgams -b$bwsd -m$minDist -i${interval} $stockCode $tradeDate $hms \
        >"$outTxt" 2>/dev/null
}
function makeAvgAmTxt {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local outTxt=$4
    local interval=$5                         #optional 
    local bwsd=$6                             #optional
    local minDist=$7                          #optional

    [[ -z $interval ]] && interval=60
    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60

    java -jar $analyzetoolsJar listavgams -b$bwsd -m$minDist -i${interval} $stockCode $tradeDate $hms \
        >"$outTxt" 2>/dev/null
}
function makeAvgAmPngs {
    local stockCode=$1
    local tradeDate=$2

    local avgamDir="$dailyDir\\$stockCode\\$tradeDate\\avgam"

    cscript.exe "$rootDir\\vbs\\makeAmDerivativePngs.vbs" "$avgamDir"
}
function makeAvgAmPngFromFile {
    local sInFile=$1
    local sOutPng=$2

    cscript.exe "$rootDir\\vbs\\makeAmDerivativePng.vbs" "$sInFile" "$sOutPng"
}

function dailyMakeTmpAvgamPngs {
    local stockCode=$1
    local tradeDate=$2
    local interval=$3
    local bwsd=$4
    local minDist=$5

    [[ -z $interval ]] && interval=1
    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60

    local options="-b$bwsd -m$minDist -i$interval -s$tradeDate -e$tradeDate"
    local a b c d
    java -jar $analyzetoolsJar avgamdelta $options $stockCode 2>/dev/null|awk '$4<0.85'|while read a b c d; 
    do 
        makeTmpAvgAmPng $a $b $c $interval $bwsd $minDist y; 
    done
}
function dailyMakeAvgamPngs {
    local stockCode=$1
    local sDate=$2
    local eDate=$3
    local interval=$4               #optional
    local bwsd=$5                   #optional
    local minDist=$6                #optional
    local step=$7                   #optional

    [[ -z $interval ]] && interval=1
    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60
    [[ -z $step ]] && step=60

    local tradeDates=`getTradeDateList $stockCode`
    [[ -z $eDate ]] && {
        eDate=${tradeDates##* }
    }
    [[ -z $sDate ]] && {
        sDate=`echo $tradeDates|sed "s/ /\n/g"|head -n6|tail -n1`
    }

    local i=
    for i in `getTradeDateRange $stockCode $sDate $eDate`
    do
        setupCfgFile $stockCode $i
        java -jar $analyzetoolsJar listavgams -b$bwsd -m60 -i1 -e$step $stockCode $i

        makeAvgAmPngs $stockCode $i
    done
}



function listMatchedAvgAm {
    local stockCode=$1 
    local tradeDate=$2
    local hms=$3
    local year=$4
    local threshold=$5
    
    [[ -z $threshold ]] && threshold=0.9

    local sFile0="$dailyDir\\$stockCode\\$tradeDate\\avgam\\$hms.txt"
    local j=
    for j in `getTradeDateRange $stockCode $year`; 
    do 
        echo $j
        local i=
        for i in `ls $dailyDir\\\\$stockCode\\\\$j\\\\avgam`; 
        do 
            local sFile1="$dailyDir\\$stockCode\\$j\\avgam\\$i"
            local correl=`fileCorrel $sFile0 $sFile1 1`; 
            isRealNumber $correl || continue

            local bCmp=`ge $correl $threshold`; 
            [[ $bCmp == 1 ]] && printf "%s %s %8.3f\n" $j $i $correl; 
        done; 
    done
}


function trackAACkpt {
    local stockCode=$1
    local tradeDate=$2
    local ckptInterval=$3                     #optional
    local interval=$4                         #optional 
    local bwsd=$5                             #optional
    local minDist=$6                          #optional

    [[ -z $ckptInterval ]] && ckptInterval=60
    [[ -z $interval ]] && interval=1
    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60

    local avgamDir="$dailyDir\\$stockCode\\$tradeDate\\avgam"
    [[ ! -e $avgamDir ]] && mkdir -p $avgamDir
    local avgamPngDir="$dailyDir\\$stockCode\\$tradeDate\\avgamPng"
    [[ ! -e $avgamPngDir ]] && mkdir -p $avgamPngDir

    #make sure analysisTxt exists
    local analysisTxt="$dailyDir\\$stockCode\\$tradeDate\\analysis.txt"
    while [[ 1 ]]
    do
        [[ ! -f $analysisTxt ]] && { sleep 1; continue; } || break;
    done

    local startAACkpt=092500
    local prevAACkpt0=`prevAACkpt $stockCode $tradeDate $startAACkpt $ckptInterval`
    local currentAACkpt="$tradeDate $startAACkpt"
    local currentHMS=`echo $currentAACkpt|awk '{print $2}'`
    local aackptTp=`convertTime2Hex $currentAACkpt`
    while [[ 1 ]]
    do
        local line=`tail -n1 $analysisTxt`
        local currentTp=`echo $line|awk '{print $1}'`
        [[ ! -z $currentTp && 0x$currentTp -ge 0x$aackptTp ]] && {
            #local time0=`currentHexTime`

            local outTxt="$avgamDir\\$currentHMS.txt"
            #makeAvgAmTxt for aackptTp
            makeAvgAmTxt $stockCode $currentAACkpt $outTxt $interval $bwsd $minDist
            #make png
            local sPngFile="$avgamPngDir\\${currentHMS}.png"
            (cscript.exe "$rootDir\\vbs\\makeAmDerivativePng.vbs" "$outTxt" "$sPngFile" 2>/dev/null|grep -v Micro &)

            #get correl with prev aaCkpt
            local correl0=`avgamCorrel $stockCode $currentAACkpt $prevAACkpt0`
            correl0=`colorCorrel "$correl0" 0.75`
            [[ $? == 0 ]] && { 
                (beep 100 &)
            }
            local upPrice=`_getUpPrice $stockCode $currentAACkpt`
            printf "%s %8s %8s\n" "$currentAACkpt" "$correl0" "$upPrice"

            #set prev aaCkpt
            prevAACkpt0="$currentAACkpt"
            #set next aaCkpt
            currentAACkpt=`_nextAACkpt $stockCode $currentAACkpt $ckptInterval`
            echo $currentAACkpt|grep "150000" && break;
            currentHMS=`echo $currentAACkpt|awk '{print $2}'`
            aackptTp=`convertTime2Hex $currentAACkpt`
            :

            #local time1=`currentHexTime`
            #echo "time0=$time0 time1=$time1 delta=$((0x$time1-0x$time0))"
        } || {
            #sleep 1
            :
        }
    done
}
function colorCorrel {
    local correl=$1
    local threshold=$2

    local rVal=1

    [[ ! -z $correl ]] && {
        local bCmp=`le $correl $threshold`
        [[ $bCmp == 1 ]] && {
            correl=`cecho RED $correl`
            rVal=0
        }
    }

    echo $correl
    return $rVal
}

function listAvgAmSeries {
    local stockCode=$1
    local year=$2

    local startDate=`getTradeDateRange $stockCode $year|head -n1`
    local endDate=`getTradeDateRange $stockCode $year|tail -n1`

    listAvgAmRange $stockCode $startDate $endDate 
}
function listAvgAmRange {
    local stockCode=$1
    local startDate=$2
    local endDate=$3

    local tradeDate; 
    for tradeDate in `getTradeDateRange $stockCode $startDate $endDate`
    do
        local i
        local avgamDir="$dailyDir\\$stockCode\\$tradeDate\\avgam"
        #echo avgamDir=$avgamDir
        for i in `ls $avgamDir|sed "s/.txt//g"`; 
        do 
            local prev=`prevAACkpt $stockCode $tradeDate $i`
            local correl=`avgamCorrel $stockCode $tradeDate $i $prev`; 
            local upprice=`getUpPrice $stockCode $tradeDate $i`; 
            printf "%s %s %8s %8s\n" $tradeDate $i $correl $upprice; 
        done
    done
}






function rangeAvgAmCorrel {
    local stockCode=$1
    local startDate=$2
    local endDate=$3
    local tradeDate=$4
    local hms=$5


    local max=2
    local cnt=0
    local i=
    for i in `getTradeDateRange $stockCode $startDate $endDate`
    do
        java -jar $analyzetoolsJar avgamcorrel -s$startDate -e$endDate $stockCode $tradeDate $hms 2>/dev/null &

        cnt=$((cnt+1))
        echo cnt=$cnt
        [[ $cnt -ge $max ]] && {
            wait 
            cnt=0
        }
    done
}
function sAvgAmFromQr {
    local fQr=$1
    local option=$2

    local max=10
    local cnt=0
    local a b c
    cat $fQr|sed "s/_/ /g"|while read a b c; do 
        sAvgAm 512880 $a $b $option &

        cnt=$((cnt+1))
        echo cnt=$cnt
        [[ $cnt -ge $max ]] && {
            wait 
            cnt=0
        }
    done
}
function sAvgAm {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local option=$4

    local fTmp=`mktemp`
    local sFile0="$dailyDir\\$stockCode\\$tradeDate\\avgam\\$hms.txt"
    local i
    for i in `getTradeDateList $stockCode`; 
    do 
        local sFile1="$dailyDir\\$stockCode\\$i\\avgam\\$hms.txt"
        local correl=`fileCorrel $sFile0 $sFile1 1`; 
        echo $i $correl; 
    done >$fTmp

    local a b
    awk '$2>0.9' $fTmp|grep -v NaN |while read a b; do 
        local line=`getLSProfit $stockCode $a $hms $option 2>/dev/null`; 
        printf "%s %s %s %s %s\n" $stockCode $tradeDate $hms $a "$line"; 
    done

    rm -rf $fTmp
}

