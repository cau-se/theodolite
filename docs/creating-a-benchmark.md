---
title: Creating Benchmarks
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
    bootstrapServer: "theodolite-cp-kafka:9092"
    topics:
      - name: "input"
        numPartitions: 40
        replicationFactor: 1
      - name: "theodolite-.*"
        removeOnly: True

```

## System under Test (SUT) and Load Generator Resources

In Thedolite, the system under test (SUT) and the load generator are described by Kubernetes resources files.
Based on these files, both the SUT and the load generator are started and stopped for each SLO experiment.
Optionally, you can also define infrastructure as Kubernetes resources files which lives over the entire duration of a benchmark run.

The recommended way to link Kubernetes resources files from a Benchmark is by bundling them in one or multiple ConfigMaps and refer to these ConfigMap from `sut.resources`, `loadGenerator.resources` or `infrastructure.resources`.
To create a ConfigMap from all the Kubernetes resources in a directory run:

```sh
kubectl create configmap <configmap-name> --from-file=<path-to-resource-dir>
```

<!--
A Benchmark refers to other Kubernetes resources (e.g., Deployments, Services, ConfigMaps), which describe the system under test, the load generator and infrastructure components such as a middleware used in the benchmark. To manage those resources, Theodolite needs to have access to them. This is done by bundling resources in ConfigMaps.

Suppose the resources needed by your benchmark are defined as YAML files, located in the `resources` directory. You can put them into the ConfigMap `benchmark-resources-custom` by running:
-->


## Load and Resource Types

Benchmarks need to specify at least one supported load and resource type for which scalability can be benchmarked.

Load and resource types are described by a name (used for reference from an Execution) and a list of patchers.

## Kafka Configuration

Theodolite allows to automatically create and remove Kafka topics for each SLO experiment.
Use the `removeOnly: True` property for topics which are created automatically by the SUT.
For those topics, also wildcards are allowed in the topic name.


<!-- Further information: API Reference -->
<!-- Further information: How to deploy -->
