package rocks.theodolite.kubernetes.operator

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.api.model.ConfigMapBuilder
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.api.model.PodListBuilder
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.fabric8.kubernetes.client.server.mock.OutputStreamMessage
import io.fabric8.kubernetes.client.utils.Utils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.ConfigMapResourceSet
import rocks.theodolite.kubernetes.ExecActionSelector
import rocks.theodolite.kubernetes.PodSelector
import rocks.theodolite.kubernetes.ResourceSets
import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRDummy
import rocks.theodolite.kubernetes.model.crd.BenchmarkState

internal class BenchmarkStateCheckerTest {
    private val server = KubernetesServer(false, false)
    private val serverCrud = KubernetesServer(false, true)
    private lateinit var checker: BenchmarkStateChecker
    private lateinit var checkerCrud: BenchmarkStateChecker

    @BeforeEach
    fun setUp() {
        server.before()
        serverCrud.before()
        val operator = TheodoliteOperator(serverCrud.client)
        checker = BenchmarkStateChecker(
            client = server.client,
            benchmarkCRDClient = operator.getBenchmarkClient(),
            benchmarkStateHandler = operator.getBenchmarkStateHandler()
        )

        checkerCrud = BenchmarkStateChecker(
            client = serverCrud.client,
            benchmarkCRDClient = operator.getBenchmarkClient(),
            benchmarkStateHandler = operator.getBenchmarkStateHandler()
        )

        val pod: Pod = PodBuilder().withNewMetadata()
            .withName("pod1")
            .withResourceVersion("1")
            .withLabels<String, String>(mapOf("app" to "pod"))
            .withNamespace("test").and()
            .build()

        val ready: Pod = createReadyFrom(pod, "True")

        val podList = PodListBuilder().build()
        podList.items.add(0, ready)


        server
            .expect()
            .withPath("/api/v1/namespaces/test/pods?labelSelector=${Utils.toUrlEncoded("app=pod1")}")
            .andReturn(200, podList)
            .always()

        server
            .expect()
            .withPath("/api/v1/namespaces/test/pods?labelSelector=${Utils.toUrlEncoded("app=pod0")}")
            .andReturn(200, emptyMap<String, String>())
            .always()


        server
            .expect()
            .get()
            .withPath("/api/v1/namespaces/test/pods/pod1")
            .andReturn(200, ready)
            .always()

        server
            .expect()
            .withPath("/api/v1/namespaces/test/pods/pod1/exec?command=ls&stdout=true&stderr=true")
            .andUpgradeToWebSocket()
            .open(OutputStreamMessage("Test-Output"))
            .done()
            .always()
    }

    @AfterEach
    fun tearDown() {
        server.after()
        serverCrud.after()
    }

    /**
     * Copied from fabric8 Kubernetes Client repository
     *
     * @param pod
     * @param status
     * @return
     */
    private fun createReadyFrom(pod: Pod, status: String): Pod {
        return PodBuilder(pod)
            .withNewStatus()
            .addNewCondition()
            .withType("Ready")
            .withStatus(status)
            .endCondition()
            .endStatus()
            .build()
    }

    private fun getActionSelector(label: Pair<String, String>): ExecActionSelector {
        val podSelector = PodSelector()
        val actionSelector = ExecActionSelector()
        actionSelector.pod = podSelector

        // pod with matching labels are deployed
        podSelector.matchLabels = mutableMapOf(label)
        return actionSelector
    }

    private fun createAndDeployConfigmapResourceSet(): ResourceSets {
        // create test deployment
        val resourceBuilder = DeploymentBuilder()
        resourceBuilder.withNewSpec().endSpec()
        resourceBuilder.withNewMetadata().endMetadata()
        val resource = resourceBuilder.build()
        resource.metadata.name = "test-deployment"
        resource.metadata.labels = mutableMapOf("app" to "pod1")
        val resourceString = ObjectMapper().writeValueAsString(resource)

        // create and deploy configmap
        val configMap1 = ConfigMapBuilder()
            .withNewMetadata().withName("test-configmap").endMetadata()
            .addToData("test-resource.yaml",resourceString)
            .build()

        serverCrud.client.configMaps().createOrReplace(configMap1)

        // create configmap resource set
        val resourceSet = ConfigMapResourceSet()
        resourceSet.name = "test-configmap"

        // create ResourceSetsList
        val set = ResourceSets()
        set.configMap = resourceSet
        return set
    }

    @Test
    fun checkIfResourceIsDeployed() {
        // pod with matching labels are deployed
        assertTrue(checker.checkIfResourceIsDeployed(getActionSelector("app" to "pod1")))

        // no pod with matching labels are deployed
        assertFalse(checker.checkIfResourceIsDeployed(getActionSelector("app" to "pod0")))
    }

    @Test
    fun checkIfResourceIsInfrastructure() {
        val resourceSets = listOf(createAndDeployConfigmapResourceSet())
        assertTrue(checkerCrud.checkIfResourceIsInfrastructure(resourceSets, getActionSelector("app" to "pod1")))
        assertFalse(checkerCrud.checkIfResourceIsInfrastructure(resourceSets, getActionSelector("app" to "pod0")))

    }

    @Test
    fun checkResources() {
        val benchmarkCR = BenchmarkCRDummy(
            name = "test-benchmark"
        )
        val benchmark = benchmarkCR.getCR().spec

        val resourceSet = KubernetesBenchmark.Resources()
        resourceSet.resources = listOf(createAndDeployConfigmapResourceSet())
        benchmark.infrastructure = resourceSet
        benchmark.loadGenerator = resourceSet
        benchmark.sut = resourceSet

        assertEquals(BenchmarkState.READY,checkerCrud.checkResources(benchmark))
    }
}