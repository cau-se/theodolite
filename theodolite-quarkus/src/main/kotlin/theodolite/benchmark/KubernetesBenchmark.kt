package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import mu.KotlinLogging
import theodolite.k8s.K8sResourceLoader
import theodolite.patcher.PatcherManager
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
    lateinit var namespace: String

    private fun loadKubernetesResources(resources: List<String>): List<Pair<String, KubernetesResource>> {
        val basePath = "./../../../resources/main/yaml/"
        val parser = YamlParser()

        namespace = System.getenv("NAMESPACE") ?: DEFAULT_NAMESPACE
        logger.info { "Using $namespace as namespace." }

        val loader = K8sResourceLoader(DefaultKubernetesClient().inNamespace(namespace))
        return resources
            .map { resource ->
                val resourcePath = "$basePath/$resource"
                val kind = parser.parse(resourcePath, HashMap<String, String>()::class.java)?.get("kind")!!
                val k8sResource = loader.loadK8sResource(kind, resourcePath)
                Pair(resource, k8sResource)
            }
    }

    override fun buildDeployment(
        load: LoadDimension,
        res: Resource,
        configurationOverrides: List<ConfigurationOverride>
    ): BenchmarkDeployment {
        val resources = loadKubernetesResources(this.appResource + this.loadGenResource)
        val patcherManager = PatcherManager()

        // patch res and load
        patcherManager.createAndApplyPatcher(res.getType(), this.resourceTypes, resources, res.get())
        patcherManager.createAndApplyPatcher(load.getType(), this.loadTypes, resources, load.get().toString())

        // patch overrides
        configurationOverrides.forEach { override ->
            patcherManager.applyPatcher(
                listOf(override.patcher),
                resources,
                override.value
            )
        }

        return KubernetesBenchmarkDeployment(
            namespace = namespace,
            resources = resources.map { r -> r.second },
            kafkaConfig = hashMapOf("bootstrap.servers" to kafkaConfig.bootstrapServer),
            topics = kafkaConfig.getKafkaTopics()
        )
    }
}
