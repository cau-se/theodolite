package theodolite.k8s

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.quarkus.test.junit.QuarkusTest
import mu.KotlinLogging
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


private val logger = KotlinLogging.logger {}

@QuarkusTest
@JsonIgnoreProperties(ignoreUnknown = true)
class K8sManagerTest {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private final val server = KubernetesServer(false, true)
    private final val testResourcePath = "./src/test/resources/k8s-resource-files/"

    private final val resourceName = "test-resource"
    private final val metadata: ObjectMeta = ObjectMetaBuilder().withName(resourceName).build()


    val defaultDeployment: Deployment = DeploymentBuilder()
        .withMetadata(metadata)
        .withNewSpec()
        .editOrNewSelector()
        .withMatchLabels<String, String>(mapOf("app" to "test"))
        .endSelector()
        .endSpec()
        .build()

    val defaultStatefulSet: StatefulSet = StatefulSetBuilder()
        .withMetadata(metadata)
        .withNewSpec()
        .editOrNewSelector()
        .withMatchLabels<String, String>(mapOf("app" to "test"))
        .endSelector()
        .endSpec()
        .build()

    val defaultService: Service = ServiceBuilder()
        .withMetadata(metadata)
        .build()

    val defaultConfigMap: ConfigMap = ConfigMapBuilder()
        .withMetadata(metadata)
        .build()

    @BeforeEach
    fun setUp() {
        server.before()

    }

    @AfterEach
    fun tearDown() {
        server.after()

    }

    @Test
    @DisplayName("Test handling of Deployments")
    fun handleDeploymentTest() {
        val manager = K8sManager(server.client)

        manager.deploy(defaultDeployment)
        assertEquals(1, server.client.apps().deployments().list().items.size)
        assertEquals(resourceName, server.client.apps().deployments().list().items.first().metadata.name)

        manager.remove(defaultDeployment)
        assertEquals(0, server.client.apps().deployments().list().items.size)
    }

    @Test
    @DisplayName("Test handling of StatefulSets")
    fun handleStatefulSetTest() {
        val manager = K8sManager(server.client)

        manager.deploy(defaultStatefulSet)
        assertEquals(1, server.client.apps().statefulSets().list().items.size)
        assertEquals(resourceName, server.client.apps().statefulSets().list().items.first().metadata.name)

        manager.remove(defaultStatefulSet)
        assertEquals(0, server.client.apps().statefulSets().list().items.size)
    }

    @Test
    @DisplayName("Test handling of Services")
    fun handleServiceTest() {
        val manager = K8sManager(server.client)

        manager.deploy(defaultService)
        assertEquals(1, server.client.services().list().items.size)
        assertEquals(resourceName, server.client.services().list().items.first().metadata.name)

        manager.remove(defaultService)
        assertEquals(0, server.client.services().list().items.size)
    }


    @Test
    @DisplayName("Test handling of ConfigMaps")
    fun handleConfigMapTest() {
        val manager = K8sManager(server.client)

        manager.deploy(defaultConfigMap)
        assertEquals(1, server.client.configMaps().list().items.size)
        assertEquals(resourceName, server.client.configMaps().list().items.first().metadata.name)

        manager.remove(defaultConfigMap)
        assertEquals(0, server.client.configMaps().list().items.size)
    }

    @Test
    @DisplayName("Test handling of custom resources")
    fun handleCustomResourcesTest() {
        val manager = K8sManager(server.client)
        val servicemonitor = K8sResourceLoader(server.client)
            .loadK8sResource("ServiceMonitor", testResourcePath + "test-service-monitor.yaml")

        val serviceMonitorContext = K8sContextFactory().create(
            api = "v1",
            scope = "Namespaced",
            group = "monitoring.coreos.com",
            plural = "servicemonitors"
        )
        manager.deploy(servicemonitor)

        var serviceMonitors = JSONObject(server.client.customResource(serviceMonitorContext).list())
            .getJSONArray("items")

        assertEquals(1, serviceMonitors.length())
        assertEquals(
            "test-service-monitor",
            serviceMonitors.getJSONObject(0).getJSONObject("metadata").getString("name")
        )

        manager.remove(servicemonitor)

        serviceMonitors = JSONObject(server.client.customResource(serviceMonitorContext).list())
            .getJSONArray("items")

        assertEquals(0, serviceMonitors.length())
    }
}