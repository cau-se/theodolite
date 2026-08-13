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
   helm repo add theodolite https://www.theodolite.rocks
   helm repo update
   helm install theodolite theodolite/theodolite -f https://raw.githubusercontent.com/cau-se/theodolite/main/helm/preconfigs/minimal.yaml
   ```

   After installation, it may take some time until all components are ready. You can check the status of the installation by running:

   ```sh
   kubectl get pods
   ```

1. Get the Theodolite examples from the [Theodolite repository](https://github.com/cau-se/theodolite) and `cd` into the HTTP server example directory:

   ```sh
   git clone https://github.com/cau-se/theodolite.git
   cd theodolite/theodolite/examples/http-server/
   ```

1. Deploy the example resources and Benchmark:

   ```sh
   kubectl apply -f example-configmap.yaml
   kubectl apply -f example-benchmark.yaml
   ```

1. Verify that the Benchmark has been deployed successfully:

   ```sh
   kubectl get benchmarks
   ```

   The output is similar to this:

   ```
   NAME                AGE   STATUS
   example-benchmark   81s   Ready
   ```

1. Run the Benchmark by deploying an Execution:

   ```sh
   kubectl apply -f example-execution.yaml
   ```

1. Verify that the Execution is running:

   ```sh
   kubectl get executions
   ```

   The output is similar to this:

   ```
   NAME                STATUS    DURATION   AGE
   example-execution   Running   13s        14s
   ```

   Theodolite provides additional information on the current status of an Execution by producing Kubernetes events. To see them:

   ```sh
   kubectl describe execution example-execution
   ```

## Next Steps

* [Full walkthrough of the HTTP server example](example-http-server)
* [Deploy and run custom benchmarks](running-benchmarks)
* [Define your own benchmarks](creating-a-benchmark)
* [Customize the Theodolite installation](installation)
