#!/bin/sh

docker-compose logs --tail 100 benchmark-taskmanager |
    sed -n "s/^.*Key:\s\(\S*\), Value:\s\(\S*\).*$/\2/p" |
    tee /dev/stderr |
    jq .identifier |
    sort |
    uniq |
    wc -l |
    grep "\b10\b"
