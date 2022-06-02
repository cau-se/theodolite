---
title: Creating Benchmarks
has_children: false
nav_order: 5
---

# Creating a Benchmark

Please note that to simply run a benchmark, it is not required to define one. Theodolite comes with a [set of benchmarks](theodolite-benchmarks), which are ready to be executed. See the [fundamental concepts](benchmarks-and-executions) page to learn more about our distinction between benchmarks and executions.

A typical benchmark looks like this:

```yaml
apiVersion: theodolite.com/v1
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
  kafkaConfig:
    bootstrapServer: "theodolite-kafka-kafka-bootstrap:9092"
    topics:
      - name: "input"
        numPartitions: 40
        replicationFactor: 1
      - name: "theodolite-.*"
        removeOnly: True

```

## System under Test (SUT), Load Generator and Infrastructure

In Theodolite, the system under test (SUT), the load generator as well as additional infrastructure (e.g., a middleware) are described by Kubernetes resources files.
All resources defined for the SUT and the load generator are started and stopped for each SLO experiment, with SUT resources being started before the load generator.
Infrastructure resources live over the entire duration of a benchmark run. They avoid time-consuming recreation of software components like middlewares, but should be used with caution to not let previous SLO experiments influence latte ones.

### Resources

#### ConfigMap

The recommended way to link Kubernetes resources files from a Benchmark is by bundling them in one or multiple ConfigMaps and refer to that ConfigMap from `sut.resources`, `loadGenerator.resources` or `infrastructure.resources`.
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

#### Filesystem

Alternatively, resources can also be read from the filesystem, Theodolite has access to. This usually requires that the Benchmark resources are available in a volume, which is mounted into the Theodolite container.

```yaml
filesystem:
  path: example/path/to/files
  files:
  - example-deployment.yaml
  - example-service.yaml
```

### Actions

Sometimes it is not sufficient to just define resources that are created and deleted when running a benchmark. Instead, it might be necessary to define certain actions that will be executed before running or after stopping the benchmark.

Theodolite allows to execute commands on running pods. This is similar to `kubectl exec` or Kubernetes' [container lifecycle handlers](https://kubernetes.io/docs/tasks/configure-pod-container/attach-handler-lifecycle-event/). Theodolite actions can run before (`beforeActions`) or after `afterActions` all `sut`, `loadGenerator` or `infrastructure` resources are deployed.
For example, the following actions will create a file in a pod with label `app: logger` before the SUT is started and delete if after the SUT is stopped:

 ```yaml
  sut:
    resources: # ...
    beforeActions:
      - selector:
          pod:
            matchLabels:
              app: logger
        exec:
          command: ["touch", "file-used-by-logger.txt"]
          timeoutSeconds: 90
    afterActions:
      - selector:
          pod:
            matchLabels:
              app: logger
        exec:
          command: [ "rm", "file-used-by-logger.txt" ]
          timeoutSeconds: 90
```

Theodolite checks if all referenced pods are available for the specified actions. That means these pods must either be defined in `infrastructure` or already deployed in the cluster. If not all referenced pods are available, the benchmark will not be set as `Ready`. Consequently, an action cannot be executed on a pod that is defined as an SUT or load generator resource.

*Note: Actions should be used sparingly. While it is possible to define entire benchmarks imperatively as actions, it is considered better practice to define as much as possible using declarative, native Kubernetes resource files.*

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

## Kafka Configuration

Theodolite allows to automatically create and remove Kafka topics for each SLO experiment by setting a `kafkaConfig`.
`bootstrapServer` needs to point your Kafka cluster and `topics` configures the list of Kafka topics to be created/removed.
For each topic, you configure its name, the number of partitions and the replication factor.

With the `removeOnly: True` property, you can also instruct Theodolite to only remove topics and not create them.
This is useful when benchmarking SUTs, which create topics on their own (e.g., Kafka Streams and Samza applications).
For those topics, also wildcards are allowed in the topic name and, of course, no partition count or replication factor must be provided.


<!-- Further information: API Reference -->
<!-- Further information: How to deploy -->
