---
title: Scalability Metrics
has_children: false
parent: Fundamental Concepts
nav_order: 1
usemathjax: true
---

# Theodolite's Scalability Metrics

## Resource Demand Metric

$$
\forall l \in L: \text{demand}(l) = \min\{r \in R \mid \forall s \in S: \text{slo}_s(l,r) = \text{true}\}
$$

## Load Capacity Metric

$$
\forall r \in R: \text{capacity}(r) = \max\{l \in L \mid \forall s \in S: \text{slo}_s(l,r) = \text{true}\}
$$



## Fun with Math

$$
\begin{aligned}
  & \phi(x,y) = \phi \left(\sum_{i=1}^n x_ie_i, \sum_{j=1}^n y_je_j \right)
  = \sum_{i=1}^n \sum_{j=1}^n x_i y_j \phi(e_i, e_j) = \\
  & (x_1, \ldots, x_n) \left( \begin{array}{ccc}
      \phi(e_1, e_1) & \cdots & \phi(e_1, e_n) \\
      \vdots & \ddots & \vdots \\
      \phi(e_n, e_1) & \cdots & \phi(e_n, e_n)
    \end{array} \right)
  \left( \begin{array}{c}
      y_1 \\
      \vdots \\
      y_n
    \end{array} \right)
\end{aligned}
$$
