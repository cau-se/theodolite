## The Benchmark Object

The *benchmark* object defines all static components of an execution of a benchmark with Theodolite.

```yaml
name: String
appResource:
  - String
  ...
loadGenResource:
  - String
  ...
resourceTypes:
  - typeName: String
    patchers:
      - type: String
        resources: String
        <Patcher Arguments> ...
      ...
loadTypes:
  - typeName: String
  patchers:
    - type: String
      resources: String
      <Patcher Arguments> ...
    ...
kafkaConfig:
  bootstrapServer: String
  topics:
    - name: String
      numPartitions: UnsignedInt
      replicationFactor: UnsignedInt
    ...
```

The properties have the following meaning:

* **name**: The name of the *benchmark*
* **appResource**: A list of file names that reference Kubernetes resources and that are deployed on the cluster for the system under test (SUT).
* **loadGenResources**: A list of file names that reference Kubernetes resources and that are deployed on the cluster for the load generator.
* **resourceTypes**: A list of resource types that can be scaled for this *benchmark*. For each resource type the concrete values are defined in the *execution* object. Each resource type has the following structure:
    * **typeName**: Name of the resource type.
    * **patchers**: List of patchers used to scale this resource type. Each patcher has the following structure:
        * **type**: Type of the Patcher. The concrete types can be looked up in the list of patchers. 
        * **resources**: Specifies the Kubernetes resource to be patched.
        * **Patcher Arguments**: (Optional) Patcher specific additional arguments.
* **resourceTypes**: A list of load types that can be scaled for this *benchmark*. For each load type the concrete values are defined in the *execution* object. Each load type has the following structure:
    * **typeName**: Name of the load type.
    * **patchers**: List of patchers used to scale * **resourceTypes**: A list of resource types that can be scaled for this *benchmark*. For each resource type the concrete values are defined in the *execution* resource object.Each resource type has the following structure:
    * **typeName**: Name of the resource type.
    * **patchers**: List of patchers used to scale this resource type. Each patcher has the following structure:
        * **type**: Type of the Patcher. The concrete types can be looked up in the list of patchers. 
        * **resources**: Specifies the Kubernetes resource to be patched.
        * **Patcher Arguments**: (Optional) Patcher specific additional arguments.this load type. Each patcher has the following structure:
        * **type**: Type of the Patcher. The concrete types can be looked up in the list of patchers. 
        * **resources**: Specifies the Kubernetes resource to be patched.
        * **Patcher Arguments**: (Optional) Patcher specific additional arguments.
* **kafkaConfig**: Contains the Kafka configuration.
    * **bootstrapServers**: The bootstrap servers connection string.
    * **topics**: List of topics to be created for each experiment.
        * **name**: The name of the topic.
        * **numPartitions**: The number of partitions of the topic.
        * **replicationFactor**: The replication factor of the topic.
    

## The Execution Object

A benchmark can be executed for different SUTs, by different users and multiple times. We call such an execution of a benchmark simply an *execution*. The *execution* object defines all conrete values of an Execution.


```yaml
name: String
benchmark: String
load:
  loadType: String
  loadValues:
    - UnsignedInt
    ...
resources:
  resourceType: String
  resourceValues:
    - UnsignedInt
    ...
slos:
  - sloType: String
    threshold: UnsignedInt
    prometheusUrl: String
    externalSloUrl: String
    offset: SignedInt
    warmup: UnsignedInt
  ...
executions:
  strategy: "LinearSearch" or "BinarySearch"
  duration: UnsignedInt
  repetition: UnsignedInt
  restrictions:
    - "LowerBound"
    ...
configurationOverrides:
  - patcher:
      type: String
      resource: String
      <Patcher Arguments> ...
  ...
```

The properties have the following meaning:

* **name**: The name of the *execution*
* **benchmark**: The name of the *benchmark* this *execution* is referring to.
* **load**: Specifies the load values that are benchmarked.
  * **loadType**: The type of the load. It must match one of the load types specified in the referenced *benchmark*.
  * **loadValues**: List of load values for the specified load type.
* **resources**: Specifies the scaling resource that is benchmarked.
  * **resourceType**: The type of the resource. It must match one of the resource types specified in the referenced *benchmark*.
  * **resourceValues**: List of resource values for the specified resource type.
* **slos**: List of the Service Level Objective (SLO) for this *execution*. Each SLO has the following fields:
  * **sloType**: The type of the SLO. It must match 'lag trend'.
  * **threshold**: 
  * prometheusUrl: String
  * externalSloUrl: String
  * offset: SignedInt
  * warmup: UnsignedInt
executions:
  strategy: "LinearSearch" or "BinarySearch"
  duration: UnsignedInt
  repetition: UnsignedInt
  restrictions:
    - "LowerBound"
    ...
configurationOverrides:
  - patcher:
      type: String
      resource: String
      <Patcher Arguments> ...
  ...

## Patchers