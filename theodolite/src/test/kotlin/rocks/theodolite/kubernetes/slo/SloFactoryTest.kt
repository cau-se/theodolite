package rocks.theodolite.kubernetes.slo

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark

@QuarkusTest
internal class SloFactoryTest {

    private val sloFactory = SloFactory()

    private val benchmark = KubernetesBenchmark().also { bench ->
        bench.slos = mutableListOf(
            KubernetesBenchmark.Slo().also {
                it.name = "test"
                it.sloType = "lag trend"
                it.prometheusUrl = "test.de"
                it.offset = 0
                it.properties = mutableMapOf(
                    "threshold" to "2000",
                    "externalSloUrl" to "http://localhost:80/evaluate-slope",
                    "warmup" to "60"
                )
            }
        )
    }

    @Test
    fun overwriteSloTest() {
        val execution = BenchmarkExecution().also { exec ->
            exec.slos = listOf(
                // SLOs, which should override benchmark SLO values for these properties
                BenchmarkExecution.SloConfiguration(). also {
                    it.name = "test"
                    it.properties = mutableMapOf(
                        // overwriting properties 'threshold' and 'warmup' and adding property 'extensionTest'
                        "threshold" to "3000",
                        "warmup" to "80",
                        "extensionTest" to "extended"
                    )
                },
                // SLO with name that isn't defined in the benchmark, therefore it should be ignored by the SloFactory
                BenchmarkExecution.SloConfiguration().also {
                    it.name = "test2"
                    it.properties = mutableMapOf() // No properties
                }
            )
        }

        val combinedSlos = this.sloFactory.createSlos(execution, this.benchmark)

        Assertions.assertEquals(1, combinedSlos.size)
        Assertions.assertEquals("test", combinedSlos[0].name)
        Assertions.assertEquals("lag trend", combinedSlos[0].sloType)
        Assertions.assertEquals("test.de", combinedSlos[0].prometheusUrl)
        Assertions.assertEquals(0, combinedSlos[0].offset)

        Assertions.assertEquals(4, combinedSlos[0].properties.size)
        Assertions.assertEquals("3000", combinedSlos[0].properties["threshold"])
        Assertions.assertEquals("http://localhost:80/evaluate-slope", combinedSlos[0].properties["externalSloUrl"])
        Assertions.assertEquals("80", combinedSlos[0].properties["warmup"])
        Assertions.assertEquals("extended", combinedSlos[0].properties["extensionTest"])
    }

}