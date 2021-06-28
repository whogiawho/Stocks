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
        local line=`getLSProfit $stockCode $a $b -m1`; 
        local nd=`getNextTradeDate $stockCode $a`; 
        local correl=`avgamCorrel $stockCode $nd 092500 $a 150000`; 
        local cp=`getCloseQuotationPrice $stockCode $a`; 
        local op=`getOpenQuotationPrice $stockCode $nd`; 
        printf "%s %s %8s %s %8s %8s %8s\n"  $a $b $correl "$line" $cp $op `substract $op $cp`; 
    done|sort -nk8,8
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
    local options=$6

    local correl=
    correl=`java -jar $analyzetoolsJar avgamcorrel $options $stockCode $tradeDate0 $hms0 $tradeDate1 $hms1 2>/dev/null`

    echo $correl
}
function _avgamCorrel {
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

function getTmpAvgAmPNPercent {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional
    local bSaveTxt=$7                         #optional

    local avgamName=${hms}_${bwsd}_${minDist}_${interval}
    local avgamtxtDir="$dailyDir\\$stockCode\\$tradeDate\\avgamTxt"
    local avgamTxt="$avgamtxtDir\\$avgamName.txt"

    [[ ! -e $avgamTxt ]] && {
        makeAvgAmTxt $stockCode $tradeDate $hms $avgamTxt $bwsd $minDist $interval
    }
    local lines=`wc $avgamTxt|awk '{print $1}'`
    local posLines=`awk '$2>0' $avgamTxt|wc|awk '{print $1}'`
    local negLines=`awk '$2<0' $avgamTxt|wc|awk '{print $1}'`
    local posR=`divide $posLines $lines`
    local negR=`divide $negLines $lines`
    [[ -z $bSaveTxt ]] && rm -rf $avgamTxt

    echo $posR $negR
}
function makeTmpAvgAmPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional
    local bSaveTxt=$7                         #optional

    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    local avgamtxtDir="$dailyDir\\$stockCode\\$tradeDate\\avgamTxt"
    [[ ! -e "$avgamtxtDir" ]] && {
        mkdir -p "$avgamtxtDir"
    }

    local avgamName=${hms}_${bwsd}_${minDist}_${interval}
    local avgamTxt="$avgamtxtDir\\$avgamName.txt"
    makeAvgAmTxt $stockCode $tradeDate $hms $avgamTxt $bwsd $minDist $interval

    local sPngFile="$avgamtxtDir\\$avgamName.png"
    makeAvgAmPngFromFile "$avgamTxt" "$sPngFile"

    [[ -z $bSaveTxt ]] && rm -rf $avgamTxt
}
function openTmpAvgAmPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional

    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    local avgamName=${hms}_${bwsd}_${minDist}_${interval}
    local avgamtxtDir="$dailyDir\\$stockCode\\$tradeDate\\avgamTxt"
    local avgamPng="$avgamtxtDir\\$avgamName.png"
    JPEGView.exe "$avgamPng" &
}
function viewTmpAvgAmPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional

    makeTmpAvgAmPng $stockCode $tradeDate $hms $bwsd $minDist $interval
    openTmpAvgAmPng $stockCode $tradeDate $hms $bwsd $minDist $interval
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


function makeAvgAmPng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional

    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    makeAvgAm $stockCode $tradeDate $hms $bwsd $minDist $interval 

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
    local bwsd=$4                             #optional
    local minDist=$5                          #optional
    local interval=$6                         #optional 

    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

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
    local bwsd=$5                             #optional
    local minDist=$6                          #optional
    local interval=$7                         #optional 

    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

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
    local bwsd=$3
    local minDist=$4
    local interval=$5

    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1

    local options="-b$bwsd -m$minDist -i$interval -s$tradeDate -e$tradeDate"
    local a b c d
    java -jar $analyzetoolsJar avgamdelta $options $stockCode 2>/dev/null|awk '$4<0.85'|while read a b c d; 
    do 
        makeTmpAvgAmPng $a $b $c $bwsd $minDist $interval y; 
    done
}
function dailyMakeAvgamPngs {
    local stockCode=$1
    local sDate=$2
    local eDate=$3
    local bwsd=$4                   #optional
    local minDist=$5                #optional
    local interval=$6               #optional
    local step=$7                   #optional

    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=60
    [[ -z $interval ]] && interval=1
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
        java -jar $analyzetoolsJar listavgams -b$bwsd -m$minDist -i1 -e$step $stockCode $i

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
    local stockCode=$1
    local fQr=$2
    local option=$3

    local max=10
    local cnt=0
    local a b c
    cat $fQr|sed "s/_/ /g"|while read a b c; do 
        sAvgAm $stockCode $a $b $option &

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
        local line=`getLSProfit $stockCode $a $hms "$option" 2>/dev/null`; 
        printf "%s %s %s %s %s\n" $stockCode $tradeDate $hms $a "$line"; 
    done

    rm -rf $fTmp
}


