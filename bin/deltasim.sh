#!/bin/bash

function makeDeltaSimStats {
    local fDelta=$1        #in
    local resDir=$2   #in
    local stats=$3    #out
    local options=$4       #in

    fDelta=`getWindowPathOfFile $fDelta`
    resDir=`getWindowPathOfFile $resDir`
    java -jar $analyzetoolsJar saadstats $options -f"$fDelta" -d"$resDir"  |tee $stats
}
function makeAvgAmStats {
    local fDelta=$1        #in
    local avgamResDir=$2   #in
    local avgamStats=$3    #out
    local options=$4       #in

    makeDeltaSimStats $fDelta $avgamResDir $avgamStats $options
}
function makeAmVolRStats {
    local fDelta=$1        #in
    local amvolrResDir=$2  #in
    local amvolrStats=$3   #out
    local options=$4       #in

    makeDeltaSimStats $fDelta $avgamResDir $avgamStats $options
}


function makem0m1m10Stats {
    local rawStats=$1      #in
    local resDir=$2  #in
    local tradeType=$3     #in

    [[ -z $tradeType ]] && tradeType=5

    resDir=`getWindowPathOfFile $resDir`
    local colStats colSS
    [[ $tradeType == $LONG ]] && {
        colStats=7
        colSS=8
    } || {
        colStats=8
        colSS=9
    }

    local a b c d e f g h
    awk '$7>0.009&&$4>3' $rawStats |sort -nk$colStats,$colStats|while read a b c d e f g h; 
    do 
        local minmaxm10 minmaxm0 minmaxm1

        [[ $tradeType == $LONG ]] && minmaxm10=$g || minmaxm10=$h
        minmaxm0=`java -jar $analyzetoolsJar saadstats -d"$resDir" -m0 -h0.90 $a,$b,$c 2>/dev/null| \
            sort -nk$colSS,$colSS|head -n1|awk "{print \\$colSS}" colSS=$colSS`; 
        minmaxm1=`java -jar $analyzetoolsJar saadstats -d"$resDir" -m1 -h0.90 $a,$b,$c 2>/dev/null| \
            sort -nk$colSS,$colSS|head -n1|awk "{print \\$colSS}" colSS=$colSS`; 

        printf "%s %s %s %4s %8s %8s %8s\n" $a $b $c $d $minmaxm10 $minmaxm0 $minmaxm1; 
    done
}
function makeAvgAmm0m1m10Stats {
    local rawStats=$1      #in
    local avgamResDir=$2  #in
    local tradeType=$3     #in

    makem0m1m10Stats $rawStats $avgamResDir $tradeType
}
function makeAmVolRm0m1m10Stats {
    local rawStats=$1      #in
    local amvolrResDir=$2  #in
    local tradeType=$3     #in

    makem0m1m10Stats $rawStats $amvolrResDir $tradeType
}


function emulateDSS {
    local fDSS=$1
    local tradeType=$2

    local col=
    [[ $tradeType == $LONG ]] && {
        col=8
    } || {
        col=9
    }
    local minmaxProfit=`sort -nk$col,$col $fDSS|head -n1|awk '{print \$col}' col=$col`
    echo minmaxProfit=$minmaxProfit
    local targetRate=`add 1 $minmaxProfit`
    local options="-p$targetRate"

    local prevLongOutTp prevShortOutTp
    local stockCode tradeDate hms dc inUp inDp sc maxDelta minDelta
    while read stockCode tradeDate hms dc inUp inDp sc maxDelta minDelta
    do
        #convert stockCode,tradeDate,hms to inTp 
        local inTp=`convertTime2Hex $tradeDate $hms`
        [[ $tradeType == $LONG && ! -z $prevLongOutTp ]] && {
            #compare $prevLongOutTp with inTp 
            [[ 0x$inTp -lt 0x$prevLongOutTp ]] && continue
            true
        } || { 
            [[ $tradeType == $SHORT && ! -z $prevShortOutTp ]] && {
                #compare $prevShortOutTp with inTp 
                [[ 0x$inTp -lt 0x$prevShortOutTp ]] && continue
            }
            true
        }

        local outPrice=
        [[ $tradeType == $LONG ]] && outPrice=`add $inUp $minmaxProfit` || {
            outPrice=`substract $inDp $minmaxProfit`
        }

        local line=`getLSProfit $stockCode $tradeDate $hms "$options"`
        prevLongOutTp=${line% *}
        prevShortOutTp=${line#* }
        printf "%s %s %s %8s %8s %8s %8s %8s %8s %8s\n" \
            $stockCode $tradeDate $hms $dc $inUp $inDp $outPrice $sc $maxDelta $minDelta
    done <$fDSS
}
