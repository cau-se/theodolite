![Theodolite](docs/assets/logo/theodolite-horizontal-transparent.svg)

# Theodolite

> A theodolite is a precision optical instrument for measuring angles between designated visible points in the horizontal and vertical planes.  -- <cite>[Wikipedia](https://en.wikipedia.org/wiki/Theodolite)</cite>

Theodolite is a framework for benchmarking the horizontal and vertical scalability of cloud-native applications in Kubernetes.

## Quickstart

Theodolite runs scalability benchmarks in Kubernetes. Follow our [quickstart guide](https://www.theodolite.rocks/quickstart.html) to get started.

## Documentation

Documentation on Theodolite itself as well as regarding its benchmarking method can be found on the [Theodolite website](https://www.theodolite.rocks).

## Project Structure

* Core of Theodolite is its Kubernetes Operator, implemented in Kotlin. The source-code can be found in [`theodolite`](theodolite).
* Theodolite's Helm chart and templates are maintained in [`helm`](helm).
* We provide Juptyer notebooks for analyzing and visualizing the results of benchmark executions in [`analysis`](analysis).
* Theodolite comes with 4 application benchmarks, which are based on typical use cases for stream processing within microservices. Implementations of these benchmarks with several state-of-the art stream processing frameworks as well as corresponding load generators can be found in [`theodolite-benchmarks`](theodolite-benchmarks). This includes both the source code of the implementations as well as benchmark definitions for Theodolite in [`theodolite-benchmarks/definitions`](theodolite-benchmarks/definitions).
* The source code of Theodolite's SLO checkers are located in [`slo-checker`](slo-checker).
* The documentation, which is hosted on [theodolite.rocks](https://www.theodolite.rocks), is located in [`docs`](docs).

## How to Cite

If you use Theodolite, please cite

> Sören Henning and Wilhelm Hasselbring. “A Configurable Method for Benchmarking Scalability of Cloud-Native Applications”. In: *Empirical Software Engineering* 27. 2022. DOI: [10.1007/s10664-022-10162-1](https://doi.org/10.1007/s10664-022-10162-1).

When referring to our stream processing benchmarks, please cite

> Sören Henning and Wilhelm Hasselbring. “Theodolite: Scalability Benchmarking of Distributed Stream Processing Engines in Microservice Architectures”. In: *Big Data Research* 25. 2021. DOI: [10.1016/j.bdr.2021.100209](https://doi.org/10.1016/j.bdr.2021.100209). arXiv:[2009.00304](https://arxiv.org/abs/2009.00304).

See our website for a [list of publications](https://www.theodolite.rocks/publications.html) directly related to Theodolite.