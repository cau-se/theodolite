package rocks.theodolite.kubernetes

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext
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
import registerResource
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRDummy
import rocks.theodolite.kubernetes.operator.ExecutionClient
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import java.io.FileInputStream

// TODO move somewhere else
typealias BenchmarkClient = MixedOperation<BenchmarkCRD, KubernetesResourceList<BenchmarkCRD>, Resource<BenchmarkCRD>>

@QuarkusTest
@WithKubernetesTestServer
internal class ConfigMapResourceSetTest {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    private val objectMapper: ObjectMapper = ObjectMapper()

    private lateinit var executionClient: ExecutionClient
    private lateinit var benchmarkClient: BenchmarkClient

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

        this.executionClient = this.server.client.resources(ExecutionCRD::class.java)
        this.benchmarkClient = this.server.client.resources(BenchmarkCRD::class.java)
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    private fun deployAndGetResource(vararg resources: HasMetadata): ConfigMapResourceSet {
        val configMap = ConfigMapBuilder()
            .withNewMetadata().withName("test-configmap").endMetadata()
            .let {
                resources.foldIndexed(it) {
                    i, b, r -> b.addToData("resource_$i.yaml", objectMapper.writeValueAsString(r))
                }
            }
            .build()

        server.client.configMaps().createOrReplace(configMap)

        val resourceSet = ConfigMapResourceSet()
        resourceSet.name = "test-configmap"

        return resourceSet
    }

    @Test
    fun testLoadDeployment() {
        val resource = DeploymentBuilder()
            .withNewSpec()
            .endSpec()
            .withNewMetadata()
            .withName("test-deployment")
            .endMetadata()
            .build()

        val createdResource = deployAndGetResource(resource).getResourceSet(server.client)
        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toList().first().second is Deployment)
        assertTrue(createdResource.toList().first().second.toString().contains(other = resource.metadata.name))
    }

    @Test
    fun testLoadStateFulSet() {
        val resource = StatefulSetBuilder()
            .withNewSpec()
            .endSpec()
            .withNewMetadata()
            .withName("test-sts")
            .endMetadata()
            .build()

        val createdResource = deployAndGetResource(resource).getResourceSet(server.client)
        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toList().first().second is StatefulSet)
        assertTrue(createdResource.toList().first().second.toString().contains(other = resource.metadata.name))
    }

    @Test
    fun testLoadService() {
        val resource = ServiceBuilder()
            .withNewSpec()
            .endSpec()
            .withNewMetadata()
            .withName("test-service")
            .endMetadata()
            .build()

        val createdResource = deployAndGetResource(resource).getResourceSet(server.client)
        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toList().first().second is Service)
        assertTrue(createdResource.toList().first().second.toString().contains(other = resource.metadata.name))
    }

    @Test
    fun testLoadConfigMap() {
        val resource = ConfigMapBuilder()
            .withNewMetadata()
            .withName("test-configmap")
            .endMetadata()
            .build()

        val createdResource = deployAndGetResource(resource).getResourceSet(server.client)
        assertEquals(1, createdResource.size)
        assertTrue(createdResource.toList().first().second is ConfigMap)
        assertTrue(createdResource.toList().first().second.toString().contains(other = resource.metadata.name))
    }

    @Test
    fun testLoadExecution() {
        val stream = javaClass.getResourceAsStream("/k8s-resource-files/test-execution.yaml")
        val execution = this.executionClient.load(stream).get()
        val createdResource = deployAndGetResource(execution).getResourceSet(server.client)

        assertEquals(1, createdResource.size)
        val loadedResource = createdResource.toList().first().second
        assertTrue(loadedResource is ExecutionCRD)
        assertEquals("example-execution", loadedResource.metadata.name)


    }

    @Test
    fun testLoadBenchmark() {
        val benchmark = BenchmarkCRDummy("example-benchmark").getCR()
        val createdResource = deployAndGetResource(benchmark).getResourceSet(server.client)

        assertEquals(1, createdResource.size)
        val loadedResource = createdResource.toList().first().second
        assertTrue(loadedResource is BenchmarkCRD)
        assertEquals("example-benchmark", loadedResource.metadata.name)
    }

    @Test
    fun testLoadServiceMonitor() {
        val serviceMonitorContext = ResourceDefinitionContext.Builder()
            .withGroup("monitoring.coreos.com")
            .withKind("ServiceMonitor")
            .withPlural("servicemonitors")
            .withNamespaced(true)
            .withVersion("v1")
            .build()
        server.registerResource(serviceMonitorContext)

        val stream = javaClass.getResourceAsStream("/k8s-resource-files/test-service-monitor.yaml")
        val serviceMonitor = server.client.load(stream).get()[0]
        val createdResource = deployAndGetResource(serviceMonitor).getResourceSet(server.client)

        assertEquals(1, createdResource.size)
        val loadedResource = createdResource.toList().first().second
        assertTrue(loadedResource is GenericKubernetesResource)
        assertEquals("ServiceMonitor", loadedResource.kind)
        assertEquals("test-service-monitor", loadedResource.metadata.name)
    }

    @Test
    fun testMultipleFiles(){
        val deployment = DeploymentBuilder()
            .withNewSpec()
            .endSpec()
            .withNewMetadata()
            .withName("test-deployment")
            .endMetadata()
            .build()
        val configMap = ConfigMapBuilder()
            .withNewMetadata()
            .withName("test-configmap")
            .endMetadata()
            .build()

        val createdResourceSet = deployAndGetResource(deployment, configMap).getResourceSet(server.client)

        assertEquals(2, createdResourceSet.size )
        assert(createdResourceSet.toList()[0].second is Deployment)
        assert(createdResourceSet.toList()[1].second is ConfigMap)
    }

    @Test
    fun testFilesRestricted() {
        val deployment = DeploymentBuilder()
            .withNewSpec()
            .endSpec()
            .withNewMetadata()
            .withName("test-deployment")
            .endMetadata()
            .build()
        val configMap = ConfigMapBuilder()
            .withNewMetadata()
            .withName("test-configmap")
            .endMetadata()
            .build()

        val createdResourceSet = deployAndGetResource(deployment, configMap)
        val allResources = createdResourceSet.getResourceSet(server.client)
        assertEquals(2, allResources.size)
        createdResourceSet.files = listOf(allResources.first().first) // only select first file from ConfigMa
        val resources = createdResourceSet.getResourceSet(server.client)
        assertEquals(1, resources.size)
        assertTrue(resources.toList().first().second is Deployment)
    }

    @Test
    fun testFileNotExist() {
        val resource = DeploymentBuilder()
            .withNewSpec()
            .endSpec()
            .withNewMetadata()
            .withName("test-deployment")
            .endMetadata()
            .build()

        val resourceSet = deployAndGetResource(resource)
        resourceSet.files = listOf("non-existing-file.yaml")
        assertThrows<DeploymentFailedException> {
            resourceSet.getResourceSet(server.client)
        }
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