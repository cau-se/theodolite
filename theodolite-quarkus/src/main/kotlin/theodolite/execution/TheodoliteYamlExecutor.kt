package theodolite.execution

import theodolite.benchmark.BenchmarkExecution
import theodolite.util.YamlParser
import theodolite.benchmark.KubernetesBenchmark

class TheodoliteYamlExecutor {
    fun run() {
        // load the BenchmarkExecution and the BenchmarkType
        var parser = YamlParser()
        val benchmarkExecution = parser.parse("./../../../resources/main/yaml/testBenchmarkExecution.yaml", BenchmarkExecution::class.java) !!
        val benchmark = parser.parse("./../../../resources/main/yaml/testBenchmarkType.yaml", KubernetesBenchmark::class.java) !!

        val executor = TheodoliteExecutor(benchmarkExecution, benchmark)
        executor.run()
    }
}