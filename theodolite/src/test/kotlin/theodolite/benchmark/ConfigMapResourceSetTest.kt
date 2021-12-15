package theodolite.benchmark

import com.google.gson.Gson
import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import theodolite.k8s.CustomResourceWrapper
import theodolite.k8s.resourceLoader.K8sResourceLoaderFromFile
import theodolite.util.DeploymentFailedException

private val testResourcePath = "./src/test/resources/k8s-resource-files/"

@QuarkusTest
class ConfigMapResourceSetTest {
    private val server = KubernetesServer(false, true)

    @BeforeEach
    fun setUp() {
        server.before()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    fun deployAndGetResource(resource: String): Collection<Pair<String, KubernetesResource>> {
        val configMap1 = ConfigMapBuilder()
            .withNewMetadata().withName("test-configmap").endMetadata()
            .addToData("test-resource.yaml",resource)
            .build()

        server.client.configMaps().createOrReplace(configMap1)

        val resourceSet = ConfigMapResourceSet()
        resourceSet.name = "test-configmap"

        return resourceSet.getResourceSet(server.client)
    }


    @Test
    fun testLoadDeployment() {
        val resourceBuilder = DeploymentBuilder()
        resourceBuilder.withNewSpec().endSpec()
        resourceBuilder.withNewMetadata().endMetadata()
        val resource = resourceBuilder.build()
        resource.metadata.name = "test-deployment"

        val createdResource = deployAndGetResource(resource = Gson().toJson(resource))
        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toMutableSet().first().second is Deployment)
        assertTrue(createdResource.toMutableSet().first().second.toString().contains(other = resource.metadata.name))
    }

    @Test
    fun testLoadStateFulSet() {
        val resourceBuilder = StatefulSetBuilder()
        resourceBuilder.withNewSpec().endSpec()
        resourceBuilder.withNewMetadata().endMetadata()
        val resource = resourceBuilder.build()
        resource.metadata.name = "test-resource"

        val createdResource = deployAndGetResource(resource = Gson().toJson(resource))
        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toMutableSet().first().second is StatefulSet)
        assertTrue(createdResource.toMutableSet().first().second.toString().contains(other = resource.metadata.name))
    }

    @Test
    fun testLoadService() {
        val resourceBuilder = ServiceBuilder()
        resourceBuilder.withNewSpec().endSpec()
        resourceBuilder.withNewMetadata().endMetadata()
        val resource = resourceBuilder.build()
        resource.metadata.name = "test-resource"

        val createdResource = deployAndGetResource(resource = Gson().toJson(resource))
        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toMutableSet().first().second is Service)
        assertTrue(createdResource.toMutableSet().first().second.toString().contains(other = resource.metadata.name))
    }

    @Test
    fun testLoadConfigMap() {
        val resourceBuilder = ConfigMapBuilder()
        resourceBuilder.withNewMetadata().endMetadata()
        val resource = resourceBuilder.build()
        resource.metadata.name = "test-resource"

        val createdResource = deployAndGetResource(resource = Gson().toJson(resource))
        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toMutableSet().first().second is ConfigMap)
        assertTrue(createdResource.toMutableSet().first().second.toString().contains(other = resource.metadata.name))
    }

    @Test
    fun testLoadExecution() {
        val loader = K8sResourceLoaderFromFile(server.client)
        val resource = loader.loadK8sResource("Execution", testResourcePath + "test-execution.yaml") as CustomResourceWrapper
        val createdResource = deployAndGetResource(resource = Gson().toJson(resource.crAsMap))

        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toMutableSet().first().second is CustomResourceWrapper)

        val loadedResource = createdResource.toMutableSet().first().second
        if (loadedResource is CustomResourceWrapper){
            assertTrue(loadedResource.getName() == "example-execution")
        }
    }

    @Test
    fun testLoadBenchmark() {
        val loader = K8sResourceLoaderFromFile(server.client)
        val resource = loader.loadK8sResource("Benchmark", testResourcePath + "test-benchmark.yaml") as CustomResourceWrapper
        val createdResource = deployAndGetResource(resource = Gson().toJson(resource.crAsMap))

        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toMutableSet().first().second is CustomResourceWrapper)

        val loadedResource = createdResource.toMutableSet().first().second
        if (loadedResource is CustomResourceWrapper){
            assertTrue(loadedResource.getName() == "example-benchmark")
        }
    }

    @Test
    fun testLoadServiceMonitor() {
        val loader = K8sResourceLoaderFromFile(server.client)
        val resource = loader.loadK8sResource("ServiceMonitor", testResourcePath + "test-service-monitor.yaml") as CustomResourceWrapper
        val createdResource = deployAndGetResource(resource = Gson().toJson(resource.crAsMap))

        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toMutableSet().first().second is CustomResourceWrapper)

        val loadedResource = createdResource.toMutableSet().first().second
        if (loadedResource is CustomResourceWrapper){
            assertTrue(loadedResource.getName() == "test-service-monitor")
        }
    }

    @Test
    fun testMultipleFiles(){
        val resourceBuilder = DeploymentBuilder()
        resourceBuilder.withNewSpec().endSpec()
        resourceBuilder.withNewMetadata().endMetadata()
        val resource = resourceBuilder.build()
        resource.metadata.name = "test-deployment"

        val resourceBuilder1 = ConfigMapBuilder()
        resourceBuilder1.withNewMetadata().endMetadata()
        val resource1 = resourceBuilder1.build()
        resource1.metadata.name = "test-configmap"

        val configMap1 = ConfigMapBuilder()
            .withNewMetadata().withName("test-configmap").endMetadata()
            .addToData("test-deployment.yaml",Gson().toJson(resource))
            .addToData("test-configmap.yaml",Gson().toJson(resource1))
            .build()

        server.client.configMaps().createOrReplace(configMap1)

        val resourceSet = ConfigMapResourceSet()
        resourceSet.name = "test-configmap"

        val createdResourcesSet = resourceSet.getResourceSet(server.client)

        assertEquals(2,createdResourcesSet.size )
        assert(createdResourcesSet.toMutableList()[0].second is Deployment)
        assert(createdResourcesSet.toMutableList()[1].second is ConfigMap)
    }

    @Test
    fun testFileIsSet(){
        val resourceBuilder = DeploymentBuilder()
        resourceBuilder.withNewSpec().endSpec()
        resourceBuilder.withNewMetadata().endMetadata()
        val resource = resourceBuilder.build()
        resource.metadata.name = "test-deployment"

        val resourceBuilder1 = ConfigMapBuilder()
        resourceBuilder1.withNewMetadata().endMetadata()
        val resource1 = resourceBuilder1.build()
        resource1.metadata.name = "test-configmap"

        val configMap1 = ConfigMapBuilder()
            .withNewMetadata().withName("test-configmap").endMetadata()
            .addToData("test-deployment.yaml",Gson().toJson(resource))
            .addToData("test-configmap.yaml",Gson().toJson(resource1))
            .build()

        server.client.configMaps().createOrReplace(configMap1)

        val resourceSet = ConfigMapResourceSet()
        resourceSet.name = "test-configmap"
        resourceSet.files = listOf("test-deployment.yaml")

        val createdResourcesSet = resourceSet.getResourceSet(server.client)

        assertEquals(1, createdResourcesSet.size )
        assert(createdResourcesSet.toMutableSet().first().second is Deployment)
    }


    @Test
    fun testConfigMapNotExist() {
        val resourceSet = ConfigMapResourceSet()
        resourceSet.name = "test-configmap1"
        assertThrows<DeploymentFailedException> {
            resourceSet.getResourceSet(server.client)
        }
    }
}