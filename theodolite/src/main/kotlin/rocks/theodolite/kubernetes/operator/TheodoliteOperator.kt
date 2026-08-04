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
 * only performs the one-time cluster cleanup on becoming leader and then opens [OperatorReadiness]
 * so those reconcilers may act, closing it again as soon as leadership is lost. The reconcilers are
 * auto-started by the operator-sdk runtime independently of leadership, so [OperatorReadiness] is
 * what keeps a non-leader replica, a leader that hasn't finished cleanup yet, or a former leader
 * that hasn't noticed the loss yet, from selecting or starting anything.
 *
 * **See Also:** [Kubernetes Operator Pattern](https://kubernetes.io/docs/concepts/extend-kubernetes/operator/)
 */
class TheodoliteOperator(private val client: NamespacedKubernetesClient) {

    fun start() {
        // Leader election blocks its thread for as long as leadership is held. Run it off the
        // Quarkus StartupEvent thread so that observer returns and the quarkus-operator-sdk
        // startup observer (which runs after this one) can start the JOSDK reconcilers.
        // This extra thread can be removed once this fabric8-based leader election is replaced
        // by the operator-sdk's built-in leader election and this whole bootstrap goes away.
        Thread {
            LeaderElector(
                client = this.client,
                name = Configuration.COMPONENT_NAME
            ).getLeadership(::startOperator, ::stopOperator)
        }.apply {
            name = "theodolite-leader-elector"
            isDaemon = true
        }.start()
    }

    /**
     * Clears orphaned cluster state on becoming the leading operator, then opens
     * [OperatorReadiness] so the reconcilers (auto-started by the operator-sdk runtime, but gated
     * until this point) start driving execution and benchmark handling.
     *
     * The term obtained before cleanup guards against losing leadership while cleanup is still
     * running: if [stopOperator] closes the gate in the meantime, this [OperatorReadiness.open]
     * call is for a stale term and becomes a no-op instead of re-opening the gate too late.
     */
    private fun startOperator() {
        logger.info { "Becoming the leading operator. Use namespace '${this.client.namespace}'." }
        val term = getReadiness().beginTerm()
        client.use {
            ClusterSetup(
                executionCRDClient = getExecutionClient(),
                benchmarkCRDClient = getBenchmarkClient(),
                client = this.client
            ).clearClusterState()
        }
        getReadiness().open(term)
    }

    /** Closes [OperatorReadiness] on losing leadership so reconcilers stop acting immediately. */
    private fun stopOperator() {
        getReadiness().close()
    }

    fun getCoordinator(): RunnerCoordinator {
        return Arc.container().instance(RunnerCoordinator::class.java).get()
    }

    fun getReadiness(): OperatorReadiness {
        return Arc.container().instance(OperatorReadiness::class.java).get()
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
