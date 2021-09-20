## Patchers

* **ReplicaPatcher**: Allows to modify the number of Replicas for a Kubernetes deployment.
  * **type**: "ReplicaPatcher"
  * **resource**: "uc1-kstreams-deployment.yaml"

* **NumSensorsLoadGeneratorReplicaPatcher**: Allows to scale the number of load generators. Scales according to the following formula: (value + 15_000 - 1) / 15_000
  * **type**: "NumSensorsLoadGeneratorReplicaPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"

* **NumNestedGroupsLoadGeneratorReplicaPatcher**: Allows to scale the number of load generators. Scales according to the following formula: (4^(value) + 15_000 -1) / 15_000
  * **type**: "NumNestedGroupsLoadGeneratorReplicaPatcher"
  * **resource**: "uc1-load-generator-deployment.yaml"

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

* **EnvVarPatcher**: Allows to modify the value of an environment variable for a container in a kubernetes deployment.
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
