package rocks.theodolite.kubernetes.execution

import io.fabric8.kubernetes.api.model.KubernetesResource
import mu.KotlinLogging
import rocks.theodolite.kubernetes.benchmark.*
import rocks.theodolite.kubernetes.k8s.K8sManager
import rocks.theodolite.kubernetes.k8s.resourceLoader.K8sResourceLoader
import rocks.theodolite.kubernetes.patcher.PatcherFactory
import rocks.theodolite.kubernetes.util.ConfigurationOverride
import rocks.theodolite.kubernetes.util.PatcherDefinition

private val logger = KotlinLogging.logger {}

class KubernetesExecutionRunner(val kubernetesBenchmark: KubernetesBenchmark) : Benchmark {

    /**
     * Loads [KubernetesResource]s.
     * It first loads them via the [YamlParserFromFile] to check for their concrete type and afterwards initializes them using
     * the [K8sResourceLoader]
     */
    fun loadKubernetesResources(resourceSet: List<ResourceSets>): Collection<Pair<String, KubernetesResource>> {
        return resourceSet.flatMap { it.loadResourceSet(kubernetesBenchmark.getClient()) }
    }

    override fun setupInfrastructure() {
        kubernetesBenchmark.infrastructure.beforeActions.forEach { it.exec(client = kubernetesBenchmark.getClient()) }
        val kubernetesManager = K8sManager(kubernetesBenchmark.getClient())
        loadKubernetesResources(kubernetesBenchmark.infrastructure.resources)
                .map{it.second}
                .forEach { kubernetesManager.deploy(it) }
    }

    override fun teardownInfrastructure() {
        val kubernetesManager = K8sManager(kubernetesBenchmark.getClient())
        loadKubernetesResources(kubernetesBenchmark.infrastructure.resources)
                .map{it.second}
                .forEach { kubernetesManager.remove(it) }
        kubernetesBenchmark.infrastructure.afterActions.forEach { it.exec(client = kubernetesBenchmark.getClient()) }
    }

    /**
     * Builds a deployment.
     * First loads all required resources and then patches them to the concrete load and resources for the experiment for the demand metric
     * or loads all loads and then patches them to the concrete load and resources for the experiment.
     * Afterwards patches additional configurations(cluster depending) into the resources (or loads).
     * @param load concrete load that will be benchmarked in this experiment (demand metric), or scaled (capacity metric).
     * @param resource concrete resource that will be scaled for this experiment (demand metric), or benchmarked (capacity metric).
     * @param configurationOverrides
     * @return a [BenchmarkDeployment]
     */
    override fun buildDeployment(
            load: Int,
            loadPatcherDefinitions: List<PatcherDefinition>,
            resource: Int,
            resourcePatcherDefinitions: List<PatcherDefinition>,
            configurationOverrides: List<ConfigurationOverride?>,
            loadGenerationDelay: Long,
            afterTeardownDelay: Long
    ): BenchmarkDeployment {
        logger.info { "Using ${kubernetesBenchmark.getNamespace()} as namespace." }

        val appResources = loadKubernetesResources(kubernetesBenchmark.sut.resources)
        val loadGenResources = loadKubernetesResources(kubernetesBenchmark.loadGenerator.resources)

        val patcherFactory = PatcherFactory()

        // patch the load dimension the resources
        loadPatcherDefinitions.forEach { patcherDefinition ->
            patcherFactory.createPatcher(patcherDefinition, loadGenResources).patch(load.toString())
        }
        resourcePatcherDefinitions.forEach { patcherDefinition ->
            patcherFactory.createPatcher(patcherDefinition, appResources).patch(resource.toString())
        }

        // Patch the given overrides
        configurationOverrides.forEach { override ->
            override?.let {
                patcherFactory.createPatcher(it.patcher, appResources + loadGenResources).patch(override.value)
            }
        }

        val kafkaConfig = kubernetesBenchmark.kafkaConfig

        return KubernetesBenchmarkDeployment(
                sutBeforeActions = kubernetesBenchmark.sut.beforeActions,
                sutAfterActions = kubernetesBenchmark.sut.afterActions,
                loadGenBeforeActions = kubernetesBenchmark.loadGenerator.beforeActions,
                loadGenAfterActions = kubernetesBenchmark.loadGenerator.afterActions,
                appResources = appResources.map { it.second },
                loadGenResources = loadGenResources.map { it.second },
                loadGenerationDelay = loadGenerationDelay,
                afterTeardownDelay = afterTeardownDelay,
                kafkaConfig = if (kafkaConfig != null) hashMapOf("bootstrap.servers" to kafkaConfig.bootstrapServer) else mapOf(),
                topics = kafkaConfig?.topics ?: listOf(),
                client = kubernetesBenchmark.getClient()
        )
    }
}