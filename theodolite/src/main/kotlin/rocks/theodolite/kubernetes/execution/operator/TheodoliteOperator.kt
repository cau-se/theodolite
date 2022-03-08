package rocks.theodolite.kubernetes.execution.operator

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.informers.SharedInformerFactory
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkExecutionList
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.KubernetesBenchmarkList
import rocks.theodolite.kubernetes.util.Configuration


private const val DEFAULT_NAMESPACE = "default"
private const val EXECUTION_SINGULAR = "execution"
private const val BENCHMARK_SINGULAR = "benchmark"
private const val API_VERSION = "v1"
private const val RESYNC_PERIOD = 10 * 60 * 1000.toLong()
private const val GROUP = "theodolite.com"
private val logger = KotlinLogging.logger {}

/**
 * Implementation of the Operator pattern for K8s.
 *
 * **See Also:** [Kubernetes Operator Pattern](https://kubernetes.io/docs/concepts/extend-kubernetes/operator/)
 */
class TheodoliteOperator {
    private val namespace = Configuration.NAMESPACE

    private val client: NamespacedKubernetesClient = DefaultKubernetesClient().inNamespace(namespace)
    private lateinit var controller: TheodoliteController
    private lateinit var executionStateHandler: ExecutionStateHandler
    private lateinit var benchmarkStateHandler: BenchmarkStateHandler
    private lateinit var benchmarkStateChecker: BenchmarkStateChecker


    fun start() {
        LeaderElector(
            client = client,
            name = Configuration.COMPONENT_NAME
        )
            .getLeadership(::startOperator)
    }

    /**
     * Start the operator.
     */
    private fun startOperator() {
        logger.info { "Using $namespace as namespace." }
        client.use {
            KubernetesDeserializer.registerCustomKind(
                "$GROUP/$API_VERSION",
                EXECUTION_SINGULAR,
                ExecutionCRD::class.java
            )

            KubernetesDeserializer.registerCustomKind(
                "$GROUP/$API_VERSION",
                BENCHMARK_SINGULAR,
                BenchmarkCRD::class.java
            )

            ClusterSetup(
                executionCRDClient = getExecutionClient(client),
                benchmarkCRDClient = getBenchmarkClient(client),
                client = client
            ).clearClusterState()

            controller = getController(
                client = client,
                executionStateHandler = getExecutionStateHandler(client = client),
                benchmarkStateChecker = getBenchmarkStateChecker(client = client)

            )
            getExecutionEventHandler(controller, client).startAllRegisteredInformers()
            controller.run()
        }
    }

    fun getExecutionEventHandler(
            controller: TheodoliteController,
            client: NamespacedKubernetesClient
    ): SharedInformerFactory {
        val factory = client.informers()
            .inNamespace(client.namespace)

        factory.sharedIndexInformerForCustomResource(
            ExecutionCRD::class.java,
            RESYNC_PERIOD
        ).addEventHandler(
            ExecutionEventHandler(
                controller = controller,
                stateHandler = ExecutionStateHandler(client)
            )
        )
        return factory
    }

    fun getExecutionStateHandler(client: NamespacedKubernetesClient): ExecutionStateHandler {
        if (!::executionStateHandler.isInitialized) {
            this.executionStateHandler = ExecutionStateHandler(client = client)
        }
        return executionStateHandler
    }

    fun getBenchmarkStateHandler(client: NamespacedKubernetesClient) : BenchmarkStateHandler {
        if (!::benchmarkStateHandler.isInitialized) {
            this.benchmarkStateHandler = BenchmarkStateHandler(client = client)
        }
        return benchmarkStateHandler
    }

    fun getBenchmarkStateChecker(client: NamespacedKubernetesClient) : BenchmarkStateChecker {
        if (!::benchmarkStateChecker.isInitialized) {
            this.benchmarkStateChecker = BenchmarkStateChecker(
                client = client,
                benchmarkStateHandler = getBenchmarkStateHandler(client = client),
                benchmarkCRDClient = getBenchmarkClient(client = client))
        }
        return benchmarkStateChecker
    }


    fun getController(
            client: NamespacedKubernetesClient,
            executionStateHandler: ExecutionStateHandler,
            benchmarkStateChecker: BenchmarkStateChecker
    ): TheodoliteController {
        if (!::controller.isInitialized) {
            this.controller = TheodoliteController(
                benchmarkCRDClient = getBenchmarkClient(client),
                executionCRDClient = getExecutionClient(client),
                executionStateHandler = executionStateHandler,
                benchmarkStateChecker = benchmarkStateChecker
            )
        }
        return this.controller
    }

    fun getExecutionClient(client: NamespacedKubernetesClient): MixedOperation<
            ExecutionCRD,
            BenchmarkExecutionList,
            Resource<ExecutionCRD>> {
        return client.customResources(
            ExecutionCRD::class.java,
            BenchmarkExecutionList::class.java
        )
    }

    fun getBenchmarkClient(client: NamespacedKubernetesClient): MixedOperation<
            BenchmarkCRD,
            KubernetesBenchmarkList,
            Resource<BenchmarkCRD>> {
        return client.customResources(
            BenchmarkCRD::class.java,
            KubernetesBenchmarkList::class.java
        )
    }
}
