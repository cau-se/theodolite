package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.patcher.PatcherFactory
import rocks.theodolite.kubernetes.util.ConfigurationOverride
import rocks.theodolite.kubernetes.patcher.PatcherDefinition

private val logger = KotlinLogging.logger {}

class KubernetesBenchmarkDeploymentBuilder (val kubernetesBenchmark: KubernetesBenchmark,
                                            private var client: NamespacedKubernetesClient)
    : BenchmarkDeploymentBuilder {


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
        logger.info { "Using ${this.client.namespace} as namespace." }

        val appResources = loadKubernetesResources(kubernetesBenchmark.sut.resources, this.client)
        val loadGenResources = loadKubernetesResources(kubernetesBenchmark.loadGenerator.resources, this.client)

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
                kafkaConfig = if (kafkaConfig != null) mapOf("bootstrap.servers" to kafkaConfig.bootstrapServer) else mapOf(),
                topics = kafkaConfig?.topics ?: listOf(),
                client = this.client
        )
    }

}