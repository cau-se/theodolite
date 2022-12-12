---
title: "Example: TeaStore"
has_children: false
parent: "Creating Benchmarks"
nav_order: 1
---

# Example: A Benchmark for the TeaStore

The [TeaStore](https://github.com/DescartesResearch/TeaStore) is a microservice reference application.
It resamples a web shop for tea, allowing customers, for example, to browse the shop catalog, receive product recommendations, or place orders.
The TeaStore consists of six microservices and a MariaDB database.
The entire application can easily deployed with Kubernetes using the provided resource files.

In this example, we will create a Theodolite benchmark for the TeaStore.
We use [Open Service Mesh (OSM)](https://openservicemesh.io/) to inject sidecar proxies into the TeaStore microservices, which allow us to gather latency and other metrics.

## Prerequisites

To get started, you need:

* A running Kubernetes cluster (for testing purposes, you might want to use [Minikube](https://minikube.sigs.k8s.io/), [kind](https://kind.sigs.k8s.io/) or [k3d](https://k3d.io/))
* [Helm installed](https://helm.sh/) on you local machine


## Cluster Preparation

Before running a benchmark, we need to install Theodolite and OSM on our cluster.

### Install Theodolite

In general, Theodolite can be installed using Helm as described in the [installation guide](installation).
However, we need to make sure that no OSM sidecards are injected into the pod of Theodolite, Prometheus, etc.
As we do not use Kafka in this example, we can omit the Strimzi installation.
If no further configuration is required, run the following command to install Theodolite:

```sh
helm install theodolite theodolite/theodolite -f https://raw.githubusercontent.com/cau-se/theodolite/main/helm/preconfigs/osm-ready.yaml -f https://raw.githubusercontent.com/cau-se/theodolite/main/helm/preconfigs/kafka-less.yaml
```

### Install Open Service Mesh

To install OSM, we use the Helm chart provided by the OSM project. Run the following commands to install and configure OSM:

```sh
export NAMESPACE=default # Kubernetes namespace to be monitored

kubectl create ns osm-system
helm install osm osm --repo https://openservicemesh.github.io/osm --namespace osm-system --version 0.9.2 # A newer version would probably work as well
 
sleep 60s # Installation may take some time, so we wait a bit

kubectl patch meshconfig osm-mesh-config -n osm-system -p '{"spec":{"traffic":{"enablePermissiveTrafficPolicyMode":true}}}'  --type=merge
kubectl patch meshconfig osm-mesh-config -n osm-system -p '{"spec":{"traffic":{"enableEgress":true}}}' --type=merge

kubectl label namespace $NAMESPACE openservicemesh.io/monitored-by=osm --overwrite
kubectl annotate namespace $NAMESPACE openservicemesh.io/metrics=enabled --overwrite
kubectl annotate namespace $NAMESPACE openservicemesh.io/sidecar-injection=enabled --overwrite
```


## Create a Benchmark for the TeaStore

According to our Theodolite benchmarking method, we need to define a system under test (SUT) and a load generator for the benchmark.
Quite obviously, the TeaStore enriched by OSM's Envoy sidecar proxies is the SUT.
As load generator, we use JMeter along with the [browse profile](https://github.com/DescartesResearch/TeaStore/blob/master/examples/jmeter/teastore_browse_nogui.jmx) provided in the TeaStore repository.
The desired benchmarking setup is shown in the following diagram:

![TeaStore Benchmark](../assets/images/example-teastore-deployment.svg)

Although the TeaStore comes with Kubernetes resources (Deployments and Services), we need some modifications such as one resource per file, resource limits, or readiness probes. (Note that these modifications are not specific for Theodolite, but are generally considered good practice.)
We created a fork of the TeaStore repository all required modifications. Clone it by running:

```sh
git clone -b add-theodolite-example git@github.com:SoerenHenning/TeaStore.git
```

We now have to create ConfigMaps bundling these resources and a Benchmark resource describing the benchmark.

### Create ConfigMaps containing all components

To create a ConfigMap containing all TeaStore resources, simply run:

```sh
kubectl create configmap teastore-deployment --from-file=teastore/examples/kubernetes/teastore-clusterip-split/
```

Likewise, we have to create a ConfigMap for the JMeter profile and a ConfigMap containing a [JMeter deployment](https://github.com/SoerenHenning/TeaStore/blob/add-theodolite-example/examples/theodolite/jmeter.yaml):

```sh
kubectl create configmap teastore-jmeter-browse --from-file=teastore/examples/jmeter/teastore_browse_nogui.jmx
kubectl create configmap teastore-jmeter-deployment --from-file=teastore/examples/theodolite/jmeter.yaml
```

### Create the Benchmark file

Once all the required resources are bundled in ConfigMaps, we can define a Benchmark resource.
The following resource defines a simple benchmark providing one load type, one resource type and one SLO.

```yaml
apiVersion: theodolite.rocks/v1beta1
kind: benchmark
metadata:
  name: teastore
spec:
  waitForResourcesEnabled: true
  sut:
    resources:
      - configMap:
          name: teastore-deployment
  loadGenerator:
    resources:
      - configMap:
          name: teastore-jmeter-deployment
  resourceTypes:
    - typeName: "Instances"
      patchers:
        - type: "ReplicaPatcher"
          resource: "teastore-auth-deployment.yaml"
        - type: "ReplicaPatcher"
          resource: "teastore-image-deployment.yaml"
        - type: "ReplicaPatcher"
          resource: "teastore-persistence-deployment.yaml"
        - type: "ReplicaPatcher"
          resource: "teastore-recommender-deployment.yaml"
        - type: "ReplicaPatcher"
          resource: "teastore-webui-deployment.yaml"
  loadTypes:
    - typeName: NumUsers
      patchers:
        - type: "EnvVarPatcher"
          resource: "jmeter.yaml"
          properties:
            container: jmeter
            variableName: NUM_USERS
  slos:
    - sloType: "generic"
      name: "uiLatency"
      prometheusUrl: "http://prometheus-operated:9090"
      offset: 0
      properties:
        externalSloUrl: "http://localhost:8082"
        promQLQuery: "histogram_quantile(0.95,sum(irate(osm_request_duration_ms_bucket{destination_name='teastore_webui'}[1m])) by (le, destination_name))"
        warmup: 600 #in seconds
        queryAggregation: max
        repetitionAggregation: median
        operator: lte
        threshold: 200
```

#### SUT and Load Generator

We simply use the ConfigMaps created previously as SUT and load generator.

#### Resource Types

We scale the number of replicas of the TeaStore's WebUI, Auth, Image, Persistence, and Recommender services equally to cope with increasing load.
Hence, our resource type *Instances* defined *ReplicaPatcher*, which modify the number of replicas of all these services.

See our [extended version of this benchmark](https://github.com/SoerenHenning/TeaStore/blob/add-theodolite-example/examples/theodolite/benchmark.yaml), also supporting two other resources types.

#### Load Types

We focus on increasing the load on the TeaStore by increasing the number of concurrent users. Each user is simulated by JMeter and performs a series of UI interactions in an endless loop. Our load type is called *NumUsers* and modifies the `NUM_USERS` environment variable of the JMeter Deployment with an *EnvVarPatcher*.

#### SLOs

The SLO is defined as that the 95th percentile of the response time of the TeaStore's WebUI service must not exceed 200ms.
If multiple repetitions are performed, the median of the response times is used. Measurements from the first 600 seconds are discarded as warmup.


## Run the Benchmark

To run the benchmark, we first have to define an Execution resource, which we afterwards have to apply to our cluster along with the Benchmark resource.

### Create an Execution for our Benchmark

A simple Execution resource for our benchmark could look like this:

```yaml
apiVersion: theodolite.rocks/v1beta1
kind: execution
metadata:
  name: teastore-example
spec:
  benchmark: teastore
  load:
    loadType: "NumUsers"
    loadValues: [5, 10, 15, 20, 25, 30, 35, 40, 45, 50]
  resources:
    resourceType: "Instances"
    resourceValues: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
  execution:
    strategy:
      name: "RestrictionSearch"
      restrictions:
        - "LowerBound"
      searchStrategy: "LinearSearch"
    duration: 1200  # in seconds
    repetitions: 1
  configOverrides: []
```

It is named `teastore-example` and defines that we want to execute the benchmark `teastore`.
We evaluate load intensities from 5 to 50 users and provision 1 to 20 instances per scaled service.
We apply the [*lower bound restriction search*](concepts/search-strategies#lower-bound-restriction-search), run each experiment for 1200 seconds and perform only one repetition.

### Start the Benchmark

To let Prometheus scrape OSM metrics, we need to create a PodMonitor.
Download the [PodMonitor from GitHub](https://github.com/SoerenHenning/TeaStore/blob/add-theodolite-example/examples/theodolite/pod-monitors.yaml) (or use the repository already cloned in the previous step) and apply it: (Of course, this could also be made part of the benchmark.)

```sh
kubectl apply -f pod-monitors.yaml
```

Next, we need to deploy our benchmark:

```sh
kubectl apply -f benchmark.yaml
```

To now start benchmark execution, we deploy our Execution resource defined previously:

```sh
kubectl apply -f execution-users.yaml
```
As described at the [Running Benchmarks](running-benchmarks), we now have to wait, observe the benchmark execution, and finally access the results.

## Further Reading

We published a short paper about this example:
* S. Henning, B. Wetzel, and W. Hasselbring. “[Cloud-Native Scalability Benchmarking with Theodolite Applied to the TeaStore Benchmark](https://oceanrep.geomar.de/id/eprint/57338/)”. In: *Symposium on Software Performance*. 2022.

You might also want to have a look at the corresponding slides presented at the [Symposium on Software Performance 2022](https://www.performance-symposium.org/fileadmin/user_upload/palladio-conference/2022/presentations/Henning-Cloud-Native-Scalability-Benchmarking-with-Theodolite.pdf).
