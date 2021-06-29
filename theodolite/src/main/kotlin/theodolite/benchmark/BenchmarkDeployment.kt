package theodolite.benchmark

/**
 *  A BenchmarkDeployment contains the necessary infrastructure to execute a benchmark.
 *  Therefore it has the capabilities to set up the deployment of a benchmark and to tear it down.
 */
interface BenchmarkDeployment {

    /**
     * Setup a benchmark. This method is responsible for deploying the resources
     * and organize the needed infrastructure.
     */
    fun setup()

    /**
     *  Tears down a benchmark. This method is responsible for deleting the deployed
     *  resources and to reset the used infrastructure.
     */
    fun teardown()
}
