package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.k8s.K8sResourceLoader
import theodolite.patcher.PatcherFactory
import theodolite.util.*

private val logger = KotlinLogging.logger {}

private var DEFAULT_NAMESPACE = "default"

@RegisterForReflection
class KubernetesBenchmark : Benchmark {
    lateinit var name: String
    lateinit var appResource: List<String>
    lateinit var loadGenResource: List<String>
    lateinit var resourceTypes: List<TypeName>
    lateinit var loadTypes: List<TypeName>
    lateinit var kafkaConfig: KafkaConfig
    val namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
    var path = System.getenv("THEODOLITE_APP_RESOURCES") ?: "./config"
    val client = DefaultKubernetesClient().inNamespace(namespace)

    private fun loadKubernetesResources(resources: List<String>): List<Pair<String, KubernetesResource>> {
        val parser = YamlParser()

        val loader = K8sResourceLoader(client)
        return resources
            .map { resource ->
                val resourcePath = "$path/$resource"
                val kind = parser.parse(resourcePath, HashMap<String, String>()::class.java)?.get("kind")!!
                val k8sResource = loader.loadK8sResource(kind, resourcePath)
                Pair(resource, k8sResource)
            }
    }

    override fun buildDeployment(
        load: LoadDimension,
        res: Resource,
        configurationOverrides: List<ConfigurationOverride?>
    ): BenchmarkDeployment {
        logger.info { "Using $namespace as namespace." }
        logger.info { "Using $path as resource path." }

        val resources = loadKubernetesResources(this.appResource + this.loadGenResource)
        val patcherFactory = PatcherFactory()

        // patch the load dimension the resources
        load.getType().forEach { patcherDefinition ->
            patcherFactory.createPatcher(patcherDefinition, resources).patch(load.get().toString())
        }
        res.getType().forEach { patcherDefinition ->
            patcherFactory.createPatcher(patcherDefinition, resources).patch(res.get().toString())
        }

        // Patch the given overrides
        configurationOverrides.forEach { override ->
            override?.let {
                patcherFactory.createPatcher(it.patcher, resources).patch(override.value)
            }
        }
        return KubernetesBenchmarkDeployment(
            namespace = namespace,
            resources = resources.map { r -> r.second },
            kafkaConfig = hashMapOf("bootstrap.servers" to kafkaConfig.bootstrapServer),
            topics = kafkaConfig.getKafkaTopics(),
            client = client
        )
    }
}
