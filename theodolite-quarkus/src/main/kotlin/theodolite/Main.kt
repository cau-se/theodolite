package theodolite
import io.quarkus.runtime.annotations.QuarkusMain


@QuarkusMain
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting Benchmarks")

        val config: ExperimentConfig = ExperimentConfig(
            exp_id = 0,
            use_case = 1,
            dim_values = arrayOf(1,2,3),
            configurations = TODO(),
            domain_restriction_strategy = TODO(),
            cpu_limit = "1000m",
            execution_minutes = 5f,
            memory_limit = "4gb",
            namespace = "default",
            partitions = 10,
            prometheus_base_url = "http://localhost:9090",
            replicass = arrayOf(1,2,3),
            reset = false,
            result_path = "./results",
            search_strategy = TODO(),
            subexperiment_evaluator = TODO(),
            subexperiment_executor = TODO()
        )

        val executor: ExperimentExecutor = ExperimentExecutor(config)
        executor.run()
    }
}