function makeAvgAmFromDelta {
    local stockCode=$1     #in
    local fDelta=$2        #in
    local avgamDir=$3      #out
    local options=$4       #in

    [[ ! -e $avgamDir ]] && mkdir -p $avgamDir

    fDelta=`getWindowPathOfFile $fDelta`
    avgamDir=`getWindowPathOfFile $avgamDir`
    java -jar $analyzetoolsJar listavgams $options -f"$fDelta" -d"$avgamDir" $stockCode 
}
#w matlab by options="-m", or w/o matlab by options w/o "-m"
#$avgamDir.res - out
function makeAvgAmRes {
    local fDelta=$1        #in
    local avgamDir=$2      #in
    local options=$3       #in

    fDelta=`getWindowPathOfFile $fDelta`
    avgamDir=`getWindowPathOfFile $avgamDir`
    java -jar $analyzetoolsJar simavgamdelta $options -f"$fDelta" -d"$avgamDir" 2>/dev/null
}

function avgamInstance {
    local stockCode=$1
    local bwsd=$2
    local minDist=$3
    local interval=$4
    local dc=$5
    local sc=$6

    [[ -z $bwsd ]] && bwsd=1170
    [[ -z $minDist ]] && minDist=1
    [[ -z $interval ]] && interval=1
    [[ -z $dc ]] && dc=0.85
    [[ -z $sc ]] && sc=0.90

    local instanceRootDir=/tmp/$stockCode/avgam/b${bwsd}md${minDist}i${interval}dc${dc}sc${sc}
    [[ ! -e $instanceRootDir ]] && mkdir -p $instanceRootDir

    #make full.delta
    local startDate=`getTradeDateList $stockCode y|head -n2|tail -n1`
    local endDate=`getTradeDateList $stockCode y|tail -n1`
    local sFull=$instanceRootDir/full.delta
    java -jar $analyzetoolsJar avgamdelta -s$startDate -e$endDate \
        -b$bwsd -m$minDist -i$interval $stockCode >$sFull

    #make name.delta${dc}
    local name=20201
    local yearPattern=" 202[01].... "
    local postfix=`echo $dc|sed "s/0\.//g"`
    postfix=`printf "%02d\n" $postfix`
    local sNameDelta=$instanceRootDir/$name.delta$postfix
    awk "\$4<$dc" $sFull | grep "$yearPattern" >$sNameDelta
    #check if lines of sNameDelta<15000
    local lines=`wc $sNameDelta|awk '{print $1}'`
    [[ $lines -gt 15000 ]] && {
        echo "lines of $sNameDelta=$lines"
        return 1
    }

    local options="-b$bwsd -m$minDist -i$interval"
    local avgamDir=$instanceRootDir/$name
    makeAvgAmFromDelta $stockCode $sNameDelta $avgamDir "$options"

    makeAvgAmRes $sNameDelta $avgamDir

    local mc=10
    local statsOptions="-m$mc"
    local avgamResDir=$instanceRootDir/$name.res
    local fStats=$instanceRootDir/$name.m10.stats
    makeAvgAmStats $sNameDelta $avgamResDir $fStats "$statsOptions"
}

function emulateAvgAm {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local tradeType=$4
    local options=$5

    local fTmp=`mktemp`
    java -jar $analyzetoolsJar saadstats $options $stockCode,$tradeDate,$hms 2>/dev/null >$fTmp

    emulateDSS $fTmp $tradeType

    rm -rf $fTmp
}
