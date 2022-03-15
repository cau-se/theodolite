package rocks.theodolite.kubernetes.resourceSet

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.quarkus.runtime.annotations.RegisterForReflection
import rocks.theodolite.kubernetes.util.exception.DeploymentFailedException
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.listDirectoryEntries


@RegisterForReflection
@JsonDeserialize
class FileSystemResourceSet: ResourceSet, KubernetesResource {
    lateinit var path: String
    var files: List<String>? = null

    override fun getResourceSet(client: NamespacedKubernetesClient): Collection<Pair<String, HasMetadata>> {
        // if files is set ...
        return files?.run {
            return this
                .map { Paths.get(path, it) }
                .map { loadSingleResource(resource = it, client = client) }
        } ?:
        try {
            Paths.get(path)
                .listDirectoryEntries()
                .filter { it.toString().endsWith(".yaml") || it.toString().endsWith(".yml") }
                .map { loadSingleResource(resource = it, client = client) }
        } catch (e: java.nio.file.NoSuchFileException) { // not to be confused with Kotlin exception
            throw  DeploymentFailedException("Could not load files located in $path", e)
        }
    }

    private fun loadSingleResource(resource: Path, client: NamespacedKubernetesClient): Pair<String, HasMetadata> {
        return try {
            val stream = FileInputStream(resource.toFile())
            val text = BufferedReader(
                InputStreamReader(stream, StandardCharsets.UTF_8)
            ).lines().collect(Collectors.joining("\n"))
            val k8sResource = client.resource(text).get()
            Pair(resource.last().toString(), k8sResource)
        } catch (e: FileNotFoundException){
            throw DeploymentFailedException("File $resource not found.", e)
        } catch (e: IllegalArgumentException) {
            throw DeploymentFailedException("Could not load resource: $resource.", e)
        }
    }
}