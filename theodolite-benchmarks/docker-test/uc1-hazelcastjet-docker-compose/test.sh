#!/bin/sh

sleep 55s # to let the benchmark and produce some output

docker-compose logs --tail 100 benchmark |
    sed -n 's/^.*key":"//p' | # cut the first part before the key
    sed 's/","value.*//' | # cut the rest after the key
    sort |
    uniq |
    wc -l |
    grep "\b10\b"
