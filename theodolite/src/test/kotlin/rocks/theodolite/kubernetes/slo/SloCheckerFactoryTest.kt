package rocks.theodolite.kubernetes.slo

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rocks.theodolite.kubernetes.model.KubernetesBenchmark

private fun slo(
    name: String = "test-slo",
    sli: String = "test-sli",
    warmupSeconds: Int = 60,
    externalSloChecker: String? = "http://localhost:1234",
    threshold: Double? = null,
    thresholdRelToLoad: Double? = null,
    thresholdRelToResources: Double? = null,
    thresholdFromExpression: String? = null,
    queryAggregation: String? = "median",
    repetitionAggregation: String? = "median",
    operator: String? = "lte"
): KubernetesBenchmark.Slo = KubernetesBenchmark.Slo().also {
    it.name = name
    it.sli = sli
    it.warmupSeconds = warmupSeconds
    it.externalSloChecker = externalSloChecker
    it.threshold = threshold
    it.thresholdRelToLoad = thresholdRelToLoad
    it.thresholdRelToResources = thresholdRelToResources
    it.thresholdFromExpression = thresholdFromExpression
    it.queryAggregation = queryAggregation
    it.repetitionAggregation = repetitionAggregation
    it.operator = operator
}

@QuarkusTest
internal class SloCheckerFactoryTest {

    private val factory = SloCheckerFactory()

    @Test
    fun testMissingUrlThrows() {
        assertThrows<IllegalArgumentException> {
            factory.create(slo(externalSloChecker = null, threshold = 1234.0), 100, 5)
        }
    }

    @Test
    fun testMissingThresholdThrows() {
        assertThrows<IllegalArgumentException> {
            factory.create(slo(threshold = null, thresholdRelToLoad = null, thresholdRelToResources = null, thresholdFromExpression = null), 100, 5)
        }
    }

    @Test
    fun testFixedThreshold() {
        val checker = factory.create(slo(threshold = 12.34), 100, 5)
        assertInstanceOf(ExternalSloChecker::class.java, checker)
        val threshold = (checker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double)
        assertEquals(12.34, threshold as Double, 0.01)
    }

    @Test
    fun testThresholdRelToLoad() {
        val checker = factory.create(slo(thresholdRelToLoad = 0.1), 100, 5)
        assertTrue(checker is ExternalSloChecker)
        val threshold = (checker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double)
        assertEquals(10.0, threshold as Double, 0.001)
    }

    @Test
    fun testThresholdRelToResources() {
        val checker = factory.create(slo(thresholdRelToResources = 0.1), 100, 5)
        assertTrue(checker is ExternalSloChecker)
        val threshold = (checker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double)
        assertEquals(0.5, threshold as Double, 0.001)
    }

    @Test
    fun testThresholdFromConstantExpression() {
        val checker = factory.create(slo(thresholdFromExpression = "1111"), 8, 5)
        assertTrue(checker is ExternalSloChecker)
        val threshold = (checker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double)
        assertEquals(1111.0, threshold as Double, 0.001)
    }

    @Test
    fun testThresholdFromExpressionWithVariables() {
        val checker = factory.create(slo(thresholdFromExpression = "L*5"), 8, 5)
        assertTrue(checker is ExternalSloChecker)
        val threshold = (checker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double)
        assertEquals(40.0, threshold as Double, 0.001)
    }

    @Test
    fun testThresholdFromComplexExpression() {
        val checker = factory.create(slo(thresholdFromExpression = "R*((2^L+4)-60)+111"), 8, 5)
        assertTrue(checker is ExternalSloChecker)
        val threshold = (checker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double)
        assertEquals(1111.0, threshold as Double, 0.001)
    }

    @Test
    fun testMetadataContainsWarmupSeconds() {
        val checker = factory.create(slo(threshold = 100.0, warmupSeconds = 42), 10, 2)
        assertTrue(checker is ExternalSloChecker)
        val metadata = (checker as ExternalSloChecker).metadata
        assertTrue(metadata.containsKey("warmupSeconds"))
        assertEquals(42, metadata["warmupSeconds"])
        assertFalse(metadata.containsKey("warmup"), "Old 'warmup' key must not be present")
    }

    @Test
    fun testMetadataOptionalFields() {
        val checker = factory.create(
            slo(threshold = 100.0, queryAggregation = "max", repetitionAggregation = "median", operator = "lte"),
            10, 2
        )
        val metadata = (checker as ExternalSloChecker).metadata
        assertEquals("max", metadata["queryAggregation"])
        assertEquals("median", metadata["repetitionAggregation"])
        assertEquals("lte", metadata["operator"])
    }
}
