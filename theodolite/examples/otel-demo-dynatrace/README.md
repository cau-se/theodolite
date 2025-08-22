
# A Theodolite Benchmark for the OpenTelemetry Demo with Dynatrace

## Installing the OpenTelemetry Demo for Dynatrace

For a detailed guide on monitoring the OpenTelemetry demo application with Dynatrace, visit:
https://www.dynatrace.com/news/blog/monitor-opentelemetry-demo-app-dashboards/

In short, we first need to create a Kubernetes secret with our Dynatrace API token and endpoint:

```sh
API_TOKEN="<your API token>"
DT_ENDPOINT=https://<your-tenant-id>.dynatrace.com/api/v2/otlp
kubectl create secret generic dynatrace --from-literal=API_TOKEN=${API_TOKEN} --from-literal=DT_ENDPOINT=${DT_ENDPOINT}
```

Now, we can install the OpenTelemetry demo application. With the provided Helm configuration, other OTel backends are disabled to reduce resource usage:

```sh
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts
helm install my-otel-demo open-telemetry/opentelemetry-demo --values otel-demo-helm-values.yaml
```

When logging into the Dynatrace UI, you should now be able to see traces, metrics, and logs from the deployed demo. For example, the Distributed Tracing app provides in-depth statistics about requests between the demo's components.

## Preparation for Automatic Benchmarking

We now remove the load generator and frontend deployments:

```sh
kubectl delete deployment load-generator frontend
```

This will allow Theodolite to deploy and scale both components as needed.

## Creation of a Theodolite Benchmark

First, we need to create a ConfigMap with the deployment manifests for the frontend and load generator such that Theodolite has access to them:

```sh
kubectl create configmap otel-demo-configmap --from-file=frontend-deployment.yaml --from-file=load-generator-deployment.yaml
```

We can now create a Theodolite benchmark:

```sh
kubectl apply -f theodolite-benchmark.yaml
```

**Note: This benchmark currently uses a dummy SLO, which always evaluates to true.**

To verify that the benchmark is now available for execution, run:

```sh
kubectl get benchmarks
```

You should see the newly created benchmark listed with status `Ready`.

## Run the Benchmark

To run the benchmark, use or the adjust the Execution YAML file and apply it:

```sh
kubectl apply -f theodolite-execution.yaml
```
