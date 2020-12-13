#!/bin/bash



function makeAvi {
    local stockCode=$1
    local tradeDate=$2

    local targetDir="$dataRoot\\amderAvi"
    [[ ! -e $targetDir ]] && {
        mkdir -p $targetDir
    }

    local curDir=$PWD
    local pngDir="$dailyDir\\$stockCode\\$tradeDate\\mergedPng"
    cd $pngDir
    mencoder mf://*.png -mf w=480:h=289:fps=1:type=png -ovc copy -oac copy -o "$targetDir\\$stockCode.$tradeDate.avi"
    cd $curDir
}
#merge pngs from amdertxtPng and derivativePng
function mergePng {
    local stockCode=$1
    local tradeDate=$2

    local dir0="$dailyDir\\$stockCode\\$tradeDate\\amdertxtPng"
    local dir1="$dailyDir\\$stockCode\\$tradeDate\\derivativePng"
    local outDir="$dailyDir\\$stockCode\\$tradeDate\\mergedPng"

    [[ ! -e $outDir ]] && {
        mkdir -p $outDir
    }

    local i=
    for i in `ls $dir0`
    do
        local hms=${i%.png}
        convert "$dir0\\$hms.png" "$dir1\\$hms.png" -append "$outDir\\$hms.png"
    done
}


function dailyMake5dAmDers {
    local stockCode=$1
    local sDate=$2
    local eDate=$3

    dailyMakeAmDers $stockCode $sDate $eDate $((14400*5))
}
function dailyMakeAmDers {
    local stockCode=$1
    local sDate=$2
    local eDate=$3
    local bwsd=$4

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
        java -jar $analyzetoolsJar listamderivatives -i60 -b$bwsd -m60 -e60 $stockCode $i
    done
}

function makeAmDerivativePngs {
    local stockCode=$1
    local tradeDate=$2

    local derivativeDir="$dailyDir\\$stockCode\\$tradeDate\\derivative"

    cscript.exe "$rootDir\\vbs\\makeAmDerivativePngs.vbs" "$derivativeDir"
}
function makeAmDerivativePng {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local interval=$4 
    local bwsd=$5                             #optional
    local r2Threshold=$6                      #optional

    [[ -z $bwsd ]] && bwsd=300
    [[ -z $r2Threshold ]] && r2Threshold=0.5

    local amderDir="$dailyDir\\$stockCode\\$tradeDate\\amderTxt"
    [[ ! -e "$amderDir" ]] && {
        mkdir -p "$amderDir"
    }

    local amDerTxt="$amderDir\\${tradeDate}_${hms}_${bwsd}_amder.txt"
    java -jar $analyzetoolsJar listamderivatives -b$bwsd -h$r2Threshold -m60 -i${interval} $stockCode $tradeDate $hms >"$amDerTxt"

    local sPngFile="$amderDir\\${tradeDate}_${hms}_${bwsd}_amder.png"
    cscript.exe "$rootDir\\vbs\\makeAmDerivativePng.vbs" "$amDerTxt" "$sPngFile"

    rm -rf $amDerTxt
}



