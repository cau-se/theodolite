#!/bin/sh

docker-compose logs --tail 100 benchmark |
    sed -n "s/^.*Record:\s\(\S*\)$/\1/p" |
    tee /dev/tty |
    jq .identifier |
    sort |
    uniq |
    wc -l |
    grep "\b10\b"
