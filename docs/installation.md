---
title: Installation
has_children: false
nav_order: 3
---

# Installing Theodolite

The easiest option to install Theodolite is using [Helm](https://helm.sh).
To install Theodolite with all its dependencies run:

```sh
helm repo add theodolite https://www.theodolite.rocks
helm repo update
helm install theodolite theodolite/theodolite
```

## Installation Options

As usual, the installation via Helm can be configured by passing a values YAML file:

```sh
helm install theodolite theodolite/theodolite --values <your-config.yaml>
```

For this purpose the [default values file](https://github.com/cau-se/theodolite/blob/main/helm/values.yaml) can serve as a template for your custom configuration.

### Minimal setup

For Kubernetes clusters with limited resources such as on local developer installations, we provide a [minimal values file](https://github.com/cau-se/theodolite/blob/main/helm/preconfigs/minimal.yaml).

### Persisting results

To store the results of benchmark executions in a [PersistentVolume](https://kubernetes.io/docs/concepts/storage/persistent-volumes), `operator.resultsVolume.persistent.enabled` has to be set to `true`. This requires that either a statically provisioned PersistentVolume is available or a dynamic provisioner exists (which is the case for many Kubernetes installations). If required, you can select a storage class with `operator.resultsVolume.persistent.storageClassName`.
You can also use an existing PersistentVolumeClaim by setting `operator.resultsVolume.persistent.existingClaim`.
If persistence is not enabled, all results will be gone upon pod termination.

### Exposing Grafana

Per default, Theodolite exposes a Grafana instance as NodePort at port `31199`. This can configured by setting `grafana.service.nodePort`.

### Additional Kubernetes cluster metrics

As long as you have sufficient permissions on your cluster, you can integrate additional Kubernetes metrics into Prometheus. This involes enabling some exporters, additional Grafana dashboards and additional permissions. We provide a [values file for enabling extended metrics](https://github.com/cau-se/theodolite/blob/main/helm/preconfigs/extended-metrics.yaml).

See the [kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack) for more details on configuring the individual exporters.

### Random scheduler

Installation of the random scheduler can be enabled via `randomScheduler.enabled`. Please note that the random scheduler is neither required in operator mode nor in standalone mode. However, it has to be installed if benchmark executions should use random scheduling.

<!-- **TODO:** link-->

### Multiple installations in the same cluster

Multiple Theodolite installations in the same namespace are currently not fully tested.
In cases, where you need to install multiple Theodolite instances, it's best to use dedicated namespaces **and** different release names.

*Note that for meaningful results, usually only one benchmark should be executed at a time.*


## Test the Installation

You can test the installation with:

```sh
helm test theodolite
```


## Uninstall Theodolite

The Theodolite Helm chart can easily be removed with:

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
kubectl delete crd kafkanodepools.kafka.strimzi.io
kubectl delete crd kafkarebalances.kafka.strimzi.io
kubectl delete crd kafkas.kafka.strimzi.io
kubectl delete crd kafkatopics.kafka.strimzi.io
kubectl delete crd kafkausers.kafka.strimzi.io
kubectl delete crd strimzipodsets.core.strimzi.io
```
