package theodolite.k8s

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.fabric8.kubernetes.client.utils.Utils
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import registerResource


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
        val serviceMonitorContext = ResourceDefinitionContext.Builder()
            .withGroup("monitoring.coreos.com")
            .withKind("ServiceMonitor")
            .withPlural("servicemonitors")
            .withNamespaced(true)
            .withVersion("v1")
            .build()
        server.registerResource(serviceMonitorContext)

        val manager = K8sManager(server.client)

        val serviceMonitorStream = javaClass.getResourceAsStream("/k8s-resource-files/test-service-monitor.yaml")
        // TODO Will be usable with Kubernetes Client 6.0+
        // val serviceMonitorResources = server.client.load(serviceMonitorStream).get()[]
        val serviceMonitorResource = server.client.genericKubernetesResources(serviceMonitorContext).load(serviceMonitorStream).get()

        manager.deploy(serviceMonitorResource)

        val serviceMonitorsDeployed = server.client.genericKubernetesResources(serviceMonitorContext).list()
        assertEquals(1, serviceMonitorsDeployed.items.size)
        assertEquals("test-service-monitor", serviceMonitorsDeployed.items[0].metadata.name)

        manager.remove(serviceMonitorResource)

        val serviceMonitorsDeleted = server.client.genericKubernetesResources(serviceMonitorContext).list()
        assertEquals(0, serviceMonitorsDeleted.items.size)
    }
}