package theodolite.benchmark

class TheodoliteYamlExecutor {
    fun run() {
        val parser = BenchmarkYamlParser<KubernetesBenchmark>()
        val benchmark= parser.parse("./../../../resources/main/yaml/test.yaml")
        System.out.println(benchmark.name)
    }
}