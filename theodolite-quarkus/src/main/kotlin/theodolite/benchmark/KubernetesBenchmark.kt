package theodolite.benchmark

import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.yaml.snakeyaml.Yaml
import theodolite.k8s.YamlLoader
import theodolite.util.LoadDimension
import theodolite.util.Resource
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class KubernetesBenchmark(): Benchmark {
    lateinit var name: String
    lateinit var appResource: List<String>
    lateinit var loadGenResource: List<String>


    private fun loadKubernetesResources(): List<KubernetesResource?> {
        val basePath = "./../../../resources/main/yaml/"
        var parser = theodolite.benchmark.BenchmarkYamlParser()
        val loader = YamlLoader(DefaultKubernetesClient().inNamespace("default"))
        return this.appResource
            .map { resource -> "$basePath/$resource" }
            .map { resourcePath ->
                val kind = parser.parse(resourcePath, HashMap<String, String>()::class.java) !!
                kind["kind"]?.let { loader.loadK8sResource(it, resourcePath) }
        }
    }

    private fun patchKubernetesResources() {

    }


    override fun buildDeployment(load: LoadDimension, res: Resource, override: Map<String, String>): BenchmarkDeployment {
        // TODO("")
       val resources = loadKubernetesResources()
        resources.forEach {x -> println(x.toString())}
        // Return KubernetesBenchmarkDeployment with individual parametrisation
        return KubernetesBenchmarkDeployment(emptyList(), hashMapOf<String, Any>(), "", emptyList() )
    }
}