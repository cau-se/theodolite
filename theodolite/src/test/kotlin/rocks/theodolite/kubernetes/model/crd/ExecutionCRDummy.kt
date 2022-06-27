package rocks.theodolite.kubernetes.model.crd

import rocks.theodolite.kubernetes.model.BenchmarkExecution

class ExecutionCRDummy(name: String, benchmark: String) {

    private val execution = BenchmarkExecution()
    private val executionState = ExecutionStatus()
    private val executionCR = ExecutionCRD()

    fun getCR(): ExecutionCRD {
        return this.executionCR
    }

    fun getStatus() : ExecutionStatus {
        return this.executionState
    }

    init {
        // configure metadata
        executionCR.spec = execution
        executionCR.metadata.name = name
        executionCR.kind = "Execution"
        executionCR.apiVersion = "v1"
        executionCR.status = executionState

        // configure execution
        val loadType = BenchmarkExecution.LoadDefinition()
        loadType.loadType = ""
        loadType.loadValues = emptyList()

        val resourceDef = BenchmarkExecution.ResourceDefinition()
        resourceDef.resourceType = ""
        resourceDef.resourceValues = emptyList()

        val strat = BenchmarkExecution.Strategy()
        strat.name = ""
        strat.restrictions = emptyList()
        strat.guessStrategy = ""
        strat.searchStrategy = ""


        val exec = BenchmarkExecution.Execution()
        exec.afterTeardownDelay = 0
        exec.duration = 0
        exec.loadGenerationDelay = 0
        exec.repetitions = 1
        exec.metric = ""
        exec.strategy = strat


        execution.benchmark = benchmark
        execution.loads = loadType
        execution.resources = resourceDef
        execution.slos = emptyList()
        execution.execution = exec
        execution.configOverrides = mutableListOf()
        execution.name = executionCR.metadata.name

        executionState.executionState = ExecutionState.PENDING
    }
}