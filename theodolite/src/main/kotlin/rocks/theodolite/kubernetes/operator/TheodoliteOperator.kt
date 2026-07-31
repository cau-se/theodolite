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
private const val GROUP = "theodolite.rocks"
private val logger = KotlinLogging.logger {}

/**
 * Implementation of the Operator pattern for K8s.
 *
 * Since the lifecycle of Executions and Benchmarks is managed by the Java Operator SDK
 * reconcilers ([ExecutionReconciler], [BenchmarkReconciler]) and [RunnerCoordinator], this class
 * only performs the one-time cluster cleanup on becoming leader. Leader election and cleanup
 * bootstrapping are migrated to the operator-sdk runtime in a later step.
 *
 * **See Also:** [Kubernetes Operator Pattern](https://kubernetes.io/docs/concepts/extend-kubernetes/operator/)
 */
class TheodoliteOperator(private val client: NamespacedKubernetesClient) {

    fun start() {
        LeaderElector(
            client = this.client,
            name = Configuration.COMPONENT_NAME
        ).getLeadership(::startOperator)
    }

    /**
     * Clears orphaned cluster state on becoming the leading operator. The reconcilers, which are
     * auto-started by the operator-sdk runtime, then drive all execution and benchmark handling.
     */
    private fun startOperator() {
        logger.info { "Becoming the leading operator. Use namespace '${this.client.namespace}'." }
        client.use {
            ClusterSetup(
                executionCRDClient = getExecutionClient(),
                benchmarkCRDClient = getBenchmarkClient(),
                client = this.client
            ).clearClusterState()
        }
    }

    fun getCoordinator(): RunnerCoordinator {
        return Arc.container().instance(RunnerCoordinator::class.java).get()
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
