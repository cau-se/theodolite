---
title: Benchmark UC2
parent: Streaming Benchmarks
has_children: false
nav_order: 1
---

# Benchmark UC2: Downsampling

Another common use case for stream processing architectures is reducing the amount of events, messages, or measurements by aggregating multiple records within consecutive, non-overlapping time windows. Typical aggregations compute the average, minimum, or maximum of measurements within a time window or
count the occurrence of same events. Such reduced amounts of data are required, for example, to save computing resources or to provide a better user experience (e.g., for data visualizations).
When using aggregation windows of ﬁxed size that succeed each other without gaps (called [tumbling windows](https://kafka.apache.org/30/documentation/streams/developer-guide/dsl-api.html#tumbling-time-windows) in many stream processing engines), the (potentially varying) message frequency is reduced to a constant value.
This is also referred to as downsampling. Downsampling allows for applying many machine learning methods that require data of a ﬁxed frequency.

## Dataflow Architecture

![Theodolite Benchmark UC2: Downsampling](../../assets/images/arch-uc2.svg){: .d-block .mx-auto }

The dataflow architecture first reads measurement data from an input stream and then assigns each measurement to a time window of fixed, but statically configurable size. Afterwards, an aggregation operator computes the summary statistics sum, count, minimum, maximum, average and population variance for a time window. Finally, the aggregation result containing all summary statistics is written to an output stream.

## Further Reading

S. Henning and W. Hasselbring. “[Theodolite: Scalability Benchmarking of Distributed Stream Processing Engines in Microservice Architectures](https://arxiv.org/abs/2009.00304)”. In: *Big Data Research* 25. 2021. DOI: [10.1016/j.bdr.2021.100209](https://doi.org/10.1016/j.bdr.2021.100209).
