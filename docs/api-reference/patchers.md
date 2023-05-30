---
title: Patchers
has_children: false
parent: API Reference
nav_order: 2
---

# Patchers

Patchers can be seen as functions which take a Kubernetes resource and a value as input and produce a modified Kubernetes resource based on a patcher-specific method.
For example, there are patchers available that modify the number of replicas of a Deployment or change an environment variable.

In general, patchers are defined as follows:

```yaml
type: GenericResourcePatcher # The name of the patcher type
resource: kubernetes-resource.yaml # The resource file to be patched
properties: # Patcher type-specific properties
  path: spec/to/field/to/change
  type: integer
```

Currently, the following patcher types are available:

## ReplicaPatcher

Modifies the number of replicas for a Kubernetes Deployment or StatefulSet.

Supported Kubernetes resources: Deployment, StatefulSet.

**DONE**

## NumSensorsLoadGeneratorReplicaPatcher

Modifies the number of load generators, according to the following formula: *(value + loadGenMaxRecords - 1) / loadGenMaxRecords*

Supported Kubernetes resources: Deployment, StatefulSet.

| Property | Description | Default |
|:----|:----|:----|
| loadGenMaxRecords |  |  |

## NumNestedGroupsLoadGeneratorReplicaPatcher

***TODO Description***

Supported Kubernetes resources: Deployment, StatefulSet.

| Property | Description | Default |
|:----|:----|:----|
| loadGenMaxRecords |  |  |
| numSensors |  |  |

## DataVolumeLoadGeneratorReplicaPatcher

***TODO Description***

Supported Kubernetes resources: Deployment, StatefulSet.

| Property | Description | Default |
|:----|:----|:----|
| maxVolume |  |  |
| container |  |  |
| variableName |  |  |

## EnvVarPatcher

Modifies an environment variable.

Supported Kubernetes resources: Pod, Deployment, StatefulSet.

| Property | Description | Optional |
|:----|:----|:----|
| container | Name of the container for which to set the environment variable. |  |
| variableName | Name of the environment variable to be patched. |  |
| factor | An integer to multiply the value with. Ignored if not an integer. | yes |
| prefix | A string prefix for the value. | yes |
| suffix | A string suffix for the value. | yes |

## ConfigMapYamlPatcher

***TODO Description***

Supported Kubernetes resources: ConfigMap.

| Property | Description | Default |
|:----|:----|:----|
| fileName |  |  |
| variableName |  |  |
| factor | An integer to multiply the value with. Ignored if not an integer. | yes |
| prefix | A string prefix for the value. | yes |
| suffix | A string suffix for the value. | yes |

## ConfigMapPropertiesPatcher

***TODO Description***

Supported Kubernetes resources: ConfigMap.

| Property | Description | Default |
|:----|:----|:----|
| fileName |  |  |
| variableName |  |  |
| factor | An integer to multiply the value with. Ignored if not an integer. | yes |
| prefix | A string prefix for the value. | yes |
| suffix | A string suffix for the value. | yes |

## NodeSelectorPatcher

***TODO Description***

Supported Kubernetes resources: Pod, Deployment, StatefulSet.

| Property | Description | Default |
|:----|:----|:----|
| nodeLabelName |  |  |


## ResourceLimitPatcher

***TODO Description***

Supported Kubernetes resources: Pod, Deployment, StatefulSet.

