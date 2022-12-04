package rocks.theodolite.kubernetes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.ConfigMapBuilder
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

@QuarkusTest
@WithKubernetesTestServer
internal class ResourceSetsTest {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    @TempDir
    @JvmField
    final var tempDir: Path? = null

    private val objectMapper: ObjectMapper = ObjectMapper(YAMLFactory())

    @BeforeEach
    fun setUp() {
        server.before()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    private fun deployAndGetResource(vararg resources: HasMetadata): ConfigMapResourceSet {
        val configMap = ConfigMapBuilder()
            .withNewMetadata().withName(resources[0].metadata.name).endMetadata()
            .let {
                resources.foldIndexed(it) { i, b, r ->
                    b.addToData("resource_$i.yaml", objectMapper.writeValueAsString(r))
                }
            }
            .build()

        server.client.configMaps().createOrReplace(configMap)

        val resourceSet = ConfigMapResourceSet()
        resourceSet.name = resources[0].metadata.name

        return resourceSet
    }

    private fun copyTestResourceFile(fileName: String, tempDir: Path) {
        val stream = javaClass.getResourceAsStream("/k8s-resource-files/$fileName")
            ?: throw IllegalArgumentException("File does not exist")
        val target = tempDir.resolve(fileName)
        Files.copy(stream, target)
    }

    @Test
    fun testLoadConfigMap() {
        val resource = ConfigMapBuilder()
            .withNewMetadata()
            .withName("test-configmap")
            .endMetadata()
            .build()
        deployAndGetResource(resource)

        val yamlString =
            """
            configMap:    
                name: test-configmap
                files:
            """

        val resourcesSet: ResourceSets = objectMapper.readValue(yamlString, ResourceSets::class.java)
        assertTrue(resourcesSet.fileSystem == null)
        assertTrue(resourcesSet.configMap != null)

        val configMap = resourcesSet.loadResourceSet(server.client)
        assertEquals(1, configMap.size)
        assertTrue(configMap.toList().first().second is ConfigMap)
        assertTrue(configMap.toList().first().second.toString().contains(other = resource.metadata.name))

        assertEquals(configMap.elementAt(0).second, resource)
    }

    @Test
    fun testLoadFileSystem(@TempDir tempDir: Path) {
        copyTestResourceFile("test-deployment.yaml", tempDir)

        val resourceSet = FileSystemResourceSet()
        resourceSet.path = tempDir.toString()
        resourceSet.files = listOf("test-deployment.yaml")
        assertEquals(1, resourceSet.getResourceSet(server.client).size)

        val yamlString =
            """
            fileSystem:    
                path: ${resourceSet.path}
                files:
                    - test-deployment.yaml
            """

        val resourcesSet: ResourceSets = objectMapper.readValue(yamlString, ResourceSets::class.java)
        assertTrue(resourcesSet.fileSystem != null)
        assertTrue(resourcesSet.configMap == null)

        val fileSystem = resourcesSet.loadResourceSet(server.client)
        assertEquals(fileSystem.size, 1)
    }

    @Test
    fun testEmptyResourceSets() {
        val resourceSet = ResourceSets()

        assertThrows<DeploymentFailedException> {
            resourceSet.loadResourceSet(server.client)
        }
    }
}