package rocks.theodolite.kubernetes.operator

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.ConfigMapBuilder
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kubernetes.client.KubernetesServer
import io.quarkus.test.kubernetes.client.KubernetesTestServer
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import rocks.theodolite.kubernetes.ConfigMapResourceSet
import rocks.theodolite.kubernetes.FileSystemResourceSet
import rocks.theodolite.kubernetes.ExecActionSelector
import rocks.theodolite.kubernetes.PodSelector
import rocks.theodolite.kubernetes.ResourceSets
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRDummy
import rocks.theodolite.kubernetes.model.crd.BenchmarkState

@QuarkusTest
@WithKubernetesTestServer(crud = true, https = false)
internal class BenchmarkReconcilerTest {

    private companion object {
        const val NAMESPACE = "test"
    }

    @KubernetesTestServer
    lateinit var server: KubernetesServer
    private val reconciler = BenchmarkReconciler()

    @BeforeEach
    fun setUp() {
        server.kubernetesMockServer.expectCustomResource(
            CustomResourceDefinitionContext.fromCustomResourceType(BenchmarkCRD::class.java)
        )
        // The reconciler persists status through the fabric8 client (patchState workaround).
        reconciler.client = server.client
        // Open by default so existing tests exercise reconcile() as if this were the leader.
        reconciler.readiness = OperatorReadiness().apply { open() }
    }

    // The Quarkus test server is shared across the class; reset CRUD state for per-test isolation.
    @AfterEach
    fun tearDown() {
        server.kubernetesMockServer.reset()
    }

    // ---- reconcile() tests: ConfigMap-based readiness -----------------------------------------

