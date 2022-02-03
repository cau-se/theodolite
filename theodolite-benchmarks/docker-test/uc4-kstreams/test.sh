#!/bin/sh

until docker-compose exec -T kcat kcat -L -b kafka:9092 -t output -J | jq -r '.topics[0].partitions | length' | grep "\b3\b"; do sleep 5s; done

docker-compose exec kcat kcat -C -b kafka:9092 -t configuration -s key=s -s value=s -r http://schema-registry:8081 -f '%k:%s\n' -o beginning -c 1

sleep 30s
docker-compose logs benchmark

sleep 30s
docker-compose logs benchmark

sleep 30s
docker-compose logs benchmark

docker-compose exec -T kcat kcat -C -b kafka:9092 -t output -s key=s -s value=avro -r http://schema-registry:8081 -f '%k:%s\n' -o beginning -c 32 |
    tee /dev/stderr |
    awk -F ':' '!/^%/ {print $1}' |
    sort |
    uniq |
    wc -l |
    grep "\b16\b"
