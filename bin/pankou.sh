#!/bin/bash


#depend on settings.txt
function splitRawPankou {
    local stockCode=$1
    local tradeDate=$2

    local pankouYear=
    pankouYear=${tradeDate:0:4}


    local pankouFile="$rawPankouDataDir\\$stockCode\\$tradeDate\\pankou\\pankou.txt"
    local emuPankouDir="$dailyDir\\$stockCode\\$tradeDate\\emuPankou"
    rm -rf "$emuPankouDir"
    [[ ! -d "$emuPankouDir" ]] && {
        mkdir -p "$emuPankouDir"
    }
    java -jar "$splitRawPankouJar" "$pankouFile" "$emuPankouDir" $pankouYear 2>/dev/null
}

function fixPankouTxt {
    local stockCode=$1
    local tradeDate=$2

    local pankouFile="$rawPankouDataDir\\$stockCode\\$tradeDate\\pankou\\pankou.txt"

    #match $reg15 for pankou.txt, and there are 3 scenarios
    local reg15="15:0[0-9]:"
    local matchNO=`grep $reg15 $pankouFile|wc|awk '{print $1}'`
    local fTmp=`mktemp`
    head -n -$matchNO $pankouFile >$fTmp

    [[ $matchNO == 0 ]] && {                 #1. if there is 0 match
        #append 2 records to pankou.txt 
        #append 15:00:00
        local lastLine=`tail -n 1 $pankouFile`
        local line=`modifyPankouTime "$lastLine" "$CloseQuotationTime"`
        echo $line >>$fTmp
        #append 15:01:00
        line=`modifyPankouTime "$lastLine" "$LastRawTradeDetailTime"`
        echo $line >>$fTmp
    } || {
        [[ $matchNO == 1 ]] && {             #2. if there is 1 match
            #fix the 1st match to 15:00:00
            local lastLine=`tail -n 1 $pankouFile`
            local line=`modifyPankouTime "$lastLine" "$CloseQuotationTime"`
            echo $line>>$fTmp

            #append 15:01:00
            line=`modifyPankouTime "$lastLine" "$LastRawTradeDetailTime"`
            echo $line >>$fTmp
        } || {
            [[ $matchNO -ge 2 ]] && {        #3. there are more than 2 matches
                #fix the 1st match to 15:00:00 
                local line1st=`tail -n $matchNO $pankouFile|head -n 1`
                line1st=`modifyPankouTime "$line1st" "$CloseQuotationTime"`
                echo $line1st >>$fTmp
                local remainsStart=2

                #fix the 2nd match to 15:01:00 if its time > 15:01:00
                local line2nd=`tail -n $matchNO $pankouFile|sed -n '2p'`
                local pankouTime2nd=`getPankouTime "$line2nd"`
                [[ $pankouTime2nd > $LastRawTradeDetailTime ]] && {
                    line2nd=`modifyPankouTime "$line2nd" "$LastRawTradeDetailTime"`
                    echo $line2nd >>$fTmp
                    remainsStart=3
                }

                #keep remains unchanged
                tail -n $matchNO $pankouFile|tail -n +$remainsStart >>$fTmp
            }
        }
    }

    #mv $pankouFile $pankouFile.bak
    mv $fTmp $pankouFile
    unix2dos $pankouFile
    [[ -f $fTmp ]] && rm -rf $fTmp
}
function getPankouTime {
    local pankouLine=$1

    local tradeTime=`echo $pankouLine|awk '{print $2}'`
    echo $tradeTime
}
function modifyPankouTime {
    local pankouLine=$1
    local newTime=$2

    local part1=`echo $pankouLine|awk '{print $1}'`
    echo "$part1 $newTime"
}

#1. all rows should have 41 columns
#2. must begin with 09:[12]
#3. must end with 15:0||14:59
function checkRawPankou {
    local stockCode=$1
    local tradeDate=$2

    local rawPankouFile="$rawPankouDataDir\\$stockCode\\$tradeDate\\pankou\\pankou.txt"
    
    local lines1=
    lines1=`wc -l $rawPankouFile|awk '{print $1}'`

    local lines2=
    lines2=`awk -F, '{print $41}' $rawPankouFile|wc -l|awk '{print $1}'`

    [[ $lines1 -le $lines2 ]] && {
        local lines=
        lines=`head -n 1 $rawPankouFile|awk -F, '{print $41}'`
        [[ ! $lines =~ 09:[12] ]] && return 1
        lines=`tail -n 1 $rawPankouFile|awk -F, '{print $41}'`
        [[ ( ! $lines =~ 15:0 ) && ( ! $lines =~ 14:59 ) ]] && return 1

        return 0
    } || {
        #lines1 > $lines2 is illegal
        return 1
    }
}
