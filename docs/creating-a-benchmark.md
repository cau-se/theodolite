---
title: Creating Benchmarks
has_children: true
nav_order: 5
---

# Creating a Benchmark

Please note that to simply run a benchmark, it is not required to define one. Theodolite comes with a [set of benchmarks](theodolite-benchmarks), which are ready to be executed. See the [fundamental concepts](concepts) page to learn more about our distinction between benchmarks and executions.

A typical benchmark looks like this:

```yaml
apiVersion: theodolite.rocks/v1beta1
kind: benchmark
metadata:
  name: example-benchmark
spec:
  sut:
    resources:
      - configMap:
         name: "example-configmap"
         files:
           - "uc1-kstreams-deployment.yaml"
  loadGenerator:
    resources:
      - configMap:
         name: "example-configmap"
         files:
            - uc1-load-generator-service.yaml
            - uc1-load-generator-deployment.yaml
  resourceTypes:
    - typeName: "Instances"
      patchers:
        - type: "ReplicaPatcher"
          resource: "uc1-kstreams-deployment.yaml"
  loadTypes:
    - typeName: "NumSensors"
      patchers:
        - type: "EnvVarPatcher"
          resource: "uc1-load-generator-deployment.yaml"
          properties:
            variableName: "NUM_SENSORS"
            container: "workload-generator"
        - type: "NumSensorsLoadGeneratorReplicaPatcher"
          resource: "uc1-load-generator-deployment.yaml"
          properties:
            loadGenMaxRecords: "150000"
  slos:
    - name: "lag trend"
      sloType: "lag trend"
      prometheusUrl: "http://prometheus-operated:9090"
      offset: 0
      properties:
        threshold: 3000
        externalSloUrl: "http://localhost:80/evaluate-slope"
        warmup: 60 # in seconds
```

## System under Test (SUT), Load Generator and Infrastructure

In Theodolite, the system under test (SUT), the load generator as well as additional infrastructure (e.g., a middleware) are described by Kubernetes resources files.
All resources defined for the SUT and the load generator are started and stopped for each SLO experiment, with SUT resources being started before the load generator.
Infrastructure resources are kept alive throughout the entire duration of a benchmark run. They avoid time-consuming recreation of software components like middlewares, but should be used with caution so that earlier SLO experiments do not influence later ones.

### Resources

The recommended way to link Kubernetes resources files from a Benchmark is by bundling them in one or multiple ConfigMaps and refer to that ConfigMap from `sut.resources`, `loadGenerator.resources` or `infrastructure.resources`.

**Note:** Theodolite requires that each resources file contains only a single resource (i.e., YAML document).

To create a ConfigMap from all the Kubernetes resources in a directory run:

```sh
kubectl create configmap <configmap-name> --from-file=<path-to-resource-dir>
```

Add an item such as the following one to the `resources` list of the `sut`, `loadGenerator` or `infrastructure` fields.

```yaml
configMap:
  name: example-configmap
  files:
  - example-deployment.yaml
  - example-service.yaml
```

### Actions

Sometimes it is not sufficient to just define resources that are created and deleted when running a benchmark. Instead, it might be necessary to define certain actions that will be executed before running or after stopping the benchmark.
Theodolite supports *actions*, which can run before (`beforeActions`) or after `afterActions` all `sut`, `loadGenerator` or `infrastructure` resources are deployed.
Theodolite provides two types of actions:

#### Exec Actions

Theodolite allows to execute commands on running pods. This is similar to `kubectl exec` or Kubernetes' [container lifecycle handlers](https://kubernetes.io/docs/tasks/configure-pod-container/attach-handler-lifecycle-event/). Theodolite actions can run before (`beforeActions`) or after `afterActions` all `sut`, `loadGenerator` or `infrastructure` resources are deployed.
For example, the following actions will create a file in a pod with label `app: logger` before the SUT is started and delete if after the SUT is stopped:

 ```yaml
  sut:
    resources: # ...
    beforeActions:
      - exec:
          selector:
            pod:
              matchLabels:
                app: logger
            container: logger # optional
          command: ["touch", "file-used-by-logger.txt"]
          timeoutSeconds: 90
    afterActions:
      - exec:
          selector:
            pod:
              matchLabels:
                app: logger
            container: logger # optional
          command: [ "rm", "file-used-by-logger.txt" ]
          timeoutSeconds: 90
```

Theodolite checks if all referenced pods are available for the specified actions. That means these pods must either be defined in `infrastructure` or already deployed in the cluster. If not all referenced pods are available, the benchmark will not be set as `Ready`. Consequently, an action cannot be executed on a pod that is defined as an SUT or load generator resource.

