package rocks.theodolite.kubernetes.slo

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.KubernetesBenchmark

@QuarkusTest
internal class SloFactoryTest {

    @Test
    fun overwriteSloTest() {

        val benchmark = KubernetesBenchmark()
        val execution = BenchmarkExecution()

        // Define Benchmark SLOs
        val slo = KubernetesBenchmark.Slo()
        slo.name="test"
        slo.sloType="lag trend"
        slo.prometheusUrl="test.de"
        slo.offset=0

        val benchmarkSloProperties = mutableMapOf<String, String>()
        benchmarkSloProperties["threshold"] = "2000"
        benchmarkSloProperties["externalSloUrl"] = "http://localhost:80/evaluate-slope"
        benchmarkSloProperties["warmup"] = "60"

        slo.properties=benchmarkSloProperties

        benchmark.slos = mutableListOf(slo)


        // Define Execution SLOs, benchmark SLO values for these properties should be overwritten
        val sloConfig = BenchmarkExecution.SloConfiguration()
        sloConfig.name = "test"

        val executionSloProperties = mutableMapOf<String, String>()
        // overwriting properties 'threshold' and 'warmup' and adding property 'extensionTest'
        executionSloProperties["threshold"] = "3000"
        executionSloProperties["warmup"] = "80"
        executionSloProperties["extensionTest"] = "extended"

        sloConfig.properties = executionSloProperties

        // SLO has 'name' that isn't defined in the benchmark, therefore it will be ignored by the SloFactory
        val sloConfig2 = BenchmarkExecution.SloConfiguration()
        sloConfig2.name = "test2"
        sloConfig2.properties = executionSloProperties

        execution.slos = listOf(sloConfig, sloConfig2)

        val sloFactory = SloFactory()
        val combinedSlos = sloFactory.createSlos(execution,benchmark)

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