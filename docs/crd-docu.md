# API Reference

Packages:

- [theodolite.com/v1](#theodolitecomv1)

# theodolite.com/v1

Resource Types:

- [benchmark](#benchmark)

- [execution](#execution)




## benchmark
<sup><sup>[↩ Parent](#theodolitecomv1 )</sup></sup>








<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
      <td><b>apiVersion</b></td>
      <td>string</td>
      <td>theodolite.com/v1</td>
      <td>true</td>
      </tr>
      <tr>
      <td><b>kind</b></td>
      <td>string</td>
      <td>benchmark</td>
      <td>true</td>
      </tr>
      <tr>
      <td><b><a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#objectmeta-v1-meta">metadata</a></b></td>
      <td>object</td>
      <td>Refer to the Kubernetes API documentation for the fields of the `metadata` field.</td>
      <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkstatus">status</a></b></td>
        <td>object</td>
        <td>
          <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspec">spec</a></b></td>
        <td>object</td>
        <td>
          <br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.status
<sup><sup>[↩ Parent](#benchmark)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>resourceSets</b></td>
        <td>string</td>
        <td>
          The status of a Benchmark indicates whether all resources are available to start the benchmark or not.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec
<sup><sup>[↩ Parent](#benchmark)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>name</b></td>
        <td>string</td>
        <td>
          This field exists only for technical reasons and should not be set by the user. The value of the field will be overwritten.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecappresourcesetsindex">appResourceSets</a></b></td>
        <td>[]object</td>
        <td>
          The appResourceSets specifies all Kubernetes resources required to start the sut. A resourceSet can be either a configMap resourceSet or a fileSystem resourceSet.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspeckafkaconfig">kafkaConfig</a></b></td>
        <td>object</td>
        <td>
          Contains the Kafka configuration.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgenresourcesetsindex">loadGenResourceSets</a></b></td>
        <td>[]object</td>
        <td>
          The loadGenResourceSets specifies all Kubernetes resources required to start the load generator. A resourceSet can be either a configMap resourceSet or a fileSystem resourceSet.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadtypesindex">loadTypes</a></b></td>
        <td>[]object</td>
        <td>
          A list of load types that can be scaled for this benchmark. For each load type the concrete values are defined in the execution object.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecresourcetypesindex">resourceTypes</a></b></td>
        <td>[]object</td>
        <td>
          A list of resource types that can be scaled for this `benchmark` resource. For each resource type the concrete values are defined in the `execution` object.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.appResourceSets[index]
<sup><sup>[↩ Parent](#benchmarkspec)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b><a href="#benchmarkspecappresourcesetsindexconfigmap">configMap</a></b></td>
        <td>object</td>
        <td>
          The configMap resourceSet loads the Kubernetes manifests from an Kubernetes configMap.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecappresourcesetsindexfilesystem">fileSystem</a></b></td>
        <td>object</td>
        <td>
          The fileSystem resourceSet loads the Kubernetes manifests from the filesystem.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.appResourceSets[index].configMap
<sup><sup>[↩ Parent](#benchmarkspecappresourcesetsindex)</sup></sup>



The configMap resourceSet loads the Kubernetes manifests from an Kubernetes configMap.

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>files</b></td>
        <td>[]string</td>
        <td>
          (Optional) Specifies which files from the configMap should be loaded. If this field is not set, all files are loaded.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>name</b></td>
        <td>string</td>
        <td>
          The name of the configMap<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.appResourceSets[index].fileSystem
<sup><sup>[↩ Parent](#benchmarkspecappresourcesetsindex)</sup></sup>



The fileSystem resourceSet loads the Kubernetes manifests from the filesystem.

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>files</b></td>
        <td>[]string</td>
        <td>
          (Optional) Specifies which files from the configMap should be loaded. If this field is not set, all files are loaded.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>path</b></td>
        <td>string</td>
        <td>
          The path to the folder which contains the Kubernetes manifests files.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.kafkaConfig
<sup><sup>[↩ Parent](#benchmarkspec)</sup></sup>



Contains the Kafka configuration.

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>bootstrapServer</b></td>
        <td>string</td>
        <td>
          The bootstrap servers connection string.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspeckafkaconfigtopicsindex">topics</a></b></td>
        <td>[]object</td>
        <td>
          List of topics to be created for each experiment. Alternative theodolite offers the possibility to remove certain topics after each experiment.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.kafkaConfig.topics[index]
<sup><sup>[↩ Parent](#benchmarkspeckafkaconfig)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>numPartitions</b></td>
        <td>integer</td>
        <td>
          The number of partitions of the topic.<br/>
          <br/>
            <i>Default</i>: 0<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>removeOnly</b></td>
        <td>boolean</td>
        <td>
          Determines if this topic should only be deleted after each experiement. For removeOnly topics the name can be a RegEx describing the topic.<br/>
          <br/>
            <i>Default</i>: false<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>replicationFactor</b></td>
        <td>integer</td>
        <td>
          The replication factor of the topic.<br/>
          <br/>
            <i>Default</i>: 0<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>name</b></td>
        <td>string</td>
        <td>
          The name of the topic.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenResourceSets[index]
<sup><sup>[↩ Parent](#benchmarkspec)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b><a href="#benchmarkspecloadgenresourcesetsindexconfigmap">configMap</a></b></td>
        <td>object</td>
        <td>
          The configMap resourceSet loads the Kubernetes manifests from an Kubernetes configMap.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgenresourcesetsindexfilesystem">fileSystem</a></b></td>
        <td>object</td>
        <td>
          The fileSystem resourceSet loads the Kubernetes manifests from the filesystem.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenResourceSets[index].configMap
<sup><sup>[↩ Parent](#benchmarkspecloadgenresourcesetsindex)</sup></sup>



The configMap resourceSet loads the Kubernetes manifests from an Kubernetes configMap.

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>files</b></td>
        <td>[]string</td>
        <td>
          (Optional) Specifies which files from the configMap should be loaded. If this field is not set, all files are loaded.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>name</b></td>
        <td>string</td>
        <td>
          The name of the configMap<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenResourceSets[index].fileSystem
<sup><sup>[↩ Parent](#benchmarkspecloadgenresourcesetsindex)</sup></sup>



The fileSystem resourceSet loads the Kubernetes manifests from the filesystem.

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>files</b></td>
        <td>[]string</td>
        <td>
          (Optional) Specifies which files from the configMap should be loaded. If this field is not set, all files are loaded.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>path</b></td>
        <td>string</td>
        <td>
          The path to the folder which contains the Kubernetes manifests files.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadTypes[index]
<sup><sup>[↩ Parent](#benchmarkspec)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b><a href="#benchmarkspecloadtypesindexpatchersindex">patchers</a></b></td>
        <td>[]object</td>
        <td>
          List of patchers used to scale this resource type.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>typeName</b></td>
        <td>string</td>
        <td>
          Name of the load type.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.loadTypes[index].patchers[index]
<sup><sup>[↩ Parent](#benchmarkspecloadtypesindex)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>properties</b></td>
        <td>map[string]string</td>
        <td>
          (Optional) Patcher specific additional arguments.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>resource</b></td>
        <td>string</td>
        <td>
          Specifies the Kubernetes resource to be patched.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>type</b></td>
        <td>string</td>
        <td>
          Type of the Patcher.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.resourceTypes[index]
<sup><sup>[↩ Parent](#benchmarkspec)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b><a href="#benchmarkspecresourcetypesindexpatchersindex">patchers</a></b></td>
        <td>[]object</td>
        <td>
          List of patchers used to scale this resource type.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>typeName</b></td>
        <td>string</td>
        <td>
          Name of the resource type.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.resourceTypes[index].patchers[index]
<sup><sup>[↩ Parent](#benchmarkspecresourcetypesindex)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>properties</b></td>
        <td>map[string]string</td>
        <td>
          (Optional) Patcher specific additional arguments.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>resource</b></td>
        <td>string</td>
        <td>
          Specifies the Kubernetes resource to be patched.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>type</b></td>
        <td>string</td>
        <td>
          Type of the patcher.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>

## execution
<sup><sup>[↩ Parent](#theodolitecomv1 )</sup></sup>








<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
      <td><b>apiVersion</b></td>
      <td>string</td>
      <td>theodolite.com/v1</td>
      <td>true</td>
      </tr>
      <tr>
      <td><b>kind</b></td>
      <td>string</td>
      <td>execution</td>
      <td>true</td>
      </tr>
      <tr>
      <td><b><a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#objectmeta-v1-meta">metadata</a></b></td>
      <td>object</td>
      <td>Refer to the Kubernetes API documentation for the fields of the `metadata` field.</td>
      <td>true</td>
      </tr><tr>
        <td><b><a href="#executionstatus">status</a></b></td>
        <td>object</td>
        <td>
          <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#executionspec">spec</a></b></td>
        <td>object</td>
        <td>
          <br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### execution.status
<sup><sup>[↩ Parent](#execution)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>executionDuration</b></td>
        <td>string</td>
        <td>
          Duration of the execution in seconds<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>executionState</b></td>
        <td>string</td>
        <td>
          <br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### execution.spec
<sup><sup>[↩ Parent](#execution)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>name</b></td>
        <td>string</td>
        <td>
          This field exists only for technical reasons and should not be set by the user. The value of the field will be overwritten.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>benchmark</b></td>
        <td>string</td>
        <td>
          The name of the benchmark this execution is referring to.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#executionspecconfigoverridesindex">configOverrides</a></b></td>
        <td>[]object</td>
        <td>
          List of patchers that are used to override existing configurations.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#executionspecexecution">execution</a></b></td>
        <td>object</td>
        <td>
          Defines the overall parameter for the execution.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#executionspecload">load</a></b></td>
        <td>object</td>
        <td>
          Specifies the load values that are benchmarked.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#executionspecresources">resources</a></b></td>
        <td>object</td>
        <td>
          Specifies the scaling resource that is benchmarked.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#executionspecslosindex">slos</a></b></td>
        <td>[]object</td>
        <td>
          List of resource values for the specified resource type.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### execution.spec.configOverrides[index]
<sup><sup>[↩ Parent](#executionspec)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b><a href="#executionspecconfigoverridesindexpatcher">patcher</a></b></td>
        <td>object</td>
        <td>
          Patcher used to patch a resource<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>value</b></td>
        <td>string</td>
        <td>
          <br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### execution.spec.configOverrides[index].patcher
<sup><sup>[↩ Parent](#executionspecconfigoverridesindex)</sup></sup>



Patcher used to patch a resource

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>properties</b></td>
        <td>map[string]string</td>
        <td>
          (Optional) Patcher specific additional arguments.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>resource</b></td>
        <td>string</td>
        <td>
          Specifies the Kubernetes resource to be patched.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>type</b></td>
        <td>string</td>
        <td>
          Type of the Patcher.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### execution.spec.execution
<sup><sup>[↩ Parent](#executionspec)</sup></sup>



Defines the overall parameter for the execution.

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>loadGenerationDelay</b></td>
        <td>integer</td>
        <td>
          Seconds to wait between the start of the SUT and the load generator.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>duration</b></td>
        <td>integer</td>
        <td>
          Defines the duration of each experiment in seconds.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>repetitions</b></td>
        <td>integer</td>
        <td>
          Numper of repititions for each experiments.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>restrictions</b></td>
        <td>[]string</td>
        <td>
          List of restriction strategys used to delimit the search space.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>strategy</b></td>
        <td>string</td>
        <td>
          Defines the used strategy for the execution, either 'LinearSearch' or 'BinarySearch'<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### execution.spec.load
<sup><sup>[↩ Parent](#executionspec)</sup></sup>



Specifies the load values that are benchmarked.

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>loadType</b></td>
        <td>string</td>
        <td>
          The type of the load. It must match one of the load types specified in the referenced benchmark.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>loadValues</b></td>
        <td>[]integer</td>
        <td>
          List of load values for the specified load type.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### execution.spec.resources
<sup><sup>[↩ Parent](#executionspec)</sup></sup>



Specifies the scaling resource that is benchmarked.

<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>resourceType</b></td>
        <td>string</td>
        <td>
          The type of the resource. It must match one of the resource types specified in the referenced benchmark.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>resourceValues</b></td>
        <td>[]integer</td>
        <td>
          List of resource values for the specified resource type.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### execution.spec.slos[index]
<sup><sup>[↩ Parent](#executionspec)</sup></sup>





<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Description</th>
            <th>Required</th>
        </tr>
    </thead>
    <tbody><tr>
        <td><b>properties</b></td>
        <td>map[string]string</td>
        <td>
          (Optional) SLO specific additional arguments.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>offset</b></td>
        <td>integer</td>
        <td>
          Hours by which the start and end timestamp will be shifted (for different timezones).<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>prometheusUrl</b></td>
        <td>string</td>
        <td>
          Connection string for Promehteus.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>sloType</b></td>
        <td>string</td>
        <td>
          The type of the SLO. It must match 'lag trend'.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>