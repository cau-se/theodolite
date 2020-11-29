#!/bin/bash

KUBE_API_KUBE_API_SERVER=($(kubectl config view -o json | jq '.clusters[0].cluster.server'))
while true;
do
    for PODNAME in $(kubectl get pods -n default -o json | jq '.items[] | select(.spec.schedulerName == "random-scheduler") | select(.spec.nodeName == null) | .metadata.name' | tr -d '"');
    do
        NODES=($(kubectl get nodes -o json | jq '.items[].metadata.name' | tr -d '"'))
        NUMNODES=${#NODES[@]}
        CHOSEN=${NODES[$[$RANDOM % $NUMNODES]]}
        curl --header "Content-Type:application/json" --request POST --data '{"apiVersion":"v1", "kind": "Binding", "metadata": {"name": "'$PODNAME'"}, "target": {"apiVersion": "v1", "kind": "Node", "name": "'$CHOSEN'"}}' $KUBE_API_SERVER/api/v1/namespaces/default/pods/$PODNAME/binding/
        echo "Assigned $PODNAME to $CHOSEN"
    done
    sleep 1
done