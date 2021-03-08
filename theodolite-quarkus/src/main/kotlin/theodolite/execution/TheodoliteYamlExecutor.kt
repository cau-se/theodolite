package theodolite.execution

import theodolite.benchmark.BenchmarkExecution
import theodolite.benchmark.KubernetesBenchmark
import theodolite.util.YamlParser

class TheodoliteYamlExecutor {
    fun run() {
        // load the BenchmarkExecution and the BenchmarkType
        val parser = YamlParser()
        val benchmarkExecution =
            parser.parse("./../../../resources/main/yaml/testBenchmarkExecution.yaml", BenchmarkExecution::class.java)!!
        val benchmark =
            parser.parse("./../../../resources/main/yaml/testBenchmarkType.yaml", KubernetesBenchmark::class.java)!!

        val executor = TheodoliteExecutor(benchmarkExecution, benchmark)
        executor.run()
    }
}
