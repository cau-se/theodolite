package theodolite

class BenchmarkExecutor(benchmark: Benchmark) {
    val benchmark: Benchmark = benchmark

    fun waitExecution(executionMinutes: Int) {
    val milliToMinutes = 60000
    System.out.println("Wait while executing")
    for (i in 1.rangeTo(executionMinutes)) {
       Thread.sleep((milliToMinutes * i).toLong())
       System.out.println("Executed: $i minutes")
    }

    System.out.println("Execution finished")
 }
    fun runExperiment() {}
}
