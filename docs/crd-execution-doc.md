# API Reference

Packages:

- [theodolite.com/v1](#theodolitecomv1)

# theodolite.com/v1

Resource Types:

- [execution](#execution)




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
          <br/>
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
        <td><b>externalSloUrl</b></td>
        <td>string</td>
        <td>
          Connection string for a external slo analysis.<br/>
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
        <td><b>threshold</b></td>
        <td>integer</td>
        <td>
          The threshold the SUT should meet for a sucessful experiment.<br/>
        </td>
        <td>true</td>
      </tr><tr>
        <td><b>warmup</b></td>
        <td>integer</td>
        <td>
          Seconds of time that are ignored in the analysis.<br/>
        </td>
        <td>true</td>
      </tr></tbody>
</table>