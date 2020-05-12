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