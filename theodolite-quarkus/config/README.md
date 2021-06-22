## The Benchmark Object

The *benchmark* object defines all static components of an execution of a benchmark with Theodolite.
An exapmle for a benchmark object is given in [example-benchmark-yaml-resource](example-benchmark-yaml-resource.yaml).


A **Benchmark** is a [*standard tool for the competitive evaluation and comparison of competing systems or components according to specific characteristics, such as performance, dependability, or security*](https://doi.org/10.1145/2668930.2688819). In Theodolite, we have [specification-based benchmarks](https://doi.org/10.1145/2668930.2688819), or at least something very close to that. That is, our benchmarks are architectural descriptions---in our case---[of typical use cases of stream processing in microservices](https://doi.org/10.1016/j.bdr.2021.100209) (e.g. our UC1). Hence, we don't really have a piece of software, which represents a benchmark. We only have implementations of benchmarks, e.g. an implementation of UC1 with Kafka Streams. For simplification, we call these *benchmark implementations* simply *benchmarks*.

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
        properties:
          <Patcher Arguments> ...
      ...
loadTypes:
  - typeName: String
  patchers:
    - type: String
      resources: String
      properties:
        <Patcher Arguments> ...
    ...
kafkaConfig:
  bootstrapServer: String
  topics:
    - name: String
      numPartitions: UnsignedInt
      replicationFactor: UnsignedInt
    - name: String
      removeOnly: bool
    ...
```

The properties have the following definitions:

* **name**: The name of the *benchmark*
* **appResource**: A list of file names that reference Kubernetes resources that are deployed on the cluster for the system under test (SUT).
* **loadGenResources**: A list of file names that reference Kubernetes resources that are deployed on the cluster for the load generator.
* **resourceTypes**: A list of resource types that can be scaled for this *benchmark*. For each resource type the concrete values are defined in the *execution* object. Each resource type has the following structure:
    * **typeName**: Name of the resource type.
    * **patchers**: List of [patchers](#Patchers) used to scale this resource type. Each patcher has the following structure:
        * **type**: Type of the [patcher](#Patchers). The concrete types can be looked up in the list of [patchers](#Patchers). 
        * **resources**: Specifies the Kubernetes resource to be patched.
        *  **properties**: *Patcher Arguments*: (Optional) Patcher specific additional arguments.
* **loadTypes**: A list of load types that can be scaled for this *benchmark*. For each load type the concrete values are defined in the *execution* object. Each load type has the following structure:
    * **typeName**: Name of the load type.
    * **patchers**: List of patchers used to scale * **resourceTypes**: A list of resource types that can be scaled for this *benchmark*. For each resource type the concrete values are defined in the *execution* resource object.Each resource type has the following structure:
    * **typeName**: Name of the resource type.
    * **patchers**: List of patchers used to scale this resource type. Each patcher has the following structure:
        * **type**: Type of the Patcher. The concrete types can be looked up in the list of patchers. 
        * **resources**: Specifies the Kubernetes resource to be patched.
        * **properties**: *Patcher Arguments*: (Optional) Patcher specific additional arguments as Map<String, String>.
* **kafkaConfig**: Contains the Kafka configuration.
    * **bootstrapServers**: The bootstrap servers connection string.
    * **topics**: List of topics to be created for each [experiment](#Experiment). Alternative theodolite offers the possibility to remove certain topics after each experiment.
        * **name**: The name of the topic.
        * **numPartitions**: The number of partitions of the topic.
        * **replicationFactor**: The replication factor of the topic.
        * **removeOnly**: determines if this topic should only be deleted after each experiement. For removeOnly topics the name can be a RegEx describing the topic.
    

## The Execution Object

A benchmark can be executed for different SUTs, by different users and multiple times. We call such an execution of a benchmark simply an *execution*. The *execution* object defines all conrete values of an Execution.
An exapmle for an execution object is given in [example-execution-yaml-resource](example-benchmark-yaml-resource.yaml).


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
      properties:
        <Patcher Arguments> ...
  ...
```

The properties have the following definitions:

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
  * **threshold**: The threshold the SUT should meet for a sucessful experiment.
  * **prometheusUrl**: Connection string for promehteus.
  * **externalSloUrl**: Connection string for a external slo analysis.
  * **offset**: Hours by which the start and end timestamp will be shifted (for different timezones).
  * **warmup**: Seconds of time that are ignored in the analysis.
* **executions**: Defines the overall parameter for the execution.
  * **strategy**: Defines the used strategy for the execution: either 'LinearSearch' or 'BinarySearch'
  * **duration**: Defines the duration of each [experiment](#Experiment) in seconds.
  * **repetition**: Unused.
  * **restrictions**: List of restriction strategys used to delimit the search space.
    **- LowerBound**: Currently only supported *restriction strategy*.
* **configurationOverrides**: List of patchers that are used to override existing configurations.
  * **patcher**: Patcher used to patch a resource. Each patcher has the following structure:
        * **type**: Type of the Patcher. The concrete types can be looked up in the list of patchers. 
        * **resources**: Specifies the Kubernetes resource to be patched.
        * **properties**: *Patcher Arguments*: (Optional) Patcher specific additional arguments.

## Patchers

* **ReplicaPatcher**: Allows to modify the number of Replicas for a kubernetes deployment.
  * **type**: "ReplicaPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"

* **NumSensorsLoadGeneratorReplicaPatcher**: Allows to scale the nummer of load generators. Scales arcording to the following formular: (value + 15_000 - 1) / 15_000
  * **type**: "NumSensorsLoadGeneratorReplicaPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"

* **NumNestedGroupsLoadGeneratorReplicaPatcher**: Allows to scale the nummer of load generators. Scales arcording to the following formular: (4^(value) + 15_000 -1) /15_000
  * **type**: "NumNestedGroupsLoadGeneratorReplicaPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"

* **ReplicaPatcher**: Allows to modify the number of Replicas for a kubernetes deployment.
  * **type**: "ReplicaPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"

* **EnvVarPatcher**: Allows to modify the value of an environment variable for a container in a kubernetes deployment. 
  * **type**: "EnvVarPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"
  * **properties**:
    * container: "workload-generator"
    * variableName: "NUM_SENSORS"

* **NodeSelectorPatcher**: Changes the node selection field in kubernetes resources.
  * **type**: "NodeSelectorPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"
  * **properties**:
    * variableName: "env"
  * **value**: "prod"

* **ResourceLimitPatcher**: Changes the resource limit for a kubernetes resource.
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * container: "uc-application"
    * variableName: "cpu" or "memory"
  * **value**:"1000m" or "2Gi"
  
* **SchedulerNamePatcher**: Changes the sheduler for kubernetes resources.
  * **type**: "SchedulerNamePatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **value**: "random-scheduler"

* **ImagePatcher**: Changes the image of a kubernetes resource. Currently not fully implemented.
  * **type**: "ImagePatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * container: "uc-application"
  * **value**: "dockerhubrepo/imagename"



## Experiment
According to [our benchmarking method](https://doi.org/10.1016/j.bdr.2021.100209), the execution of a benchmark requires performing multiple **Experiments**. I think what is actually done within/during an experiment is another level of detail. (But just for the sake of completeness: In an experiment, the benchmark implementation is deployed, load is generated according to the benchmark specification, some SLOs are monitored continuously, etc.)



