package rocks.theodolite.core

import java.util.concurrent.atomic.AtomicBoolean


/**
 * Abstract class acts as an interface for the theodolite core to run experiments.
 * The results of the experiments are stored in [results].
 *
 * @property results
 */
abstract class ExperimentRunner(val results: Results) {

    var run: AtomicBoolean = AtomicBoolean(true)

    /**
     * Run an experiment for the given parametrization, evaluate the
     * experiment and save the result.
     *
     * @param load to be tested.
     * @param resource to be tested.
     * @return True, if the number of resources are suitable for the
     *     given load, false otherwise (demand metric), or
     *     True, if there is a load suitable for the
     *     given resource, false otherwise.
     */
    abstract fun runExperiment(load: Int, resource: Int): Boolean

}
