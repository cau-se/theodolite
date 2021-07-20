---
title: Theodolite
nav_order: 1
permalink: /
---

# Theodolite

> A theodolite is a precision optical instrument for measuring angles between designated visible points in the horizontal and vertical planes.  -- <cite>[Wikipedia](https://en.wikipedia.org/wiki/Theodolite)</cite>

Theodolite is a framework for benchmarking the horizontal and vertical scalability of stream processing engines. It consists of three modules:

## Theodolite Benchmarks

Theodolite contains 4 application benchmarks, which are based on typical use cases for stream processing within microservices. For each benchmark, a corresponding workload generator is provided. Currently, this repository provides benchmark implementations for Kafka Streams.


## Theodolite Execution Framework

Theodolite aims to benchmark scalability of stream processing engines for real use cases. Microservices that apply stream processing techniques are usually deployed in elastic cloud environments. Hence, Theodolite's cloud-native benchmarking framework deploys as components in a cloud environment, orchestrated by Kubernetes. More information on how to execute scalability benchmarks can be found in [Thedolite execution framework](execution).


## Theodolite Analysis Tools

Theodolite's benchmarking method create a *scalability graph* allowing to draw conclusions about the scalability of a stream processing engine or its deployment. A scalability graph shows how resource demand evolves with an increasing workload. Theodolite provides Jupyter notebooks for creating such scalability graphs based on benchmarking results from the execution framework. More information can be found in [Theodolite analysis tool](analysis).
