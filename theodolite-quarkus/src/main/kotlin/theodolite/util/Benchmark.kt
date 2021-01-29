package theodolite.util

// todo: needs cluster and resource config
abstract class Benchmark(val config: Map<String, Any>) {
    abstract fun start(load: LoadDimension, resources: Resource);

    abstract fun stop();

    abstract fun startWorkloadGenerator(wg: String, load: LoadDimension, ucId: String);

}