package rocks.theodolite.kubernetes.slo

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@QuarkusTest
internal class SloCheckerFactoryTest {

    private val factory = SloCheckerFactory()

    @Test
    fun testCreateGenericSloWithoutUrl() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "warmup" to "60",
                    "queryAggregation" to "median",
                    "repetitionAggregation" to "median",
                    "operator" to "lte",
                    "threshold" to "1234"
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutWarmup() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "queryAggregation" to "median",
                    "repetitionAggregation" to "median",
                    "operator" to "lte",
                    "threshold" to "1234"
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutQueryAggregation() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                    "repetitionAggregation" to "median",
                    "operator" to "lte",
                    "threshold" to "1234"
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutRepetitionAggregation() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                    "queryAggregation" to "median",
                    "operator" to "lte",
                    "threshold" to "1234"
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutOperator() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                    "queryAggregation" to "median",
                    "repetitionAggregation" to "median",
                    "threshold" to "1234"
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutThreshold() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                    "queryAggregation" to "median",
                    "repetitionAggregation" to "median",
                    "operator" to "lte",
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateGenericSloFloatThreshold() {
        val sloChecker = this.factory.create(
            SloTypes.GENERIC.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "queryAggregation" to "median",
                "repetitionAggregation" to "median",
                "operator" to "lte",
                "threshold" to "12.34"
            ),
            100,
            5
        )
        assertInstanceOf(ExternalSloChecker::class.java, sloChecker)
        val threshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double, "Expected threshold to be Double.")
        assertEquals(12.34, threshold as Double, 0.01)
    }

    @Test
    fun testCreateGenericSloWithThresholdRelToLoad() {
        val sloChecker = this.factory.create(
            SloTypes.GENERIC.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "queryAggregation" to "median",
                "repetitionAggregation" to "median",
                "operator" to "lte",
                "thresholdRelToLoad" to "0.1"
            ),
            100,
            5
        )
        assertTrue(sloChecker is ExternalSloChecker)
        val computedThreshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(computedThreshold is Double)
        assertEquals(10.0, computedThreshold as Double, 0.001)
    }

    @Test
    fun testCreateGenericSloWithThresholdRelToLoadAndInvalidThreshold() {
        val sloChecker = this.factory.create(
            SloTypes.GENERIC.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "queryAggregation" to "median",
                "repetitionAggregation" to "median",
                "operator" to "lte",
                "threshold" to "",
                "thresholdRelToLoad" to "0.1"
            ),
            100,
            5
        )
        assertTrue(sloChecker is ExternalSloChecker)
        val computedThreshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(computedThreshold is Double)
        assertEquals(10.0, computedThreshold as Double, 0.001)
    }

    @Test
    fun testCreateGenericSloWithThresholdRelToResources() {
        val sloChecker = this.factory.create(
            SloTypes.GENERIC.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "queryAggregation" to "median",
                "repetitionAggregation" to "median",
                "operator" to "lte",
                "thresholdRelToResources" to "0.1"
            ),
            100,
            5
        )
        assertTrue(sloChecker is ExternalSloChecker)
        val computedThreshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(computedThreshold is Double)
        assertEquals(0.5, computedThreshold as Double, 0.001)
    }

    @Test
    fun testCreateGenericSloWithConstantThresholdFromExpression() {
        val sloChecker = this.factory.create(
            SloTypes.GENERIC.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "queryAggregation" to "median",
                "repetitionAggregation" to "median",
                "operator" to "lte",
                "thresholdFromExpression" to "1111"
            ),
            8,
            5
        )
        assertTrue(sloChecker is ExternalSloChecker)
        val computedThreshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(computedThreshold is Double)
        assertEquals(1111.0, computedThreshold as Double, 0.001)
    }

    @Test
    fun testCreateGenericSloWithSimpleThresholdFromExpression() {
        val sloChecker = this.factory.create(
            SloTypes.GENERIC.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "queryAggregation" to "median",
                "repetitionAggregation" to "median",
                "operator" to "lte",
                "thresholdFromExpression" to "L*5"
            ),
            8,
            5
        )
        assertTrue(sloChecker is ExternalSloChecker)
        val computedThreshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(computedThreshold is Double)
        assertEquals(40.0, computedThreshold as Double, 0.001)
    }

    @Test
    fun testCreateGenericSloWithComplexThresholdFromExpression() {
        val sloChecker = this.factory.create(
            SloTypes.GENERIC.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "queryAggregation" to "median",
                "repetitionAggregation" to "median",
                "operator" to "lte",
                "thresholdFromExpression" to "R*((2^L+4)-60)+111"
            ),
            8,
            5
        )
        assertTrue(sloChecker is ExternalSloChecker)
        val computedThreshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(computedThreshold is Double)
        assertEquals(1111.0, computedThreshold as Double, 0.001)
    }

    @Test
    fun testCreateLagTrendSloWithoutUrl() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.LAG_TREND.value,
                mapOf(
                    "warmup" to "60",
                    "threshold" to "1234"
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateLagTrendSloWithoutWarmup() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.LAG_TREND.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "threshold" to "1234"
                ),
                100,
                5
            )
        }
    }


    @Test
    fun testCreateLagTrendSloWithoutThreshold() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.LAG_TREND.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateLagTrendSloFloatThreshold() {
        val sloChecker = this.factory.create(
            SloTypes.LAG_TREND.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "threshold" to "12.34"
            ),
            100,
            5
        )
        assertInstanceOf(ExternalSloChecker::class.java, sloChecker)
        val threshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double, "Expected threshold to be Double.")
        assertEquals(12.34, threshold as Double, 0.01)
    }

    @Test
    fun testCreateLagTrendSloWithThresholdRelToLoad() {
        val sloChecker = this.factory.create(
            SloTypes.LAG_TREND.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "thresholdRelToLoad" to "0.1"
            ),
            100,
            5
        )
        assertTrue(sloChecker is ExternalSloChecker)
        val computedThreshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(computedThreshold is Double)
        assertEquals(10.0, computedThreshold as Double, 0.001)
    }

    @Test
    fun testCreateLagTrendSloWithThresholdRelToLoadAndInvalidThreshold() {
        val sloChecker = this.factory.create(
            SloTypes.LAG_TREND.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "threshold" to "",
                "thresholdRelToLoad" to "0.1"
            ),
            100,
            5
        )
        assertTrue(sloChecker is ExternalSloChecker)
        val computedThreshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(computedThreshold is Double)
        assertEquals(10.0, computedThreshold as Double, 0.001)
    }

    @Test
    fun testCreateLagTrendSloWithThresholdRelToResources() {
        val sloChecker = this.factory.create(
            SloTypes.LAG_TREND.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "thresholdRelToResources" to "0.1"
            ),
            100,
            5
        )
        assertTrue(sloChecker is ExternalSloChecker)
        val computedThreshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(computedThreshold is Double)
        assertEquals(0.5, computedThreshold as Double, 0.001)
    }

    @Test
    fun testCreateLagTrendRatioSloWithoutUrl() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.LAG_TREND_RATIO.value,
                mapOf(
                    "warmup" to "60",
                    "ratio" to "0.123"
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateLagTrendRatioSloWithoutWarmup() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.LAG_TREND_RATIO.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "ratio" to "0.123"
                ),
                100,
                5
            )
        }
    }


    @Test
    fun testCreateLagTrendRatioSloWithoutRatioThreshold() {
        assertThrows<IllegalArgumentException> {
            this.factory.create(
                SloTypes.LAG_TREND_RATIO.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                ),
                100,
                5
            )
        }
    }

    @Test
    fun testCreateLagTrendRatioSloFloatThreshold() {
        val sloChecker = this.factory.create(
            SloTypes.LAG_TREND_RATIO.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "ratio" to "0.123"
            ),
            100,
            5
        )
        assertInstanceOf(ExternalSloChecker::class.java, sloChecker)
        val threshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double, "Expected threshold to be Double.")
        assertEquals(12.3, threshold as Double, 0.01)
    }
}