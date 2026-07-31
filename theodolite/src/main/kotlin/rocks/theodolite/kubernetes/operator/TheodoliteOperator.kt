package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.quarkus.arc.Arc
import mu.KotlinLogging
import rocks.theodolite.kubernetes.Configuration
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkExecutionList
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.KubernetesBenchmarkList


private const val EXECUTION_SINGULAR = "execution"
private const val BENCHMARK_SINGULAR = "benchmark"
private const val API_VERSION = "v1beta2"
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

            val coordinator = getCoordinator()
            coordinator.initialize(
                client = this.client,
                executionCRDClient = getExecutionClient(),
                benchmarkCRDClient = getBenchmarkClient(),
                executionStateHandler = getExecutionStateHandler()
            )

            controller = getController(coordinator = coordinator)

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

    fun getCoordinator(): RunnerCoordinator {
        return Arc.container().instance(RunnerCoordinator::class.java).get()
    }

    fun getController(coordinator: RunnerCoordinator): TheodoliteController {
        if (!::controller.isInitialized) {
            this.controller = TheodoliteController(coordinator = coordinator)
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
