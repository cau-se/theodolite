# Theodolite

> A theodolite is a precision optical instrument for measuring angles between designated visible points in the horizontal and vertical planes.  -- <cite>[Wikipedia](https://en.wikipedia.org/wiki/Theodolite)</cite>

Theodolite is a framework for benchmarking the horizontal and vertical scalability of stream processing engines. It consists of three modules:

## Theodolite Benchmarks

Theodolite contains 4 application benchmarks, which are based on typical use cases for stream processing within microservices. For each benchmark, a corresponding workload generator is provided. Currently, this repository provides benchmark implementations for Apache Kafka Streams and Apache Flink. The benchmark sources can be found in [Thedolite benchmarks](theodolite-benchmarks).


## Theodolite Execution Framework

Theodolite aims to benchmark scalability of stream processing engines for real use cases. Microservices that apply stream processing techniques are usually deployed in elastic cloud environments. Hence, Theodolite's cloud-native benchmarking framework deploys its components in a cloud environment, orchestrated by Kubernetes. It is recommended to install theodolite with Kubernetes package manager Helm, more information on how to instal Theodoolite can be found in [Theodolite Helm Chart](helm).

For the execution of an benchmark, a `benchmark`and an `execution` must be definined, examples can be found [here](./theodolite/examples). More information can be found at [docs](docs).


## Theodolite Analysis Tools

Theodolite's benchmarking method creates a *scalability graph* allowing to draw conclusions about the scalability of a stream processing engine or its deployment. A scalability graph shows how resource demand evolves with an increasing workload. Theodolite provides Jupyter notebooks for creating such scalability graphs based on benchmarking results from the execution framework. More information can be found in [Theodolite analysis tool](analysis).


## How to Cite

If you use Theodolite, please cite

> Sören Henning and Wilhelm Hasselbring. (2021). Theodolite: Scalability Benchmarking of Distributed Stream Processing Engines in Microservice Architectures. Big Data Research, Volume 25. DOI: [10.1016/j.bdr.2021.100209](https://doi.org/10.1016/j.bdr.2021.100209). arXiv:[2009.00304](https://arxiv.org/abs/2009.00304).
