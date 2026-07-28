# Theodolite Benchmark for the OpenTelemetry Demo with Dynatrace

This example demonstrates how to benchmark the [OpenTelemetry Demo application](https://opentelemetry.io/docs/demo/) using Theodolite with **Dynatrace as the metric provider** (via DQL — Dynatrace Query Language). The SLO checks the 90th-percentile end-to-end request latency.

Metric collection and SLO evaluation are fully configured via the Helm chart — the benchmark YAML itself does not require any Dynatrace-specific URLs or credentials.

## Prerequisites

- A running Kubernetes cluster with Theodolite installed via Helm
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

## Step 3: Configure Theodolite with Dynatrace Credentials via Helm

All Dynatrace configuration is set in the Theodolite Helm chart. Create a Kubernetes Secret with your DQL OAuth credentials:

```sh
kubectl create secret generic theodolite-dynatrace \
  --from-literal=clientId="<oauth-client-id>" \
  --from-literal=clientSecret="<oauth-client-secret>" \
  --from-literal=scope="storage:query:read" \
  --from-literal=resource="urn:dynatrace:environment:<environment-id>" \
  --from-literal=authUrl="https://sso.dynatrace.com/sso/oauth2/token"
```

Then install or upgrade the Theodolite Helm chart with:

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

The generic SLO checker sidecar is deployed automatically by the Helm chart and is used as the default SLO checker — no further configuration is needed in the benchmark YAML.

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

Theodolite will iterate over the configured user counts and instance counts, collecting DQL metrics for each combination and evaluating the p90 latency SLO against a 500 ms threshold.

Follow progress with:

```sh
kubectl get executions -w
```

Results (CSV files per SLI per repetition) are written to the operator pod's result folder.
