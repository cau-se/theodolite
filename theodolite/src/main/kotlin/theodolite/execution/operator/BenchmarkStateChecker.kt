package theodolite.execution.operator

import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import theodolite.benchmark.Action
import theodolite.benchmark.ActionSelector
import theodolite.benchmark.KubernetesBenchmark
import theodolite.benchmark.ResourceSets
import theodolite.model.crd.BenchmarkCRD
import theodolite.model.crd.BenchmarkStates
import theodolite.model.crd.KubernetesBenchmarkList

class BenchmarkStateChecker(
    private val benchmarkCRDClient: MixedOperation<BenchmarkCRD, KubernetesBenchmarkList, Resource<BenchmarkCRD>>,
    private val benchmarkStateHandler: BenchmarkStateHandler,
    private val client: NamespacedKubernetesClient

) {

    fun start(running: Boolean) {
        Thread {
            while (running) {
                updateBenchmarkStatus()
                Thread.sleep(100 * 1)
            }
        }.start()
    }

    /**
     * Checks and updates the states off all deployed benchmarks.
     *
     */
    fun updateBenchmarkStatus() {
        this.benchmarkCRDClient
            .list()
            .items
            .map { it.spec.name = it.metadata.name; it }
            .map { Pair(it, checkState(it.spec)) }
            .forEach { setState(it.first, it.second) }
    }

    private fun setState(resource: BenchmarkCRD, state: BenchmarkStates) {
        benchmarkStateHandler.setResourceSetState(resource.spec.name, state)
    }

    /**
     * Checks the state of the benchmark.
     *
     * @param benchmark The benchmark to check
     * @return [BenchmarkStates.READY] iff all resource could be loaded and all actions could be executed, [BenchmarkStates.PENDING] else
     */
    private fun checkState(benchmark: KubernetesBenchmark): BenchmarkStates {
        return if (checkActionCommands(benchmark) == BenchmarkStates.READY
            && checkResources(benchmark) == BenchmarkStates.READY
        ) {
            BenchmarkStates.READY
        } else {
            BenchmarkStates.PENDING
        }
    }

    /**
     * Checks if all specified actions of the given benchmark could be executed or not
     *
     * @param benchmark The benchmark to check
     * @return The state of this benchmark. [BenchmarkStates.READY] if all actions could be executed, else [BenchmarkStates.PENDING]
     */
    private fun checkActionCommands(benchmark: KubernetesBenchmark): BenchmarkStates {
        return if (checkIfActionPossible(benchmark.infrastructure.resources, benchmark.sut.beforeActions)
            && checkIfActionPossible(benchmark.infrastructure.resources, benchmark.sut.afterActions)
            && checkIfActionPossible(benchmark.infrastructure.resources, benchmark.loadGenerator.beforeActions)
            && checkIfActionPossible(benchmark.infrastructure.resources, benchmark.loadGenerator.beforeActions)
        ) {
            BenchmarkStates.READY
        } else {
            BenchmarkStates.PENDING
        }
    }

    /**
     * Action commands are called on a pod. To verify that an action command can be executed,
     * it checks that the specified pods are either currently running in the cluster or
     * have been specified as infrastructure in the benchmark.
     *
     * @param benchmark the benchmark to check
     * @param actions the actions
     * @return true if all actions could be executed, else false
     */
    private fun checkIfActionPossible(resourcesSets: List<ResourceSets>, actions: List<Action>): Boolean {
        return !actions.map {
            checkIfResourceIsDeployed(it.selector) || checkIfResourceIsInfrastructure(resourcesSets, it.selector)
        }.contains(false)
    }

    /**
     * Checks for the given actionSelector whether the required resources are already deployed in the cluster or not
     *
     * @param selector the actionSelector to check
     * @return true if the required resources are found, else false
     */
    fun checkIfResourceIsDeployed(selector: ActionSelector): Boolean {
        val pods = this.client
            .pods()
            .withLabels(selector.pod.matchLabels)
            .list()
            .items

        return if (pods.isNotEmpty() && selector.container.isNotEmpty()) {
            pods.map { pod ->
                pod
                    .spec
                    .containers
                    .map { it.name }
                    .contains(selector.container)
            }.contains(true)
        } else {
            pods.isNotEmpty()
        }
    }

    /**
     * Checks for the given actionSelector whether the required resources are specified as infrastructure or not
     *
     * @param benchmark the benchmark to check
     * @param selector the actionSelector to check
     * @return true if the required resources are found, else false
     */
    fun checkIfResourceIsInfrastructure(resourcesSets: List<ResourceSets>, selector: ActionSelector): Boolean {
        val resources = resourcesSets.flatMap { it.loadResourceSet(this.client) }

        return if (resources.isEmpty()) {
            false
        } else {
            resources.map { it.second }
                .filterIsInstance<Deployment>()
                .filter { it.metadata.labels.containsMatchLabels(selector.pod.matchLabels) }
                .any {
                    if (selector.container.isNotEmpty()) {
                        it.spec.template.spec.containers.map { it.name }.contains(selector.container)
                    } else {
                        true
                    }
                }
        }
    }

    /**
     * Checks if it is possible to load all specified Kubernetes manifests.
     *
     * @param benchmark The benchmark to check
     * @return The state of this benchmark. [BenchmarkStates.READY] if all resources could be loaded, else [BenchmarkStates.PENDING]
     */
    fun checkResources(benchmark: KubernetesBenchmark): BenchmarkStates {
        return try {
            val appResources =
                benchmark.loadKubernetesResources(resourceSet = benchmark.sut.resources)
            val loadGenResources =
                benchmark.loadKubernetesResources(resourceSet = benchmark.loadGenerator.resources)
            if (appResources.isNotEmpty() && loadGenResources.isNotEmpty()) {
                BenchmarkStates.READY
            } else {
                BenchmarkStates.PENDING
            }
        } catch (e: Exception) {
            BenchmarkStates.PENDING
        }
    }
}

private fun <K, V> MutableMap<K, V>.containsMatchLabels(matchLabels: MutableMap<V, V>) : Boolean {
    for (kv in matchLabels) {
        if (kv.value != this[kv.key as K]) {
            return false
        }
    }
    return true
}

