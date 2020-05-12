# Requirements


## Kubernetes Cluster

For executing benchmarks, access to Kubernetes cluster is required. We suggest
to create a dedicated namespace for executing our benchmarks. The following
services need to be available as well.

### Prometheus (+ Grafana)

We suggest to use the Prometheus Operator and create a dedicated prometheus and
grafana instance for these benchmarks.

**TODO** Add required configuration, introduce service Monitors

### A Kafka cluster

One possible way to set up a Kafka cluster is via [Confluent's Helm Charts](https://github.com/confluentinc/cp-helm-charts).
For using these Helm charts and conjuction with the Prometheus Operator (see
below), we provide a [patch](https://github.com/SoerenHenning/cp-helm-charts)
for these helm charts. Note that this patch is only required for observation and
not for the actual benchmark execution and evaluation.

#### Our patched Confluent Helm Charts

To use our patched Confluent Helm Charts clone the
[chart's repsoitory](https://github.com/SoerenHenning/cp-helm-charts) and run

```sh
helm install my-confluent .
```

from within the cloned repository. Further configuration is possible by using a
helm YAML configuration file, passed by `-f values.yaml` to helm's install
command.

**TODO** Add required configuration, installation

#### Other options for Kafka

Other Kafka deployment, for example, using Strimzi, should work in similiar way.

### The Kafka Lag Exporter

Lightbend's Kafka Lag Exporter can be installed via helm:

```sh
helm install kafka-lag-exporter https://github.com/lightbend/kafka-lag-exporter/releases/download/v0.6.0/kafka-lag-exporter-0.6.0.tgz
```

**TODO** Add configuration + ServiceMonitor


## Python 3.7

For executing benchmarks and analyzing their results, a **Python 3.7** installation
is required. We suggest to use a virtual environment placed in the `.venv` directory.

As set of requirements is needed for the analysis Jupyter notebooks and the
execution tool. You can install them with the following command (make sure to
be in your virtual environment if you use one):

```sh
pip install -r requirements.txt 
```


## Required Manual Adjustments

Depending on your setup, some additional adjustments may be necessary:

* Change Kafka and Zookeeper servers in the Kubernetes deployments (uc1-application etc.) and `run_XX.sh` scripts
* Change Prometheus' URL in `lag_analysis.py`
* Change the path to your Python 3.7 virtual environment in the `run_XX.sh` schripts (to find the venv's `bin/activate`)
* *Please let us know if there are further adjustments necessary*



# Execution

The `./run_loop.sh` is the entrypoint for all benchmark executions. Is has to be called as follows:

```sh
./run_loop.sh <use-case> <wl-values> <instances> <partitions> <cpu-limit> <memory-limit> <commit-interval> <duration>
```

* `<use-case>`: Stream processing use case to be benchmarked. Has to be one of `1`, `2`, `3` or `4`.
* `<wl-values>`: Values for the workload generator to be tested, separated by commas. For example `100000, 200000, 300000`.
* `<instances>`: Numbers of instances to be benchmarked, separated by commas. For example `1, 2, 3, 4`.
* `<partitions>`: Number of partitions for Kafka topics. Optional. Default `40`.
* `<cpu-limit>`: Kubernetes CPU limit. Optional. Default `1000m`.
* `<memory-limit>`: Kubernetes memory limit. Optional. Default `4Gi`.
* `<commit-interval>`: Kafka Streams' commit interval in milliseconds. Optional. Default `100`.
* `<duration>`: Duration in minutes subexperiments should be executed for. Optional. Default `5`.
