---
title: Search Strategies
has_children: false
parent: Fundamental Concepts
nav_order: 2
---

# Theodolite's Search Strategies

Theodolite measures [its metrics](metrics) by performing isolated experiments for different load intensities and provisioned resource amounts.
However, to determine a system's resource demand or load capacity or a good approximation of those, it is often not necessary to evaluate each possible combination. Instead, Theodolite provides search strategies, which decide at benchmark runtime which combinations of load and resources to evaluate.

The following search strategies are available:

### Full Search

The full search strategy performs SLO experiments for each combination of resource configuration and load intensity. Its advantage is that it allows for extensive evaluation after the benchmark has been executed. This also includes that based on the same SLO experiments, both the demand and the capacity metric can be evaluated. However, this comes at the cost of significantly longer execution times.

### Linear Search

The linear search strategy reduces the overall execution time by not running SLO experiments whose results are not required by the metric.
For the resource demand metric this means, as soon as a sufficient resource configuration for a certain load intensity is found, no further resource configurations are tested for that load.

### Binary Search

The binary search strategy adopts the well known binary search algorithm for sorted arrays.
For the resource demand metric this means, the strategy starts by performing the SLO experiments for the middle resource configuration.
Depending on whether this experiment was successful or not, it then continues searching in the lower or upper half, respectively.
The binary search is particularly advantageous if the search space is very large.
However it is based on the assumption that with additional resources for the same load, performance does not substantially decrease.

### Lower Bound Restriction Search

The lower bound restriction is a search strategy that uses the results of already performed SLO experiments to narrow the search space.
For the resource demand, it starts searching (with another strategy) beginning from the minimal required resources of all lower load intensities.
The lower bound restriction is based on the assumption that with increasing load intensity, the resource demand never decreases.

## Further Reading

S. Henning and W. Hasselbring. “[A Configurable Method for Benchmarking Scalability of Cloud-Native Applications](https://doi.org/10.1007/s10664-022-10162-1)”. In: *Empirical Software Engineering* 27. 2022. DOI: [10.1007/s10664-022-10162-1](https://doi.org/10.1007/s10664-022-10162-1).
