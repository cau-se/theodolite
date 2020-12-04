#!/bin/bash

KUBE_API_SERVER=($(kubectl cluster-info | grep 'Kubernetes master' | awk '/http/ {print $NF}' | awk -F : '{print $1$2}' | awk '{ gsub(/\/\//, "://"); print }'))
echo "K8's API server: $KUBE_API_SERVER"
echo "Target Namespace: $TARGET_NAMESPACE"
while true;
do
    for PODNAME in $(kubectl get pods -n $TARGET_NAMESPACE -o json | jq '.items[] | select(.spec.schedulerName == "random-scheduler") | select(.spec.nodeName == null) | .metadata.name' | tr -d '"');
    do
        NODES=($(kubectl get nodes -o json | jq '.items[].metadata.name' | tr -d '"'))
        NUMNODES=${#NODES[@]}
        CHOSEN=${NODES[$[$RANDOM % $NUMNODES]]}
        curl --insecure --header "Content-Type:application/json" --request POST --data '{"apiVersion":"v1", "kind": "Binding", "metadata": {"name": "'$PODNAME'"}, "target": {"apiVersion": "v1", "kind": "Node", "name": "'$CHOSEN'"}}' https://10.96.0.1/api/v1/namespaces/$TARGET_NAMESPACE/pods/$PODNAME/binding/
        echo "Assigned $PODNAME to $CHOSEN"
    done
    sleep 1
done