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

* **EnvVarPatcher**: Modifies the value of an environment variable for a container in a Kubernetes deployment. 
  * **type**: "EnvVarPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"
  * **properties**:
    * container: "workload-generator"
    * variableName: "NUM_SENSORS"

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

* **LabelPatcher**: Changes the label of a Kubernetes Deployment or StatefulSet. The patches field is: `metadata.labels`
  * **type**: "LabelPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * variableName: "app"
  * **example value**: "theodolite-sut"

* **MatchLabelPatcher**: Changes the match labels of a Kubernetes Deployment or StatefulSet. The patches field is: `spec.selector.matchLabels`
  * **type**: "MatchLabelPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * variableName: "app"
  * **example value**: "theodolite-sut"

* **TemplateLabelPatcher**: Changes the template labels of a Kubernetes Deployment or StatefulSet. The patches field is: `spec.template.metadata.labels`
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
