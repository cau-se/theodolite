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
