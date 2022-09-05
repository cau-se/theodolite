---
title: Creating an Execution
has_children: false
parent: Running Benchmarks
nav_order: 6
---

# Creating an Execution

Theodolite Executions look similar to the following example.

<!-- TODO align with upstream -->
```yaml
apiVersion: theodolite.rocks/v1beta1
kind: execution
metadata:
  name: theodolite-example-execution
spec:
  benchmark: "example-benchmark"
  load:
    loadType: "NumSensors"
    loadValues: [25000, 50000]
  resources:
    resourceType: "Instances"
    resourceValues: [1, 2]
  slos:
    - name: "lag trend"
      properties:
        threshold: 2000
  execution:
    metric: "demand"
    strategy:
      name: "RestrictionSearch"
      restrictions:
        - "LowerBound"
      searchStrategy: "LinearSearch"
    duration: 300 # in seconds
    repetitions: 1
    loadGenerationDelay: 30 # in seconds
  configOverrides:
    - patcher:
        type: "SchedulerNamePatcher"
        resource: "uc1-kstreams-deployment.yaml"
      value: "random-scheduler"
```

## Naming and Labeling

Similar to [Kubernetes Jobs](https://kubernetes.io/docs/concepts/workloads/controllers/job/), Theodolite Executions are not automatically deleted after they are finished. Therefore, it is recommended to choose a meaningful name for your Execution. Additionally, you can add labels to your Executions as with any other Kubernetes resource.

## Reference to a Benchmark

An Execution always refers to a Benchmark. For the Execution to run, the Benchmark must be registered with Kubernetes and it must be in state *Ready*. If this is not the case, the Execution will remain in state *Pending*.

## Selecting Load Type, Resource Type and SLOs

As a Benchmark may define multiple supported load and resource types, an Execution has to pick exactly one of each by its name. Additionally, it defines the set of load values and resource values the benchmark should be executed with.
Both these values are represented as integers, which are interpreted in a [Benchmark-specific way](creating-a-benchmark#load-and-resource-types) to configure the SUT and load generator.
Similarly, an Execution must select a subset of the [SLOs defined in the Benchmark](creating-a-benchmark#service-level-objectives-slos). Additionally, these SLOs can be configured by their `properties`.
<!-- TODO: What happpens if slos are not set? -->

## Experimental Setup

According to Theodolite's measurement method, isolated SLO experiments are performed for different combinations of load intensity and resource amounts.
The experimental setup can be configured by:

* A [scalability metric](concepts/metrics) (`metric`). Supported values are `demand` and `capacity`, with `demand` being the default.
* A [search strategy](concepts/search-strategies) (`strategy`), which determines which load and resource combinations should be tested. Supported values are `FullSearch`, `LinearSearch` and `BinarySearch`. Additionally, a `restrictions` can be set to `LowerBound`.
* The `duration` per SLO experiment in seconds.
* The number of repetitions (`repetitions`) for each SLO experiment.
* A `loadGenerationDelay`, specifying the time in seconds before the load generation starts.

## Configuration Overrides

In cases where only small modifications of a system under test should be benchmarked, it is not necessary to [create a new benchmark](creating-a-benchmark).
Instead, also Executions allow to do small reconfigurations, such as switching on or off a specific Pod scheduler.

This is done by defining `configOverrides` in the Execution. Each override consists of a patcher, defining which Kubernetes resource should be patched in which way, and a value the patcher is applied with.

<!-- Further information: API Reference -->
<!-- Further information: How to run -->
