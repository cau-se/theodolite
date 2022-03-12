package theodolite.execution.operator

import rocks.theodolite.kubernetes.model.KubernetesBenchmark
import rocks.theodolite.kubernetes.model.crd.BenchmarkCRD
import rocks.theodolite.kubernetes.model.crd.KafkaConfig

class BenchmarkCRDummy(name: String) {

    private val benchmark = KubernetesBenchmark()
    private val benchmarkCR = BenchmarkCRD()

    fun getCR(): BenchmarkCRD {
        return benchmarkCR
    }

    init {
        val kafkaConfig = KafkaConfig()

        kafkaConfig.bootstrapServer = ""
        kafkaConfig.topics = emptyList()

        benchmarkCR.spec = benchmark
        benchmarkCR.metadata.name = name
        benchmarkCR.kind = "Benchmark"
        benchmarkCR.apiVersion = "v1"


        benchmark.infrastructure = KubernetesBenchmark.Resources()
        benchmark.sut = KubernetesBenchmark.Resources()
        benchmark.loadGenerator = KubernetesBenchmark.Resources()

        benchmark.infrastructure.resources = emptyList()
        benchmark.sut.resources = emptyList()
        benchmark.loadGenerator.resources = emptyList()

        benchmark.infrastructure.beforeActions = emptyList()
        benchmark.infrastructure.afterActions = emptyList()
        benchmark.sut.beforeActions = emptyList()
        benchmark.sut.afterActions = emptyList()
        benchmark.loadGenerator.beforeActions = emptyList()
        benchmark.loadGenerator.afterActions = emptyList()

        benchmark.resourceTypes = emptyList()
        benchmark.loadTypes = emptyList()
        benchmark.slos = mutableListOf()
        benchmark.kafkaConfig = kafkaConfig
        benchmark.name = benchmarkCR.metadata.name
    }
}