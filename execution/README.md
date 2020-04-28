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

**TODO** Add required configuration, installation

### The Kafka Lag Exporter

Lightbend's Kafka Lag Exporter can be installed via helm:

```sh
helm install kafka-lag-exporter https://github.com/lightbend/kafka-lag-exporter/releases/download/v0.6.0/kafka-lag-exporter-0.6.0.tgz
```

**TODO** Add configuration + ServiceMonitor


## Python 3.7

For executing benchmarks and analyzing their results, a Python 3.7 installation
is required. We suggest to use a virtual environment placed in the `.venv` directory.

**TODO** Show how to install requirements
