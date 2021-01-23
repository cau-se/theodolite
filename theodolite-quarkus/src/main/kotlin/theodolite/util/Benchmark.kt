package theodolite.util

// todo: needs cluster and resource config
abstract class Benchmark(val config: Map<String, Any>) {
    abstract fun start();

    abstract fun stop();
}