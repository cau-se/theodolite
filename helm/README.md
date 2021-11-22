# Theodolite Helm Chart

## Installation

The Theodolite Helm chart with all its dependencies can be installed via:

```sh
helm dependencies update .
helm install theodolite .
```

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
# CRDs from Theodolite
kubectl delete crd executions.theodolite.com
kubectl delete crd benchmarks.theodolite.com
# CRDs from Prometheus operator (see https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack#uninstall-chart)
kubectl delete crd alertmanagerconfigs.monitoring.coreos.com
kubectl delete crd alertmanagers.monitoring.coreos.com
kubectl delete crd podmonitors.monitoring.coreos.com
kubectl delete crd probes.monitoring.coreos.com
kubectl delete crd prometheuses.monitoring.coreos.com
kubectl delete crd prometheusrules.monitoring.coreos.com
kubectl delete crd servicemonitors.monitoring.coreos.com
kubectl delete crd thanosrulers.monitoring.coreos.com
```

## Development

### Dependencies

The following 3rd party charts are used by Theodolite:

- Kube Prometheus Stack (to install the Prometheus Operator, which is used to create a Prometheus instances)
- Grafana (including a dashboard and a data source configuration)
- Confluent Platform (for Kafka and Zookeeper)
- Kafka Lag Exporter (used to collect monitoring data of the Kafka lag)

### Hints

#### Grafana

Grafana ConfigMaps contain expressions like `{{ topic }}`. Helm uses the same syntax for template function. More information [here](https://github.com/helm/helm/issues/2798)
  - Escape braces: {{ "{{" topic }}
  - Let Helm render the template as raw string: {{ `{{ <config>}}` }}
  