---
title: Patchers
has_children: false
parent: API Reference
nav_order: 2
---

# Patchers

Patchers can be seen as functions which take a value as input and modify a Kubernetes resource in a patcher-specific way.

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
