package theodolite.execution.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import theodolite.k8s.K8sContextFactory
import theodolite.model.crd.*

private const val SCOPE = "Namespaced"
private const val EXECUTION_SINGULAR = "execution"
private const val EXECUTION_PLURAL = "executions"
private const val BENCHMARK_SINGULAR = "benchmark"
private const val BENCHMARK_PLURAL = "benchmarks"
private const val API_VERSION = "v1"
private const val GROUP = "theodolite.com"

class ControllerDummy(val client: NamespacedKubernetesClient) {

    private var controller: TheodoliteController
    val executionContext = K8sContextFactory()
        .create(
            API_VERSION,
            SCOPE,
            GROUP,
            EXECUTION_PLURAL
        )
    val benchmarkContext = K8sContextFactory()
        .create(
            API_VERSION,
            SCOPE,
            GROUP,
            BENCHMARK_PLURAL
        )

    val executionStateHandler = ExecutionStateHandler(
        context = executionContext,
        client = client
    )

    fun getController(): TheodoliteController {
        return this.controller
    }

    init {
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

        val executionCRDClient: MixedOperation<
                ExecutionCRD,
                BenchmarkExecutionList,
                DoneableExecution,
                Resource<ExecutionCRD, DoneableExecution>> = client.customResources(
            executionContext,
            ExecutionCRD::class.java,
            BenchmarkExecutionList::class.java,
            DoneableExecution::class.java
        )

        val benchmarkCRDClient = client.customResources(
            benchmarkContext,
            BenchmarkCRD::class.java,
            KubernetesBenchmarkList::class.java,
            DoneableBenchmark::class.java
        )

        val appResource = System.getenv("THEODOLITE_APP_RESOURCES") ?: "./config"
        this.controller =
            TheodoliteController(
                namespace = client.namespace,
                path = appResource,
                benchmarkCRDClient = benchmarkCRDClient,
                executionCRDClient = executionCRDClient,
                executionStateHandler = executionStateHandler
            )
    }
}