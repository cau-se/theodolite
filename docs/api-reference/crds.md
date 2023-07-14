---
title: Theodolite CRDs
has_children: false
parent: API Reference
nav_order: 1
---


# API Reference

Packages:

- [theodolite.rocks/v1beta1](#theodoliterocksv1beta1)

# theodolite.rocks/v1beta1

Resource Types:

- [benchmark](#benchmark)

- [execution](#execution)




## benchmark
<sup><sup>[↩ Parent](#theodoliterocksv1beta1 )</sup></sup>








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
      <td>theodolite.rocks/v1beta1</td>
      <td>true</td>
      </tr>
      <tr>
      <td><b>kind</b></td>
      <td>string</td>
      <td>benchmark</td>
      <td>true</td>
      </tr>
      <tr>
      <td><b><a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.27/#objectmeta-v1-meta">metadata</a></b></td>
      <td>object</td>
      <td>Refer to the Kubernetes API documentation for the fields of the `metadata` field.</td>
      <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspec">spec</a></b></td>
        <td>object</td>
        <td>
          <br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkstatus">status</a></b></td>
        <td>object</td>
        <td>
          <br/>
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
        <td><b><a href="#benchmarkspecloadgenerator">loadGenerator</a></b></td>
        <td>object</td>
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
      </tr><tr>
        <td><b><a href="#benchmarkspecslosindex">slos</a></b></td>
        <td>[]object</td>
        <td>
          List of resource values for the specified resource type.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsut">sut</a></b></td>
        <td>object</td>
        <td>
          The appResourceSets specifies all Kubernetes resources required to start the sut. A resourceSet can be either a configMap resourceSet or a fileSystem resourceSet.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructure">infrastructure</a></b></td>
        <td>object</td>
        <td>
          (Optional) A list of file names that reference Kubernetes resources that are deployed on the cluster to create the required infrastructure.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspeckafkaconfig">kafkaConfig</a></b></td>
        <td>object</td>
        <td>
          Contains the Kafka configuration.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>name</b></td>
        <td>string</td>
        <td>
          This field exists only for technical reasons and should not be set by the user. The value of the field will be overwritten.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>waitForResourcesEnabled</b></td>
        <td>boolean</td>
        <td>
          If true, Theodolite waits to create the resource for the SUT until the infrastructure resources are ready, and analogously, Theodolite waits to create the load-gen resource until the resources of the SUT are ready.<br/>
          <br/>
            <i>Default</i>: false<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator
<sup><sup>[↩ Parent](#benchmarkspec)</sup></sup>



The loadGenResourceSets specifies all Kubernetes resources required to start the load generator. A resourceSet can be either a configMap resourceSet or a fileSystem resourceSet.

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
        <td><b><a href="#benchmarkspecloadgeneratorafteractionsindex">afterActions</a></b></td>
        <td>[]object</td>
        <td>
          Load generator after actions are executed after the teardown of the load generator.<br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgeneratorbeforeactionsindex">beforeActions</a></b></td>
        <td>[]object</td>
        <td>
          Load generator before actions are executed before the load generator is started.<br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgeneratorresourcesindex">resources</a></b></td>
        <td>[]object</td>
        <td>
          <br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.afterActions[index]
<sup><sup>[↩ Parent](#benchmarkspecloadgenerator)</sup></sup>





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
        <td><b><a href="#benchmarkspecloadgeneratorafteractionsindexdelete">delete</a></b></td>
        <td>object</td>
        <td>
          Specifies deletion of a resource.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgeneratorafteractionsindexexec">exec</a></b></td>
        <td>object</td>
        <td>
          Specifies a command that gets executed within a Container of a Pod<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.afterActions[index].delete
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorafteractionsindex)</sup></sup>



Specifies deletion of a resource.

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
        <td><b><a href="#benchmarkspecloadgeneratorafteractionsindexdeleteselector">selector</a></b></td>
        <td>object</td>
        <td>
          Defines how to select the resource to delete.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.afterActions[index].delete.selector
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorafteractionsindexdelete)</sup></sup>



Defines how to select the resource to delete.

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
        <td>
          Specifies the api/version of the resource that should be deleted, e.g. 'kafka.strimzi.io/v1beta2'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>kind</b></td>
        <td>string</td>
        <td>
          Specifies the Kind of the resource that should be deleted, e.g. 'KafkaTopic'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>nameRegex</b></td>
        <td>string</td>
        <td>
          Specifies a regular expression that is matched with the metadata.name property of all resources with given api/version and Kind that should be deleted.<br/>
          <br/>
            <i>Default</i>: *<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.afterActions[index].exec
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorafteractionsindex)</sup></sup>



Specifies a command that gets executed within a Container of a Pod

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
        <td><b>command</b></td>
        <td>[]string</td>
        <td>
          The command to be executed as string array.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgeneratorafteractionsindexexecselector">selector</a></b></td>
        <td>object</td>
        <td>
          The selector specifies which resource should be selected for the execution of the command.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>timeoutSeconds</b></td>
        <td>integer</td>
        <td>
          Specifies the timeout (in seconds) for the specified command.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.afterActions[index].exec.selector
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorafteractionsindexexec)</sup></sup>



The selector specifies which resource should be selected for the execution of the command.

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
        <td><b>container</b></td>
        <td>string</td>
        <td>
          Specifies the container.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgeneratorafteractionsindexexecselectorpod">pod</a></b></td>
        <td>object</td>
        <td>
          Specifies the pod.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.afterActions[index].exec.selector.pod
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorafteractionsindexexecselector)</sup></sup>



Specifies the pod.

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
        <td><b>matchLabels</b></td>
        <td>map[string]string</td>
        <td>
          The matchLabels of the desired pod.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.beforeActions[index]
<sup><sup>[↩ Parent](#benchmarkspecloadgenerator)</sup></sup>





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
        <td><b><a href="#benchmarkspecloadgeneratorbeforeactionsindexdelete">delete</a></b></td>
        <td>object</td>
        <td>
          Specifies deletion of a resource.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgeneratorbeforeactionsindexexec">exec</a></b></td>
        <td>object</td>
        <td>
          Specifies a command that gets executed within a Container of a Pod<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.beforeActions[index].delete
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorbeforeactionsindex)</sup></sup>



Specifies deletion of a resource.

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
        <td><b><a href="#benchmarkspecloadgeneratorbeforeactionsindexdeleteselector">selector</a></b></td>
        <td>object</td>
        <td>
          Defines how to select the resource to delete.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.beforeActions[index].delete.selector
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorbeforeactionsindexdelete)</sup></sup>



