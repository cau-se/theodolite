#!/bin/sh

docker-compose logs --tail 100 benchmark-taskmanager |
    sed -n "s/^.*Record:\s\(\S*\)$/\1/p" |
    tee /dev/stderr |
    jq .identifier |
    sort |
    uniq |
    wc -l |
    grep "\b10\b"
