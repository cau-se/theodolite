# Theodolite Execution Framework

This directory contains the Theodolite framework for executing scalability
benchmarks in a Kubernetes cluster. As Theodolite aims for executing benchmarks
in realistic execution environments, some third-party components are [required](#installation).
After everything is installed and configured, you can move on the [execution of
benchmarks](#execution).

## Installation

### Kubernetes Cluster

For executing benchmarks, access to Kubernetes cluster is required. We suggest
to create a dedicated namespace for executing your benchmarks. The following
services need to be available as well.

### Kubernetes Volume

For executing the benchmark as a Kubernetes job it is required to use a volume to store the results of the executions.
In `infrastructure/kubernetes` are two files for creating a volume.
Either one of them should be used.

The `volumeSingle.yaml` is meant for systems where Kubernetes is run locally (e.g. minikube, kind etc.).
However, you can also use the other file.
In `volumeSingle.yaml` you need to set `path` to the path on your machine where the results should be stored.

The `volumeCluster.yaml` should be used when Kubernetes runs in the cloud.
In the `nodeAffinity` section you need to exchange `<node-name>` to the name of the node where the volume should be created (this node will most likely execute also the job).
However, you can also set a different `nodeAffinity`.
Further you need to set `path` to the path on the node where the results should be stored.

After setting the properties you can create the volume with:

```sh
kubectl apply -f infrastructure/kubernetes/volume(Single|Cluster).yaml
```

#### Prometheus

We suggest to use the [Prometheus Operator](https://github.com/coreos/prometheus-operator)
and create a dedicated Prometheus instance for these benchmarks.

If Prometheus Operator is not already available on your cluster, a convenient
way to install is via the [**unofficial** Prometheus Operator Helm chart](https://github.com/helm/charts/tree/master/stable/prometheus-operator).
As you may not need an entire cluster monitoring stack, you can use our Helm
configuration to only install the operator:

```sh
helm install prometheus-operator stable/prometheus-operator -f infrastructure/prometheus/helm-values.yaml
```

After installation, you need to create a Prometheus instance:

```sh
kubectl apply -f infrastructure/prometheus/prometheus.yaml
```

You might also need to apply the [ServiceAccount](infrastructure/prometheus/service-account.yaml), [ClusterRole](infrastructure/prometheus/cluster-role.yaml)
and the [CusterRoleBinding](infrastructure/prometheus/cluster-role-binding.yaml),
depending on your cluster's security policies.

For the individual benchmarking components to be monitored, [ServiceMonitors](https://github.com/coreos/prometheus-operator#customresourcedefinitions)
are used. See the corresponding sections below for how to install them.

#### Grafana

As with Prometheus, we suggest to create a dedicated Grafana instance. Grafana
with our default configuration can be installed with Helm:

```sh
helm install grafana stable/grafana -f infrastructure/grafana/values.yaml
```

The official [Grafana Helm Chart repository](https://github.com/helm/charts/tree/master/stable/grafana)
provides further documentation including a table of configuration options.

We provide ConfigMaps for a [Grafana dashboard](infrastructure/grafana/dashboard-config-map.yaml) and a [Grafana data source](infrastructure/grafana/prometheus-datasource-config-map.yaml).
Create them as follows:

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
helm install my-confluent https://github.com/SoerenHenning/cp-helm-charts/releases/download/v6.0.1-1-JMX-FIX/cp-helm-charts-0.6.0.tgz -f infrastructure/kafka/values.yaml
```

To let Prometheus scrape Kafka metrics, deploy a ServiceMonitor:

```sh
kubectl apply -f infrastructure/kafka/service-monitor.yaml
```

Other Kafka deployments, for example, using Strimzi, should work in a similar way.

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
helm install kafka-lag-exporter https://github.com/lightbend/kafka-lag-exporter/releases/download/v0.6.3/kafka-lag-exporter-0.6.3.tgz -f infrastructure/kafka-lag-exporter/values.yaml
```


### Python 3.7 (Only required for local Execution Control)

For executing benchmarks, a **Python 3.7** installation is required. We suggest
to use a virtual environment placed in the `.venv` directory (in the Theodolite
root directory). As set of requirements is needed. You can install them with the following
command (make sure to be in your virtual environment if you use one):

```sh
pip install -r requirements.txt
```


### Required Manual Adjustments

Depending on your setup, some additional adjustments may be necessary:

* Change Kafka and Zookeeper servers in the Kubernetes deployments (uc1-application etc.) and `run_XX.sh` scripts
* Change the name of your Kubernetes namespace for [Prometheus' ClusterRoleBinding](infrastructure/prometheus/cluster-role-binding.yaml)
* *Please let us know if there are further adjustments necessary*



## Execution

You can either execute the Execution Control on your machine or also deploy the Execution control in Kubernetes.

### Local Execution

Please note that a **Python 3.7** installation is required for executing Theodolite.

The `theodolite.py` is the entrypoint for all benchmark executions. Is has to be called as follows:

```python
python theodolite.py --uc <uc> --loads <load> [<load> ...] --instances <instances> [<instances> ...]
```

The command above is the minimal command for execution.
Further configurations options are described [below](#configuration) or available via `python theodolite.py -h`

### Kubernetes Execution

The Execution Control will be run by a Kubernetes Job.
This Job creates a pod that will execute the Executuion Control.
To configure the parameters, the `theodolite.yaml` need to be changed.
For the options take a look at [configuration](#configuration).

To start the Benchmark the following command need to be executed:
```sh
kubectl apply -f theodolite.yaml
```

With `kubectl logs -f theodolite-<*>` you can show the log of the execution control.

When the job is finished, your results should be in your mounted [Kubernetes volume](#kubernetes-volume).
In order to start a new benchmark, the old job needs to be deleted.
This can be done with:
```sh
kubectl delete -f theodolite.yaml
```


### Configuration

| Python               | Kubernetes          | Description                                                  |
| -------------------- | ------------------- | ------------------------------------------------------------ |
| --uc                 | UC                  | **[Mandatory]** Stream processing use case to be benchmarked. Has to be one of `1`, `2`, `3` or `4`. |
| --loads              | LOADS               | **[Mandatory]** Values for the workload generator to be tested, should be sorted in ascending order. |
| --instances          | INSTANCES           | **[Mandatory]** Numbers of instances to be benchmarked, should be sorted in ascending order. |
| --duration           | DURATION            | Duration in minutes subexperiments should be executed for. *Default:* `5`. |
| --partitions         | PARTITIONS          | Number of partitions for Kafka topics. *Default:* `40`.      |
| --cpu-limit          | CPU_LIMIT           | Kubernetes CPU limit for a single Pod.  *Default:* `1000m`.  |
| --memory-limiT       | MEMORY_LIMIT        | Kubernetes memory limit for a single Pod. *Default:* `4Gi`.  |
| --domain-restriction | DOMAIN_RESTRICTION  | A flag that indiciates domain restriction should be used. *Default:* not set. For more details see Section [Domain Restriction](#domain-restriction). |
| --search-strategy    | SEARCH_STRATEGY     | The benchmarking search strategy. Can be set to `check-all`, `linear-search` or `binary-search`. *Default:* `check-all`. For more details see Section [Benchmarking Search Strategies](#benchmarking-search-strategies). |
| --reset              | RESET               | Resets the environment before each subexperiment. Useful if execution was aborted and just one experiment should be executed. |
| --reset-only         | RESET_ONLY          | Only resets the environment. Ignores all other parameters. Useful if execution was aborted and one want a clean state for new executions. |
| --prometheus         | PROMETHEUS_BASE_URL | Defines where to find the prometheus instance. *Default:* `http://localhost:9090` |
| --path               | RESULT_PATH         | A directory path for the results. Relative to the Execution folder. *Default:* `results` |
| --configurations     | CONFIGURATIONS      | Defines environment variables for the use cases and, thus, enables further configuration options. |

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
