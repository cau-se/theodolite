---
title: Running Benchmarks
has_children: false
nav_order: 4
---

# Running Scalability Benchmarks

Running scalability benchmarks with Theodolite involves two things:

1. [Deploying a benchmark to Kubernetes](#deploying-a-benchmark)
1. [Creating an execution](#creating-an-execution), which describes the experimental setup for running the benchmark

## Deploying a Benchmark

A benchmark specification consists of two things:

* A Benchmark resource YAML file
* A set of Kubernetes resource YAML files used by the benchmark
<!-- - One or multiple ConfigMap YAML files containing all the Kubernetes resource used by the benchmark as YAML files -->

These files are usually provided by benchmark designers.
For example, we ship Theodolite with a set of [benchmarks for event-driven microservices](theodolite-benchmarks).
Alternatively, you can also [create your own benchmarks](creating-a-benchmark).

<!-- Theodolite >v0.5
Once you have collected all Kubernetes resources for the benchmark (Benchmark resource and ConfigMaps) in a specific directory, you can deploy everything to Kubernetes by running:

```sh
kubectl apply -f .
```
-->

### Create the Benchmark

Suppose your Benchmark resource is stored in `example-benchmark.yaml`, you can deploy it to Kubernetes by running:

```sh
kubectl apply -f example-benchmark.yaml
```

To list all benchmarks currently deployed run:

```sh
kubectl get benchmarks
```

<!-- TODO output-->

### Create the Benchmark Resources ConfigMaps

A Benchmark resource refers to other Kubernetes resources (e.g., Deployments, Services, ConfigMaps), which describe the system under test, the load generator and infrastructure components such as a middleware used in the benchmark. To manage those resources, Theodolite needs to have access to them. This is done by bundling resources in ConfigMaps.

Suppose the resources needed by your benchmark are defined as YAML files, located in the `resources` directory. You can put them into the ConfigMap `benchmark-resources-custom` by running:

```sh
kubectl create configmap benchmark-resources-custom --from-file=./resources -o yaml --dry-run=client | kubectl apply -f -
```


## Creating an Execution

To run a benchmark, an Execution YAML file needs to be created such as the following one.

```yaml
apiVersion: theodolite.com/v1
kind: execution
metadata:
  name: theodolite-example-execution # (1) give your execution a name
spec:
  benchmark: "uc1-kstreams" # (2) refer to the benchmark to be run
  load:
    loadType: "NumSensors" # (3) chose one of the benchmark's load types
    loadValues: [25000, 50000] # (4) select a set of load intensities
  resources:
    resourceType: "Instances" # (5) chose one of the benchmark's resource types
    resourceValues: [1, 2] # (6) select a set of resource amounts
  slos: # (7) set your SLOs
    - sloType: "lag trend"
      prometheusUrl: "http://prometheus-operated:9090"
      offset: 0
      properties:
        threshold: 2000
        externalSloUrl: "http://localhost:80/evaluate-slope"
        warmup: 60 # in seconds
  execution:
    strategy: "LinearSearch" # (8) chose a search strategy
    restrictions: ["LowerBound"] # (9) add restrictions for the strategy
    duration: 300 # (10) set the experiment duration in seconds
    repetitions: 1 # (11) set the number of repetitions
    loadGenerationDelay: 30 # (12) configure a delay before load generation
  configOverrides: []
```

See [Creating an Execution](creating-an-execution) for a more detailed explanation on how to create Executions.

Suppose your `Execution` resource is stored in `example-execution.yaml`, you
can deploy it by running:

```sh
kubectl apply -f example-execution.yaml
```

To list all deployed executions run:

```sh
kubectl get executions
```

The output is similar to this:

```
NAME                           STATUS    DURATION   AGE
theodolite-example-execution   RUNNING   13s        14s
```

The `STATUS` field will tell you whether a benchmark execution has been
started, finished or failed due to some error. The `DURATION` field tells you
for how long that execution is running (so far). Similar to a Kubernetes Job,
an `Execution` is not automatically deleted once it is finished. This makes it
easier to keep track of all the benchmark executions and to organize benchmark
results.

Theodolite provides additional information on the current status of an Execution by producing Kubernetes events. To see them:

```sh
kubectl describe execution <execution-name>
```