function makeAmDerTxtPngs {
    local stockCode=$1
    local tradeDate=$2

    local amderTxtDir="$dailyDir\\$stockCode\\$tradeDate\\amderTxt"

    cscript.exe "$rootDir\\vbs\\makeAmDerTxtPngs.vbs" "$amderTxtDir"
}
function makeAmDerAnalysis {
    local stockCode=$1
    local tradeDate=$2
    local bwsd=$3
    local interval=$4

    local amderDir="$dailyDir\\$stockCode\\$tradeDate\\amderTxt"
    [[ ! -e "$amderDir" ]] && {
        mkdir -p "$amderDir"
    }

    #local startTp=`convertTime2Hex $tradeDate $CallAuctionEndTime`
    #local endTp=`convertTime2Hex $tradeDate $CloseQuotationTime`
    local startSd=`getAbs $stockCode $tradeDate $CallAuctionEndTime`
    local endSd=`getAbs $stockCode $tradeDate $CloseQuotationTime`
    local i=
    for i in `seq $startSd $endSd`
    do
        local tp=`rgetAbs $stockCode $i`
        local hms=`convertHex2Time $tp y|awk -F, '{print $2}'`

        local sSd=$((i-bwsd))
        local sTp=`rgetAbs $stockCode $sSd`
        local str=`convertHex2Time $sTp y`
        local sDate=`echo $str|awk -F, '{print $1}'`
        local sHMS=`echo $str|awk -F, '{print $2}'`

        getAnalysis $stockCode ${sDate} ${sHMS} ${tradeDate} ${hms} ${interval} >"$amderDir\\$hms.txt"
    done
}





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
function amderSearchHMS {
    local stockCode=$1
    local startSd=$2                          #optional
    local endSd=$3                            #optional
    local step=$4                             #optional
    local naThreshold=$5                      #optional
    local bwsd=$6                             #optional
    local r2Threshold=$7                      #optional


    [[ -z $startSd ]] && startSd=$((14405*2))
    [[ -z $endSd ]] && endSd=40319594
    [[ -z $step ]] && step=60
    [[ -z $naThreshold ]] && naThreshold=0.90
    [[ -z $bwsd ]] && bwsd=$((14405*2))
    [[ -z $r2Threshold ]] && r2Threshold=0.5

    local amDerTxt=`mktemp`
    local i=
    for i in `seq $startSd $step $endSd`
    do
        local sTp=`rgetAbs $stockCode $i`
        local str=`convertHex2Time $sTp y`
        local tradeDate=`echo $str|awk -F, '{print $1}'`
        local hms=`echo $str|awk -F, '{print $2}'`

        java -jar $analyzetoolsJar listamderivatives -b$bwsd -h$r2Threshold $stockCode $tradeDate $hms >"$amDerTxt" 2>/dev/null
        local sum=`wc $amDerTxt|awk '{print $1}'`
        local naCnt=`awk "\\$1<0.5{print \\$0}" $amDerTxt|wc|awk '{print $1}'`
        local rate=`divide $naCnt $sum`

        local bCmp=`ge $rate $naThreshold`
        [[ $bCmp == 1 ]] && {
            printf "%10d %8s %8s %8.3f\n" $i $tradeDate $hms $rate
        } || {
            printf "%10d %8.3f\n" $i $rate
        }
    done
}
function getCodecSeries {
    local fAmDer=$1
    local hextp=$2
    local bwsd=$3

    echo $hextp|grep -q "," && {
        local ymd=${hextp%,*}
        local hms=${hextp#*,}
        hextp=`convertTime2Hex $ymd $hms`
    }
    local startHexTp=$(("0x"$hextp-bwsd))
    startHexTp=`printf "%x" $startHexTp`

    sed -n "/$startHexTp/,/$hextp/p" $fAmDer|awk '{print $12}'|tr "\n" ","
}
function makeAmPerms {
    local stockCode=$1

    local amderDir=/tmp/amderivatives
    mkdir -p $amderDir
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

    local max=10
    local cnt=0
    local tradeDates=`getTradeDateList $stockCode y|tail -n +2`
    local i=
    for i in $tradeDates
    do
        java -jar $analyzetoolsJar listamderivatives -s $stockCode $i >$amderDir/$i.txt &

        cnt=$((cnt+1))
        echo cnt=$cnt
        [[ $cnt -ge $max ]] && {
            wait -n
            cnt=$((cnt-1))
        }
    done
}
function amderGetAnalysis {
    local stockCode=$1
    local tradeDate=$2
    local hms=$3
    local bwsd=$4
    local interval=$5

    local amderDir="$dailyDir\\$stockCode\\$tradeDate\\amderTxt"
    [[ ! -e "$amderDir" ]] && {
        mkdir -p "$amderDir"
    }

    local hmsSd=`getAbs $stockCode $tradeDate $hms`
    local sSd=$((hmsSd-bwsd))
    local sTp=`rgetAbs $stockCode $sSd`
    local str=`convertHex2Time $sTp y`
    local sDate=`echo $str|awk -F, '{print $1}'`
    local sHMS=`echo $str|awk -F, '{print $2}'`
    local hmsTxt="$amderDir\\${hms}_${bwsd}_analysis.txt"
    getAnalysis $stockCode ${sDate} ${sHMS} ${tradeDate} ${hms} ${interval} >"$hmsTxt"
}


