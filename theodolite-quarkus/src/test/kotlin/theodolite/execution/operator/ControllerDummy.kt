package theodolite.execution.operator

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import theodolite.model.crd.BenchmarkCRD
import theodolite.model.crd.BenchmarkExecutionList
import theodolite.model.crd.ExecutionCRD
import theodolite.model.crd.KubernetesBenchmarkList

private const val SCOPE = "Namespaced"
private const val EXECUTION_SINGULAR = "execution"
private const val EXECUTION_PLURAL = "executions"
private const val BENCHMARK_SINGULAR = "benchmark"
private const val BENCHMARK_PLURAL = "benchmarks"
private const val API_VERSION = "v1"
private const val GROUP = "theodolite.com"

class ControllerDummy(client: NamespacedKubernetesClient) {

    private var controller: TheodoliteController
    val executionStateHandler = ExecutionStateHandler(
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
                Resource<ExecutionCRD>> = client.customResources(
            ExecutionCRD::class.java,
            BenchmarkExecutionList::class.java
        )

        val benchmarkCRDClient = client.customResources(
            BenchmarkCRD::class.java,
            KubernetesBenchmarkList::class.java
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