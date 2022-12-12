---
title: Fundamental Concepts
has_children: true
nav_order: 2
---

# Fundamental Concepts

> A theodolite is a precision optical instrument for measuring angles between designated visible points in the horizontal and vertical planes.  -- <cite>[Wikipedia](https://en.wikipedia.org/wiki/Theodolite)</cite>

Inspired by its namesake, Theodolite is a framework for benchmarking the horizontal and vertical scalability of [cloud-native applications](https://github.com/cncf/toc/blob/main/DEFINITION.md).
It relies on Kubernetes, the de-facto standard for orchestrating cloud-native applications, as a platform to define and execute benchmarks.


Theodolite adopts established definitions of scalability in cloud computing for its benchmarking method. It quantifies
scalability by running isolated experiments for different load intensities and provisioned resource amounts, which assess whether specified SLOs are fulfilled. [Two metrics are available](metrics): The demand metric describes how the amount of minimal required resources evolve with increasing load intensities, while the capacity metric describes how the maximal processable load evolves with increasing resources. Hence, both metrics are functions. <!--Example?-->

The terms load, resources and SLOs are consciously kept abstract as Theodolite leaves it to the benchmark designer to define what type of load, resources, and SLOs should be evaluated. For example, horizontal scalability can be benchmarked by varying the amount of Kubernetes Pods, while vertical scalability can be benchmarked by varying CPU and memory constraints of Pods.

To balance statistical grounding and time-efficient benchmark execution, Theodolite comes with different [heuristic for
evaluating the search space](search-strategies) of load and resource combinations. Other configuration options include the number of repetitions, the experiment and warm-up duration, as well as the amount of different load and resource values to be evaluated.
