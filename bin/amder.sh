#!/bin/bash



function makeAllContAmStatsTxt {
    local stockCode=$1
    local startDate=$2
    local endDate=$3

    local max=10
    local cnt=0
    local i=
    for i in `getTradeDateRange $stockCode $startDate $endDate`
    do
        makeContAmStatsTxt $stockCode $i &

        cnt=$((cnt+1))
        [[ $cnt -ge $max ]] && {
            wait
            cnt=0
        }
    done
}
function makeContAmStatsTxt {
    local stockCode=$1
    local tradeDate=$2

    local sContAmStatsTxt="$dailyDir\\$stockCode\\$tradeDate\\contamstats.txt"
    rm -rf $sContAmStatsTxt

    local derivativeDir="$dailyDir\\$stockCode\\$tradeDate\\derivative"
    local i=
    for i in `ls $derivativeDir`; 
    do 
        local stats=`getContAmStats $derivativeDir/$i`; 
        echo $stockCode $tradeDate $i $stats |tee -a $sContAmStatsTxt
    done
}
function getContAmStats {
    local fAmDer=$1

    local posCnt=0 negCnt=0 zeorCnt=0
    local i=
    local amTypes=`getContAmType $fAmDer`
    for i in $amTypes 
    do
        [[ $i -eq 1 ]] && posCnt=$((posCnt+1)) || {
            [[ $i -eq -1 ]] && negCnt=$((negCnt+1)) || {
                zeorCnt=$((zeorCnt+1))
            }
        }
    done
    amTypes=`echo $amTypes|sed "s/ /,/g"`

    echo $posCnt $zeorCnt $negCnt $amTypes
}
function getContAmType {
    local fAmDer=$1

    local ams=
    ams=`awk '{print $2}' $fAmDer`

    local line=
    echo $ams|sed "s@\( #N/A\)\+@\n@g"|while read line
    do
        getAmLineType "$line" 30 150 
    done
}
function getAmLineType {
    local line=$1
    local min=$2
    local max=$3

    local options=
    [[ ! -z $min ]] && options=$options"-i$min "
    [[ ! -z $max ]] && options=$options"-a$max "
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" java -jar $analyzetoolsJar getamlinetype $options "$line" 2>/dev/null
}


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
    local bSaveTxt=$7                         #optional

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

    [[ -z $bSaveTxt ]] && rm -rf $amDerTxt
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
function amderSearch {
    local stockCode=$1
    local minAMCnt=$2
    local maxAMCnt=$3
    local fOut=$4

    local max=10
    local cnt=0
    local i=
    for i in `getTradeDateList $stockCode`
    do
        amderSearchHMS $stockCode $i $minAMCnt $maxAMCnt $fOut&

        cnt=$((cnt+1))
        [[ $cnt -ge $max ]] && {
            wait
            cnt=0
        }
    done
}
function amderSearchHMS {
    local stockCode=$1
    local tradeDate=$2
    local minAMCnt=$3
    local maxAMCnt=$4
    local fOut=$5

    local derivativeDir="$dailyDir\\$stockCode\\$tradeDate\\derivative"
    local i=
    for i in `ls $derivativeDir`; 
    do 
        local ams=
        ams=`awk '{print $2}' $derivativeDir/$i`

        local line=
        echo $ams|sed "s@\( #N/A\)\+@\n@g"|while read line
        do
            local wordCnt=`echo $line|wc|awk '{print $2}'`
            [[ $wordCnt -ge $minAMCnt && $wordCnt -le $maxAMCnt ]] && {
                echo $stockCode $tradeDate $i \"$line\" >>$fOut
            }
        done
    done
}
function _amderSearchHMS {
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


