#!/usr/bin/env bash
echo "HELLO from ROACH will run $@"
echo "EXPECTED_INSTANCE_COUNT = ${EXPECTED_INSTANCE_COUNT}"
echo "RUNNING_PORT = ${RUNNING_PORT}"
echo "DNS_NAME = ${DNS_NAME}"
echo "INSTANCE_SLOT = ${INSTANCE_SLOT}"


if [ -z ${EXPECTED_INSTANCE_COUNT+x} ]; then echo "var EXPECTED_INSTANCE_COUNT is unset"; exit 1; fi
if [ -z ${RUNNING_PORT+x} ]; then echo "var RUNNING_PORT is unset"; exit 1; fi
if [ -z ${DNS_NAME+x} ]; then echo "var DNS_NAME is unset"; exit 1; fi
if [ -z ${INSTANCE_SLOT+x} ]; then echo "var INSTANCE_SLOT is unset"; exit 1; fi

function running_count {
    nslookup ${DNS_NAME} | grep 'Address: [0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}' | wc -l
}

function running_addresses_with_port {
    PORT=$1
    FIRST=1
    for i in $(nslookup ${DNS_NAME} | grep 'Address: [0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}' | cut -d' ' -f2); do
        if [ $FIRST -ne 1 ]; then printf ","; fi
        FIRST=0
        printf "${i}:${PORT}"
    done
}

function running_addresses {
    echo $(running_addresses_with_port ${RUNNING_PORT})
}


TEST_NUM=0
INSTANCES_RUNNING=$(running_count)
while [ $INSTANCES_RUNNING -lt $EXPECTED_INSTANCE_COUNT ]; do
    echo "Only $INSTANCES_RUNNING instances found, waiting for at least $EXPECTED_INSTANCE_COUNT"
    TEST_NUM=$(expr $TEST_NUM + 1)
    if [ $TEST_NUM -gt 10 ]; then
        echo "Too many attempts"
        exit 1
    fi
    sleep 1
    INSTANCES_RUNNING=$(running_count)
done
ALL_ADDRESES=$(running_addresses)
echo "Will join to $ALL_ADDRESES"
HOST_TO_INIT=$(nslookup ${DNS_NAME} | grep 'Address: [0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}' | cut -d' ' -f2 | head -n 1)
if [ $INSTANCE_SLOT -eq 1 ]; then
    if [ -f /cockroach/cockroach-data/already-inited ]; then
        echo "Already initialized so skipping init"
    else
        echo "first slow will try to init cluster"
        /init.sh 30 &
    fi
fi
exec /cockroach/cockroach start --join $ALL_ADDRESES "$@"

