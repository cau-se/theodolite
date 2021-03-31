package theodolite.benchmark

/**
 *  A BenchmarkDeployment contains the necessary infrastructure to execute a Benchmark.
 *  Therefore it has the capabilities to set up the deployment of a benchmark and to tear it down.
 */
interface BenchmarkDeployment {

    /**
     * Setup a Benchmark. This method is responsible for deploying the resources
     * and organize the needed infrastructure.
     */
    fun setup()

    /**
     *  Tears a Benchmark down. This method is responsible for deleting the deployed
     *  resources and to reset the used infrastructure.
     */
    fun teardown()
}
