#!/bin/bash

EXP_ID=$1
DIM_VALUE=$2
INSTANCES=$3
PARTITIONS=${4:-40}
CPU_LIMIT=${5:-1000m}
MEMORY_LIMIT=${6:-4Gi}
KAFKA_STREAMS_COMMIT_INTERVAL_MS=${7:-100}
EXECUTION_MINUTES=${8:-5}

echo "EXP_ID: $EXP_ID"
echo "DIM_VALUE: $DIM_VALUE"
echo "INSTANCES: $INSTANCES"
echo "PARTITIONS: $PARTITIONS"
echo "CPU_LIMIT: $CPU_LIMIT"
echo "MEMORY_LIMIT: $MEMORY_LIMIT"
echo "KAFKA_STREAMS_COMMIT_INTERVAL_MS: $KAFKA_STREAMS_COMMIT_INTERVAL_MS"
echo "EXECUTION_MINUTES: $EXECUTION_MINUTES"

KAFKA_CLIENT=$(kubectl get pods --selector=app=kafka-client -o jsonpath='{.items[*].metadata.name}')

# Create Topics
#PARTITIONS=40
#kubectl run temp-kafka --rm --attach --restart=Never --image=solsson/kafka --command -- bash -c "./bin/kafka-topics.sh --zookeeper my-confluent-cp-zookeeper:2181 --create --topic input --partitions $PARTITIONS --replication-factor 1; ./bin/kafka-topics.sh --zookeeper my-confluent-cp-zookeeper:2181 --create --topic configuration --partitions 1 --replication-factor 1; ./bin/kafka-topics.sh --zookeeper my-confluent-cp-zookeeper:2181 --create --topic output --partitions $PARTITIONS --replication-factor 1"
PARTITIONS=$PARTITIONS
kubectl exec $KAFKA_CLIENT -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --create --topic input --partitions $PARTITIONS --replication-factor 1; kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --create --topic configuration --partitions 1 --replication-factor 1; kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --create --topic output --partitions $PARTITIONS --replication-factor 1"

# Start workload generator
NUM_SENSORS=$DIM_VALUE
WL_MAX_RECORDS=150000
WL_INSTANCES=$(((NUM_SENSORS + (WL_MAX_RECORDS -1 ))/ WL_MAX_RECORDS))

WORKLOAD_GENERATOR_YAML=$(sed "s/{{NUM_SENSORS}}/$NUM_SENSORS/g; s/{{INSTANCES}}/$WL_INSTANCES/g" uc3-workload-generator/deployment.yaml)
echo "$WORKLOAD_GENERATOR_YAML" | kubectl apply -f -

# Start application
REPLICAS=$INSTANCES
# When not using `sed` anymore, use `kubectl apply -f uc3-application`
CONFIG_YAML=$(sed "s/{{REPLICAS}}/$REPLICAS/g; s/{{CPU_LIMIT}/$CPU_LIMIT/g; s/{{MEMORY_LIMIT}}/$MEMORY_LIMIT/g;" uc3-application/flink-configuration-configmap.yaml)

echo "$CONFIG_YAML" | kubectl apply -f -
kubectl apply -f uc3-application/service-monitor.yaml
kubectl apply -f uc3-application/jobmanager-service.yaml
kubectl apply -f uc3-application/jobmanager-rest-service.yaml
kubectl apply -f uc3-application/taskmanager-service.yaml
#kubectl apply -f uc3-application/aggregation-deployment.yaml
JOBMANAGER_YAML=$(sed "s/{{CPU_LIMIT}}/$CPU_LIMIT/g; s/{{MEMORY_LIMIT}}/$MEMORY_LIMIT/g; s/{{KAFKA_STREAMS_COMMIT_INTERVAL_MS}}/$KAFKA_STREAMS_COMMIT_INTERVAL_MS/g; s/{{REPLICAS}}/$REPLICAS/g" uc3-application/jobmanager-job.yaml)
echo "$JOBMANAGER_YAML" | kubectl apply -f -
TASKMANAGER_YAML=$(sed "s/{{CPU_LIMIT}}/$CPU_LIMIT/g; s/{{MEMORY_LIMIT}}/$MEMORY_LIMIT/g; s/{{KAFKA_STREAMS_COMMIT_INTERVAL_MS}}/$KAFKA_STREAMS_COMMIT_INTERVAL_MS/g; s/{{REPLICAS}}/$REPLICAS/g" uc3-application/taskmanager-job-deployment.yaml)
echo "$TASKMANAGER_YAML" | kubectl apply -f -

# Execute for certain time
sleep ${EXECUTION_MINUTES}m

# Run eval script
source ../.venv/bin/activate
python lag_analysis.py $EXP_ID uc3 $DIM_VALUE $INSTANCES $EXECUTION_MINUTES
deactivate

# Stop wl and app

echo "$WORKLOAD_GENERATOR_YAML" | kubectl delete -f -
echo "$CONFIG_YAML"| kubectl delete -f -
kubectl delete -f uc3-application/service-monitor.yaml
kubectl delete -f uc3-application/jobmanager-service.yaml
kubectl delete -f uc3-application/jobmanager-rest-service.yaml
kubectl delete -f uc3-application/taskmanager-service.yaml

echo "$JOBMANAGER_YAML" | kubectl delete -f -
echo "$TASKMANAGER_YAML" | kubectl delete -f -

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
while test $(kubectl exec $KAFKA_CLIENT -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list" | sed -n -E '/^(theodolite-.*|input|output|configuration)( - marked for deletion)?$/p' | wc -l) -gt 0
do
    kubectl exec $KAFKA_CLIENT -- bash -c "kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --delete --topic 'input|output|configuration|theodolite-.*'"
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
