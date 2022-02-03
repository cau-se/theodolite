#!/bin/sh

sleep 55s # to let the benchmark and produce some output
docker-compose logs --tail 100 benchmark |
    sed -n "s/^.*Record:\s\(\S*\)$/\1/p" |
    tee /dev/stderr |
    jq .identifier |
    sort |
    uniq |
    wc -l |
    grep "\b10\b"
