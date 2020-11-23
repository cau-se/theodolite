# Theodolite Helm Chart

## Installation

```sh
helm install thedolite ./theodolite-chart
```

This chart installs requirements to execute benchmarks with theodolite.

Dependencies and sub charts:

- prometheus operator
- prometheus
- grafana (incl. dashboard and data source configuration)
- kafka
- kafka-client
- zookeeper

## Development

### Sub charts

Sub-diagrams can be added using a repository or a path to a directory containing the diagram files. Currently we cannot add cp-confluent-chart and the kafka-lag-exporter with a repository URL. This might be possible in the future. The folder [dependencies](./dependencies) contains the required files.


**Hints**:

- Grafana configuration: Grafana config maps contains expressions like {{ topic }}. Helm uses the same syntax for template function.  More information [here](https://github.com/helm/helm/issues/2798)
  - Escape braces: {{ "{{" topic }}
  - Let Helm render the template as raw string: {{ `{{ <config>}}` }}
  