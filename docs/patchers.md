---
title: Patchers
has_children: false
#nav_order: 1
---

# Patchers

Patchers can be seen as functions which take a value as input and modify a Kubernetes resource in a patcher-specific way.

## Available Patchers

* **ReplicaPatcher**: Allows to modify the number of Replicas for a Kubernetes deployment.
  * **type**: "ReplicaPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"

* **NumSensorsLoadGeneratorReplicaPatcher**: Allows to scale the number of load generators. Scales according to the following formula: (value + 15_000 - 1) / 15_000
  * **type**: "NumSensorsLoadGeneratorReplicaPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"

* **NumNestedGroupsLoadGeneratorReplicaPatcher**: Allows to scale the number of load generators. Scales according to the following formula: (4^(value) + 15_000 -1) /15_000
  * **type**: "NumNestedGroupsLoadGeneratorReplicaPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"

* **EnvVarPatcher**: Allows to modify the value of an environment variable for a container in a Kubernetes deployment. 
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
  * **value**: "prod"

* **ResourceLimitPatcher**: Changes the resource limit for a Kubernetes resource.
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * container: "uc-application"
    * variableName: "cpu" or "memory"
  * **value**:"1000m" or "2Gi"
  
* **SchedulerNamePatcher**: Changes the scheduler for Kubernetes resources.
  * **type**: "SchedulerNamePatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **value**: "random-scheduler"

* **ImagePatcher**: Changes the image of a Kubernetes resource. Currently not fully implemented.
  * **type**: "ImagePatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"
  * **properties**:
    * container: "uc-application"
  * **value**: "dockerhub-org/image-name"
