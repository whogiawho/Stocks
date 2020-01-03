#!/bin/bash


function getValue {
    local key=$1

    local analyzetoolsJar="$rootDir\\build\\jar\\analyzetools.jar"
    java -jar $analyzetoolsJar getvalue $key 2>/dev/null
}

function setValue {
    local key=$1
    local value=$2

    local analyzetoolsJar="$rootDir\\build\\jar\\analyzetools.jar"
    java -jar $analyzetoolsJar setvalue $key $value 2>/dev/null
}




