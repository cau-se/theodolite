---
title: Benchmark UC4
parent: Streaming Benchmarks
has_children: false
nav_order: 1
---

# Benchmark UC4: Hierarchical Aggregation

For analyzing sensor data, often not only the individual measurements of sensors are of interest, but also aggregated data for
groups of sensors. When monitoring energy consumption in industrial facilities, for example, comparing the total consumption
of machine types often provides better insights than comparing the consumption of all individual machines. Additionally, it may
be necessary to combine groups further into larger groups and adjust these group hierarchies at runtime.

## Dataflow Architecture

![Theodolite Benchmark UC4: Hierarchical Aggregation](../../assets/images/arch-uc4.svg){: .d-block .mx-auto }

The dataflow architecture requires two input data streams: a stream of sensor measurements and a stream tracking changes to the hierarchies of sensor groups. In the consecutive steps, both streams are joined, measurements are duplicated for each relevant group, assigned to time windows, and the measurements for all sensors in a group per window are aggregated. Finally, the aggregation results are exposed via a new data stream. Additionally, the output stream is fed back as an input stream in order to compute aggregations for groups containing subgroups. To also support unknown record frequencies, this dataflow architecture can be configured to use sliding windows instead of tumbling windows (see [further reading](#further-reading)).

## Further Reading

* S. Henning and W. Hasselbring. “[Theodolite: Scalability Benchmarking of Distributed Stream Processing Engines in Microservice Architectures](https://arxiv.org/abs/2009.00304)”. In: *Big Data Research* 25. 2021. DOI: [10.1016/j.bdr.2021.100209](https://doi.org/10.1016/j.bdr.2021.100209).
* S. Henning and W. Hasselbring. “[Scalable and reliable multi-dimensional sensor data aggregation in data-streaming architectures](https://doi.org/10.1007/s41688-020-00041-3)”. In: *Data-Enabled Discovery and Applications* 4.1. 2020. DOI: [10.1007/s41688-020-00041-3](https://doi.org/10.1007/s41688-020-00041-3).