Defines how to select the resource to delete.

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
        <td>
          Specifies the api/version of the resource that should be deleted, e.g. 'kafka.strimzi.io/v1beta2'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>kind</b></td>
        <td>string</td>
        <td>
          Specifies the Kind of the resource that should be deleted, e.g. 'KafkaTopic'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>nameRegex</b></td>
        <td>string</td>
        <td>
          Specifies a regular expression that is matched with the metadata.name property of all resources with given api/version and Kind that should be deleted.<br/>
          <br/>
            <i>Default</i>: *<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.beforeActions[index].exec
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorbeforeactionsindex)</sup></sup>



Specifies a command that gets executed within a Container of a Pod

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
        <td><b>command</b></td>
        <td>[]string</td>
        <td>
          The command to be executed as string array.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgeneratorbeforeactionsindexexecselector">selector</a></b></td>
        <td>object</td>
        <td>
          The selector specifies which resource should be selected for the execution of the command.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>timeoutSeconds</b></td>
        <td>integer</td>
        <td>
          Specifies the timeout (in seconds) for the specified command.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.beforeActions[index].exec.selector
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorbeforeactionsindexexec)</sup></sup>



The selector specifies which resource should be selected for the execution of the command.

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
        <td><b>container</b></td>
        <td>string</td>
        <td>
          Specifies the container.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgeneratorbeforeactionsindexexecselectorpod">pod</a></b></td>
        <td>object</td>
        <td>
          Specifies the pod.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.beforeActions[index].exec.selector.pod
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorbeforeactionsindexexecselector)</sup></sup>



