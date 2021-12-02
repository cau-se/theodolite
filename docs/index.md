---
title: Home
nav_order: 1
#nav_exclude: true
permalink: /
---

# Theodolite

> A theodolite is a precision optical instrument for measuring angles between designated visible points in the horizontal and vertical planes.  -- <cite>[Wikipedia](https://en.wikipedia.org/wiki/Theodolite)</cite>

Theodolite is a framework for benchmarking the horizontal and vertical scalability of stream processing engines.

[Get started now](quickstart){: .btn .btn-primary .fs-5 .mb-4 .mb-md-0 .mr-4 }
[Learn more](benchmarks-and-executions){: .btn .fs-5 .mb-4 .mb-md-0 }

---

More content...

<!--
## Theodolite Benchmarking Tool

Theodolite aims to benchmark scalability of stream processing engines for real use cases. Microservices that apply stream processing techniques are usually deployed in elastic cloud environments. Hence, Theodolite's cloud-native benchmarking framework deploys its components in a cloud environment, orchestrated by Kubernetes. It is recommended to install Theodolite with the package manager Helm. The Theodolite Helm chart along with instructions how to install it can be found in the [`helm`](helm) directory.

## Theodolite Analysis Tools

Theodolite's benchmarking method maps load intensities to the resource amounts that are required for processing them. A plot showing how resource demand evolves with an increasing load allows to draw conclusions about the scalability of a stream processing engine or its deployment. Theodolite provides Jupyter notebooks for creating such plots based on benchmarking results from the execution framework. More information can be found in [Theodolite analysis tool](analysis).

## Theodolite Benchmarks

Theodolite comes with 4 application benchmarks, which are based on typical use cases for stream processing within microservices. For each benchmark, a corresponding load generator is provided. Currently, this repository provides benchmark implementations for Apache Kafka Streams and Apache Flink. The benchmark sources can be found in [Thedolite benchmarks](theodolite-benchmarks).
-->

## How to Cite

If you use Theodolite, please cite

> Sören Henning and Wilhelm Hasselbring. (2021). Theodolite: Scalability Benchmarking of Distributed Stream Processing Engines in Microservice Architectures. Big Data Research, Volume 25. DOI: [10.1016/j.bdr.2021.100209](https://doi.org/10.1016/j.bdr.2021.100209). arXiv:[2009.00304](https://arxiv.org/abs/2009.00304).
