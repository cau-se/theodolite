package theodolite.execution.operator

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import mu.KotlinLogging
import theodolite.k8s.K8sContextFactory
import theodolite.model.crd.*


private const val DEFAULT_NAMESPACE = "default"
private const val SCOPE = "Namespaced"
private const val EXECUTION_SINGULAR = "execution"
private const val EXECUTION_PLURAL = "executions"
private const val BENCHMARK_SINGULAR = "benchmark"
private const val BENCHMARK_PLURAL = "benchmarks"
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
    private val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
    val client: NamespacedKubernetesClient = DefaultKubernetesClient().inNamespace(namespace)


    fun start() {
        LeaderElector(
            client = client,
            name = "theodolite-operator"
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

            val contextFactory = K8sContextFactory()
            val executionContext = contextFactory.create(API_VERSION, SCOPE, GROUP, EXECUTION_PLURAL)
            val benchmarkContext = contextFactory.create(API_VERSION, SCOPE, GROUP, BENCHMARK_PLURAL)

            val executionCRDClient: MixedOperation<
                    ExecutionCRD,
                    BenchmarkExecutionList,
                    DoneableExecution,
                    Resource<ExecutionCRD, DoneableExecution>>
                = client.customResources(
                    executionContext,
                    ExecutionCRD::class.java,
                    BenchmarkExecutionList::class.java,
                    DoneableExecution::class.java)

            val benchmarkCRDClient: MixedOperation<
                    BenchmarkCRD,
                    KubernetesBenchmarkList,
                    DoneableBenchmark,
                    Resource<BenchmarkCRD, DoneableBenchmark>>
                = client.customResources(
                    benchmarkContext,
                    BenchmarkCRD::class.java,
                    KubernetesBenchmarkList::class.java,
                    DoneableBenchmark::class.java)

            val executionStateHandler = ExecutionStateHandler(
                context = executionContext,
                client = client)

            val appResource = System.getenv("THEODOLITE_APP_RESOURCES") ?: "./config"
            val controller =
                TheodoliteController(
                    namespace = client.namespace,
                    path = appResource,
                    benchmarkCRDClient = benchmarkCRDClient,
                    executionCRDClient = executionCRDClient,
                    executionStateHandler = executionStateHandler)

            val informerFactory = client.informers()
            val informerExecution = informerFactory.sharedIndexInformerForCustomResource(
                executionContext,
                ExecutionCRD::class.java,
                BenchmarkExecutionList::class.java,
                RESYNC_PERIOD
            )

            informerExecution.addEventHandler(ExecutionHandler(
                controller = controller,
                stateHandler = executionStateHandler))

            ClusterSetup(
                executionCRDClient = executionCRDClient,
                benchmarkCRDClient = benchmarkCRDClient,
                client = client
            ).clearClusterState()

            informerFactory.startAllRegisteredInformers()
            controller.run()

        }
    }
}
