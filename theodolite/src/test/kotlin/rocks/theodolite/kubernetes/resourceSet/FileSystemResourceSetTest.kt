package rocks.theodolite.kubernetes.resourceSet

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import registerResource
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.util.exception.DeploymentFailedException
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path

@QuarkusTest
@WithKubernetesTestServer
class FileSystemResourceSetTest {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    @TempDir
    @JvmField
    final var tempDir: Path? = null

    @BeforeEach
    fun setUp() {
        server.before()
        this.server.client
            .apiextensions().v1()
            .customResourceDefinitions()
            .load(FileInputStream("crd/crd-execution.yaml"))
            .create()
        this.server.client
            .apiextensions().v1()
            .customResourceDefinitions()
            .load(FileInputStream("crd/crd-benchmark.yaml"))
            .create()

        // Apparently we need create CRD clients once
        this.server.client.resources(ExecutionCRD::class.java)
        this.server.client.resources(BenchmarkCRD::class.java)
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    private fun copyTestResourceFile(fileName: String, tempDir: Path) {
        val stream = javaClass.getResourceAsStream("/k8s-resource-files/$fileName")
            ?: throw IllegalArgumentException("File does not exist")
        val target = tempDir.resolve(fileName)
        Files.copy(stream, target)
    }

    @Test
    fun testLoadDeployment(@TempDir tempDir: Path) {
        copyTestResourceFile("test-deployment.yaml", tempDir)

        val resourceSet = FileSystemResourceSet()
        resourceSet.path = tempDir.toString()
        resourceSet.files = listOf("test-deployment.yaml")
        assertEquals(1, resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toList().first().second is Deployment)
    }

    @Test
    fun testLoadService(@TempDir tempDir: Path) {
        copyTestResourceFile("test-service.yaml", tempDir)

        val resourceSet = FileSystemResourceSet()
        resourceSet.path = tempDir.toString()
        resourceSet.files = listOf("test-service.yaml")
        assertEquals(1, resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toList().first().second is Service)
    }

    @Test
    fun testLoadStatefulSet(@TempDir tempDir: Path) {
        copyTestResourceFile("test-statefulset.yaml", tempDir)

        val resourceSet = FileSystemResourceSet()
        resourceSet.path = tempDir.toString()
        resourceSet.files = listOf("test-statefulset.yaml")
        assertEquals(1, resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toList().first().second is StatefulSet)
    }

    @Test
    fun testLoadConfigMap(@TempDir tempDir: Path) {
        copyTestResourceFile("test-configmap.yaml", tempDir)

        val resourceSet = FileSystemResourceSet()
        resourceSet.path = tempDir.toString()
        resourceSet.files = listOf("test-configmap.yaml")
        assertEquals(1, resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toList().first().second is ConfigMap)
    }

    @Test
    fun testLoadServiceMonitor(@TempDir tempDir: Path) {
        val serviceMonitorContext = ResourceDefinitionContext.Builder()
            .withGroup("monitoring.coreos.com")
            .withKind("ServiceMonitor")
            .withPlural("servicemonitors")
            .withNamespaced(true)
            .withVersion("v1")
            .build()
        server.registerResource(serviceMonitorContext)

        copyTestResourceFile("test-service-monitor.yaml", tempDir)

        val resourceSet = FileSystemResourceSet()
        resourceSet.path = tempDir.toString()
        resourceSet.files = listOf("test-service-monitor.yaml")
        assertEquals(1, resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toList().first().second is GenericKubernetesResource)
    }

    @Test
    fun testLoadBenchmark(@TempDir tempDir: Path) {
        copyTestResourceFile("test-benchmark.yaml", tempDir)

        val resourceSet = FileSystemResourceSet()
        resourceSet.path = tempDir.toString()
        resourceSet.files = listOf("test-benchmark.yaml")
        assertEquals(1, resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toList().first().second is BenchmarkCRD)
    }

    @Test
    fun testLoadExecution(@TempDir tempDir: Path) {
        copyTestResourceFile("test-execution.yaml", tempDir)

        val resourceSet = FileSystemResourceSet()
        resourceSet.path = tempDir.toString()
        resourceSet.files = listOf("test-execution.yaml")
        assertEquals(1, resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toList().first().second is ExecutionCRD)
    }

    @Test
    fun testFilesNotSet(@TempDir tempDir: Path) {
        copyTestResourceFile("test-deployment.yaml", tempDir)
        copyTestResourceFile("test-service.yaml", tempDir)

        val resourceSet = FileSystemResourceSet()
        resourceSet.path = tempDir.toString()
        assertEquals(2, resourceSet.getResourceSet(server.client).size)
    }

    @Test
    fun testWrongPath(@TempDir tempDir: Path) {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = "/not/existing/path"
        assertThrows<DeploymentFailedException> {
            resourceSet.getResourceSet(server.client)
        }
    }
}