package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.yaml.snakeyaml.Yaml
import theodolite.k8s.YamlLoader
import theodolite.patcher.AbstractPatcher
import theodolite.patcher.Patcher
import theodolite.patcher.ReplicaPatcher
import theodolite.util.LoadDimension
import theodolite.util.PatcherDefinition
import theodolite.util.Resource
import theodolite.util.TypeName
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.IllegalArgumentException

class KubernetesBenchmark(): Benchmark {
    lateinit var name: String
    lateinit var appResource: List<String>
    lateinit var loadGenResource: List<String>
    lateinit var resourceTypes: List<TypeName>


    private fun loadKubernetesResources(resources: List<String>): List<Pair<String, KubernetesResource?>> {
        val basePath = "./../../../resources/main/yaml/"
        var parser = theodolite.benchmark.BenchmarkYamlParser()
        val loader = YamlLoader(DefaultKubernetesClient().inNamespace("default"))
        return resources
            .map { resource ->
                val resourcePath = "$basePath/$resource"
                val kind = parser.parse(resourcePath, HashMap<String, String>()::class.java) !!
                val k8sResource = kind["kind"]?.let { loader.loadK8sResource(it, resourcePath) }
                Pair<String, KubernetesResource?>(resource, k8sResource)
            }
    }

    private fun createK8sPatcher(patcherDefinition: PatcherDefinition, k8sResources: List<Pair<String, KubernetesResource>>): Patcher<Int> {
        return when(patcherDefinition.type) {
            "ReplicaPatcher" -> ReplicaPatcher(k8sResources.filter { it.first == patcherDefinition.resource}.map { resource -> resource.second }[0])
            "EnvVarPatcher" -> TODO("create env var patcher")
            else -> throw IllegalArgumentException("Patcher type ${patcherDefinition.type} not fount")
        }
        TODO("Use reflection to load patchers")
    }


    override fun buildDeployment(load: LoadDimension, res: Resource, override: Map<String, String>): BenchmarkDeployment {
        // TODO("set node selector")
        val resources = loadKubernetesResources(this.appResource + this.loadGenResource)
        val patchers = this.resourceTypes.map { patcherDef -> createK8sPatcher(patcherDef.patchers[0],
            resources as List<Pair<String, KubernetesResource>>
        ) }
        // exemplary only for replica patcher
        patchers.forEach{ patcher -> patcher.patch(res.get()) }
        return KubernetesBenchmarkDeployment(emptyList(), hashMapOf<String, Any>(), "", emptyList())
    }
}
