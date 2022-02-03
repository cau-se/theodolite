#!/bin/sh

COMPOSE_FILE_PATH=$1
echo "Run test for '$COMPOSE_FILE_PATH'."
echo "Use image tag '${THEODOLITE_TAG:-latest}'."

cd $COMPOSE_FILE_PATH
docker-compose pull -q
docker-compose up -d kafka zookeeper schema-registry
sleep 30s
docker-compose up -d
sleep 5s
docker-compose ps

if test -f "./test.sh"; then
    #timeout --foreground 3m ./test.sh
    ./test.sh
    RETURN=$?
else
    RETURN=$?
    echo "test.sh does not exists for '$COMPOSE_FILE_PATH'." 
fi
if [ $RETURN -eq 0 ]; then
    echo "Test for '$COMPOSE_FILE_PATH' has passed."
else
    echo "Test for '$COMPOSE_FILE_PATH' has failed."
fi

docker-compose down
exit $RETURN