    @Test
    fun `reconcile does not throw when spec is uninitialized`() {
        val benchmark = BenchmarkCRD()
        benchmark.metadata.name = "bare-benchmark"
        benchmark.metadata.namespace = NAMESPACE
        // spec fields are not set (lateinit) – computeReadiness must not throw and, since the CR
        // is not present in the cluster, patchState must be a safe no-op.

        val result = reconciler.reconcile(benchmark, mockContextWith(emptySet()))

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile persists initial PENDING when benchmark references no ConfigMaps`() {
        // BenchmarkCRDummy has no resources → empty SUT/loadGen ConfigMap lists → PENDING
        val benchmark = BenchmarkCRDummy("empty-benchmark").getCR()
        benchmark.metadata.namespace = NAMESPACE
        createOnServer(benchmark)

        reconciler.reconcile(benchmark, mockContextWith(emptySet()))

        assertEquals(BenchmarkState.PENDING, persistedState("empty-benchmark"))
    }

    @Test
    fun `computeReadiness is READY when all SUT and loadGen ConfigMaps are present`() {
        val benchmark = benchmarkWithConfigMaps(sut = "cm-sut", loadGenerator = "cm-loadgen")

        val context = mockContextWith(setOf(configMap("cm-sut"), configMap("cm-loadgen")))

        assertEquals(BenchmarkState.READY, reconciler.computeReadiness(benchmark, context))
    }

    @Test
    fun `reconcile returns noUpdate when already READY and all ConfigMaps still present`() {
        val benchmark = benchmarkWithConfigMaps(sut = "cm-sut", loadGenerator = "cm-loadgen")
        benchmark.status.resourceSetsState = BenchmarkState.READY

        val context = mockContextWith(setOf(configMap("cm-sut"), configMap("cm-loadgen")))
        val result = reconciler.reconcile(benchmark, context)

        // Desired = READY, current = READY → no status change
        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile persists PENDING when a referenced ConfigMap is missing`() {
        val benchmark = benchmarkWithConfigMaps(sut = "cm-present", loadGenerator = "cm-missing")
        createOnServer(benchmark)

        reconciler.reconcile(benchmark, mockContextWith(setOf(configMap("cm-present"))))

        // Desired = PENDING, no persisted status yet → PENDING must be written.
        assertEquals(BenchmarkState.PENDING, persistedState(benchmark.metadata.name))
    }

    @Test
    fun `computeReadiness is PENDING when a required ConfigMap is missing`() {
        val benchmark = benchmarkWithConfigMaps(sut = "cm-sut", loadGenerator = "cm-loadgen")

        // loadgen ConfigMap is gone
        val context = mockContextWith(setOf(configMap("cm-sut")))

        assertEquals(BenchmarkState.PENDING, reconciler.computeReadiness(benchmark, context))
    }

    @Test
    fun `computeReadiness is READY for a benchmark with only fileSystem resource sets`() {
        // fileSystem resources are local-pod files; no ConfigMaps → secondary cache is empty but
        // the section is non-empty, so it must be treated as ready.
        val benchmark = BenchmarkCRDummy("fs-benchmark").getCR()
        benchmark.spec.sut.resources = listOf(fileSystemResourceSet("/mnt/sut"))
        benchmark.spec.loadGenerator.resources = listOf(fileSystemResourceSet("/mnt/loadgen"))

        val context = mockContextWith(emptySet())

        assertEquals(BenchmarkState.READY, reconciler.computeReadiness(benchmark, context))
    }

    @Test
    fun `computeReadiness is READY for a mixed ConfigMap and fileSystem benchmark when ConfigMaps are present`() {
        val benchmark = BenchmarkCRDummy("mixed-benchmark").getCR()
        benchmark.spec.sut.resources = listOf(resourceSetsFor("cm-sut"), fileSystemResourceSet("/mnt/extra"))
        benchmark.spec.loadGenerator.resources = listOf(resourceSetsFor("cm-lg"))

        val context = mockContextWith(setOf(configMap("cm-sut"), configMap("cm-lg")))

        assertEquals(BenchmarkState.READY, reconciler.computeReadiness(benchmark, context))
    }

    @Test
    fun `reconcile persists PENDING for a mixed benchmark when the ConfigMap is missing`() {
        val benchmark = BenchmarkCRDummy("mixed-pending").getCR()
        benchmark.metadata.namespace = NAMESPACE
        benchmark.spec.sut.resources = listOf(resourceSetsFor("cm-missing"), fileSystemResourceSet("/mnt/extra"))
        benchmark.spec.loadGenerator.resources = listOf(resourceSetsFor("cm-lg"))
        createOnServer(benchmark)

        val context = mockContextWith(setOf(configMap("cm-lg"))) // cm-missing not present
        reconciler.reconcile(benchmark, context)

        assertEquals(BenchmarkState.PENDING, persistedState("mixed-pending"))
    }

    // ---- OperatorReadiness gate -----------------------------------------------------------

    @Test
    fun `reconcile does nothing and reschedules while the operator readiness gate is closed`() {
        val benchmark = BenchmarkCRDummy("empty-benchmark").getCR()
        benchmark.metadata.namespace = NAMESPACE
        createOnServer(benchmark)
        reconciler.readiness = OperatorReadiness() // closed: simulates a non-leader replica

        val result = reconciler.reconcile(benchmark, mockContextWith(emptySet()))

        assertTrue(result.isNoUpdate)
        assertEquals(2000L, result.scheduleDelay.get())
        assertEquals(null, persistedState("empty-benchmark"))
    }

    @Test
    fun `reconcile becomes a no-op and reschedules once a previously open gate is closed`() {
        // Simulates this replica losing leadership after having been the leader: the desired
        // state (READY) must not be written once the gate closes.
        val benchmark = benchmarkWithConfigMaps(sut = "cm-sut", loadGenerator = "cm-loadgen")
        benchmark.status.resourceSetsState = BenchmarkState.PENDING
        createOnServer(benchmark)
        reconciler.readiness.close()

        val context = mockContextWith(setOf(configMap("cm-sut"), configMap("cm-loadgen")))
        val result = reconciler.reconcile(benchmark, context)

        assertTrue(result.isNoUpdate)
        assertEquals(2000L, result.scheduleDelay.get())
        assertEquals(BenchmarkState.PENDING, persistedState(benchmark.metadata.name))
    }

    // ---- configMapNamesOf() tests --------------------------------------------------------------

    @Test
    fun `configMapNamesOf returns empty set for uninitialized spec`() {
        val benchmark = BenchmarkCRD()
        benchmark.metadata.name = "bare"

        assertTrue(reconciler.configMapNamesOf(benchmark).isEmpty())
    }

    @Test
    fun `configMapNamesOf returns empty set when no ConfigMaps are referenced`() {
        val benchmark = BenchmarkCRDummy("no-cms").getCR()

        assertTrue(reconciler.configMapNamesOf(benchmark).isEmpty())
    }

    @Test
    fun `configMapNamesOf collects names from sut, loadGenerator, and infrastructure`() {
        val benchmark = benchmarkWithConfigMaps(
            sut = "cm-sut",
            loadGenerator = "cm-loadgen",
            infrastructure = "cm-infra"
        )

        assertEquals(setOf("cm-sut", "cm-loadgen", "cm-infra"), reconciler.configMapNamesOf(benchmark))
    }

    @Test
    fun `configMapNamesOf deduplicates names referenced in multiple resource sections`() {
        val benchmark = benchmarkWithConfigMaps(sut = "shared-cm", loadGenerator = "shared-cm")

        assertEquals(setOf("shared-cm"), reconciler.configMapNamesOf(benchmark))
    }

    // ---- checkIfResourceIsDeployed() tests ----------------------------------------------------

    @Test
    fun `checkIfResourceIsDeployed returns true when a pod with matching labels is running`() {
        val pod = PodBuilder()
            .withNewMetadata().withName("pod1").withNamespace("test")
            .withLabels<String, String>(mapOf("app" to "pod1")).endMetadata()
            .withNewSpec().endSpec()
            .build()
        server.client.pods().inNamespace("test").create(pod)

        assertTrue(reconciler.checkIfResourceIsDeployed(actionSelector("app" to "pod1"), server.client))
    }

    @Test
    fun `checkIfResourceIsDeployed returns false when no pod with matching labels exists`() {
        assertFalse(reconciler.checkIfResourceIsDeployed(actionSelector("app" to "no-such-pod"), server.client))
    }

    // ---- checkIfResourceIsInfrastructure() tests ----------------------------------------------

    @Test
    fun `checkIfResourceIsInfrastructure returns true when infra contains a Deployment matching the selector`() {
        val resourceSets = listOf(deploymentConfigMapResourceSet("app" to "pod1"))

        assertTrue(
            reconciler.checkIfResourceIsInfrastructure(
                resourceSets, actionSelector("app" to "pod1"), server.client
            )
        )
    }

    @Test
    fun `checkIfResourceIsInfrastructure returns false when infra has no Deployment matching the selector`() {
        val resourceSets = listOf(deploymentConfigMapResourceSet("app" to "pod1"))

        assertFalse(
            reconciler.checkIfResourceIsInfrastructure(
                resourceSets, actionSelector("app" to "other-pod"), server.client
            )
        )
    }

    // ---- helpers -------------------------------------------------------------------------------

    /** Creates the benchmark CR in the mock server so the reconciler can patch its status. */
    private fun createOnServer(benchmark: BenchmarkCRD) {
        server.client.resources(BenchmarkCRD::class.java)
            .inNamespace(benchmark.metadata.namespace)
            .resource(benchmark)
            .create()
    }

    /** Reads the persisted `resourceSetsState` of the named benchmark back from the mock server. */
    private fun persistedState(name: String): BenchmarkState? =
        server.client.resources(BenchmarkCRD::class.java)
            .inNamespace(NAMESPACE)
            .withName(name)
            .get()
            ?.status?.resourceSetsState

    @Suppress("UNCHECKED_CAST")
    private fun mockContextWith(configMaps: Set<ConfigMap>): Context<BenchmarkCRD> {
        val context: Context<BenchmarkCRD> = mock()
        whenever(context.getSecondaryResources(ConfigMap::class.java)).thenReturn(configMaps)
        return context
    }

    private fun benchmarkWithConfigMaps(
        sut: String? = null,
        loadGenerator: String? = null,
        infrastructure: String? = null
    ): BenchmarkCRD {
        val base = BenchmarkCRDummy("test-benchmark").getCR()
        base.metadata.namespace = NAMESPACE
        val spec = base.spec
        spec.sut.resources = listOfNotNull(sut?.let { resourceSetsFor(it) })
        spec.loadGenerator.resources = listOfNotNull(loadGenerator?.let { resourceSetsFor(it) })
        spec.infrastructure.resources = listOfNotNull(infrastructure?.let { resourceSetsFor(it) })
        return base
    }

    private fun resourceSetsFor(configMapName: String): ResourceSets {
        val cms = ConfigMapResourceSet()
        cms.name = configMapName
        return ResourceSets().also { it.configMap = cms }
    }

    private fun fileSystemResourceSet(path: String): ResourceSets {
        val fs = FileSystemResourceSet()
        fs.path = path
        return ResourceSets().also { it.fileSystem = fs }
    }

    private fun configMap(name: String): ConfigMap =
        ConfigMapBuilder().withNewMetadata().withName(name).endMetadata().build()

    private fun actionSelector(label: Pair<String, String>): ExecActionSelector {
        val podSelector = PodSelector()
        podSelector.matchLabels = mutableMapOf(label)
        return ExecActionSelector().also { it.pod = podSelector }
    }

    /**
     * Creates a ConfigMap in the mock CRUD server that contains a single Deployment resource
     * with the given label, then returns a [ResourceSets] pointing to that ConfigMap.
     */
    private fun deploymentConfigMapResourceSet(label: Pair<String, String>): ResourceSets {
        val deployment = DeploymentBuilder()
            .withNewMetadata().withName("infra-deployment")
            .withLabels<String, String>(mapOf(label)).endMetadata()
            .withNewSpec().endSpec()
            .build()
        val deploymentYaml = ObjectMapper().writeValueAsString(deployment)

        val cm = ConfigMapBuilder()
            .withNewMetadata().withName("infra-cm").endMetadata()
            .addToData("deployment.yaml", deploymentYaml)
            .build()
        server.client.configMaps().inNamespace("test").createOrReplace(cm)

        val resourceSet = ConfigMapResourceSet()
        resourceSet.name = "infra-cm"
        return ResourceSets().also { it.configMap = resourceSet }
    }
}
