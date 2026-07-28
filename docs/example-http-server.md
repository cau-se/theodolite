---
title: "Example: HTTP Server"
has_children: false
parent: "Creating Benchmarks"
nav_order: 1
---

# Example: A Benchmark for a Simple HTTP Server

This example shows how to benchmark a simple HTTP server with Theodolite using Prometheus as the metric provider. It is a self-contained, easy-to-run benchmark that does not require Kafka — only a Kubernetes cluster with Theodolite and Prometheus installed.

All example files are located in [`theodolite/examples/http-server/`](https://github.com/cau-se/theodolite/tree/main/theodolite/examples/http-server).

## What this benchmark measures

The benchmark answers the scalability question: *How many server instances are needed to handle a given request rate while keeping the 5xx error rate below 1%?*

- **Load dimension (`CallsPerSecond`)**: the number of HTTP requests per second sent by the load generator.
- **Resource dimension (`Instances`)**: the number of running server pods.
- **SLO**: the fraction of 5xx responses (measured over a 1-minute window) must not exceed 1%.

Theodolite runs a configurable search strategy (e.g. binary search or linear search) over the (load, instances) space to approximate the demand curve `demand(load) = min instances needed to meet the SLO at that load`.

## System Under Test (SUT)

Each server pod runs two containers:

- **httpbin** — a lightweight HTTP server that exposes a `/delay/1` endpoint, which replies after ~1 second. Constrained to a small CPU limit so it becomes a bottleneck under sufficient load.
- **Envoy** — a reverse proxy that forwards all requests at `/` to httpbin's `/delay/1`. Envoy enforces a 2-second timeout: if httpbin does not respond in time, Envoy returns a 502. Envoy publishes rich Prometheus metrics on its admin port (`:9901/stats/prometheus`) without any extra exporter sidecar.

A Prometheus `ServiceMonitor` scrapes the Envoy admin endpoint every 10 seconds.

## Load Generator

The load generator is a single-container pod running `curl` in a tight loop. The number of calls per second is controlled by the `CALLS_PER_SECOND` environment variable, which Theodolite patches via `EnvVarPatcher` for each experiment. Requests are issued in the background (`curl ... &`) so the rate is independent of response time.

## SLI and SLO

The SLI (Service Level Indicator) is the **5xx error rate** computed from Envoy's downstream request counters:

```
sum(rate(envoy_http_downstream_rq_xx{envoy_response_code_class="5"}[1m]))
/ sum(rate(envoy_http_downstream_rq_xx[1m]))
```

This is a ratio in [0, 1] where 0 means no errors. The SLO evaluates the maximum value of this ratio observed across the experiment (after the warmup period) and checks that it is ≤ 0.01 (1%).

Theodolite now separates metric collection from evaluation:

- The **SLI** in the `benchmark` resource specifies the Prometheus query.
- The **SLO** in both the `benchmark` (as default configuration) and the `execution` (as an override) specifies the evaluation logic: aggregation function, operator, and threshold.

## Prerequisites

- A running Kubernetes cluster with Theodolite installed via Helm (see [installation](installation))
- `kubectl` and `helm`

## Run the example

**Step 1:** Apply the ConfigMap and benchmark definition:

```sh
cd theodolite/examples/http-server
kubectl apply -f example-configmap.yaml
kubectl apply -f example-benchmark.yaml
```

Verify the benchmark was accepted:

```sh
kubectl get benchmarks
```

**Step 2:** Apply the execution:

```sh
kubectl apply -f example-execution.yaml
```

**Step 3:** Watch progress:

```sh
kubectl get executions -w
```

The execution transitions through `Pending → Running → Finished`. Theodolite logs each experiment's SLO result. Final results (one CSV file per SLI per repetition) are written to the results volume in the operator pod.

**Step 4:** Clean up:

```sh
kubectl delete -f example-execution.yaml
kubectl delete -f example-benchmark.yaml
kubectl delete -f example-configmap.yaml
```

## Customizing the Benchmark

**Change the load range**: edit `loadValues` in `example-execution.yaml`. The default `[10, 20, 30, 50, 75, 100]` (calls/s) is suitable for a small cluster. For larger clusters, extend the range.

**Change the resource range**: edit `resourceValues` (number of server instances). The default `[1, 2, 3, 4, 5]` covers a small range.

**Change the SLO threshold**: the `threshold: 0.01` in `example-benchmark.yaml` allows up to 1% errors. Set `threshold: 0` to require zero errors.

**Change the search strategy**: the default `RestrictionSearch` with `LinearSearch` scans load values left-to-right and uses a lower-bound restriction to avoid re-testing obviously insufficient resource counts. Switch to `BinarySearch` for fewer experiments when the space is large.

**Change the experiment duration**: `duration: 180` (seconds) gives the SUT time to stabilize. Reduce for faster iteration during development, increase for more stable measurements.

**Change the warmup period**: `warmupSeconds: 60` discards the first 60 seconds of each experiment from SLO evaluation, allowing Envoy metrics to stabilize after pod start.
