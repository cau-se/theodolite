package theodolite.util

import theodolite.k8s.UC1Benchmark

abstract class Benchmark(val config: UC1Benchmark.UC1BenchmarkConfig) {
    fun start(load: LoadDimension, resources: Resource) {
        this.clearClusterEnvironment()
        this.initializeClusterEnvironment()
        this.startSUT(resources)
        this.startWorkloadGenerator(load)
    }

    abstract fun initializeClusterEnvironment();
    abstract fun clearClusterEnvironment();

    abstract fun startSUT(resources: Resource);

    abstract fun startWorkloadGenerator(load: LoadDimension)
}
