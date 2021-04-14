# Theodolite Execution Framework

This directory contains the Theodolite framework for executing scalability
benchmarks in a Kubernetes cluster. As Theodolite aims for executing benchmarks
in realistic execution environments, some third-party components are [required](#installation).
After everything is installed and configured, you can move on the [execution of
benchmarks](#execution).

*Note: The currently released version of Theodolite only allows running Kafka Streams benchmarks. With the upcoming release `0.5`, we plan to support arbitrary stream processing engines, in particular, our already available implementations for Apache Flink. To already run them now, please contact us.*

## Installation

For executing benchmarks, access to a Kubernetes cluster is required. If you already run other applications inside your
cluster, you might want to consider creating a dedicated namespace for your benchmarks.

### Installing Dependencies

The following third-party services need to be installed in your cluster. For most of them, the suggested way to install
them is via [Helm](https://helm.sh).

#### Prometheus

We suggest to use the [Prometheus Operator](https://github.com/coreos/prometheus-operator)
and create a dedicated Prometheus instance for these benchmarks.

If Prometheus Operator is not already available on your cluster, a convenient way to install it is via the
[Prometheus community Helm chart](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack).
As you may not need an entire cluster monitoring stack, you can use our Helm configuration to only install the
operator:

```sh
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install prometheus-operator prometheus-community/kube-prometheus-stack -f infrastructure/prometheus/helm-values.yaml
```

After installation, you need to create a Prometheus instance:

```sh
kubectl apply -f infrastructure/prometheus/prometheus.yaml
```

You might also need to apply the [ClusterRole](infrastructure/prometheus/cluster-role.yaml), the
[CusterRoleBinding](infrastructure/prometheus/cluster-role-binding.yaml) and the
[ServiceAccount](infrastructure/prometheus/service-account.yaml), depending on your cluster's security
policies. If you are not in the *default* namespace, alter the namespace in
[Prometheus' ClusterRoleBinding](infrastructure/prometheus/cluster-role-binding.yaml) accordingly.

```sh
kubectl apply -f infrastructure/prometheus/cluster-role.yaml
kubectl apply -f infrastructure/prometheus/cluster-role-binding.yaml
kubectl apply -f infrastructure/prometheus/service-account.yaml
```

For the individual benchmarking components to be monitored, [ServiceMonitors](https://github.com/coreos/prometheus-operator#customresourcedefinitions)
are used. See the corresponding sections below for how to install them.

#### Grafana

As with Prometheus, we suggest to create a dedicated Grafana instance. Grafana
with our default configuration can be installed with Helm:

```sh
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
helm install grafana grafana/grafana -f infrastructure/grafana/values.yaml
```

The official [Grafana Helm Chart repository](https://github.com/helm/charts/tree/master/stable/grafana)
provides further documentation including a table of configuration options.

We provide ConfigMaps for a [Grafana dashboard](infrastructure/grafana/dashboard-config-map.yaml) and a
[Grafana data source](infrastructure/grafana/prometheus-datasource-config-map.yaml). Create them as follows:

```sh
kubectl apply -f infrastructure/grafana/dashboard-config-map.yaml
kubectl apply -f infrastructure/grafana/prometheus-datasource-config-map.yaml
```

#### A Kafka cluster

We suggest to set up a Kafka cluster via [Confluent's Helm Charts](https://github.com/confluentinc/cp-helm-charts).
Currently, these charts do not expose all metrics, we would like to display. Thus, we provide a patched version of this chart.
We also provide a [default configuration](infrastructure/kafka/values.yaml). If you do
not want to deploy 10 Kafka and 3 Zookeeper instances, alter the configuration
file accordingly. To install the patched Confluent's Kafka with our configuration:

```sh
helm install my-confluent https://github.com/SoerenHenning/cp-helm-charts/releases/download/v6.0.1-1-JMX-FIX-2/cp-helm-charts-0.6.0.tgz -f infrastructure/kafka/values.yaml
```

To let Prometheus scrape Kafka metrics, deploy a ServiceMonitor:

```sh
kubectl apply -f infrastructure/kafka/service-monitor.yaml
```

Other Kafka deployments, for example, using Strimzi, should work in a similar way.

*Please note that currently, even if installed differently, the corresponding services must run at
`my-confluent-cp-kafka:9092`, `my-confluent-cp-zookeeper:2181` and `my-confluent-cp-schema-registry:8081`.*

#### A Kafka Client Pod

A permanently running pod used for Kafka configuration is started via:

```sh
kubectl apply -f infrastructure/kafka/kafka-client.yaml
```

#### A Zookeeper Client Pod

Also a permanently running pod for ZooKeeper access is started via:

```sh
kubectl apply -f infrastructure/zookeeper-client.yaml
```

#### The Kafka Lag Exporter

[Lightbend's Kafka Lag Exporter](https://github.com/lightbend/kafka-lag-exporter)
can be installed via Helm. We also provide a [default configuration](infrastructure/kafka-lag-exporter/values.yaml).
To install it:

```sh
helm repo add kafka-lag-exporter https://lightbend.github.io/kafka-lag-exporter/repo/
helm repo update
helm install kafka-lag-exporter kafka-lag-exporter/kafka-lag-exporter -f infrastructure/kafka-lag-exporter/values.yaml
```

### Installing Theodolite

While Theodolite itself has not be installed as it is loaded at runtime (see [execution](#Execution)), it requires some
resources to be deployed in your cluster. These resources are grouped under RBAC and Volume in the following paragraphs.

#### Theodolite RBAC

**The following step is only required if RBAC is enabled in your cluster.** If you are not sure whether this is the
case, you want to simply try it without the following step.

If RBAC is enabled in your cluster, you have to allow Theodolite to start and stop pods etc. To do so, deploy the RBAC
resources via:

```sh
kubectl apply -f infrastructure/kubernetes/rbac/role.yaml
kubectl apply -f infrastructure/kubernetes/rbac/role-binding.yaml
kubectl apply -f infrastructure/kubernetes/rbac/service-account.yaml
```

#### Theodolite Volume

In order to persistently store benchmark results, Theodolite needs a volume mounted. We provide pre-configured
declarations for different volume types.

##### *hostPath* volume

Using a [hostPath volume](https://kubernetes.io/docs/concepts/storage/volumes/#hostpath) is the easiest option when
running Theodolite locally, e.g., with minikube or kind.

Just modify `infrastructure/kubernetes/volume-hostpath.yaml` by setting `path` to the directory on your host machine where
all benchmark results should be stored and run:

```sh
kubectl apply -f infrastructure/kubernetes/volume-hostpath.yaml
```

##### *local* volume

A [local volume](https://kubernetes.io/docs/concepts/storage/volumes/#local) is a simple option to use when having
access (e.g. via SSH) to one of your cluster nodes.

You first need to create a directory on a selected node where all benchmark results should be stored. Next, modify
`infrastructure/kubernetes/volume-local.yaml` by setting `<node-name>` to your selected node. (This node will most
likely also execute the [Theodolite job](#Execution).) Further, you have to set `path` to the directory on the node you just created. To deploy
your volume run:

```sh
kubectl apply -f infrastructure/kubernetes/volume-local.yaml
```

##### *Oracle Cloud Infrastructure* volume

When you are running in the Oracle Cloud, you can provision a persistent volume claim by attaching a volume from the
Oracle Cloud Infrastructure Block Volume service. To create your volume, run: 

```sh
kubectl apply -f infrastructure/kubernetes/volume-oci.yaml
```

More information can be found in the official documentation:
[Oracle Cloud Infrastructure: Creating a Persistent Volume Claim](https://docs.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengcreatingpersistentvolumeclaim.htm)

##### Other volumes

To use volumes provided by other public cloud providers or network-based file systems, you can use the definitions in
`infrastructure/kubernetes/` as a starting point. See the offical
[volumes documentation](https://kubernetes.io/docs/concepts/storage/volumes/) for additional information.

##### Accessing benchmark results via Kubernetes

In cases where you do not have direct access to the underlying storage infrasturcture of your volume (e.g., if your
admin configures a local or hostPath volume for you and you do not have SSH access to the node), you can deploy our
Theodolite results access deployment:

```sh
kubectl apply -f infrastructure/kubernetes/volume-access.yaml
```

It allows you to browse the benchmark results or copy files your Kubernetes client via the following commands:

```sh
kubectl exec -it $(kubectl get pods -o=name -l app=theodolite-results-access) -- sh
kubectl cp $(kubectl get pods --no-headers -o custom-columns=":metadata.name" -l app=theodolite-results-access):app/results <target-dir>
```


## Execution

The preferred way to run scalability benchmarks with Theodolite is to deploy Theodolite
[Kubernetes Jobs](https://kubernetes.io/docs/concepts/workloads/controllers/job/) in your cluster. For running
Theodolite locally on your machine see the description below.

`theodolite.yaml` provides a template for your own Theodolite job. To run your own job, create a copy, give it a name
(`metadata.name`) and adjust configuration parameters as desired. For a description of available configuration options
see the [Configuration](#configuration) section below. Note, that you might uncomment the `serviceAccountName` line if
RBAC is enabled on your cluster (see installation of [Theodolite RBAC](#Theodolite-RBAC)).

To start the execution of a benchmark run (with `<your-theodolite-yaml>` being your job definition):

```sh
kubectl create -f <your-theodolite-yaml>
kubectl create configmap app-resources-configmap --from-file=<folder-with-all-required-k8s-resources>
kubectl create configmap execution-configmap --from-file=<execution.yaml>
kubectl create configmap benchmark-configmap --from-file=<benchmark.yaml>
```


This will create a pod with a name such as `your-job-name-xxxxxx`. You can verifiy this via `kubectl get pods`. With
`kubectl logs -f <your-job-name-xxxxxx>`, you can follow the benchmark execution logs.

Once your job is completed (you can verify via `kubectl get jobs), its results are stored inside your configured
Kubernetes volume.

**Make sure to always run only one Theodolite job at a time.**

### Configuration

Be sure, that the names of the configmap corresponds correctly to the specifications of the mounted `configmaps`, `volumes`, `mountPath`.

### Domain Restriction

For dimension value, we have a domain of the amounts of instances. As a consequence, for each dimension value the maximum number of lag experiments is equal to the size of the domain. How the domain is determined is defined by the following domain restriction strategies.

* `no-domain-restriction`: For each dimension value, the domain of instances is equal to the set of all amounts of instances.
* `restrict-domain`: For each dimension value, the domain is computed as follows:
    * If the dimension value is the smallest dimension value the domain of the amounts of instances is equal to the set of all amounts of instances.
    * If the dimension value is not the smallest dimension value and N is the amount of minimal amount of instances that was suitable for the last smaller dimension value the domain for this dimension value contains all amounts of instances greater than, or equal to N.

### Benchmarking Search Strategies

There are the following benchmarking strategies:

* `check-all`: For each dimension value, execute one lag experiment for all amounts of instances within the current domain.
* `linear-search`: A heuristic which works as follows: For each dimension value, execute one lag experiment for all number of instances within the current domain. The execution order is from the lowest number of instances to the highest amount of instances and the execution for each dimension value is stopped, when a suitable amount of instances is found or if all lag experiments for the dimension value were not successful.
* `binary-search`: A heuristic which works as follows: For each dimension value, execute one lag experiment for all number of instances within the current domain. The execution order is in a binary-search-like manner. The execution is stopped, when a suitable amount of instances is found or if all lag experiments for the dimension value were not successful.

## Observation

The installed Grafana instance provides a dashboard to observe the benchmark execution. Unless configured otherwise,
this dashboard can be accessed via `http://<cluster-ip>:31199` or via `http://localhost:31199` if proxied with
`kubectl port-forward svc/grafana 8080:service`. Default credentials are user *admin* with password *admin*.


## Local Execution (e.g. for Development)

As an alternative to executing Theodolite as a Kubernetes Job, it is also possible to run it from your local system,
for example, for development purposes. In addition to the generel installation instructions, the following adjustments
are neccessary.

### Installation

For local execution a **Python 3.7** installation is required. We suggest to use a virtual environment placed in the `.venv`
directory (in the Theodolite root directory). A set of requirements is needed. You can install them with the following
command (make sure to be in your virtual environment if you use one):

```sh
pip install -r requirements.txt
```

Kubernetes volumes and service accounts, roles, and role bindings for Theodolite are not required in this case.

### Local Execution

The `theodolite.py` is the entrypoint for all benchmark executions. Is has to be called as follows:

```python
python theodolite.py --uc <uc> --loads <load> [<load> ...] --instances <instances> [<instances> ...]
```

This command is the minimal command for execution. Further configurations options are described [above](#configuration)
or available via `python theodolite.py -h`.