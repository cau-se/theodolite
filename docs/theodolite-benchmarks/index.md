---
title: Available Benchmarks
has_children: false
nav_order: 7
---

# Theodolite Benchmarks

Theodolite comes with 4 application benchmarks, which are based on typical use cases for stream processing within microservices. For each benchmark, a corresponding load generator is provided. Currently, Theodolite provides benchmark implementations for Apache Kafka Streams and Apache Flink.


Theodolite's benchmarks are based on typical use cases for stream processing within microservices. Specifically, all benchmarks represent some sort of microservice doing Industrial Internet of Things data analytics. 

## UC1: Database Storage

A simple, but common use case in event-driven architectures is that events or messages should be stored permanently, for example, in a NoSQL database.


## UC2: Downsampling

Another common use case for stream processing architectures is reducing the amount of events, messages, or measurements by aggregating multiple records within consecutive, non-overlapping time windows. Typical aggregations compute the average, minimum, or maximum of measurements within a time window or
count the occurrence of same events. Such reduced amounts of data are required, for example, to save computing resources or to provide a better user experience (e.g., for data visualizations).
When using aggregation windows of ﬁxed size that succeed each other without gaps (called [tumbling windows](https://kafka.apache.org/20/documentation/streams/developer-guide/dsl-api.html#tumbling-time-windows) in many stream processing engines), the (potentially varying) message frequency is reduced to a constant value.
This is also referred to as downsampling. Downsampling allows for applying many machine learning methods that require data of a ﬁxed frequency.


## UC3: Time Attribute-Based Aggregation

A second type of temporal aggregation is aggregating messages that have the same time attribute. Such a time attribute is, for example, the hour of day, day of week, or day in the year. This type of aggregation can be used to compute, for example, an average course over the day, the week, or the year. It allows to demonstrate or discover seasonal patterns in the data.

## UC4: Hierarchical Aggregation

For analyzing sensor data, often not only the individual measurements of sensors are of interest, but also aggregated data for
groups of sensors. When monitoring energy consumption in industrial facilities, for example, comparing the total consumption
of machine types often provides better insights than comparing the consumption of all individual machines. Additionally, it may
be necessary to combine groups further into larger groups and adjust these group hierarchies at runtime.
