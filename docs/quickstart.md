---
title: Getting Started
has_children: false
nav_order: 1
---

# Getting Started

All you need to get started is access to a Kubernetes cluster plus kubectl and Helm installed on your machine.

<!--### Installation-->

1. To install Theodolite with all its dependencies run:

    ```sh
    helm repo add theodolite https://cau-se.github.io/theodolite
    helm repo update
    helm install theodolite theodolite/theodolite
    ```

1. Get the Theodolite examples from the [Theodolite repository](https://github.com/cau-se/theodolite) and `cd` into its example directory:

    ```sh
    git clone https://github.com/cau-se/theodolite.git
    cd theodolite/theodolite/examples/
    ```

1. Deploy the example Benchmark and package its associated Kubernetes resources in a ConfigMap:

    ```sh
    kubectl apply -f operator/example-benchmark.yaml
    kubectl create configmap benchmark-resources-custom --from-file=./resources -o yaml --dry-run=client | kubectl apply -f -
    ```

1. Verify that the Benchmark has been deployed successfully:

    ```sh
    kubectl get benchmarks
    ```

    The output is similar to this:

    ```
    NAME           AGE
    uc1-kstreams   100s
    ```

    <!-- State with newest version -->

1. Run the Benchmark by deploying an Execution:

    ```sh
    kubectl apply -f operator/example-execution.yaml 
    ```

1. Verify that the Executions is running:

    ```sh
    kubectl get executions
    ```

    The output is similar to this:

    ```
    NAME                           STATUS    DURATION   AGE
    theodolite-example-execution   RUNNING   13s        14s
    ```

    You can get additional information about the current status of an Execution by running:
    Theodolite provides additional information on the current status of an Execution by producing Kubernetes events. To see them:

    ```sh
    kubectl describe execution theodolite-example-execution
    ```

<!--
## Next Steps

* Deploy and run custom benchmarks
* Define your own benchmarks
* Customize the benchmark

## Further Readings

* Customize the Theodolite Installation
-->
