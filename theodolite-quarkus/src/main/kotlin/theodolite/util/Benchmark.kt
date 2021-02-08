package theodolite.util

interface Benchmark {
    fun start(load: LoadDimension, resources: Resource) {
    }

    fun initializeClusterEnvironment();
    fun clearClusterEnvironment();

    fun startSUT(resources: Resource);
    fun startWorkloadGenerator(load: LoadDimension)
}