package rocks.theodolite.kubernetes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.WatcherException
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.KubernetesBenchmark


private val logger = KotlinLogging.logger {}

@QuarkusTest
@WithKubernetesTestServer()
class BenchmarkResourceTeardownTest {

    @KubernetesTestServer
    private lateinit var server: KubernetesServer

    private val objectMapper: ObjectMapper = ObjectMapper(YAMLFactory())

    val addedOrder: MutableList<String> = mutableListOf()
    val deletedOrder: MutableList<String> = mutableListOf()

    @BeforeEach
    fun setUp() {
        server.before()

        server.client.resources(Deployment::class.java).watch(object : Watcher<Deployment> {
            override fun eventReceived(p0: Watcher.Action?, p1: Deployment?) {
                if (p0 != null && p1 != null) {
                    if (p0.name == "ADDED") {
                        addedOrder.add(p1.metadata.name)
                    } else if (p0.name == "DELETED") {
                        deletedOrder.add(p1.metadata.name)
                    }
                }
            }

            override fun onClose(p0: WatcherException?) {
                logger.info { p0 }
            }

        })
        server.client.resources(Service::class.java).watch(object : Watcher<Service> {
            override fun eventReceived(p0: Watcher.Action?, p1: Service?) {
                if (p0 != null && p1 != null) {
                    if (p0.name == "ADDED") {
                        addedOrder.add(p1.metadata.name)
                    } else if (p0.name == "DELETED") {
                        deletedOrder.add(p1.metadata.name)
                    }
                }
            }

            override fun onClose(p0: WatcherException?) {
                logger.info { p0 }
            }
        })
    }

    @AfterEach
    fun tearDown() {
        server.after()
        addedOrder.clear()
        deletedOrder.clear()
    }

    fun setupResources(benchmarkPath: String): BenchmarkDeployment {
        val benchmark: KubernetesBenchmark =
            objectMapper.readValue(javaClass.getResourceAsStream(benchmarkPath), KubernetesBenchmark::class.java)

        val sut1ConfigMap = objectMapper.readValue(
            javaClass.getResourceAsStream("/resource-teardown-test-files/sut-configmap-1.yaml"),
            ConfigMap::class.java
        )
        server.client.configMaps().createOrReplace(sut1ConfigMap)

        val sut2ConfigMap = objectMapper.readValue(
            javaClass.getResourceAsStream("/resource-teardown-test-files/sut-configmap-2.yaml"),
            ConfigMap::class.java
        )
        server.client.configMaps().createOrReplace(sut2ConfigMap)

        val loadgenConfigMap = objectMapper.readValue(
            javaClass.getResourceAsStream("/resource-teardown-test-files/load-generator-configmap.yaml"),
            ConfigMap::class.java
        )
        server.client.configMaps().createOrReplace(loadgenConfigMap)

        return KubernetesBenchmarkDeploymentBuilder(benchmark, server.client).buildDeployment(
            0,
            emptyList(),
            1,
            emptyList(),
            emptyList(),
            2,
            2,
            false
        )
    }

    @Test
    fun testFullFilesProperties() {
        // Make sure lists are cleared for the current test.
        assertTrue(addedOrder.size == 0)
        assertTrue(deletedOrder.size == 0)

        // Setup
        val benchmarkDeployment =
            setupResources("/resource-teardown-test-files/example-benchmark-full-files-property.yaml")

        // Run code of interest
        benchmarkDeployment.setup()
        benchmarkDeployment.teardown()

        // Assertions
        assertTrue(addedOrder.size > 0)
        assertTrue(deletedOrder.size > 0)
        assertTrue(addedOrder == deletedOrder.reversed())
    }

    @Test
    fun testPartialFilesProperties() {
        // Make sure lists are cleared for the current test.
        assertTrue(addedOrder.size == 0)
        assertTrue(deletedOrder.size == 0)

        // Setup
        val benchmarkDeployment =
            setupResources("/resource-teardown-test-files/example-benchmark-partial-files-property.yaml")

        // Run code of interest
        benchmarkDeployment.setup()
        benchmarkDeployment.teardown()

        // Assertions
        assertTrue(addedOrder.size > 0)
        assertTrue(deletedOrder.size > 0)
        assertTrue(addedOrder == deletedOrder.reversed())
    }

    @Test
    fun testNoFilesProperties() {
        // Make sure lists are cleared for the current test.
        assertTrue(addedOrder.size == 0)
        assertTrue(deletedOrder.size == 0)

        // Setup
        val benchmarkDeployment =
            setupResources("/resource-teardown-test-files/example-benchmark-no-files-property.yaml")

        // Run code of interest
        benchmarkDeployment.setup()
        benchmarkDeployment.teardown()

        // Assertions
        assertTrue(addedOrder.size > 0)
        assertTrue(deletedOrder.size > 0)
        assertTrue(addedOrder == deletedOrder.reversed())
    }
}
