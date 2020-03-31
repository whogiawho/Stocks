#!/bin/bash

function setProxy {
    local server=$1
    local port=$2
    local type=$3

    gcloud config set proxy/address $server
    gcloud config set proxy/port $port
    gcloud config set proxy/type $type
}

function setShadowSocks {
    setProxy 127.0.0.1 1080 socks5
}

function setFreeGate {
    setProxy 127.0.0.1 8580 http 
}

