# Theodolite Helm Chart

## Installation

The Theodolite Helm chart with all its dependencies can be installed via:

```sh
helm dependencies update .
helm install theodolite .
```

**Hint for Windows users:** The Theodolite Helm chart makes use of some symbolic links. These are not properly created when this repository is checked out with Windows. There are a couple of solutions presented in this [Stack Overflow post](https://stackoverflow.com/q/5917249/4121056). A simpler workaround is to manually delete the symbolic links and replace them by the files and folders, they are pointing to. The relevant symbolic links are `benchmark-definitions/examples`, `benchmark-definitions/theodolite-benchmarks` and the files inside `crd`.

## Customize Installation

As usual, the installation with Helm can be configured by passing a values YAML file:

```
helm install theodolite . -f <your-config.yaml>
```

We provide a minimal configuration, especially suited for development environments, with the `preconfigs/minimal.yaml`
file.

Per default, Helm installs the Theodolite CRDs used for the operator. If Theodolite will not be used as operator or if
the CRDs are already installed, you can skip their installation by adding the flag `--skip-crds`.

## Test Installation

Test the installation with:

```sh
helm test theodolite
```

Our test files are located [here](templates/tests). Many subcharts have their own tests, which are also executed.
Please note: If a test fails, Helm will stop testing.

## Uninstall this Chart

The Theodolite Helm can easily be removed with:

```sh
helm uninstall theodolite
```

Helm does not remove any CRDs created by this chart. You can remove them manually with:

```sh
# CRDs for Theodolite
kubectl delete crd executions.theodolite.rocks
kubectl delete crd benchmarks.theodolite.rocks
# CRDs for Prometheus operator (see https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack#uninstall-chart)
kubectl delete crd alertmanagerconfigs.monitoring.coreos.com
kubectl delete crd alertmanagers.monitoring.coreos.com
kubectl delete crd podmonitors.monitoring.coreos.com
kubectl delete crd probes.monitoring.coreos.com
kubectl delete crd prometheusagents.monitoring.coreos.com
kubectl delete crd prometheuses.monitoring.coreos.com
kubectl delete crd prometheusrules.monitoring.coreos.com
kubectl delete crd scrapeconfigs.monitoring.coreos.com
kubectl delete crd servicemonitors.monitoring.coreos.com
kubectl delete crd thanosrulers.monitoring.coreos.com
# CRDs for Strimzi
kubectl delete crd kafkabridges.kafka.strimzi.io
kubectl delete crd kafkaconnectors.kafka.strimzi.io
kubectl delete crd kafkaconnects.kafka.strimzi.io
kubectl delete crd kafkamirrormaker2s.kafka.strimzi.io
kubectl delete crd kafkamirrormakers.kafka.strimzi.io
kubectl delete crd kafkarebalances.kafka.strimzi.io
kubectl delete crd kafkas.kafka.strimzi.io
kubectl delete crd kafkatopics.kafka.strimzi.io
kubectl delete crd kafkausers.kafka.strimzi.io
kubectl delete crd strimzipodsets.core.strimzi.io
```

## Development

### Dependencies

The following 3rd party charts are used by Theodolite:

- Kube Prometheus Stack
  - to install the Prometheus Operator, which is used to create a Prometheus instances
  - to deploy Grafana (including a dashboard and a data source configuration)
- Grafana (deprecated as replaced by Kube Prometheus Stack)
- Strimzi (for managing Kafka and Zookeeper)

### Hints

#### Grafana

Grafana ConfigMaps contain expressions like `{{ topic }}`. Helm uses the same syntax for template function. More information [here](https://github.com/helm/helm/issues/2798)
  - Escape braces: {{ "{{" topic }}
  - Let Helm render the template as raw string: {{ `{{ <config>}}` }}
  