# Theodolite Helm Chart

## Installation

Install the chart via:

```sh
helm dependencies update .
helm install theodolite .
```

This chart installs requirements to execute benchmarks with Theodolite.

Dependencies and subcharts:

- Prometheus Operator
- Prometheus
- Grafana (incl. dashboard and data source configuration)
- Kafka
- Zookeeper
- A Kafka client pod

## Test

Test the installation:

```sh
helm test theodolite
```

Our test files are located [here](templates/../../theodolite-chart/templates/tests). Many subcharts have their own tests, these are also executed and are placed in the respective /templates folders. 

Please note: If a test fails, Helm will stop testing.

It is possible that the tests are not running successfully at the moment. This is because the Helm tests of the subchart cp-confluent receive a timeout exception. There is an [issue](https://github.com/confluentinc/cp-helm-charts/issues/318) for this problem on GitHub.

## Configuration

In development environments Kubernetes resources are often low. To reduce resource consumption, we provide an `one-broker-value.yaml` file. This file can be used with:

```sh
helm install theodolite . -f preconfigs/one-broker-values.yaml
```

## Uninstall this Chart

To uninstall/delete the `theodolite` deployment:

```sh
helm delete theodolite
```

This command does not remove the CRDs which are created by this chart. Remove them manually with:

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

**Hints**:

- Grafana configuration: Grafana ConfigMaps contains expressions like {{ topic }}. Helm uses the same syntax for template function. More information [here](https://github.com/helm/helm/issues/2798)
  - Escape braces: {{ "{{" topic }}
  - Let Helm render the template as raw string: {{ `{{ <config>}}` }}
  