---
title: "Example: OTel Demo with Dynatrace"
has_children: false
parent: "Creating Benchmarks"
nav_order: 2
---

# Example: A Benchmark for the OpenTelemetry Demo with Dynatrace

The [OpenTelemetry Demo](https://opentelemetry.io/docs/demo/) is a microservice-based web shop used to showcase OpenTelemetry instrumentation across multiple languages. This example shows how to benchmark it with Theodolite using **Dynatrace** as the metric provider — collecting span-based latency data via DQL (Dynatrace Query Language) instead of Prometheus.

All example files are located in [`theodolite/examples/otel-demo-dynatrace/`](https://github.com/cau-se/theodolite/tree/main/theodolite/examples/otel-demo-dynatrace).

## Prerequisites

- A running Kubernetes cluster with Theodolite installed
- A Dynatrace tenant (SaaS or Managed) with:
  - An OTel-compatible API token for data ingestion
  - An OAuth client with the `storage:query:read` scope for DQL queries
- Helm CLI

## Cluster Preparation

### Step 1: Deploy the OTel Demo with Dynatrace

Create a Kubernetes secret with your Dynatrace credentials:

```sh
API_TOKEN="<your-dynatrace-api-token>"
DT_ENDPOINT="https://<your-tenant-id>.live.dynatrace.com/api/v2/otlp"
kubectl create secret generic dynatrace \
  --from-literal=API_TOKEN=${API_TOKEN} \
  --from-literal=DT_ENDPOINT=${DT_ENDPOINT}
```

Install the OTel Demo application via Helm:

```sh
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts
helm install my-otel-demo open-telemetry/opentelemetry-demo \
  --values otel-demo-helm-values.yaml
```

After a few minutes, verify that traces appear in the Dynatrace UI under *Distributed Tracing* or *Notebooks*.

### Step 2: Remove the Managed Load Generator and Frontend

Theodolite controls load and scaling, so remove the statically deployed components:

```sh
kubectl delete deployment load-generator frontend
```

### Step 3: Configure Theodolite with Dynatrace Credentials via Helm

All Dynatrace configuration is managed in the Theodolite Helm chart. Create a Kubernetes Secret with your DQL OAuth credentials:

```sh
kubectl create secret generic theodolite-dynatrace \
  --from-literal=clientId="<oauth-client-id>" \
  --from-literal=clientSecret="<oauth-client-secret>" \
  --from-literal=scope="storage:query:read" \
  --from-literal=resource="urn:dynatrace:environment:<environment-id>" \
  --from-literal=authUrl="https://sso.dynatrace.com/sso/oauth2/token"
```

Install or upgrade Theodolite with your Dynatrace values:

```yaml
# theodolite-values.yaml
operator:
  dynatrace:
    url: "https://<tenant-id>.apps.dynatrace.com/platform/storage/query/v1/query"
    existingSecret: "theodolite-dynatrace"
```

```sh
helm upgrade --install theodolite theodolite/theodolite --values theodolite-values.yaml
```

The generic SLO checker sidecar is deployed automatically — the benchmark YAML does not require an `externalSloChecker` URL.

## Defining the Benchmark

### SLI — What to Measure

The benchmark defines a single SLI using `provider: dynatrace`. The DQL query computes the 90th-percentile end-to-end latency across all root spans:

```yaml
slis:
  - name: "p90Latency"
    provider: "dynatrace"
    query: >-
      fetch spans, samplingRatio: 10, scanLimitGBytes: 50
      | filter request.is_root_span == true AND isNotNull(endpoint.name)
      | makeTimeseries {p90 = percentile(duration, 90)}, bins: 120, by: { dt.system.sampling_ratio }
```

The Dynatrace API endpoint and OAuth credentials come from the Helm chart (`operator.dynatrace.url` / `operator.dynatrace.existingSecret`) — no URLs or credentials appear in the benchmark YAML.

### SLO — When to Pass

The SLO evaluates the collected latency data against a fixed threshold:

```yaml
slos:
  - name: "p90-latency-slo"
    sli: "p90Latency"
    warmupSeconds: 60
    queryAggregation: "mean"
    repetitionAggregation: "median"
    operator: "lte"
    threshold: 500000000   # 500 ms in nanoseconds
```

`externalSloChecker` is omitted — Theodolite defaults to the generic SLO checker sidecar deployed alongside the operator by the Helm chart. Dynatrace reports span durations in nanoseconds, so 500 ms = 500,000,000 ns.

## Running the Benchmark

Create a ConfigMap containing the deployment manifests:

```sh
kubectl create configmap otel-demo-configmap \
  --from-file=frontend-deployment.yaml \
  --from-file=load-generator-deployment.yaml
```

Apply the benchmark and execution:

```sh
kubectl apply -f otel-demo-benchmark.yaml
kubectl apply -f otel-demo-execution.yaml
```

Theodolite will iterate over the configured user counts and replica counts, fetch DQL metrics for each combination, and evaluate the p90 latency SLO. Follow progress with:

```sh
kubectl get executions -w
```

Results (one CSV file per SLI per experiment repetition) are written to the operator pod's result folder.
