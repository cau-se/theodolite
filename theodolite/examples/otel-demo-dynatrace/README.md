# Theodolite Benchmark for the OpenTelemetry Demo with Dynatrace

This example demonstrates how to benchmark the [OpenTelemetry Demo application](https://opentelemetry.io/docs/demo/) using Theodolite with **Dynatrace as the metric provider** (via DQL — Dynatrace Query Language). The SLO checks the 90th-percentile end-to-end request latency.

## Prerequisites

- A running Kubernetes cluster with Theodolite installed
- A Dynatrace tenant with an OAuth client configured to access the DQL query API
- Helm CLI

## Step 1: Deploy the OpenTelemetry Demo with Dynatrace

For a detailed guide on monitoring the OpenTelemetry Demo with Dynatrace, see:
https://www.dynatrace.com/news/blog/monitor-opentelemetry-demo-app-dashboards/

Create a Kubernetes secret with your Dynatrace API token and OTLP endpoint:

```sh
API_TOKEN="<your-dynatrace-api-token>"
DT_ENDPOINT="https://<your-tenant-id>.live.dynatrace.com/api/v2/otlp"
kubectl create secret generic dynatrace \
  --from-literal=API_TOKEN=${API_TOKEN} \
  --from-literal=DT_ENDPOINT=${DT_ENDPOINT}
```

Install the OTel Demo application (the provided Helm values disable other backends):

```sh
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts
helm install my-otel-demo open-telemetry/opentelemetry-demo --values otel-demo-helm-values.yaml
```

After deployment, verify that traces are visible in the Dynatrace UI (e.g. via *Distributed Tracing*).

## Step 2: Prepare for Benchmarking

Remove the default load generator and frontend deployments so Theodolite can manage them:

```sh
kubectl delete deployment load-generator frontend
```

## Step 3: Configure the Theodolite Operator for DQL

Theodolite's Dynatrace metric fetcher reads OAuth credentials from environment variables on the operator pod. Set the following environment variables (e.g. via a Kubernetes Secret and an `envFrom` reference in the Helm values):

| Variable | Description |
|---|---|
| `DQL_CLIENTID` | OAuth client ID |
| `DQL_CLIENTSECRET` | OAuth client secret |
| `DQL_SCOPE` | OAuth scope (e.g. `storage:query:read`) |
| `DQL_RESOURCE` | OAuth resource URN of your Dynatrace environment |
| `DQL_AUTHURL` | Token endpoint URL (e.g. `https://sso.dynatrace.com/sso/oauth2/token`) |

Update `otel-demo-benchmark.yaml` to set `providerConfig.dynatraceUrl` to the DQL query API endpoint of your tenant:

```yaml
providerConfig:
  dynatraceUrl: "https://<tenant-id>.apps.dynatrace.com/platform/storage/query/v1/query"
```

## Step 4: Create the Benchmark

Create a ConfigMap with the deployment manifests so Theodolite can access them:

```sh
kubectl create configmap otel-demo-configmap \
  --from-file=frontend-deployment.yaml \
  --from-file=load-generator-deployment.yaml
```

Apply the benchmark definition:

```sh
kubectl apply -f otel-demo-benchmark.yaml
```

Verify the benchmark is ready:

```sh
kubectl get benchmarks
```

## Step 5: Run the Benchmark

Adjust `otel-demo-execution.yaml` if needed (load values, resource values, duration), then apply:

```sh
kubectl apply -f otel-demo-execution.yaml
```

Theodolite will iterate over the configured user counts and instance counts, collecting DQL metrics for each combination and evaluating the p90 latency SLO.

Follow progress with:

```sh
kubectl get executions -w
```

Results (CSV files per SLI per repetition) are written to the operator pod's result folder.
