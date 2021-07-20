
# Defining a Benchmark

Please note that to simply run a benchmark, it is not required to define one. Theodolite comes with a set of benchmarks, which are ready to be executed. See the Benchmarks and Executions docs page to learn more about our distinction between benchmarks and executions.


* Create a benchmark for operator mode
* Create a benchmark for standalone mode
* Deploy a benchmark

## Creating a benchmark for operator mode

### App and Workload Generator Resources

### Load and Resource Types

### Kafka Configuration


### Make benchmark Kubernetes resources accessible

* A benchmark is defined by a set of Kubernetes resources, which are automatically deployed and scaled by Theodolite.
* To make these resources accessible by the Theodolite operator, we need to add them to a volume, already mounted by Theodolite. The easiest way to do this is by adding the resources to the ConfigMap `benchmark-resources-custom`, which is mounted by Theodolite with its default installation.





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