---
title: Scalability Metrics
has_children: false
parent: Fundamental Concepts
nav_order: 1
usemathjax: true
---

# Theodolite's Scalability Metrics

Theodolite's scalability metrics are based on the following definition:

> Scalability is the ability of [a] system to sustain increasing workloads by making use of additional resources. -- <cite>[Herbst et al. (2013)](https://www.usenix.org/conference/icac13/technical-sessions/presentation/herbst)</cite>

Based on this definition, scalability can be characterized by the following three attributes:

* **Load intensity** is the input variable to which a system is subjected. Scalability is evaluated within a range of load intensities.
* **Service levels objectives (SLOs)** are measurable quality criteria that have to be fulfilled for every load intensity.
* **Provisioned resources** can be increased to meet the SLOs if load intensities increase.


## Scalability Metrics

Theodolite uses these attributes to define two scalability metrics:

| Resource Demand Metric | Load Capacity Metric |
|:----|:----|
| The resource demand metric quantifies scalability by describing how the amount of minimal required resources (i.e., all SLOs are fulfilled) evolves with increasing load intensities. | The load capacity metric quantifies scalability by describing how the maximal processable load (i.e., all SLOs are fulfilled) evolves with increasing resources. |
| ![Example for resource demand metric](../../assets/images/demand.svg){: .d-block .mx-auto } *Example: Scalability of two stream processing engines measured with the demand metric.*{: .d-block .text-center } | ![Example for load capacity metric](../../assets/images/capacity.svg){: .d-block .mx-auto }  *Example: Scalability of two stream processing engines measured with the capacity metric.*{: .d-block .text-center } |
{: .fixed-colums }

<!--
## Resource Demand Metric

![](../../assets/images/capacity.svg){: .d-block .mx-auto }


## Load Capacity Metric

![](../../assets/images/demand.svg){: .d-block .mx-auto }
-->


## Formal Definition

For a more formal definition of both metrics, we define the load type as the set of possible load intensities for that
type, denoted as $$L$$.
Similarly, we define the resource type as the set of possible resources, denoted as $$R$$.
We also require that there exists an ordering on both sets $$L$$ and $$R$$.
We define the set of all SLOs as $$S$$ and denote an SLO $$s \in S$$ as Boolean-valued function
$$\text{slo}_s: L \times R \to \{\text{false},\text{true}\}$$ with $$\text{slo}_s(l,r) = \text{true}$$ if a system deployed with $$r$$ resource amounts does not violate SLO $$s$$ when processing load intensity $$l$$.

We can denote our **resource demand** metric as $$\text{demand: } L \to R$$, defined as:

$$
\forall l \in L: \text{demand}(l) = \min\{r \in R \mid \forall s \in S: \text{slo}_s(l,r) = \text{true}\}
$$

And similarly denote our **resource capacity** metric as $$\text{capacity: } R \to L$$, defined as:

$$
\forall r \in R: \text{capacity}(r) = \max\{l \in L \mid \forall s \in S: \text{slo}_s(l,r) = \text{true}\}
$$

## Further Reading

S. Henning and W. Hasselbring. “[A Configurable Method for Benchmarking Scalability of Cloud-Native Applications](https://doi.org/10.1007/s10664-022-10162-1)”. In: *Empirical Software Engineering* 27. 2022. DOI: [10.1007/s10664-022-10162-1](https://doi.org/10.1007/s10664-022-10162-1).