| Property | Description | Optional |
|:----|:----|:----|
| container | The name of the container to be patched. | |
| limitedResource | The resource to be limited (e.g., *cpu* or *memory*) | |
| format | Format added to the provided value (e.g., `GBi` or `m`, see Kubernetes documentation for valid values. | yes |
| factor | An integer to multiply the value with. Both *factor* and *format* are ignored if *factor* is not an integer. | yes |


## ResourceRequestPatcher

***TODO Description***

Supported Kubernetes resources: Pod, Deployment, StatefulSet.

| Property | Description | Optional |
|:----|:----|:----|
| container | The name of the container to be patched. | |
| limitedResource | The resource to be limited (e.g., *cpu* or *memory*) | |
| format | Format added to the provided value (e.g., `GBi` or `m`, see Kubernetes documentation for valid values. | yes |
| factor | An integer to multiply the value with. Both *factor* and *format* are ignored if *factor* is not an integer. | yes |


## SchedulerNamePatcher

***TODO Description***

Supported Kubernetes resources: Pod, Deployment, StatefulSet.


## LabelPatcher

***TODO Description***

Supported Kubernetes resources: All.

| Property | Description | Default |
|:----|:----|:----|
| labelName |  |  |

## MatchLabelPatcher

***TODO Description***

Supported Kubernetes resources: Deployment, StatefulSet.

| Property | Description | Default |
|:----|:----|:----|
| labelName |  |  |


## TemplateLabelPatcher

***TODO Description***

Supported Kubernetes resources: Deployment, StatefulSet.

| Property | Description | Default |
|:----|:----|:----|
| labelName |  |  |


## ServiceSelectorPatcher

***TODO Description***

Supported Kubernetes resources: Service.

| Property | Description | Default |
|:----|:----|:----|
| labelName |  |  |


## ImagePatcher

***TODO Description***

Supported Kubernetes resources: Pod, Deployment, StatefulSet.

| Property | Description | Default |
|:----|:----|:----|
| container | Name of the container for which the image should be patched. |  |

## NamePatcher

***TODO Description***

Supported Kubernetes resources: All.


## Available Patchers

* **ReplicaPatcher**: Modifies the number of replicas for a Kubernetes deployment.
  * **type**: "ReplicaPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"

* **NumSensorsLoadGeneratorReplicaPatcher**: Modifies the number of load generators, according to the following formula: *(value + loadGenMaxRecords - 1) / loadGenMaxRecords*
  * **type**: "NumSensorsLoadGeneratorReplicaPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"
  * **properties**:
    * loadGenMaxRecords: 150000

* **NumNestedGroupsLoadGeneratorReplicaPatcher**: Modifies the number of load generators, according to the following formula: *(4^(value) + loadGenMaxRecords - 1) / loadGenMaxRecords*
  * **type**: "NumNestedGroupsLoadGeneratorReplicaPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"
  * **properties**:
    * loadGenMaxRecords: 150000

* **DataVolumeLoadGeneratorReplicaPatcher**: Takes the total load that should be generated and computes the number of instances needed for this load based on the `maxVolume` ((load + maxVolume - 1) / maxVolume) and calculates the load per instance (loadPerInstance = load / instances). The number of instances are set for the load generator and the given variable is set to the load per instance.
  * **type**: "DataVolumeLoadGeneratorReplicaPatcher"
  * **resource**: "osp-load-generator-deployment.yaml"
  * **properties**:
    * maxVolume: "50"
    * container: "workload-generator"
    * variableName: "DATA_VOLUME"

* **ReplicaPatcher**: Allows to modify the number of Replicas for a kubernetes deployment.
  * **type**: "ReplicaPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"

* **EnvVarPatcher**: Modifies the value of an environment variable for a container in a Kubernetes deployment. 
  * **type**: "EnvVarPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"
  * **properties**:
    * container: "workload-generator"
    * variableName: "NUM_SENSORS"

* **ConfigMapYamlPatcher**: allows to add/modify a key-value pair in a YAML file of a ConfigMap
  * **type**: "ConfigMapYamlPatcher"
  * **resource**: "flink-configuration-configmap.yaml"
  * **properties**:
    * fileName: "flink-conf.yaml"
    * variableName: "jobmanager.memory.process.size"
  * **value**: "4Gb"

* **NodeSelectorPatcher**: Changes the node selection field in Kubernetes resources.
  * **type**: "NodeSelectorPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"
  * **properties**:
    * variableName: "env"
  * **example value**: "prod"

* **ResourceLimitPatcher**: Changes the resource limit for a Kubernetes resource.
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * container: "uc-application"
    * variableName: "cpu" or "memory"
  * **example value**:"1000m" or "2Gi"
  
* **SchedulerNamePatcher**: Changes the scheduler for Kubernetes resources.
  * **type**: "SchedulerNamePatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **example value**: "random-scheduler"

* **LabelPatcher**: Changes the label of a Kubernetes Deployment or StatefulSet. The patched field is: `metadata.labels`
  * **type**: "LabelPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * variableName: "app"
  * **example value**: "theodolite-sut"

* **MatchLabelPatcher**: Changes the match labels of a Kubernetes Deployment or StatefulSet. The patched field is: `spec.selector.matchLabels`
  * **type**: "MatchLabelPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * variableName: "app"
  * **example value**: "theodolite-sut"

* **TemplateLabelPatcher**: Changes the template labels of a Kubernetes Deployment or StatefulSet. The patched field is: `spec.template.metadata.labels`
  * **type**: "MatchLabelPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * variableName: "app"
  * **example value**: "theodolite-sut"

* **ImagePatcher**: Changes the image of a Kubernetes resource. **Currently not fully implemented.**
  * **type**: "ImagePatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * container: "uc-application"
  * **example value**: "dockerhub-org/image-name"