Specifies the pod.

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
        <td><b>matchLabels</b></td>
        <td>map[string]string</td>
        <td>
          The matchLabels of the desired pod.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.resources[index]
<sup><sup>[↩ Parent](#benchmarkspecloadgenerator)</sup></sup>





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
        <td><b><a href="#benchmarkspecloadgeneratorresourcesindexconfigmap">configMap</a></b></td>
        <td>object</td>
        <td>
          The configMap resourceSet loads the Kubernetes manifests from an Kubernetes configMap.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecloadgeneratorresourcesindexfilesystem">fileSystem</a></b></td>
        <td>object</td>
        <td>
          The fileSystem resourceSet loads the Kubernetes manifests from the filesystem.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.loadGenerator.resources[index].configMap
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorresourcesindex)</sup></sup>



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


### benchmark.spec.loadGenerator.resources[index].fileSystem
<sup><sup>[↩ Parent](#benchmarkspecloadgeneratorresourcesindex)</sup></sup>



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
      </tr><tr>
        <td><b>properties</b></td>
        <td>map[string]string</td>
        <td>
          (Optional) Patcher specific additional arguments.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
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
      </tr><tr>
        <td><b>properties</b></td>
        <td>map[string]string</td>
        <td>
          (Optional) Patcher specific additional arguments.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.slos[index]
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
        <td><b>name</b></td>
        <td>string</td>
        <td>
          The name of the SLO.<br/>
        </td>
        <td>true</td>
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
      </tr><tr>
        <td><b>properties</b></td>
        <td>map[string]string</td>
        <td>
          (Optional) SLO specific additional arguments.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut
<sup><sup>[↩ Parent](#benchmarkspec)</sup></sup>



The appResourceSets specifies all Kubernetes resources required to start the sut. A resourceSet can be either a configMap resourceSet or a fileSystem resourceSet.

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
        <td><b><a href="#benchmarkspecsutafteractionsindex">afterActions</a></b></td>
        <td>[]object</td>
        <td>
          SUT after actions are executed after the teardown of the SUT.<br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsutbeforeactionsindex">beforeActions</a></b></td>
        <td>[]object</td>
        <td>
          SUT before actions are executed before the SUT is started.<br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsutresourcesindex">resources</a></b></td>
        <td>[]object</td>
        <td>
          <br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.afterActions[index]
<sup><sup>[↩ Parent](#benchmarkspecsut)</sup></sup>





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
        <td><b><a href="#benchmarkspecsutafteractionsindexdelete">delete</a></b></td>
        <td>object</td>
        <td>
          Specifies deletion of a resource.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsutafteractionsindexexec">exec</a></b></td>
        <td>object</td>
        <td>
          Specifies a command that gets executed within a Container of a Pod<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.afterActions[index].delete
<sup><sup>[↩ Parent](#benchmarkspecsutafteractionsindex)</sup></sup>



Specifies deletion of a resource.

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
        <td><b><a href="#benchmarkspecsutafteractionsindexdeleteselector">selector</a></b></td>
        <td>object</td>
        <td>
          Defines how to select the resource to delete.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.afterActions[index].delete.selector
<sup><sup>[↩ Parent](#benchmarkspecsutafteractionsindexdelete)</sup></sup>



Defines how to select the resource to delete.

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
        <td>
          Specifies the api/version of the resource that should be deleted, e.g. 'kafka.strimzi.io/v1beta2'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>kind</b></td>
        <td>string</td>
        <td>
          Specifies the Kind of the resource that should be deleted, e.g. 'KafkaTopic'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>nameRegex</b></td>
        <td>string</td>
        <td>
          Specifies a regular expression that is matched with the metadata.name property of all resources with given api/version and Kind that should be deleted.<br/>
          <br/>
            <i>Default</i>: *<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.afterActions[index].exec
<sup><sup>[↩ Parent](#benchmarkspecsutafteractionsindex)</sup></sup>



Specifies a command that gets executed within a Container of a Pod

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
        <td><b>command</b></td>
        <td>[]string</td>
        <td>
          The command to be executed as string array.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsutafteractionsindexexecselector">selector</a></b></td>
        <td>object</td>
        <td>
          The selector specifies which resource should be selected for the execution of the command.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>timeoutSeconds</b></td>
        <td>integer</td>
        <td>
          Specifies the timeout (in seconds) for the specified command.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.afterActions[index].exec.selector
<sup><sup>[↩ Parent](#benchmarkspecsutafteractionsindexexec)</sup></sup>



The selector specifies which resource should be selected for the execution of the command.

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
        <td><b>container</b></td>
        <td>string</td>
        <td>
          Specifies the container.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsutafteractionsindexexecselectorpod">pod</a></b></td>
        <td>object</td>
        <td>
          Specifies the pod.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.afterActions[index].exec.selector.pod
<sup><sup>[↩ Parent](#benchmarkspecsutafteractionsindexexecselector)</sup></sup>



Specifies the pod.

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
        <td><b>matchLabels</b></td>
        <td>map[string]string</td>
        <td>
          The matchLabels of the desired pod.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.beforeActions[index]
<sup><sup>[↩ Parent](#benchmarkspecsut)</sup></sup>





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
        <td><b><a href="#benchmarkspecsutbeforeactionsindexdelete">delete</a></b></td>
        <td>object</td>
        <td>
          Specifies deletion of a resource.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsutbeforeactionsindexexec">exec</a></b></td>
        <td>object</td>
        <td>
          Specifies a command that gets executed within a Container of a Pod<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.beforeActions[index].delete
<sup><sup>[↩ Parent](#benchmarkspecsutbeforeactionsindex)</sup></sup>



Specifies deletion of a resource.

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
        <td><b><a href="#benchmarkspecsutbeforeactionsindexdeleteselector">selector</a></b></td>
        <td>object</td>
        <td>
          Defines how to select the resource to delete.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.beforeActions[index].delete.selector
<sup><sup>[↩ Parent](#benchmarkspecsutbeforeactionsindexdelete)</sup></sup>



Defines how to select the resource to delete.

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
        <td>
          Specifies the api/version of the resource that should be deleted, e.g. 'kafka.strimzi.io/v1beta2'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>kind</b></td>
        <td>string</td>
        <td>
          Specifies the Kind of the resource that should be deleted, e.g. 'KafkaTopic'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>nameRegex</b></td>
        <td>string</td>
        <td>
          Specifies a regular expression that is matched with the metadata.name property of all resources with given api/version and Kind that should be deleted.<br/>
          <br/>
            <i>Default</i>: *<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.beforeActions[index].exec
<sup><sup>[↩ Parent](#benchmarkspecsutbeforeactionsindex)</sup></sup>



Specifies a command that gets executed within a Container of a Pod

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
        <td><b>command</b></td>
        <td>[]string</td>
        <td>
          The command to be executed as string array.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsutbeforeactionsindexexecselector">selector</a></b></td>
        <td>object</td>
        <td>
          The selector specifies which resource should be selected for the execution of the command.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>timeoutSeconds</b></td>
        <td>integer</td>
        <td>
          Specifies the timeout (in seconds) for the specified command.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.beforeActions[index].exec.selector
<sup><sup>[↩ Parent](#benchmarkspecsutbeforeactionsindexexec)</sup></sup>



The selector specifies which resource should be selected for the execution of the command.

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
        <td><b>container</b></td>
        <td>string</td>
        <td>
          Specifies the container.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsutbeforeactionsindexexecselectorpod">pod</a></b></td>
        <td>object</td>
        <td>
          Specifies the pod.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.beforeActions[index].exec.selector.pod
<sup><sup>[↩ Parent](#benchmarkspecsutbeforeactionsindexexecselector)</sup></sup>



Specifies the pod.

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
        <td><b>matchLabels</b></td>
        <td>map[string]string</td>
        <td>
          The matchLabels of the desired pod.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.resources[index]
<sup><sup>[↩ Parent](#benchmarkspecsut)</sup></sup>





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
        <td><b><a href="#benchmarkspecsutresourcesindexconfigmap">configMap</a></b></td>
        <td>object</td>
        <td>
          The configMap resourceSet loads the Kubernetes manifests from an Kubernetes configMap.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecsutresourcesindexfilesystem">fileSystem</a></b></td>
        <td>object</td>
        <td>
          The fileSystem resourceSet loads the Kubernetes manifests from the filesystem.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.sut.resources[index].configMap
<sup><sup>[↩ Parent](#benchmarkspecsutresourcesindex)</sup></sup>



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


### benchmark.spec.sut.resources[index].fileSystem
<sup><sup>[↩ Parent](#benchmarkspecsutresourcesindex)</sup></sup>



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


### benchmark.spec.infrastructure
<sup><sup>[↩ Parent](#benchmarkspec)</sup></sup>



(Optional) A list of file names that reference Kubernetes resources that are deployed on the cluster to create the required infrastructure.

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
        <td><b><a href="#benchmarkspecinfrastructureafteractionsindex">afterActions</a></b></td>
        <td>[]object</td>
        <td>
          Infrastructure after actions are executed after the teardown of the infrastructure.<br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructurebeforeactionsindex">beforeActions</a></b></td>
        <td>[]object</td>
        <td>
          Infrastructure before actions are executed before the infrastructure is set up.<br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructureresourcesindex">resources</a></b></td>
        <td>[]object</td>
        <td>
          <br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.afterActions[index]
<sup><sup>[↩ Parent](#benchmarkspecinfrastructure)</sup></sup>





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
        <td><b><a href="#benchmarkspecinfrastructureafteractionsindexdelete">delete</a></b></td>
        <td>object</td>
        <td>
          Specifies deletion of a resource.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructureafteractionsindexexec">exec</a></b></td>
        <td>object</td>
        <td>
          Specifies a command that gets executed within a Container of a Pod<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.afterActions[index].delete
<sup><sup>[↩ Parent](#benchmarkspecinfrastructureafteractionsindex)</sup></sup>



Specifies deletion of a resource.

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
        <td><b><a href="#benchmarkspecinfrastructureafteractionsindexdeleteselector">selector</a></b></td>
        <td>object</td>
        <td>
          Defines how to select the resource to delete.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.afterActions[index].delete.selector
<sup><sup>[↩ Parent](#benchmarkspecinfrastructureafteractionsindexdelete)</sup></sup>



Defines how to select the resource to delete.

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
        <td>
          Specifies the api/version of the resource that should be deleted, e.g. 'kafka.strimzi.io/v1beta2'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>kind</b></td>
        <td>string</td>
        <td>
          Specifies the Kind of the resource that should be deleted, e.g. 'KafkaTopic'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>nameRegex</b></td>
        <td>string</td>
        <td>
          Specifies a regular expression that is matched with the metadata.name property of all resources with given api/version and Kind that should be deleted.<br/>
          <br/>
            <i>Default</i>: *<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.afterActions[index].exec
<sup><sup>[↩ Parent](#benchmarkspecinfrastructureafteractionsindex)</sup></sup>



Specifies a command that gets executed within a Container of a Pod

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
        <td><b>command</b></td>
        <td>[]string</td>
        <td>
          The command to be executed as string array.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructureafteractionsindexexecselector">selector</a></b></td>
        <td>object</td>
        <td>
          The selector specifies which resource should be selected for the execution of the command.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>timeoutSeconds</b></td>
        <td>integer</td>
        <td>
          Specifies the timeout (in seconds) for the specified command.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.afterActions[index].exec.selector
<sup><sup>[↩ Parent](#benchmarkspecinfrastructureafteractionsindexexec)</sup></sup>



The selector specifies which resource should be selected for the execution of the command.

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
        <td><b>container</b></td>
        <td>string</td>
        <td>
          Specifies the container.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructureafteractionsindexexecselectorpod">pod</a></b></td>
        <td>object</td>
        <td>
          Specifies the pod.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.afterActions[index].exec.selector.pod
<sup><sup>[↩ Parent](#benchmarkspecinfrastructureafteractionsindexexecselector)</sup></sup>



Specifies the pod.

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
        <td><b>matchLabels</b></td>
        <td>map[string]string</td>
        <td>
          The matchLabels of the desired pod.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.beforeActions[index]
<sup><sup>[↩ Parent](#benchmarkspecinfrastructure)</sup></sup>





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
        <td><b><a href="#benchmarkspecinfrastructurebeforeactionsindexdelete">delete</a></b></td>
        <td>object</td>
        <td>
          Specifies deletion of a resource.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructurebeforeactionsindexexec">exec</a></b></td>
        <td>object</td>
        <td>
          Specifies a command that gets executed within a Container of a Pod<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.beforeActions[index].delete
<sup><sup>[↩ Parent](#benchmarkspecinfrastructurebeforeactionsindex)</sup></sup>



Specifies deletion of a resource.

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
        <td><b><a href="#benchmarkspecinfrastructurebeforeactionsindexdeleteselector">selector</a></b></td>
        <td>object</td>
        <td>
          Defines how to select the resource to delete.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.beforeActions[index].delete.selector
<sup><sup>[↩ Parent](#benchmarkspecinfrastructurebeforeactionsindexdelete)</sup></sup>



Defines how to select the resource to delete.

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
        <td>
          Specifies the api/version of the resource that should be deleted, e.g. 'kafka.strimzi.io/v1beta2'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>kind</b></td>
        <td>string</td>
        <td>
          Specifies the Kind of the resource that should be deleted, e.g. 'KafkaTopic'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>nameRegex</b></td>
        <td>string</td>
        <td>
          Specifies a regular expression that is matched with the metadata.name property of all resources with given api/version and Kind that should be deleted.<br/>
          <br/>
            <i>Default</i>: *<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.beforeActions[index].exec
<sup><sup>[↩ Parent](#benchmarkspecinfrastructurebeforeactionsindex)</sup></sup>



Specifies a command that gets executed within a Container of a Pod

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
        <td><b>command</b></td>
        <td>[]string</td>
        <td>
          The command to be executed as string array.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructurebeforeactionsindexexecselector">selector</a></b></td>
        <td>object</td>
        <td>
          The selector specifies which resource should be selected for the execution of the command.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>timeoutSeconds</b></td>
        <td>integer</td>
        <td>
          Specifies the timeout (in seconds) for the specified command.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.beforeActions[index].exec.selector
<sup><sup>[↩ Parent](#benchmarkspecinfrastructurebeforeactionsindexexec)</sup></sup>



The selector specifies which resource should be selected for the execution of the command.

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
        <td><b>container</b></td>
        <td>string</td>
        <td>
          Specifies the container.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructurebeforeactionsindexexecselectorpod">pod</a></b></td>
        <td>object</td>
        <td>
          Specifies the pod.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.beforeActions[index].exec.selector.pod
<sup><sup>[↩ Parent](#benchmarkspecinfrastructurebeforeactionsindexexecselector)</sup></sup>



Specifies the pod.

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
        <td><b>matchLabels</b></td>
        <td>map[string]string</td>
        <td>
          The matchLabels of the desired pod.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.resources[index]
<sup><sup>[↩ Parent](#benchmarkspecinfrastructure)</sup></sup>





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
        <td><b><a href="#benchmarkspecinfrastructureresourcesindexconfigmap">configMap</a></b></td>
        <td>object</td>
        <td>
          The configMap resourceSet loads the Kubernetes manifests from an Kubernetes configMap.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#benchmarkspecinfrastructureresourcesindexfilesystem">fileSystem</a></b></td>
        <td>object</td>
        <td>
          The fileSystem resourceSet loads the Kubernetes manifests from the filesystem.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### benchmark.spec.infrastructure.resources[index].configMap
<sup><sup>[↩ Parent](#benchmarkspecinfrastructureresourcesindex)</sup></sup>



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


### benchmark.spec.infrastructure.resources[index].fileSystem
<sup><sup>[↩ Parent](#benchmarkspecinfrastructureresourcesindex)</sup></sup>



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
        <td><b>name</b></td>
        <td>string</td>
        <td>
          The name of the topic.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>true</td>
      </tr><tr>
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
        <td><b>resourceSetsState</b></td>
        <td>string</td>
        <td>
          The status of a Benchmark indicates whether all resources are available to start the benchmark or not.<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>

## execution
<sup><sup>[↩ Parent](#theodoliterocksv1beta1 )</sup></sup>








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
      <td>theodolite.rocks/v1beta1</td>
      <td>true</td>
      </tr>
      <tr>
      <td><b>kind</b></td>
      <td>string</td>
      <td>execution</td>
      <td>true</td>
      </tr>
      <tr>
      <td><b><a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.27/#objectmeta-v1-meta">metadata</a></b></td>
      <td>object</td>
      <td>Refer to the Kubernetes API documentation for the fields of the `metadata` field.</td>
      <td>true</td>
      </tr><tr>
        <td><b><a href="#executionspec">spec</a></b></td>
        <td>object</td>
        <td>
          <br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#executionstatus">status</a></b></td>
        <td>object</td>
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
        <td><b>benchmark</b></td>
        <td>string</td>
        <td>
          The name of the benchmark this execution is referring to.<br/>
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
        <td><b><a href="#executionspecconfigoverridesindex">configOverrides</a></b></td>
        <td>[]object</td>
        <td>
          List of patchers that are used to override existing configurations.<br/>
          <br/>
            <i>Default</i>: []<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>name</b></td>
        <td>string</td>
        <td>
          This field exists only for technical reasons and should not be set by the user. The value of the field will be overwritten.<br/>
          <br/>
            <i>Default</i>: <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b><a href="#executionspecslosindex">slos</a></b></td>
        <td>[]object</td>
        <td>
          List of SLOs with their properties, which differ from the benchmark definition.<br/>
        </td>
        <td>false</td>
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
          Number of repititions for each experiment.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b><a href="#executionspecexecutionstrategy">strategy</a></b></td>
        <td>object</td>
        <td>
          Defines the used strategy for the execution, either 'LinearSearch', 'BinarySearch' or 'InitialGuessSearch'.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>loadGenerationDelay</b></td>
        <td>integer</td>
        <td>
          Seconds to wait between the start of the SUT and the load generator.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>metric</b></td>
        <td>string</td>
        <td>
          <br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>


### execution.spec.execution.strategy
<sup><sup>[↩ Parent](#executionspecexecution)</sup></sup>



Defines the used strategy for the execution, either 'LinearSearch', 'BinarySearch' or 'InitialGuessSearch'.

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
          <br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>guessStrategy</b></td>
        <td>string</td>
        <td>
          <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>restrictions</b></td>
        <td>[]string</td>
        <td>
          List of restriction strategies used to delimit the search space.<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>searchStrategy</b></td>
        <td>string</td>
        <td>
          <br/>
        </td>
        <td>false</td>
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
      </tr><tr>
        <td><b>properties</b></td>
        <td>map[string]string</td>
        <td>
          (Optional) Patcher specific additional arguments.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
        </td>
        <td>false</td>
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
        <td><b>name</b></td>
        <td>string</td>
        <td>
          The name of the SLO. It must match a SLO specified in the Benchmark.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>properties</b></td>
        <td>map[string]string</td>
        <td>
          (Optional) SLO specific additional arguments.<br/>
          <br/>
            <i>Default</i>: map[]<br/>
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
        <td><b>completionTime</b></td>
        <td>string</td>
        <td>
          Time when this execution was stopped<br/>
          <br/>
            <i>Format</i>: date-time<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>executionDuration</b></td>
        <td>string</td>
        <td>
          Duration of the execution<br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>executionState</b></td>
        <td>string</td>
        <td>
          <br/>
        </td>
        <td>false</td>
      </tr><tr>
        <td><b>startTime</b></td>
        <td>string</td>
        <td>
          Time this execution started<br/>
          <br/>
            <i>Format</i>: date-time<br/>
        </td>
        <td>false</td>
      </tr></tbody>
</table>