package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.informers.SharedInformerFactory
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import mu.KotlinLogging
import rocks.theodolite.kubernetes.Configuration
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkExecutionList
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.KubernetesBenchmarkList


private const val EXECUTION_SINGULAR = "execution"
private const val BENCHMARK_SINGULAR = "benchmark"
private const val API_VERSION = "v1beta1"
private const val RESYNC_PERIOD = 10 * 60 * 1000.toLong()
private const val GROUP = "theodolite.rocks"
private val logger = KotlinLogging.logger {}

/**
 * Implementation of the Operator pattern for K8s.
 *
 * **See Also:** [Kubernetes Operator Pattern](https://kubernetes.io/docs/concepts/extend-kubernetes/operator/)
 */
class TheodoliteOperator(private val client: NamespacedKubernetesClient) {
    private lateinit var controller: TheodoliteController
    private lateinit var executionStateHandler: ExecutionStateHandler
    private lateinit var benchmarkStateHandler: BenchmarkStateHandler
    private lateinit var benchmarkStateChecker: BenchmarkStateChecker


    fun start() {
        LeaderElector(
            client = this.client,
            name = Configuration.COMPONENT_NAME
        ).getLeadership(::startOperator)
    }

    /**
     * Start the operator.
     */
    private fun startOperator() {
        logger.info { "Becoming the leading operator. Use namespace '${this.client.namespace}'." }
        client.use {

            ClusterSetup(
                executionCRDClient = getExecutionClient(),
                benchmarkCRDClient = getBenchmarkClient(),
                client = this.client
            ).clearClusterState()

            controller = getController(
                executionStateHandler = getExecutionStateHandler(),
                benchmarkStateChecker = getBenchmarkStateChecker()

            )

            getExecutionClient().inform().addEventHandlerWithResyncPeriod(
                ExecutionEventHandler(
                    controller = controller,
                    stateHandler = ExecutionStateHandler(this.client)
                ),
                RESYNC_PERIOD
            )

            this.client.informers().startAllRegisteredInformers()
            controller.run()
        }
    }

    fun getExecutionStateHandler(): ExecutionStateHandler {
        if (!::executionStateHandler.isInitialized) {
            this.executionStateHandler = ExecutionStateHandler(client = this.client)
        }
        return executionStateHandler
    }

    fun getBenchmarkStateHandler() : BenchmarkStateHandler {
        if (!::benchmarkStateHandler.isInitialized) {
            this.benchmarkStateHandler = BenchmarkStateHandler(client = this.client)
        }
        return benchmarkStateHandler
    }

    fun getBenchmarkStateChecker() : BenchmarkStateChecker {
        if (!::benchmarkStateChecker.isInitialized) {
            this.benchmarkStateChecker = BenchmarkStateChecker(
                client = this.client,
                benchmarkStateHandler = getBenchmarkStateHandler(),
                benchmarkCRDClient = getBenchmarkClient())
        }
        return benchmarkStateChecker
    }


    fun getController(
            executionStateHandler: ExecutionStateHandler,
            benchmarkStateChecker: BenchmarkStateChecker
    ): TheodoliteController {
        if (!::controller.isInitialized) {
            this.controller = TheodoliteController(
                client = this.client,
                benchmarkCRDClient = getBenchmarkClient(),
                executionCRDClient = getExecutionClient(),
                executionStateHandler = executionStateHandler,
                benchmarkStateChecker = benchmarkStateChecker
            )
        }
        return this.controller
    }

    fun getExecutionClient(): MixedOperation<
            ExecutionCRD,
            BenchmarkExecutionList,
            Resource<ExecutionCRD>> {
        return client.resources(
            ExecutionCRD::class.java,
            BenchmarkExecutionList::class.java
        )
    }

    fun getBenchmarkClient(): MixedOperation<
            BenchmarkCRD,
            KubernetesBenchmarkList,
            Resource<BenchmarkCRD>> {
        return client.resources(
            BenchmarkCRD::class.java,
            KubernetesBenchmarkList::class.java
        )
    }
}
