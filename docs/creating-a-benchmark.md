---
title: Creating a Benchmark
has_children: false
nav_order: 5
---

# Creating a Benchmark

Please note that to simply run a benchmark, it is not required to define one. Theodolite comes with a set of benchmarks, which are ready to be executed. See the Benchmarks and Executions docs page to learn more about our distinction between benchmarks and executions.

A typical benchmark looks as follow.

```yaml
apiVersion: theodolite.com/v1
kind: benchmark
metadata:
  name: uc1-kstreams
spec:
  appResource:
    - "custom/uc1-kstreams-deployment.yaml"
  loadGenResource:
    - "custom/uc1-load-generator-deployment.yaml"
    - "custom/uc1-load-generator-service.yaml"
  resourceTypes:
    - typeName: "Instances"
      patchers:
        - type: "ReplicaPatcher"
          resource: "custom/uc1-kstreams-deployment.yaml"
  loadTypes:
    - typeName: "NumSensors"
      patchers:
        - type: "EnvVarPatcher"
          resource: "custom/uc1-load-generator-deployment.yaml"
          properties:
            variableName: "NUM_SENSORS"
            container: "workload-generator"
        - type: "NumSensorsLoadGeneratorReplicaPatcher"
          resource: "custom/uc1-load-generator-deployment.yaml"
          properties:
            loadGenMaxRecords: "15000"
  kafkaConfig:
    bootstrapServer: "theodolite-cp-kafka:9092"
    topics:
      - name: "input"
        numPartitions: 40
        replicationFactor: 1
      - name: "theodolite-.*"
        removeOnly: True

```

### System under Test (SUT) and Load Generator Resources

In Thedolite, the system under test (SUT) and the load generator are described by Kubernetes resources files.
Based on these files, both the SUT and the load generator are started and stopped for each SLO experiment.

All Kubernetes resource files listed under `appResource` and `loadGenResource` must be accessible by Theodolite.
The recommended way to achieve this is by bundling them in one or multiple ConfigMaps, which can be done by:

```sh
kubectl create configmap <configmap-name> --from-file=<path-to-resources>
```

### Load and Resource Types

Benchmarks need to specify at least one supported load and resource type for which scalability can be benchmarked.

Load and resource types are described by a name (used for reference from an Execution) and a list of patchers.

### Kafka Configuration

Theodolite allows to automatically create and remove Kafka topics for each SLO experiment.
Use the `removeOnly: True` property for topics which are created automatically by the SUT.
For those topics, also wildcards are allowed in the topic name.


<!-- Further information: API Reference -->
<!-- Further information: How to deploy -->

-----

* Create a benchmark for operator mode
* Create a benchmark for standalone mode
* Deploy a benchmark

## Creating a benchmark for operator mode

### App and Workload Generator Resources

### Load and Resource Types

### Kafka Configuration


### Make benchmark Kubernetes resources accessible

* A benchmark is defined by a set of Kubernetes resources, which are automatically deployed and scaled by Theodolite.
* To make these resources accessible by the Theodolite operator, we need to add them to a volume, already mounted by Theodolite. The easiest way to do this is by adding the resources to the ConfigMap `benchmark-resources-custom`, which is mounted by Theodolite with its default installation.





## Creating a benchmark for standalone mode

Creating a benchmark in standalone mode is similar to operator mode. However,
instead of defining a benchmark as `Benchmark` resource, it is defined as a
benchmark YAML file. Such a file deviates in the following points from a
`Benchmark` resource:

* The fields `apiVersion`, `kind` and `metadata` should be removed.
* The benchmark's name (`metadata.name` in `Benchmark` resources) must be defined by the top-level field `name`.
* Everything that is defined in `spec` has to be moved to the top-level.

**TODO: example**

**TODO: Create a ConfigMap containing the benchmark YAML files as well as all Kubernetes resources for that benchmark + deploy**