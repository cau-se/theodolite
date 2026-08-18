---
title: Manually Testing Examples with k3d
has_children: false
parent: Contributing
nav_order: 20
---

# Manually Testing Examples with k3d

This page describes how to run the HTTP server example end-to-end against locally built images using [k3d](https://k3d.io/). It also covers running the Helm chart test. Use this to verify changes to the operator, the SLO checkers, or the example itself before pushing.

## Prerequisites

- [k3d](https://k3d.io/) (≥ v5)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [helm](https://helm.sh/docs/intro/install/) (≥ v3)
- [Docker](https://docs.docker.com/get-docker/)
- JDK 21 (for building the operator)

## Step 1: Create a k3d cluster

```sh
k3d cluster create theodolite-test --agents 1 # --k3s-arg '--kubelet-arg=eviction-hard=imagefs.available<1%,nodefs.available<1%@agent:*' --k3s-arg '--kubelet-arg=eviction-minimum-reclaim=imagefs.available=1%,nodefs.available=1%@agent:*' # Add if you see "disk pressure" errors on the k3d nodes

```

## Step 2: Compile the operator

```sh
theodolite/gradlew -p theodolite quarkusBuild --no-daemon
```

> If your default JDK is not 21, set it first, e.g. with sdkman: `sdk use java 21.0.6-tem`

## Step 3: Build and import all images

Build each component image locally and import it into the k3d cluster so Kubernetes can pull them without a registry:

```sh
docker build -f theodolite/src/main/docker/Dockerfile.jvm -t theodolite:dev theodolite/
docker build -t theodolite-slo-checker-generic:dev slo-checker/generic/

k3d image import \
  theodolite:dev \
  theodolite-slo-checker-generic:dev \
  -c theodolite-test
```

## Step 4: Install the Helm chart

Install Theodolite with the Kafka-less preconfig, pointing every image to the locally built `:dev` tags:

```sh
helm dependencies update ./helm
helm install theodolite ./helm \
  -f helm/preconfigs/kafka-less.yaml \
  --set operator.image=theodolite \
  --set operator.imageTag=dev \
  --set operator.imagePullPolicy=IfNotPresent \
  --set operator.sloChecker.generic.image=theodolite-slo-checker-generic \
  --set operator.sloChecker.generic.imageTag=dev \
  --set operator.sloChecker.generic.imagePullPolicy=IfNotPresent
```

Wait for all pods to be ready:

```sh
kubectl wait --for=condition=Ready pod -l app=theodolite --timeout=120s
```

## Step 5: Run the HTTP server example end-to-end

```sh
cd theodolite/examples/http-server

kubectl apply -f example-configmap.yaml
kubectl apply -f example-benchmark.yaml
kubectl apply -f example-execution.yaml

kubectl get executions -w
```

The execution transitions through `Pending → Running → Finished`. A successful run produces CSV result files in the operator pod under `/results/`. You can copy them out with:

```sh
kubectl cp \
  $(kubectl get pod -l app=theodolite -o jsonpath="{.items[0].metadata.name}"):results \
  . -c results-access
```

Check the operator logs for SLI fetch and SLO evaluation output:

```sh
kubectl logs -l app=theodolite -c theodolite --tail=200
```

Clean up:

```sh
kubectl delete -f example-execution.yaml
kubectl delete -f example-benchmark.yaml
kubectl delete -f example-configmap.yaml
```

## Step 6: Run the Helm test

The Helm test re-deploys the example through the chart's built-in test mechanism and verifies the execution reaches `Finished` state within 3 minutes:

```sh
helm test theodolite
```

Expected output:

```
NAME: theodolite
LAST DEPLOYED: ...
STATUS: deployed
...
TEST SUITE:     theodolite-grafana-test
...
Phase: Succeeded
TEST SUITE:     theodolite-test-example-files
...
Phase: Succeeded
TEST SUITE:     theodolite-test-prometheus
...
Phase: Succeeded
TEST SUITE:     theodolite-test-example
...
Phase: Succeeded
```

All four test suites must show `Phase: Succeeded`.

## Step 7: Teardown

```sh
k3d cluster delete theodolite-test
```
