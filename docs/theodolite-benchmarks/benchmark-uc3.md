---
title: Benchmark UC3
parent: Streaming Benchmarks
has_children: false
nav_order: 1
---

# Benchmark UC3: Time Attribute-Based Aggregation

A second type of temporal aggregation is aggregating messages that have the same time attribute. Such a time attribute is, for example, the hour of day, day of week, or day in the year. This type of aggregation can be used to compute, for example, an average course over the day, the week, or the year. It allows to demonstrate or discover seasonal patterns in the data.

## Dataflow Architecture

![Theodolite Benchmark UC3: Time Attribute-Based Aggregation](../../assets/images/arch-uc3.svg){: .d-block .mx-auto }

The first step is to read measurement data from the input stream. Then, a new key is set for each message, which consists of the original key (i.e., the identifier of a sensor) and the selected time attribute (e.g., day of week) extracted from the record’s timestamp. In the next step, the message is duplicated for each sliding window it is contained in. Then, all measurements of the same sensor and the same time attribute are aggregated for each sliding time window by computing the summary statistics sum, count, minimum, maximum, average and population variance. The aggregation results per identifier, time attribute, and window are written to an output stream.

## Further Reading

S. Henning and W. Hasselbring. “[Theodolite: Scalability Benchmarking of Distributed Stream Processing Engines in Microservice Architectures](https://arxiv.org/abs/2009.00304)”. In: *Big Data Research* 25. 2021. DOI: [10.1016/j.bdr.2021.100209](https://doi.org/10.1016/j.bdr.2021.100209).
