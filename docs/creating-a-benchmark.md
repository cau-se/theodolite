* Create a benchmark for operator mode
* Create a benchmark for standalone mode
* Deploy a benchmark

## Creating a benchmark for operator mode


## Creating a benchmark for standalone mode

Creating a benchmark in standalone mode is similar to operator mode. However,
instead of defining a benchmark as `Benchmark` resource, it is defined as a
benchmark YAML file. Such a file deviates in the following points from a
`Benchmark` resource:

* The fields `apiVersion`, `kind` and `metadata` should be removed.
* The benchmark's name (`metadata.name` in `Benchmark` resources) must be defined by the top-level field `name`.
* Everything that is defined in `spec` has to be moved to the top-level.

**TODO: example**

**TODO: Create a ConfigMap containing the benchmark YAML files as well as all Kubernetes resources for that benchmark + deploy**