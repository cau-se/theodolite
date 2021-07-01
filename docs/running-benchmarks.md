---
title: Running Benchmarks
has_children: false
nav_order: 2
---

# Running Scalability Benchmarks

There are two options to run scalability benchmarks with Theodolite:

1. The [Theodolite Operator](#running-benchmarks-with-the-theodolite-operator) is a long-running service in your cluster, which accepts submitted benchmarks and executes them.
2. In [standalone mode](#running-benchmarks-in-standalone-mode), Theodolite is started as a Kubernetes Job and only runs for the duration of one benchmark execution.

While the operator is a bit more complex to install then the standalone mode,
it makes it way easier to manage benchmarks and their executions. In
particular, it allows to submit a set of experiments, which are then executed
automatically one after another.
**Therefore, running benchmarks with the operator is recommended.** 


## Running Benchmarks with the Theodolite Operator

The general process for running Theodolite benchmarks in standalone mode is as follows:

1. Create and deploy a new `Benchmark` resource or deploy one of the already existing ones.
2. Define your benchmark execution as an `Execution` resource and deploy it.

### 1. Creating a benchmark

Benchmarks are defined as resources of our custom resource definition
`Benchmark`. You can either create a new `Benchmark` for your custom benchmark
or system under test or deploy one of the existing benchmarks, which can be
found in [`theodolite-benchmarks/definitions`](theodolite-benchmarks/definitions). **TODO link**

Suppose your `Benchmark` resource is stored in `example-benchmark.yaml`, you
can deploy it by running:

```sh
kubectl apply -f example-benchmark.yaml
```

To see the list of all deployed benchmarks run:

```sh
kubectl get benchmarks
```

Additionally you need to make all your benchmark's Kubernetes resources available to the operator.

**TODO benchmark resources**

Once your benchmark is deployed, it is ready to be executed.

### 2. Creating an execution

To execute a benchmark, you need to describe this execution by creating an `Execution` resource.

**TODO: details execution** 

Suppose your `Execution` resource is stored in `example-execution.yaml`, you
can deploy it by running:

```sh
kubectl apply -f example-execution.yaml
```

To see the list of all deployed benchmarks run:

```sh
kubectl get executions
```

The `STATUS` field will tell you whether a benchmark execution has been
started, finished or failed due to some error. The `DURATION` field tells you
for how long that execution is running (so far). Similar to a Kubernetes Job,
an `Execution` is not automatically deleted once it is finished. This makes it
easier to keep track of all the benchmark executions and to organize benchmark
results.


## Running Benchmarks in Standalone Mode

The general process for running Theodolite benchmarks in standalone mode is as follows:

1. Create a benchmark by writing a YAML file or select one of the already existing ones and create a ConfigMap from it.
2. Define your benchmark execution as a YAML file and create a ConfigMap from it. We provide a template for this.
3. Create a Theodolite Job from our template and mount your benchmark and benchmark execution with it.

### 1. Creating a benchmark

Creating a benchmark in standalone mode is similar to operator mode. However,
instead of defining a benchmark as `Benchmark` resource, it is defined as a
benchmark YAML file. Such a file deviates in the following points from a
`Benchmark` resource:

* The fields `apiVersion`, `kind` and `metadata` should be removed.
* The benchmark's name (`metadata.name` in `Benchmark` resources) must be defined by the top-level field `name`.
* Everything that is defined in `spec` has to be moved to the top-level.

**TODO: example**

**TODO: Create a ConfigMap containing the benchmark YAML files as well as all Kubernetes resources for that benchmark + deploy**

### 2. Creating an execution

**TODO: see above**


### 3. Create a Theodolite Job

**TODO example**