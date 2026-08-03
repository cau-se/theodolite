package rocks.theodolite.kubernetes.operator

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.ConfigMapBuilder
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.server.mock.KubernetesServer
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.quarkus.test.junit.QuarkusTest
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
internal class BenchmarkReconcilerTest {

    /** CRUD-mode mock server used for action-command tests that need real k8s objects. */
    private val server = KubernetesServer(false, true)

    private val reconciler = BenchmarkReconciler()

    @BeforeEach
    fun setUp() {
        server.before()
    }

    @AfterEach
    fun tearDown() {
        server.after()
    }

    // ---- reconcile() tests: ConfigMap-based readiness -----------------------------------------

    @Test
    fun `reconcile patches initial PENDING when spec is uninitialized`() {
        val benchmark = BenchmarkCRD()
        benchmark.metadata.name = "bare-benchmark"
        // spec fields are not set (lateinit) – computeReadiness must not throw

        val context = mockContextWith(emptySet())
        val result = reconciler.reconcile(benchmark, context)

        // A freshly applied benchmark has no persisted status (null); desired PENDING is written.
        assertFalse(result.isNoUpdate)
        assertEquals(BenchmarkState.PENDING, result.resource!!.status.resourceSetsState)
    }

    @Test
    fun `reconcile patches initial PENDING when benchmark references no ConfigMaps`() {
        // BenchmarkCRDummy has no resources → empty SUT/loadGen ConfigMap lists → PENDING
        val benchmark = BenchmarkCRDummy("empty-benchmark").getCR()

        val context = mockContextWith(emptySet())
        val result = reconciler.reconcile(benchmark, context)

        assertFalse(result.isNoUpdate)
        assertEquals(BenchmarkState.PENDING, result.resource!!.status.resourceSetsState)
    }

    @Test
    fun `reconcile patches status to READY when all SUT and loadGen ConfigMaps are present`() {
        val benchmark = benchmarkWithConfigMaps(sut = "cm-sut", loadGenerator = "cm-loadgen")

        val context = mockContextWith(setOf(configMap("cm-sut"), configMap("cm-loadgen")))
        val result = reconciler.reconcile(benchmark, context)

        // Desired = READY, current = PENDING (default) → patchStatus
        assertFalse(result.isNoUpdate)
        assertEquals(BenchmarkState.READY, result.resource!!.status.resourceSetsState)
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
    fun `reconcile patches initial PENDING when a referenced ConfigMap is missing`() {
        val benchmark = benchmarkWithConfigMaps(sut = "cm-present", loadGenerator = "cm-missing")

        val context = mockContextWith(setOf(configMap("cm-present")))
        val result = reconciler.reconcile(benchmark, context)

        // Desired = PENDING, current = null (no persisted status) → PENDING is written.
        assertFalse(result.isNoUpdate)
        assertEquals(BenchmarkState.PENDING, result.resource!!.status.resourceSetsState)
    }

    @Test
    fun `reconcile patches status to PENDING when a required ConfigMap disappears`() {
        val benchmark = benchmarkWithConfigMaps(sut = "cm-sut", loadGenerator = "cm-loadgen")
        benchmark.status.resourceSetsState = BenchmarkState.READY

        // loadgen ConfigMap is gone
        val context = mockContextWith(setOf(configMap("cm-sut")))
        val result = reconciler.reconcile(benchmark, context)

        assertFalse(result.isNoUpdate)
        assertEquals(BenchmarkState.PENDING, result.resource!!.status.resourceSetsState)
    }

    @Test
    fun `reconcile patches status to READY for a benchmark with only fileSystem resource sets`() {
        // fileSystem resources are local-pod files; no ConfigMaps → secondary cache is empty but
        // the section is non-empty, so it must be treated as ready.
        val benchmark = BenchmarkCRDummy("fs-benchmark").getCR()
        benchmark.spec.sut.resources = listOf(fileSystemResourceSet("/mnt/sut"))
        benchmark.spec.loadGenerator.resources = listOf(fileSystemResourceSet("/mnt/loadgen"))

        val context = mockContextWith(emptySet())
        val result = reconciler.reconcile(benchmark, context)

        assertFalse(result.isNoUpdate)
        assertEquals(BenchmarkState.READY, result.resource!!.status.resourceSetsState)
    }

    @Test
    fun `reconcile patches status to READY for a mixed ConfigMap and fileSystem benchmark when ConfigMaps are present`() {
        val benchmark = BenchmarkCRDummy("mixed-benchmark").getCR()
        benchmark.spec.sut.resources = listOf(resourceSetsFor("cm-sut"), fileSystemResourceSet("/mnt/extra"))
        benchmark.spec.loadGenerator.resources = listOf(resourceSetsFor("cm-lg"))

        val context = mockContextWith(setOf(configMap("cm-sut"), configMap("cm-lg")))
        val result = reconciler.reconcile(benchmark, context)

        assertFalse(result.isNoUpdate)
        assertEquals(BenchmarkState.READY, result.resource!!.status.resourceSetsState)
    }

    @Test
    fun `reconcile patches initial PENDING for a mixed benchmark when the ConfigMap is missing`() {
        val benchmark = BenchmarkCRDummy("mixed-pending").getCR()
        benchmark.spec.sut.resources = listOf(resourceSetsFor("cm-missing"), fileSystemResourceSet("/mnt/extra"))
        benchmark.spec.loadGenerator.resources = listOf(resourceSetsFor("cm-lg"))

        val context = mockContextWith(setOf(configMap("cm-lg"))) // cm-missing not present
        val result = reconciler.reconcile(benchmark, context)

        assertFalse(result.isNoUpdate)
        assertEquals(BenchmarkState.PENDING, result.resource!!.status.resourceSetsState)
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
