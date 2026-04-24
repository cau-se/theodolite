package rocks.theodolite.kubernetes.slo

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark

@QuarkusTest
internal class SloFactoryTest {

    private val sloFactory = SloFactory()

    private val benchmark = KubernetesBenchmark().also { bench ->
        bench.slis = mutableListOf(
            KubernetesBenchmark.Sli().also {
                it.name = "consumerLag"
                it.provider = "prometheus"
                it.query = "sum by(consumergroup) (kafka_consumergroup_lag >= 0)"
            }
        )
        bench.slos = mutableListOf(
            KubernetesBenchmark.Slo().also {
                it.name = "lag trend"
                it.sli = "consumerLag"
                it.warmupSeconds = 60
                it.externalSloChecker = "http://localhost:80/evaluate-slope"
                it.threshold = 2000.0
            }
        )
    }

    @Test
    fun overwriteSloTest() {
        val execution = BenchmarkExecution().also { exec ->
            exec.slos = listOf(
                BenchmarkExecution.SloOverride().also {
                    it.name = "lag trend"
                    it.threshold = 3000.0
                    it.warmupSeconds = 80
                },
                // SLO with name not in benchmark — should be ignored
                BenchmarkExecution.SloOverride().also {
                    it.name = "nonexistent"
                    it.threshold = 999.0
                }
            )
        }

        val sliNames = benchmark.slis.map { it.name }.toSet()
        val combinedSlos = this.sloFactory.createSlos(execution, this.benchmark, sliNames)

        Assertions.assertEquals(1, combinedSlos.size)
        Assertions.assertEquals("lag trend", combinedSlos[0].name)
        Assertions.assertEquals("consumerLag", combinedSlos[0].sli)
        Assertions.assertEquals(80, combinedSlos[0].warmupSeconds)
        Assertions.assertEquals(3000.0, combinedSlos[0].threshold)
        Assertions.assertEquals("http://localhost:80/evaluate-slope", combinedSlos[0].externalSloChecker)
    }

    @Test
    fun unknownSliReferenceThrowsTest() {
        val badBenchmark = KubernetesBenchmark().also { bench ->
            bench.slis = mutableListOf()
            bench.slos = mutableListOf(
                KubernetesBenchmark.Slo().also {
                    it.name = "bad-slo"
                    it.sli = "doesNotExist"
                    it.warmupSeconds = 0
                    it.externalSloChecker = "http://localhost:80"
                    it.threshold = 1.0
                }
            )
        }
        val execution = BenchmarkExecution().also { exec -> exec.slos = null }
        assertThrows<IllegalArgumentException> {
            this.sloFactory.createSlos(execution, badBenchmark, emptySet())
        }
    }
}
