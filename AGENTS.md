# Agent Guide for Theodolite

This file provides guidance to AI coding agents when working with code in this repository.

## What is Theodolite

Theodolite is a generic framework for benchmarking the **scalability** of cloud-native applications on Kubernetes, implemented as a **Kubernetes Operator**. Scalability is characterized by three attributes — *load intensity*, *provisioned resources*, and *SLOs* — and Theodolite quantifies it via two complementary metrics:

- **Demand metric** `demand(l) = min resources to meet all SLOs at load l` — answers "how many resources does this load require?"
- **Capacity metric** `capacity(r) = max load that meets all SLOs with r resources` — answers "how much load can these resources handle?"

Both metrics are functions (not single values), and Theodolite approximates them by running isolated SLO experiments for discrete (load, resource) combinations. Each experiment deploys the SUT at a fixed load and resource level for a configured duration, evaluates SLO compliance, and feeds the result into a search strategy to select the next combination to test.

Theodolite was originally designed for benchmarking **stream processing frameworks** and ships with a suite of stream processing benchmarks, though the framework itself is generic and not coupled to any specific benchmark or type of system.

## Repository Structure

| Directory | Language | Purpose |
|-----------|----------|---------|
| `theodolite/` | Kotlin/Quarkus | Core Kubernetes operator |
| `theodolite-benchmarks/` | Java | 4 benchmark use cases × multiple streaming frameworks |
| `slo-checker/` | Python | SLO checker sidecars (run inside the Theodolite operator pod) |
| `helm/` | YAML | Helm chart (includes Prometheus; Strimzi only needed for stream processing benchmarks) |
| `docs/` | Jekyll/Ruby | Documentation website |
| `buildimages/` | Dockerfile | CI build infrastructure images |
| `util/` | - | Utilities (e.g., random-scheduler) |

When working within a subdirectory, read its `README.md` first for local conventions, build commands, and architecture details. The `docs/` directory contains examples and architecture descriptions that are useful for broader context.

## Theodolite Operator Architecture

The operator is implemented in Kotlin/Quarkus and lives in `theodolite/`. It manages two custom resource definitions (CRDs), defined in `theodolite/crd/`:

- `benchmarks.theodolite.rocks` — **static, reusable** definition: SUT + load generator deployment artifacts, supported load types, resource types, SLIs, and SLOs. Benchmark designers publish these; they have no lifecycle.
- `executions.theodolite.rocks` — **one-time configured run**: references a Benchmark and adds the specific load values, resource values, search strategy, repetition count, experiment duration, and optional SLI/SLO overrides. Executions have a lifecycle (`Pending → Running → Finished/Failed`).

SLOs are evaluated via a pipeline: an **SLI** defines *how* to collect a metric from an external system such as Prometheus or Dynatrace; an **SLO** defines *how* to evaluate it (threshold, aggregation, operator). Actual SLO evaluation is externalized to the **SLO checker** sidecar (`slo-checker/`), keeping that logic out of the operator itself.

## Theodolite Stream Processing Benchmarks

Theodolite ships with 4 stream processing benchmarks implemented across multiple frameworks. They are independent of the operator, require a Kafka cluster (via Strimzi, enabled by default), and their Kubernetes resource manifests live in `theodolite-benchmarks/definitions/`.

## Contributing

* **Commits** follow [Chris Beams' style](http://chris.beams.io/posts/git-commit/): imperative subject line, ≤50 characters, capitalized, no trailing period; use the body (wrapped at 72 characters) to explain *what* and *why*, not *how*.
* **Tests**: new features must be covered by tests and all existing tests must pass before committing.
* **Docs**: changes to public-facing behavior (CRD fields, configuration, CLI flags, benchmark definitions) must be reflected in `docs/`.

