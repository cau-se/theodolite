package theodolite.k8s

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@QuarkusTest
class K8sResourceLoaderTest {
    private final val server = KubernetesServer(false, true)
    private final val testResourcePath = "./src/test/resources/k8s-resource-files/"

    @BeforeEach
    fun setUp() {
        server.before()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    @Test
    @DisplayName("Test loading of Deployments")
    fun loadDeploymentTest() {
        val loader = K8sResourceLoader(server.client)
        val resource = loader.loadK8sResource("Deployment", testResourcePath + "test-deployment.yaml")

        assertTrue(resource is Deployment)
        assertTrue(resource.toString().contains("name=test-deployment"))
    }

    @Test
    @DisplayName("Test loading of StatefulSet")
    fun loadStatefulSetTest() {
        val loader = K8sResourceLoader(server.client)
        val resource = loader.loadK8sResource("StatefulSet", testResourcePath + "test-statefulset.yaml")

        assertTrue(resource is StatefulSet)
        assertTrue(resource.toString().contains("name=test-statefulset"))
    }

    @Test
    @DisplayName("Test loading of Service")
    fun loadServiceTest() {
        val loader = K8sResourceLoader(server.client)
        val resource = loader.loadK8sResource("Service", testResourcePath + "test-service.yaml")

        assertTrue(resource is Service)
        assertTrue(resource.toString().contains("name=test-service"))
    }

    @Test
    @DisplayName("Test loading of ConfigMap")
    fun loadConfigMapTest() {
        val loader = K8sResourceLoader(server.client)
        val resource = loader.loadK8sResource("ConfigMap", testResourcePath + "test-configmap.yaml")

        assertTrue(resource is ConfigMap)
        assertTrue(resource.toString().contains("name=test-configmap"))
    }

    @Test
    @DisplayName("Test loading of ServiceMonitors")
    fun loadServiceMonitorTest() {
        val loader = K8sResourceLoader(server.client)
        val resource = loader.loadK8sResource("ServiceMonitor", testResourcePath + "test-service-monitor.yaml")

        assertTrue(resource is CustomResourceWrapper)
        if (resource is CustomResourceWrapper) {
            assertEquals("test-service-monitor", resource.getName())

        }
    }

    @Test
    @DisplayName("Test loading of Executions")
    fun loadExecutionTest() {
        val loader = K8sResourceLoader(server.client)
        val resource = loader.loadK8sResource("Execution", testResourcePath + "test-execution.yaml")

        assertTrue(resource is CustomResourceWrapper)
        if (resource is CustomResourceWrapper) {
            assertEquals("example-execution", resource.getName())

        }
    }

    @Test
    @DisplayName("Test loading of Benchmarks")
    fun loadBenchmarkTest() {
        val loader = K8sResourceLoader(server.client)
        val resource = loader.loadK8sResource("Benchmark", testResourcePath + "test-benchmark.yaml")

        assertTrue(resource is CustomResourceWrapper)
        if (resource is CustomResourceWrapper) {
            assertEquals("example-benchmark", resource.getName())

        }
    }

}