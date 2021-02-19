package theodolite.deprecated

import theodolite.util.LoadDimension
import theodolite.util.Resource

interface Benchmark {
    fun start(load: LoadDimension, resources: Resource) {
    }

    fun initializeClusterEnvironment();
    fun clearClusterEnvironment();

    fun startSUT(resources: Resource);
    fun startWorkloadGenerator(load: LoadDimension)
}
