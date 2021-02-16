package theodolite.benchmark

class TheodoliteYamlExecutor {
    fun run() {

        // load the Benchmark context and the benchmark type
        var parser = theodolite.benchmark.BenchmarkYamlParser()
        val benchmarkContext = parser.parse("./../../../resources/main/yaml/testContext.yaml", BenchmarkContext::class.java) !!
        val benchmark = parser.parse("./../../../resources/main/yaml/testBenchmarkType.yaml", KubernetesBenchmark::class.java) !!

        // TheodoliteExecutor benchmarkContext, benchmark
        val executor = TheodoliteBenchmarkExecutor(benchmarkContext, benchmark)
        executor.run()



        System.out.println(benchmark.name)
        System.out.println(benchmarkContext.name)

    }
}