package rocks.theodolite.kubernetes.operator

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.ConfigMapBuilder
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import rocks.theodolite.kubernetes.ConfigMapResourceSet
import rocks.theodolite.kubernetes.ResourceSets
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRDummy
import rocks.theodolite.kubernetes.model.crd.BenchmarkState

@QuarkusTest
internal class BenchmarkReconcilerTest {

    private val reconciler = BenchmarkReconciler()

    @Test
    fun `reconcile returns noUpdate when spec is uninitialized`() {
        val benchmark = BenchmarkCRD()
        benchmark.metadata.name = "bare-benchmark"
        // spec fields are not set (lateinit) – configMapNamesOf must not throw

        @Suppress("UNCHECKED_CAST")
        val context: Context<BenchmarkCRD> = mock()
        whenever(context.getSecondaryResources(ConfigMap::class.java)).thenReturn(emptySet())

        val result = reconciler.reconcile(benchmark, context)

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile returns noUpdate when benchmark references no ConfigMaps`() {
        val benchmark = BenchmarkCRDummy("empty-benchmark").getCR()

        @Suppress("UNCHECKED_CAST")
        val context: Context<BenchmarkCRD> = mock()
        whenever(context.getSecondaryResources(ConfigMap::class.java)).thenReturn(emptySet())

        val result = reconciler.reconcile(benchmark, context)

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile returns noUpdate when all referenced ConfigMaps are present`() {
        val benchmark = benchmarkWithConfigMaps(sut = "cm-sut", loadGenerator = "cm-loadgen")

        @Suppress("UNCHECKED_CAST")
        val context: Context<BenchmarkCRD> = mock()
        whenever(context.getSecondaryResources(ConfigMap::class.java))
            .thenReturn(setOf(configMap("cm-sut"), configMap("cm-loadgen")))

        val result = reconciler.reconcile(benchmark, context)

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `reconcile returns noUpdate when a referenced ConfigMap is missing`() {
        val benchmark = benchmarkWithConfigMaps(sut = "cm-present", loadGenerator = "cm-missing")

        @Suppress("UNCHECKED_CAST")
        val context: Context<BenchmarkCRD> = mock()
        whenever(context.getSecondaryResources(ConfigMap::class.java))
            .thenReturn(setOf(configMap("cm-present")))

        val result = reconciler.reconcile(benchmark, context)

        assertTrue(result.isNoUpdate)
    }

    @Test
    fun `configMapNamesOf returns empty set for uninitialized spec`() {
        val benchmark = BenchmarkCRD()
        benchmark.metadata.name = "bare"

        val names = reconciler.configMapNamesOf(benchmark)

        assertTrue(names.isEmpty())
    }

    @Test
    fun `configMapNamesOf returns empty set when no ConfigMaps are referenced`() {
        val benchmark = BenchmarkCRDummy("no-cms").getCR()

        val names = reconciler.configMapNamesOf(benchmark)

        assertTrue(names.isEmpty())
    }

    @Test
    fun `configMapNamesOf collects names from sut, loadGenerator, and infrastructure`() {
        val benchmark = benchmarkWithConfigMaps(
            sut = "cm-sut",
            loadGenerator = "cm-loadgen",
            infrastructure = "cm-infra"
        )

        val names = reconciler.configMapNamesOf(benchmark)

        assertEquals(setOf("cm-sut", "cm-loadgen", "cm-infra"), names)
    }

    @Test
    fun `configMapNamesOf deduplicates names referenced in multiple resource sections`() {
        val benchmark = benchmarkWithConfigMaps(sut = "shared-cm", loadGenerator = "shared-cm")

        val names = reconciler.configMapNamesOf(benchmark)

        assertEquals(setOf("shared-cm"), names)
    }

    private fun benchmarkWithConfigMaps(
        sut: String? = null,
        loadGenerator: String? = null,
        infrastructure: String? = null
    ): BenchmarkCRD {
        val base = BenchmarkCRDummy("test-benchmark").getCR()

        fun resourceSetsFor(name: String): ResourceSets {
            val cms = ConfigMapResourceSet()
            cms.name = name
            return ResourceSets().also { it.configMap = cms }
        }

        val spec = base.spec
        spec.sut.resources = listOfNotNull(sut?.let { resourceSetsFor(it) })
        spec.loadGenerator.resources = listOfNotNull(loadGenerator?.let { resourceSetsFor(it) })
        spec.infrastructure.resources = listOfNotNull(infrastructure?.let { resourceSetsFor(it) })

        return base
    }

    private fun configMap(name: String): ConfigMap =
        ConfigMapBuilder().withNewMetadata().withName(name).endMetadata().build()
}
