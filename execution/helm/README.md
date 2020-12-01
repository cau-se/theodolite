# Theodolite Helm Chart

## Installation

```sh
helm install my-confluent ./theodolite-chart
```

**Please note: The execution python scripts uses hard-coded urls, to connect to Kafka and Zookeeper. For that reason, the name of this chart must be `my-confluent`**

This chart installs requirements to execute benchmarks with theodolite.

Dependencies and sub charts:

- prometheus operator
- prometheus
- grafana (incl. dashboard and data source configuration)
- kafka
- kafka-client
- zookeeper

## Test

Test the installation:

```sh
helm test <release-name>
```

Our test files are located [here](./theodlote-chart/templates/../../theodolite-chart/templates/tests). Many subcharts have their own tests, these are also executed and are placed in the respective /templates folders. Please note: If a test fails, helmet will stop testing.


## Configuration

In development environments Kubernetes resources are often low.  To reduce resource consumption, we provide an `one broker-value.yaml` file. This file can be used with:

```sh
helm install theodolite <path-to-chart> -f one-broker-values.yaml
```

## Development

### Sub charts

Sub-diagrams can be added using a repository or a path to a directory containing the diagram files. Currently we cannot add cp-confluent-chart and the kafka-lag-exporter with a repository URL. This might be possible in the future. The folder [dependencies](./dependencies) contains the required files.


**Hints**:

- Grafana configuration: Grafana config maps contains expressions like {{ topic }}. Helm uses the same syntax for template function.  More information [here](https://github.com/helm/helm/issues/2798)
  - Escape braces: {{ "{{" topic }}
  - Let Helm render the template as raw string: {{ `{{ <config>}}` }}
  