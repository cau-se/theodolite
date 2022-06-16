package rocks.theodolite.kubernetes.slo

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rocks.theodolite.core.strategies.Metric

@QuarkusTest
internal class SloCheckerFactoryTest {

    @Test
    fun testCreateGenericSloWithoutUrl() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "warmup" to "60",
                    "queryAggregation" to "median",
                    "repetitionAggregation" to "median",
                    "operator" to "lte",
                    "threshold" to "1234"
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutWarmup() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "queryAggregation" to "median",
                    "repetitionAggregation" to "median",
                    "operator" to "lte",
                    "threshold" to "1234"
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutQueryAggregation() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                    "repetitionAggregation" to "median",
                    "operator" to "lte",
                    "threshold" to "1234"
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutRepetitionAggregation() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                    "queryAggregation" to "median",
                    "operator" to "lte",
                    "threshold" to "1234"
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutOperator() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                    "queryAggregation" to "median",
                    "repetitionAggregation" to "median",
                    "threshold" to "1234"
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateGenericSloWithoutThreshold() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.GENERIC.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                    "queryAggregation" to "median",
                    "repetitionAggregation" to "median",
                    "operator" to "lte",
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateGenericSloFloatThreshold() {
        val factory = SloCheckerFactory()
        val sloChecker = factory.create(
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
            5,
            Metric.DEMAND
        )
        assertInstanceOf(ExternalSloChecker::class.java, sloChecker)
        val threshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double, "Expected threshold to be Double.")
        assertEquals(12.34, threshold as Double, 0.01)
    }

    @Test
    fun testCreateLagTrendSloWithoutUrl() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.LAG_TREND.value,
                mapOf(
                    "warmup" to "60",
                    "threshold" to "1234"
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateLagTrendSloWithoutWarmup() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.LAG_TREND.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "threshold" to "1234"
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }


    @Test
    fun testCreateLagTrendSloWithoutThreshold() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.LAG_TREND.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateLagTrendSloFloatThreshold() {
        val factory = SloCheckerFactory()
        val sloChecker = factory.create(
            SloTypes.LAG_TREND.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "threshold" to "12.34"
            ),
            100,
            5,
            Metric.DEMAND
        )
        assertInstanceOf(ExternalSloChecker::class.java, sloChecker)
        val threshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double, "Expected threshold to be Double.")
        assertEquals(12.34, threshold as Double, 0.01)
    }

    @Test
    fun testCreateLagTrendRatioSloWithoutUrl() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.LAG_TREND_RATIO.value,
                mapOf(
                    "warmup" to "60",
                    "ratio" to "0.123"
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateLagTrendRatioSloWithoutWarmup() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.LAG_TREND_RATIO.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "ratio" to "0.123"
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }


    @Test
    fun testCreateLagTrendRatioSloWithoutRatioThreshold() {
        val factory = SloCheckerFactory()
        assertThrows<IllegalArgumentException> {
            factory.create(
                SloTypes.LAG_TREND_RATIO.value,
                mapOf(
                    "externalSloUrl" to "http://localhost:1234",
                    "warmup" to "60",
                ),
                100,
                5,
                Metric.DEMAND
            )
        }
    }

    @Test
    fun testCreateLagTrendRatioSloFloatThreshold() {
        val factory = SloCheckerFactory()
        val sloChecker = factory.create(
            SloTypes.LAG_TREND_RATIO.value,
            mapOf(
                "externalSloUrl" to "http://localhost:1234",
                "warmup" to "60",
                "ratio" to "0.123"
            ),
            100,
            5,
            Metric.DEMAND
        )
        assertInstanceOf(ExternalSloChecker::class.java, sloChecker)
        val threshold = (sloChecker as ExternalSloChecker).metadata["threshold"]
        assertTrue(threshold is Double, "Expected threshold to be Double.")
        assertEquals(12.3, threshold as Double, 0.01)
    }
}