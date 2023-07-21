---
title: Benchmark UC1
parent: Streaming Benchmarks
has_children: false
nav_order: 1
---

# Benchmark UC1: Database Storage

A simple, but common use case in event-driven architectures is that events or messages should be stored permanently, for example, in a NoSQL database.

## Dataflow Architecture

![Theodolite Benchmark UC1: Database Storage](../../assets/images/arch-uc1.svg){: .d-block .mx-auto }

The first step is to read data records from a messaging system. Then, these records are converted into another data format in order to match the often different formats required by the database. Finally, the converted records are written to an external database. 
Per default, this benchmark does not use a real database, but instead writes all incoming records to system out. Otherwise, due to the simple, stateless stream processing topology, the benchmark would primarily test the database’s write capabilities. However, for implementations of some stream processing engines, also a real database can be configured.

## Further Reading

S. Henning and W. Hasselbring. “[Theodolite: Scalability Benchmarking of Distributed Stream Processing Engines in Microservice Architectures](https://arxiv.org/abs/2009.00304)”. In: *Big Data Research* 25. 2021. DOI: [10.1016/j.bdr.2021.100209](https://doi.org/10.1016/j.bdr.2021.100209).
