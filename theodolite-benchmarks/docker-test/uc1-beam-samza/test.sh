#!/bin/sh

docker-compose logs --tail 100 benchmark |
    sed -n "s/^.*Key:\s\(\S*\), Value:\s\(\S*\).*$/\2/p" |
    tee /dev/tty |
    jq .identifier |
    sort |
    uniq |
    wc -l |
    grep "\b10\b"


