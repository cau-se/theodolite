#!/bin/sh

docker-compose exec -T kcat kcat -C -b kafka:9092 -t output -s key=s -s value=avro -r http://schema-registry:8081 -f '%k:%s\n' -c 2000 |
    tee /dev/stderr |
    awk -F ':' '!/^%/ {print $1}' |
    sort |
    uniq |
    wc -l |
    grep "\b21\b"
