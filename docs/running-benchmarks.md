---
title: Running Benchmarks
has_children: true
nav_order: 4
---

# Running Scalability Benchmarks

Running scalability benchmarks with Theodolite involves the following steps:

1. [Deploying a benchmark to Kubernetes](#deploying-a-benchmark)
1. [Creating an execution](#creating-an-execution), which describes the experimental setup for running the benchmark
1. [Accessing benchmark results](#accessing-benchmark-results)


## Deploying a Benchmark

A benchmark specification consists of two things:

* A Benchmark resource YAML file
* One or more ConfigMap YAML files containing all the Kubernetes resources used by the benchmark

These files are usually provided by benchmark designers.
For example, we ship Theodolite with a set of [benchmarks for event-driven microservices](theodolite-benchmarks).
Alternatively, you can also [create your own benchmarks](creating-a-benchmark).

Suppose your benchmark is defined in `example-benchmark.yaml` and all resources required by this benchmark are bundled in `example-configmap.yaml`.
You can deploy both to Kubernetes by running:

```sh
kubectl apply -f example-benchmark.yaml
kubectl apply -f example-configmap.yaml
```

To list all benchmarks currently deployed run:

```sh
kubectl get benchmarks
```

The output is similar to this:

```
NAME                AGE   STATUS
example-benchmark   81s   Ready
```

The status of a benchmark tells you whether executions of it are ready to run:
* *Ready* means that Theodolite has access to all resources referred from the benchmark.
* *Pending* implies that not all benchmark resources are available (yet). Please ensure that you have deployed them, for example, by running `kubectl get configmaps`.


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
theodolite-example-execution   Running   13s        14s
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


## Accessing Benchmark Results

Theodolite stores the results of benchmark executions in CSV files, whose names are starting with `exp<id>_...`. These files can be read and analyzed by Theodolite's analysis notebooks.

If [persisting results](installation#persisting-results) is enabled in Theodolite's installation, the result files are stored in a PersistentVolume. Depending on the cluster setup or Theodolite's configuration, the content of these volumes can usually be mounted into your host system in some way or accessed via your cloud provider.

For installations without persistence, but also as an alternative for installations with persistence, we provide a second option to access results: Theodolite comes with a *results access sidecar*. It allows to copy all benchmark results from the Theodolite pod to your current working directory on your host machine with the following command:

```sh
kubectl cp $(kubectl get pod -l app=theodolite -o jsonpath="{.items[0].metadata.name}"):/results . -c results-access
```
