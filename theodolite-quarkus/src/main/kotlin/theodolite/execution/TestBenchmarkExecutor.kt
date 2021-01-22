package theodolite.execution

import theodolite.execution.BenchmarkExecutor
import theodolite.util.LoadDimension
import theodolite.util.Resource

class TestBenchmarkExecutor(val mockResults: Array<Array<Boolean>>): BenchmarkExecutor {

    override fun runExperiment(load: LoadDimension, res: Resource): Boolean {
        System.out.println("load :" + load.get().toString() + ", res: " + res.get().toString())
        return this.mockResults[load.get()][res.get()]
    }
}