*Note: Exec actions should be used sparingly. While it is possible to define entire benchmarks imperatively as actions, it is considered better practice to define as much as possible using declarative, native Kubernetes resource files.*

#### Delete Actions

Sometimes it is required to delete Kubernetes resources before or after running a benchmark.
This is typically the case for resources that are automatically created while running a benchmark.
For example, Kafka Streams creates internal Kafka topics. When using the [Strimzi](https://strimzi.io/) Kafka operator, we can delete these topics by deleting the corresponding Kafka topic resource.

As shown in the following example, delete actions select the resources to be deleted by specifying their *apiVersion*, *kind* and a [regular expression](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html) for their name.

```yaml
  sut:
    resources: # ...
    beforeActions:
      - delete:
          selector:
            apiVersion: kafka.strimzi.io/v1beta2
            kind: KafkaTopic
            nameRegex: ^some-internal-topic-.*
```


<!--
A Benchmark refers to other Kubernetes resources (e.g., Deployments, Services, ConfigMaps), which describe the system under test, the load generator and infrastructure components such as a middleware used in the benchmark. To manage those resources, Theodolite needs to have access to them. This is done by bundling resources in ConfigMaps.

Suppose the resources needed by your benchmark are defined as YAML files, located in the `resources` directory. You can put them into the ConfigMap `benchmark-resources-custom` by running:
-->


## Load and Resource Types

Benchmarks need to specify at least one supported load and resource type for which scalability can be benchmarked.

Load and resource types are described by a name (used for reference from an Execution) and a list of patchers.
Patchers can be seen as functions, which take a value as input and modify a Kubernetes resource in a patcher-specific way. Examples of patchers are the *ReplicaPatcher*, which modifies the replica specification of a deployment, or the *EnvVarPatcher*, which modifies an environment variable.
See the [patcher API reference](api-reference/patchers) for an overview of available patchers.

If a benchmark is [executed by an Execution](running-benchmarks), these patchers are used to configure SUT and load generator according to the [load and resource values](creating-an-execution) set in the Execution.

## Service Level Objectives SLOs

SLOs provide a way to quantify whether a certain load intensity can be handled by a certain amount of provisioned resources.
In Theodolite, SLOs are evaluated by requesting monitoring data from Prometheus and analyzing it in a benchmark-specific way.
An Execution must at least define one SLO to be checked.

A good choice to get started is defining an SLO of type `generic`:

```yaml
- name: droppedRecords
  sloType: generic
  prometheusUrl: "http://prometheus-operated:9090"
  offset: 0
  properties:
    externalSloUrl: "http://localhost:8082"
    promQLQuery: "sum by(job) (kafka_streams_stream_task_metrics_dropped_records_total>=0)"
    warmup: 60 # in seconds
    queryAggregation: max
    repetitionAggregation: median
    operator: lte
    threshold: 1000
```

All you have to do is to define a [PromQL query](https://prometheus.io/docs/prometheus/latest/querying/basics/) describing which metrics should be requested (`promQLQuery`) and how the resulting time series should be evaluated. With `queryAggregation` you specify how the resulting time series is aggregated to a single value and `repetitionAggregation` describes how the results of multiple repetitions are aggregated. Possible values are
`mean`, `median`, `mode`, `sum`, `count`, `max`, `min`, `std`, `var`, `skew`, `kurt`, `first`, `last` as well as percentiles such as `p99` or `p99.9` and `trend` which computes the increase or decrease slope using linear regression. The result of aggregating all repetitions is checked against `threshold`. This check is performed using an `operator`, which describes that the result must be "less than" (`lt`), "less than equal" (`lte`), "greater than" (`gt`) or "greater than equal" (`gte`) to the threshold.

If you do not want to have a static threshold, you can also define it relatively to the tested load with `thresholdRelToLoad` or relatively to the tested resource value with `thresholdRelToResources`. For example, setting `thresholdRelToLoad: 0.01` means that in each experiment, the threshold is 1% of the generated load.
Even more complex thresholds can be defined with `thresholdFromExpression`. This field accepts a mathematical expression with two variables `L` and `R` for the load and resources, respectively. The previous example with a threshold of 1% of the generated load can thus also be defined with `thresholdFromExpression: 0.01*L`. For further details of allowed expressions, see the documentation of the underlying [exp4j](https://github.com/fasseg/exp4j) library.

In case you need to evaluate monitoring data in a more flexible fashion, you can also change the value of `externalSloUrl` to your custom SLO checker. Have a look at the source code of the [generic SLO checker](https://github.com/cau-se/theodolite/tree/main/slo-checker/generic) to get started.

<!-- Further information: API Reference -->
<!-- Further information: How to deploy -->
