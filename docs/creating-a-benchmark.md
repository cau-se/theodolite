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
    - "uc1-kstreams-deployment.yaml"
  loadGenResource:
    - "uc1-load-generator-deployment.yaml"
    - "uc1-load-generator-service.yaml"
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
