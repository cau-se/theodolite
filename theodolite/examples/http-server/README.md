# HTTP Server Example

This benchmark measures the scalability of a simple HTTP server: specifically, how many instances of the server are needed to handle a given request rate while keeping the 5xx error rate below 1%.

The SUT is [httpbin](https://httpbin.org/) behind an [Envoy](https://www.envoyproxy.io/) reverse proxy. Envoy exposes rich Prometheus metrics natively on its admin port, so no extra exporter sidecar is required. The load generator issues HTTP requests at a configurable rate (`CALLS_PER_SECOND`). Envoy times out requests that take longer than 2 seconds (returning a 502), so the SLO naturally captures both latency and overload.

For a full walkthrough, including the SUT architecture, how to interpret results, and how to customize the benchmark, see the [docs page](https://www.theodolite.rocks/example-http-server).

## Prerequisites

- A running Kubernetes cluster with Theodolite installed via Helm (see [installation](https://www.theodolite.rocks/installation))
- `kubectl` and `helm`

## Run the example

```sh
cd theodolite/examples/http-server

kubectl apply -f example-configmap.yaml
kubectl apply -f example-benchmark.yaml
kubectl apply -f example-execution.yaml

kubectl get executions -w
```
