#!/usr/bin/env bash

DELAY=$1
HOST_TO_INIT=$2
echo "waiting for $DELAY"
sleep $DELAY
echo "initing on $HOST_TO_INIT"
OUT=$(/cockroach/cockroach init --insecure --host=$HOST_TO_INIT 2>1&)
if [ $? -eq 0 ]; then
     echo "Init ok: $OUT"
     touch /cockroach/cockroach-data/already-inited
     exit 0
else
    echo $OUT | grep "code = AlreadyExists"
    if [ $? -eq 0 ]; then
        echo "Already initialized: $OUT"
        touch /cockroach/cockroach-data/already-inited
        exit 0
    else
        echo "unknown problem initiating: $OUT"
        exit 1
    fi
fi