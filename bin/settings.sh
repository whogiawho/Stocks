#!/bin/bash


function getValue {
    local key=$1

    java -jar $analyzetoolsJar getvalue $key 2>/dev/null
}

function setValue {
    local key=$1
    local value=$2

    java -jar $analyzetoolsJar setvalue $key $value 2>/dev/null
}




