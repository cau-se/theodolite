package theodolite.benchmark

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rocks.theodolite.kubernetes.benchmark.FileSystemResourceSet
import rocks.theodolite.kubernetes.k8s.CustomResourceWrapper
import rocks.theodolite.kubernetes.util.exception.DeploymentFailedException

private const val testResourcePath = "./src/test/resources/k8s-resource-files/"

class FileSystemResourceSetTest {

    private val server = KubernetesServer(false, true)

    @BeforeEach
    fun setUp() {
        server.before()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    @Test
    fun testLoadDeployment() {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = testResourcePath
        resourceSet.files = listOf("test-deployment.yaml")
        assertEquals(1,resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toMutableSet().first().second is Deployment)
    }

    @Test
    fun testLoadService() {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = testResourcePath
        resourceSet.files = listOf("test-service.yaml")
        assertEquals(1,resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toMutableSet().first().second is Service)
    }

    @Test
    fun testLoadStatefulSet() {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = testResourcePath
        resourceSet.files = listOf("test-statefulset.yaml")
        assertEquals(1,resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toMutableSet().first().second is StatefulSet)
    }

    @Test
    fun testLoadConfigMap() {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = testResourcePath
        resourceSet.files = listOf("test-configmap.yaml")
        assertEquals(1,resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toMutableSet().first().second is ConfigMap)
    }

    @Test
    fun testLoadServiceMonitor() {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = testResourcePath
        resourceSet.files = listOf("test-service-monitor.yaml")
        assertEquals(1,resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toMutableSet().first().second is CustomResourceWrapper)
    }

    @Test
    fun testLoadBenchmark() {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = testResourcePath
        resourceSet.files = listOf("test-benchmark.yaml")
        assertEquals(1,resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toMutableSet().first().second is CustomResourceWrapper)
    }

    @Test
    fun testLoadExecution() {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = testResourcePath
        resourceSet.files = listOf("test-execution.yaml")
        assertEquals(1,resourceSet.getResourceSet(server.client).size)
        assertTrue(resourceSet.getResourceSet(server.client).toMutableSet().first().second is CustomResourceWrapper)
    }

    @Test
    fun testFilesNotSet() {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = testResourcePath
        assertEquals(9,resourceSet.getResourceSet(server.client).size)
    }

    @Test
    fun testWrongPath() {
        val resourceSet = FileSystemResourceSet()
        resourceSet.path = "/abc/not-exist"
        assertThrows<DeploymentFailedException> {
            resourceSet.getResourceSet(server.client)
        }
    }
}