---
title: Home
nav_order: 1
#nav_exclude: true
permalink: /
---

![Theodolite](assets/theodolite-stacked-transparent.svg){: .d-block .mx-auto .mb-8 .theodolite-logo }


Theodolite is a framework for benchmarking the horizontal and vertical scalability of cloud-native applications.
{: .fs-6 .fw-300 .text-center }

[Get started now](quickstart){: .btn .btn-primary .fs-5 .mb-4 .mb-md-0 .mr-4 }
[Learn the underlying concepts](benchmarks-and-executions){: .btn .fs-5 .mb-4 .mb-md-0 }
{: .text-center }

---



## Kubernetes Operator for Cloud-Native Benchmarking Experience

With Theodolite, you run and manage scalability benchmarks via the Kubernetes API.
<!--After installing Theodolite, -->
Benchmarks and their executions are purely declaratively described in Kubernetes YAML files, which can be submitted to Kubernetes via `kubectl`.
This way you get the full experience of Kubernetes tooling. <!-- including auto-completion, schema validation and fehler behandlung-->
Theodolite itself runs as an operator in your cluster and watches for newly scheduled benchmark executions.
After installation, Theodolite runs continuously inside your Kubernetes cluster and watches for newly scheduled benchmark executions.

<!-- Integration with established cloud-native tooling such as Prometheus>

<!-- Designers of scalability benchmarks are freed from ... -->
<!-- Benchmarks -->

<!--
* ... for cloud-native experience
* Use kubectl to run benchmarks
* runs in background on your Kubernetes cluster
* Benchmarks and their executions are defined as
  * CRDs/YAMl files, managed by the Kubernetes API
-->

## Grounded on Established Scalability Definitions and Benchmarking Best Practices

<!-- Theodolite measures scalability by running individual experiments for different load intensities and resource amounts -->
Theodolite measures scalability by running a system under test (SUT) for different load intensities and resource amounts.
For each combination of generated load and provisioned resources, Theodolite checks whether certain service-level objects (SLOs) are fulfilled.
This way, Theodolite empirically derives how resource demand evolves with increasing load.

Each load-resource-combination is tested individually for a configurable amount of time and with multiple repetitions to ensure statistically grounded results.
To balance statistical grounding and time-efficient execution, Theodolite provides heuristics to shrink the search space.
 
For best applicability, Theodolite does not make any restrictions on the type of SUT, load, resources or SLOs. Everything that can be deployed in Kubernetes can also be benchmarked with Theodolite.

<!--* Cloud-native benchmarking by using CRDs
* Operator based (easy to use)
* Arbitrary load and resource types
* statistical grounding + time-efficient execution-->

## Ready-to-use Benchmarks for Event-Driven Microservices

Theodolite comes with 4 benchmarks for big data stream processing frameworks. 
 inspired by typical use for big data stream processing within microservices.
Implementations: Kstreams, flink + 
Load Generator, Metrics (SLO checkers), implementations


<!--
## Theodolite Benchmarking Tool

Theodolite aims to benchmark scalability of stream processing engines for real use cases. Microservices that apply stream processing techniques are usually deployed in elastic cloud environments. Hence, Theodolite's cloud-native benchmarking framework deploys its components in a cloud environment, orchestrated by Kubernetes. It is recommended to install Theodolite with the package manager Helm. The Theodolite Helm chart along with instructions how to install it can be found in the [`helm`](helm) directory.

## Theodolite Analysis Tools

Theodolite's benchmarking method maps load intensities to the resource amounts that are required for processing them. A plot showing how resource demand evolves with an increasing load allows to draw conclusions about the scalability of a stream processing engine or its deployment. Theodolite provides Jupyter notebooks for creating such plots based on benchmarking results from the execution framework. More information can be found in [Theodolite analysis tool](analysis).

## Theodolite Benchmarks

Theodolite comes with 4 application benchmarks, which are based on typical use cases for stream processing within microservices. For each benchmark, a corresponding load generator is provided. Currently, this repository provides benchmark implementations for Apache Kafka Streams and Apache Flink. The benchmark sources can be found in [Thedolite benchmarks](theodolite-benchmarks).
-->

## Free and Open Source Research Software

Theodolite is entwickelt as free and open source research software at Kiel University's [Software Engineering Group](https://www.se.informatik.uni-kiel.de).

Feel free to use, extend or modify for your research or ... engineering.

You find a list of publications associated with Theodolite.

If you use Theodolite in your research, please cite as:

> Sören Henning and Wilhelm Hasselbring. (2021). Theodolite: Scalability Benchmarking of Distributed Stream Processing Engines in Microservice Architectures. Big Data Research, Volume 25. DOI: [10.1016/j.bdr.2021.100209](https://doi.org/10.1016/j.bdr.2021.100209). arXiv:[2009.00304](https://arxiv.org/abs/2009.00304).
