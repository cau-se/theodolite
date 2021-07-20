---
title: Benchmarks and Executions
has_children: false
nav_order: 2
---

# Benchmarks and Executions

In Theodolite, we distinguish between the static description of a scalability benchmark and its execution.

## Benchmarks

Benchmarks define what should be executed in scalability experiments. They
consists of system under test (SUT) and an associated load generator, where
both SUT and load generator are represented as sets of Kubernetes resources
such Pods, Services, or ConfigMaps.
Additionally, benchmarks define one or more load and resource types, scalability
can be evaluated for.

[TODO]: # (Link to metrics)

Benchmarks are portable, that is they can be provided by standardization
organizations, researchers, etc. and installed by other researchers,
engineers, etc.
They do not have a life-cycle. Instead, to run a benchmark, an execution of a
benchmark is created and passed to Theodolite.

## Execution

An execution represents a one-time execution of a benchmark with a
specific configuration. Hence, an execution refers to a benchmark and one of
the load and resource types defined by that benchmark. Additionally, executions
allow to alter the SUT or the load generator to evaluate regarding specific
configurations.

Executions also describe details regarding the scalability measurement method,
such as for long experiment should be performed or how often they should be
repeated.

In contrast to benchmarks, execution have a life-cycle. They can be planned,
executed, or aborted. Each execution of a benchmark is represented by an
individual entity. This supports repeatability as executions can be archived
and shared.
