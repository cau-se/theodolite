#!/bin/bash

# use kubectl in proxy mode in order to allow curl requesting the k8's api server
kubectl proxy --port 8080 &

echo "Target Namespace: $TARGET_NAMESPACE"
while true;
do
    for PODNAME in $(kubectl get pods -n $TARGET_NAMESPACE -o json | jq '.items[] | select(.spec.schedulerName == "random-scheduler") | select(.spec.nodeName == null) | .metadata.name' | tr -d '"');
    do
        NODES=($(kubectl get nodes -o json | jq '.items[].metadata.name' | tr -d '"'))
        NUMNODES=${#NODES[@]}
        CHOSEN=${NODES[$[$RANDOM % $NUMNODES]]}
        curl --header "Content-Type:application/json" --request POST --data '{"apiVersion":"v1", "kind": "Binding", "metadata": {"name": "'$PODNAME'"}, "target": {"apiVersion": "v1", "kind": "Node", "name": "'$CHOSEN'"}}' localhost:8080/api/v1/namespaces/$TARGET_NAMESPACE/pods/$PODNAME/binding/
        echo "Assigned $PODNAME to $CHOSEN"
    done
    sleep 1
done