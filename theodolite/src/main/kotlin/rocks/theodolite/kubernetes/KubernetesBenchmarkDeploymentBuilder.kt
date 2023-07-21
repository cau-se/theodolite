package rocks.theodolite.kubernetes

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.patcher.PatchHandler
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
            afterTeardownDelay: Long,
            waitForResourcesEnabled: Boolean
    ): BenchmarkDeployment {
        logger.info { "Using ${this.client.namespace} as namespace." }

        val appResources = loadKubernetesResources(kubernetesBenchmark.sut.resources, this.client).toResourceMap()
        val loadGenResources = loadKubernetesResources(kubernetesBenchmark.loadGenerator.resources, this.client).toResourceMap()

        // patch the load dimension
        loadPatcherDefinitions.forEach { patcherDefinition ->
            if (appResources.keys.contains(patcherDefinition.resource)) {
                appResources[patcherDefinition.resource] =
                    PatchHandler.patchResource(appResources, patcherDefinition, load.toString())
            } else {
                loadGenResources[patcherDefinition.resource] =
                    PatchHandler.patchResource(loadGenResources, patcherDefinition, load.toString())
            }
        }
        // patch the resource dimension
        resourcePatcherDefinitions.forEach { patcherDefinition ->
            if (appResources.keys.contains(patcherDefinition.resource)) {
                appResources[patcherDefinition.resource] =
                    PatchHandler.patchResource(appResources, patcherDefinition, resource.toString())
            } else {
                loadGenResources[patcherDefinition.resource] =
                    PatchHandler.patchResource(loadGenResources, patcherDefinition, resource.toString())
            }
        }

        // Patch the given overrides
        configurationOverrides.forEach { override ->
            override?.let {
                if (appResources.keys.contains(it.patcher.resource)) {
                    appResources[it.patcher.resource] =
                        PatchHandler.patchResource(appResources, override.patcher, override.value)
                } else {
                    loadGenResources[it.patcher.resource] =
                        PatchHandler.patchResource(loadGenResources, override.patcher, override.value)
                }
            }
        }

        val kafkaConfig = kubernetesBenchmark.kafkaConfig

        return KubernetesBenchmarkDeployment(
                sutBeforeActions = kubernetesBenchmark.sut.beforeActions,
                sutAfterActions = kubernetesBenchmark.sut.afterActions,
                loadGenBeforeActions = kubernetesBenchmark.loadGenerator.beforeActions,
                loadGenAfterActions = kubernetesBenchmark.loadGenerator.afterActions,
                appResources = appResources.toList().flatMap { it.second },
                loadGenResources = loadGenResources.toList().flatMap { it.second },
                loadGenerationDelay = loadGenerationDelay,
                afterTeardownDelay = afterTeardownDelay,
                kafkaConfig = if (kafkaConfig != null) mapOf("bootstrap.servers" to kafkaConfig.bootstrapServer) else mapOf(),
                topics = kafkaConfig?.topics ?: listOf(),
                client = this.client,
                rolloutMode = waitForResourcesEnabled
        )
    }

}

private fun Collection<Pair<String, HasMetadata>>.toResourceMap(): MutableMap<String, List<HasMetadata>> {
    return this.toMap()
        .toMutableMap()
        .map { Pair(it.key, listOf(it.value)) }
        .toMap()
        .toMutableMap()
}
