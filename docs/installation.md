---
title: Installation
has_children: false
nav_order: 3
---

# Installing Theodolite

The easiest option to install Theodolite is using [Helm](https://helm.sh).
To install Theodolite with all its dependencies run:

```sh
helm repo add theodolite https://cau-se.github.io/theodolite
helm repo update
helm install theodolite theodolite/theodolite
```

This installs Theodolite in operator mode. Operator mode is the easiest to use, but requires some permissions in the installation. If those cannot be granted, Theodolite can also be installed for standalone mode. 


## Installation Options

As usual, the installation via Helm can be configured by passing a values YAML file:

```
helm install theodolite theodolite/theodolite --values <your-config.yaml>
```

For this purpose the [default values file]() can serve as a template for your custom configuration.

### Operator mode

The Theodolite operator allows to run and manage benchmarks through the Kubernetes API. It is installed by setting `operator.enabled` to `true`. In addition to Theodolite's dependencies, this will deploy all resources that are required for the operator as well as the CRDs, users will interact with.

**TODO:** link

### Standalone mode

For running Theodolite in standalone mode, it is sufficient to disable the operator by setting `operator.enabled` to `false`. With this setting, only Theodolite's dependencies as well as resources to get the necessary permissions are installed.

### Random Scheduler

Installation of the random scheduler can be enabled and via `randomScheduler.enabled`. Please note that the random scheduler is neither required in operator mode nor in standalone mode. However, it has to be installed if benchmark executions should use random scheduling.

**TODO:** link

### Multiple installations in the same cluster

Multiple Theodolite installations in the same namespace are currently not fully tested.
In cases, where you need to install multiple Theodolite instances, it's best to use dedicated namespaces **and** different release names.

*Note that for meaningful results, usually only one benchmark should be executed at a time.*

## Installation Hints

### Installation with a release name other than `theodolite`

When using another release name than `theodolite`, make sure to adjust the Kafka Lag Exporter configuration of you `values.yaml` accordingly:

```yaml
kafka-lag-exporter:
  clusters:
  - name: "<your-release-name>-cp-kafka"
    bootstrapBrokers: "<your-release-name>-cp-kafka:9092"
```

This seems unfortunately to be necessary as Helm does not let us inject values into dependency charts.
