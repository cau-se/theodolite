package rocks.theodolite.kubernetes.standalone

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.TheodoliteExecutor
import rocks.theodolite.kubernetes.util.YamlParserFromFile
import rocks.theodolite.kubernetes.slo.EvaluationFailedException
import rocks.theodolite.kubernetes.ExecutionFailedException
import rocks.theodolite.kubernetes.Shutdown
import kotlin.concurrent.thread
import kotlin.system.exitProcess


private val logger = KotlinLogging.logger {}


/**
 * The Theodolite yaml executor loads the required configurations
 * of the executions and the benchmark from yaml files and run the
 * corresponding experiments.
 *
 * The location of the execution, benchmarks and Kubernetes resource
 * files can be configured via the following environment variables:
 * `THEODOLITE_EXECUTION`
 *
 * `THEODOLITE_BENCHMARK`
 *
 * `THEODOLITE_APP_RESOURCES`
 *
 * @constructor Create empty Theodolite yaml executor
 */
class TheodoliteStandalone (private val client: NamespacedKubernetesClient) {
    private val parser = YamlParserFromFile()

    fun start() {
        logger.info { "Theodolite started" }

        val executionPath = System.getenv("THEODOLITE_EXECUTION") ?: "execution/execution.yaml"
        val benchmarkPath = System.getenv("THEODOLITE_BENCHMARK") ?: "benchmark/benchmark.yaml"

        logger.info { "Using $executionPath for BenchmarkExecution" }
        logger.info { "Using $benchmarkPath for BenchmarkType" }


        // load the BenchmarkExecution and the BenchmarkType
        val benchmarkExecution =
            parser.parse(path = executionPath, clazz = BenchmarkExecution::class.java)!!
        val benchmark =
            parser.parse(path = benchmarkPath, clazz = KubernetesBenchmark::class.java)!!

        // Add shutdown hook
        // Use thread{} with start = false, else the thread will start right away
        val shutdown = thread(start = false) { Shutdown(benchmarkExecution, benchmark, client).run() }
        Runtime.getRuntime().addShutdownHook(shutdown)

        try {
            TheodoliteExecutor(benchmarkExecution, benchmark, client).setupAndRunExecution()
        } catch (e: EvaluationFailedException) {
            logger.error { "Evaluation failed with error: ${e.message}" }
        }catch (e: ExecutionFailedException) {
            logger.error { "Execution failed with error: ${e.message}" }
        }

        logger.info { "Theodolite finished" }
        Runtime.getRuntime().removeShutdownHook(shutdown)
        exitProcess(0)
    }
}
