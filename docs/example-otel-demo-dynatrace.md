---
title: "Example: OTel Demo with Dynatrace"
has_children: false
parent: "Creating Benchmarks"
nav_order: 2
---

# Example: A Benchmark for the OpenTelemetry Demo with Dynatrace

The [OpenTelemetry Demo](https://opentelemetry.io/docs/demo/) is a microservice-based web shop used to showcase OpenTelemetry instrumentation across multiple languages. This example shows how to benchmark it with Theodolite using **Dynatrace** as the metric provider — collecting span-based latency data via DQL (Dynatrace Query Language).

All example files are located in [`theodolite/examples/otel-demo-dynatrace/`](https://github.com/cau-se/theodolite/tree/main/theodolite/examples/otel-demo-dynatrace).

## Prerequisites

- A running Kubernetes cluster with [Theodolite installed](installation)
- A Dynatrace tenant with:
  - An [OTel-compatible API token](https://docs.dynatrace.com/docs/ingest-from/opentelemetry/otlp-api#authentication-export-to-activegate) for data ingestion
  - An [OAuth client](https://docs.dynatrace.com/docs/manage/identity-access-management/access-tokens-and-oauth-clients/oauth-clients#create-an-oauth2-client) with the `storage:query:read` scope for DQL queries
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

## Defining the Benchmark

The benchmark measures how the OTel Demo web shop scales with increasing load. Load is the number of simulated **users** and resources are the number of frontend **replicas**. For each (users, replicas) combination, Theodolite runs the load generator at the configured user count, collects DQL span metrics, and checks whether the p90 latency stays below 500 ms. The result is the **capacity metric**: the maximum number of users the web shop can handle at a given number of replicas while meeting the latency SLO.

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

Note that Dynatrace reports span durations in nanoseconds, so 500 ms = 500,000,000 ns.

## Running the Benchmark

Create a ConfigMap containing the deployment manifests:

```sh
kubectl create configmap otel-demo-configmap \
  --from-file=frontend-deployment.yaml \
  --from-file=load-generator-deployment.yaml
```

Apply the benchmark:

```sh
kubectl apply -f otel-demo-benchmark.yaml
```

Verify that the benchmark is ready before starting the execution:

```sh
kubectl get benchmarks
```

Then apply the execution to start the benchmark run:

```sh
kubectl apply -f otel-demo-execution.yaml
```

Theodolite will iterate over the configured user counts and replica counts, fetch DQL metrics for each combination, and evaluate the p90 latency SLO. Follow progress with:

```sh
kubectl get executions -w
```

Results (one CSV file per SLI per experiment repetition) are written to the operator pod's result folder.
