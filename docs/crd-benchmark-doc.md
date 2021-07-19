# API Reference

Packages:

- [theodolite.com/v1](#theodolitecomv1)

# theodolite.com/v1

Resource Types:

- [benchmark](#benchmark)




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
        <td><b><a href="#benchmarkspec">spec</a></b></td>
        <td>object</td>
        <td>
          <br/>
        </td>
        <td>true</td>
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
        <td><b>appResource</b></td>
        <td>[]string</td>
        <td>
          A list of file names that reference Kubernetes resources that are deployed on the cluster for the system under test (SUT).<br/>
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
        <td><b>loadGenResource</b></td>
        <td>[]string</td>
        <td>
          A list of file names that reference Kubernetes resources that are deployed on the cluster for the load generator.<br/>
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
        <td><b>removeOnly</b></td>
        <td>boolean</td>
        <td>
          Determines if this topic should only be deleted after each experiement. For removeOnly topics the name can be a RegEx describing the topic.<br/>
          <br/>
            <i>Default</i>: false<br/>
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
      </tr><tr>
        <td><b>numPartitions</b></td>
        <td>integer</td>
        <td>
          The number of partitions of the topic.<br/>
          <br/>
            <i>Default</i>: 0<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>replicationFactor</b></td>
        <td>integer</td>
        <td>
          The replication factor of the topic.<br/>
          <br/>
            <i>Default</i>: 0<br/>
        </td>
        <td>true</td>
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
        <td>object</td>
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
        <td>object</td>
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