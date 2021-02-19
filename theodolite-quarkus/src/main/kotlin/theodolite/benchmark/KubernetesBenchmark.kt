package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import theodolite.k8s.YamlLoader
import theodolite.patcher.PatcherManager
import theodolite.util.LoadDimension
import theodolite.util.OverridePatcherDefinition
import theodolite.util.Resource
import theodolite.util.TypeName

class KubernetesBenchmark(): Benchmark {
    lateinit var name: String
    lateinit var appResource: List<String>
    lateinit var loadGenResource: List<String>
    lateinit var resourceTypes: List<TypeName>
    lateinit var loadTypes: List<TypeName>



    private fun loadKubernetesResources(resources: List<String>): List<Pair<String, KubernetesResource>> {
        val basePath = "./../../../resources/main/yaml/"
        var parser = theodolite.benchmark.BenchmarkYamlParser()
        val loader = YamlLoader(DefaultKubernetesClient().inNamespace("default"))
        return resources
            .map { resource ->
                val resourcePath = "$basePath/$resource"
                val kind = parser.parse(resourcePath, HashMap<String, String>()::class.java)?.get("kind") !!
                val k8sResource = loader.loadK8sResource(kind , resourcePath)
                Pair<String, KubernetesResource>(resource, k8sResource)
            }
        }




    override fun buildDeployment(load: LoadDimension, res: Resource, overrides: List<OverridePatcherDefinition>): BenchmarkDeployment {
        // TODO("set node selector")
        val resources = loadKubernetesResources(this.appResource + this.loadGenResource)
        val patcherManager = PatcherManager()

        // patch res and load
        patcherManager.applyPatcher(res.getType(), this.resourceTypes, resources, res.get())
        patcherManager.applyPatcher(load.getType(), this.loadTypes, resources, load.get().toString())

        // patch overrides
        overrides.forEach {override ->  patcherManager.applyPatcher(override, resources)}

        resources.forEach {x -> println(x)}

        return KubernetesBenchmarkDeployment(emptyList(), hashMapOf<String, Any>(), "", emptyList())
    }
}
