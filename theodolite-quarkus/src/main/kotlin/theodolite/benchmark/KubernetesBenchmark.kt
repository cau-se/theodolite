package theodolite.benchmark

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.k8s.K8sResourceLoader
import theodolite.patcher.PatcherFactory
import theodolite.util.*

private val logger = KotlinLogging.logger {}

private var DEFAULT_NAMESPACE = "default"

/**
 * Represents a benchmark in Kubernetes. An example for this is the BenchmarkType.yaml
 * Contains a of:
 * - [name] of the benchmark,
 * - [appResource] list of the resources that have to be deployed for the benchmark,
 * - [loadGenResource] resource that generates the load,
 * - [resourceTypes] types of scaling resources,
 * - [loadTypes] types of loads that can be scaled for the benchmark,
 * - [kafkaConfig] for the [theodolite.k8s.TopicManager],
 * - [namespace] for the client,
 * - [path] under which the resource yamls can be found.
 *
 *  This class is used for the parsing(in the [theodolite.execution.TheodoliteYamlExecutor]) and
 *  for the deserializing in the [theodolite.execution.operator.TheodoliteOperator].
 * @constructor construct an empty Benchmark.
 */
@JsonDeserialize
@RegisterForReflection
class KubernetesBenchmark: KubernetesResource, Benchmark{
    lateinit var name: String
    lateinit var appResource: List<String>
    lateinit var loadGenResource: List<String>
    lateinit var resourceTypes: List<TypeName>
    lateinit var loadTypes: List<TypeName>
    lateinit var kafkaConfig: KafkaConfig
    var namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
    var path =  System.getenv("THEODOLITE_APP_RESOURCES") ?: "./config"


    /**
     * Loads [KubernetesResource]s.
     * It first loads them via the [YamlParser] to check for their concrete type and afterwards initializes them using
     * the [K8sResourceLoader]
     */
    private fun loadKubernetesResources(resources: List<String>): List<Pair<String, KubernetesResource>> {
        val parser = YamlParser()
        val loader = K8sResourceLoader(DefaultKubernetesClient().inNamespace(namespace))
        return resources
            .map { resource ->
                val resourcePath = "$path/$resource"
                val kind = parser.parse(resourcePath, HashMap<String, String>()::class.java)?.get("kind")!!
                val k8sResource = loader.loadK8sResource(kind, resourcePath)
                Pair(resource, k8sResource)
            }
    }

    /**
     * Builds a deployment.
     * First loads all required resources and then patches them to the concrete load and resources for the experiment.
     * Afterwards patches additional configurations(cluster depending) into the resources.
     * @param load concrete load that will be benchmarked in this experiment.
     * @param res concrete resource that will be scaled for this experiment.
     * @param configurationOverrides
     * @return a [BenchmarkDeployment]
     */
    override fun buildDeployment(
        load: LoadDimension,
        res: Resource,
        configurationOverrides: List<ConfigurationOverride?>,
        loadGenerationDelay: Long,
        afterTeardownDelay: Long
    ): BenchmarkDeployment {
        logger.info { "Using $namespace as namespace." }
        logger.info { "Using $path as resource path." }

        val appResources = loadKubernetesResources(this.appResource)
        val loadGenResources = loadKubernetesResources(this.loadGenResource)

        val patcherFactory = PatcherFactory()

        // patch the load dimension the resources
        load.getType().forEach { patcherDefinition ->
            patcherFactory.createPatcher(patcherDefinition, loadGenResources).patch(load.get().toString())
        }
        res.getType().forEach { patcherDefinition ->
            patcherFactory.createPatcher(patcherDefinition, appResources).patch(res.get().toString())
        }

        // Patch the given overrides
        configurationOverrides.forEach { override ->
            override?.let {
                patcherFactory.createPatcher(it.patcher, appResources + loadGenResources).patch(override.value)
            }
        }
        return KubernetesBenchmarkDeployment(
            namespace = namespace,
            appResources = appResources.map { it.second },
            loadGenResources = loadGenResources.map { it.second },
            loadGenerationDelay = loadGenerationDelay,
            afterTeardownDelay = afterTeardownDelay,
            kafkaConfig = hashMapOf("bootstrap.servers" to kafkaConfig.bootstrapServer),
            topics = kafkaConfig.topics,
            client = DefaultKubernetesClient().inNamespace(namespace)
        )
    }
}
