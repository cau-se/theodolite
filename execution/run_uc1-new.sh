#!/bin/bash

EXP_ID=$1
DIM_VALUE=$2
INSTANCES=$3
PARTITIONS=$4
#CPU_LIMIT=1000
#MEMORY_LIMIT=4Gi
#KAFKA_STREAMS_COMMIT_INTERVAL_MS=100
EXECUTION_MINUTES=5

# Start up Kafka
# TODO

# Create Topics
#PARTITIONS=40
#kubectl run temp-kafka --rm --attach --restart=Never --image=solsson/kafka --command -- bash -c "./bin/kafka-topics.sh --zookeeper my-confluent-cp-zookeeper:2181 --create --topic input --partitions $PARTITIONS --replication-factor 1; ./bin/kafka-topics.sh --zookeeper my-confluent-cp-zookeeper:2181 --create --topic configuration --partitions 1 --replication-factor 1; ./bin/kafka-topics.sh --zookeeper my-confluent-cp-zookeeper:2181 --create --topic output --partitions $PARTITIONS --replication-factor 1"
echo "Print topics:"
kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list" | sed -n '/^titan-.*/p;/^input$/p;/^output$/p;/^configuration$/p'
PARTITIONS=$PARTITIONS
kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --create --topic input --partitions $PARTITIONS --replication-factor 1; kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --create --topic configuration --partitions 1 --replication-factor 1; kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --create --topic output --partitions $PARTITIONS --replication-factor 1"

# Start workload generator
NUM_SENSORS=$DIM_VALUE
WL_MAX_RECORDS=150000
WL_INSTANCES=$(((NUM_SENSORS + (WL_MAX_RECORDS -1 ))/ WL_MAX_RECORDS))
sed "s/{{NUM_SENSORS}}/$NUM_SENSORS/g; s/{{INSTANCES}}/$WL_INSTANCES/g" uc1-workload-generator/deployment.yaml | kubectl apply -f -

# Start application
REPLICAS=$INSTANCES
kubectl apply -f uc1-application/aggregation-deployment.yaml
kubectl scale deployment titan-ccp-aggregation --replicas=$REPLICAS

# Execute for certain time
sleep ${EXECUTION_MINUTES}m

# Run eval script
source ../.venv/bin/activate
python lag_analysis.py $EXP_ID uc1 $DIM_VALUE $INSTANCES
deactivate

# Stop wl and app
#kubectl delete -f uc1-workload-generator/deployment.yaml
sed "s/{{NUM_SENSORS}}/$NUM_SENSORS/g; s/{{INSTANCES}}/$WL_INSTANCES/g" uc1-workload-generator/deployment.yaml | kubectl delete -f -
kubectl delete -f uc1-application/aggregation-deployment.yaml


# Delete topics instead of Kafka
#kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --delete --topic 'input,output,configuration,titan-.*'"
# kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --delete --topic '.*'
#sleep 30s # TODO check
#kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list" | sed -n '/^titan-.*/p;/^input$/p;/^output$/p;/^configuration$/p'
#kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list" | sed -n '/^titan-.*/p;/^input$/p;/^output$/p;/^configuration$/p' | wc -l
#kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list"

#kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --delete --topic 'input,output,configuration,titan-.*'"
echo "Finished execution, print topics:"
#kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list" | sed -n -E '/^(titan-.*|input|output|configuration)( - marked for deletion)?$/p'
while test $(kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list" | sed -n -E '/^(titan-.*|input|output|configuration)( - marked for deletion)?$/p' | wc -l) -gt 0
do
    kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --delete --topic 'input,output,configuration,titan-.*'"
    echo "Wait for topic deletion"
    sleep 5s
    #echo "Finished waiting, print topics:"
    #kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list" | sed -n -E '/^(titan-.*|input|output|configuration)( - marked for deletion)?$/p'
    # Sometimes a second deletion seems to be required
done
echo "Finish topic deletion, print topics:"
#kubectl exec kafka-client -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list" | sed -n -E '/^(titan-.*|input|output|configuration)( - marked for deletion)?$/p'
echo "Exiting script"

KAFKA_LAG_EXPORTER_POD=$(kubectl get pod -l app.kubernetes.io/name=kafka-lag-exporter -o jsonpath="{.items[0].metadata.name}")
kubectl delete pod $KAFKA_LAG_EXPORTER_POD
