package theodolite.util

import theodolite.k8s.UC1Benchmark

// todo: needs cluster and resource config
abstract class Benchmark(val config: UC1Benchmark.UC1BenchmarkConfig) {
    abstract fun start(load: LoadDimension, resources: Resource);

    abstract fun stop();

    abstract fun startWorkloadGenerator(wg: String, load: LoadDimension, ucId: String);

}