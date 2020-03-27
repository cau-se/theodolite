#!/bin/bash

EXP_ID=$1
DIM_VALUE=$2
INSTANCES=$3
PARTITIONS=$4

# Maybe start up Kafka

# Create Topics
#PARTITIONS=40
#kubectl run temp-kafka --rm --attach --restart=Never --image=solsson/kafka --command -- bash -c "./bin/kafka-topics.sh --zookeeper my-confluent-cp-zookeeper:2181 --create --topic input --partitions $PARTITIONS --replication-factor 1; ./bin/kafka-topics.sh --zookeeper my-confluent-cp-zookeeper:2181 --create --topic configuration --partitions 1 --replication-factor 1; ./bin/kafka-topics.sh --zookeeper my-confluent-cp-zookeeper:2181 --create --topic output --partitions $PARTITIONS --replication-factor 1"
PARTITIONS=$PARTITIONS
kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --create --topic input --partitions $PARTITIONS --replication-factor 1; kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --create --topic configuration --partitions 1 --replication-factor 1; kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --create --topic output --partitions $PARTITIONS --replication-factor 1"

# Start workload generator
NUM_SENSORS=$DIM_VALUE
sed "s/{{NUM_SENSORS}}/$NUM_SENSORS/g" uc3-workload-generator/deployment.yaml | kubectl apply -f -

# Start application
REPLICAS=$INSTANCES
kubectl apply -f uc3-application/aggregation-deployment.yaml
kubectl scale deployment titan-ccp-aggregation --replicas=$REPLICAS

# Execute for certain time
sleep 5m

# Run eval script
source ../.venv/bin/activate
python lag_analysis.py $EXP_ID uc3 $DIM_VALUE $INSTANCES
deactivate

# Stop wl and app
kubectl delete -f uc3-workload-generator/deployment.yaml
kubectl delete -f uc3-application/aggregation-deployment.yaml


# Delete topics instead of Kafka
kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --delete --topic 'input,output,configuration,titan-.*'"
# kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --delete --topic '.*'
sleep 10s # TODO check
kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list"
