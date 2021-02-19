package theodolite.execution

import theodolite.benchmark.BenchmarkContext
import theodolite.util.YamlParser
import theodolite.benchmark.KubernetesBenchmark

class TheodoliteYamlExecutor {
    fun run() {

        // load the Benchmark context and the benchmark type
        var parser = YamlParser()
        val benchmarkContext = parser.parse("./../../../resources/main/yaml/testContext.yaml", BenchmarkContext::class.java) !!
        val benchmark = parser.parse("./../../../resources/main/yaml/testBenchmarkType.yaml", KubernetesBenchmark::class.java) !!

        // TheodoliteExecutor benchmarkContext, benchmark
        val executor = TheodoliteExecutor(benchmarkContext, benchmark)
        executor.run()
    }
}