package theodolite.execution

import theodolite.benchmark.BenchmarkExecution
import theodolite.util.YamlParser
import theodolite.benchmark.KubernetesBenchmark

class TheodoliteYamlExecutor {
    fun run() {
        // load the Benchmark context and the benchmark type
        var parser = YamlParser()
        val benchmarkExecution = parser.parse("./../../../resources/main/yaml/testBenchmarkExecution.yaml", BenchmarkExecution::class.java) !!
        val benchmark = parser.parse("./../../../resources/main/yaml/testBenchmarkType.yaml", KubernetesBenchmark::class.java) !!

        val executor = TheodoliteExecutor(benchmarkExecution, benchmark)
        executor.run()
    }
}