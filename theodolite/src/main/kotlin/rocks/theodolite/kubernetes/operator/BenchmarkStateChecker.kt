package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import rocks.theodolite.kubernetes.benchmark.Action
import rocks.theodolite.kubernetes.benchmark.ActionSelector
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.resourceSet.ResourceSets
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkState
import rocks.theodolite.kubernetes.model.crd.KubernetesBenchmarkList

class BenchmarkStateChecker(
        private val benchmarkCRDClient: MixedOperation<BenchmarkCRD, KubernetesBenchmarkList, Resource<BenchmarkCRD>>,
        private val benchmarkStateHandler: BenchmarkStateHandler,
        private val client: NamespacedKubernetesClient,

) {

    fun start(running: Boolean) {
        Thread {
            while (running) {
                updateBenchmarkStatus()
                Thread.sleep(1000)
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

    private fun setState(resource: BenchmarkCRD, state: BenchmarkState) {
        benchmarkStateHandler.setResourceSetState(resource.spec.name, state)
    }

    /**
     * Checks the state of the benchmark.
     *
     * @param benchmark The benchmark to check
     * @return [BenchmarkStates.READY] iff all resource could be loaded and all actions could be executed, [BenchmarkStates.PENDING] else
     */
    private fun checkState(benchmark: KubernetesBenchmark): BenchmarkState {
        return if (checkActionCommands(benchmark) == BenchmarkState.READY
            && checkResources(benchmark) == BenchmarkState.READY
        ) {
            BenchmarkState.READY
        } else {
            BenchmarkState.PENDING
        }
    }

    /**
     * Checks if all specified actions of the given benchmark could be executed or not
     *
     * @param benchmark The benchmark to check
     * @return The state of this benchmark. [BenchmarkStates.READY] if all actions could be executed, else [BenchmarkStates.PENDING]
     */
    private fun checkActionCommands(benchmark: KubernetesBenchmark): BenchmarkState {
        return if (checkIfActionPossible(benchmark.infrastructure.resources, benchmark.sut.beforeActions)
            && checkIfActionPossible(benchmark.infrastructure.resources, benchmark.sut.afterActions)
            && checkIfActionPossible(benchmark.infrastructure.resources, benchmark.loadGenerator.beforeActions)
            && checkIfActionPossible(benchmark.infrastructure.resources, benchmark.loadGenerator.beforeActions)
        ) {
            BenchmarkState.READY
        } else {
            BenchmarkState.PENDING
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
        if (resources.isEmpty()) {
            return false
        }

        var podExist = resources.map { it.second }
            .filterIsInstance<Deployment>()
            .filter { it.metadata.labels.containsMatchLabels(selector.pod.matchLabels) }
            .any {
                if (selector.container.isNotEmpty()) {
                    it.spec.template.spec.containers.map { it.name }.contains(selector.container)
                } else {
                    true
                }
            }

        if (podExist) {
            return true
        }

        podExist = resources.map { it.second }
            .filterIsInstance<StatefulSet>()
            .filter { it.metadata.labels.containsMatchLabels(selector.pod.matchLabels) }
            .any {
                if (selector.container.isNotEmpty()) {
                    it.spec.template.spec.containers.map { it.name }.contains(selector.container)
                } else {
                    true
                }
            }

        if (podExist) {
            return true
        }

        return false
    }

    /**
     * Checks if it is possible to load all specified Kubernetes manifests.
     *
     * @param benchmark The benchmark to check
     * @return The state of this benchmark. [BenchmarkState.READY] if all resources could be loaded, else [BenchmarkState.PENDING]
     */
    fun checkResources(benchmark: KubernetesBenchmark): BenchmarkState {
        return try {
            val appResources =
                    loadKubernetesResources(resourceSet = benchmark.sut.resources)
            val loadGenResources =
                    loadKubernetesResources(resourceSet = benchmark.loadGenerator.resources)
            if (appResources.isNotEmpty() && loadGenResources.isNotEmpty()) {
                BenchmarkState.READY
            } else {
                BenchmarkState.PENDING
            }
        } catch (e: Exception) {
            BenchmarkState.PENDING
        }
    }

    /**
     * Loads [KubernetesResource]s.
     * It first loads them via the [YamlParserFromFile] to check for their concrete type and afterwards initializes them.
     */
    private fun loadKubernetesResources(resourceSet: List<ResourceSets>): Collection<Pair<String, KubernetesResource>> {
        return resourceSet.flatMap { it.loadResourceSet(this.client) }
    }
}

private fun <K, V> MutableMap<K, V>.containsMatchLabels(matchLabels: MutableMap<V, V>): Boolean {
    for (kv in matchLabels) {
        if (kv.value != this[kv.key as K]) {
            return false
        }
    }
    return true
}

