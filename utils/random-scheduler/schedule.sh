#!/bin/bash

# use kubectl in proxy mode in order to allow curl requesting the k8's api server
kubectl proxy --port 8080 &

echo "Target Namespace: $TARGET_NAMESPACE"
while true;
do
    for PODNAME in $(kubectl get pods -n $TARGET_NAMESPACE -o json | jq '.items[] | select(.spec.schedulerName == "random-scheduler") | select(.spec.nodeName == null) | .metadata.name' | tr -d '"');
    do
        NODE_SELECTOR=$(kubectl get pod $PODNAME -n $TARGET_NAMESPACE -o json | jq -S 'if .spec.nodeSelector != null then .spec.nodeSelector else {} end')
        NODES=($(kubectl get nodes -o json | jq --argjson nodeSelector "$NODE_SELECTOR" '.items[] | select(.metadata.labels | contains($nodeSelector)) | .metadata.name' | tr -d '"'))
        NUMNODES=${#NODES[@]}
        if [ $NUMNODES -eq 0 ]; then
            echo "No nodes found matching the node selector: $NODE_SELECTOR from pod $PODNAME"
            echo "Pod $PODNAME cannot be scheduled."
            continue;
        fi
        echo "Found $NUM_NODES suitable nodes for pod $PODNAME"
        CHOSEN=${NODES[$[$RANDOM % $NUMNODES]]}
        curl --header "Content-Type:application/json" --request POST --data '{"apiVersion":"v1", "kind": "Binding", "metadata": {"name": "'$PODNAME'"}, "target": {"apiVersion": "v1", "kind": "Node", "name": "'$CHOSEN'"}}' localhost:8080/api/v1/namespaces/$TARGET_NAMESPACE/pods/$PODNAME/binding/
        echo "Assigned $PODNAME to $CHOSEN"
    done
    sleep 1
done
