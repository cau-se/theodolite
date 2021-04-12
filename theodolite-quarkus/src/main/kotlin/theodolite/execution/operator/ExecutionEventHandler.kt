package theodolite.execution.operator

import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution

private val logger = KotlinLogging.logger {}

class ExecutionHandler(private val controller: TheodoliteController): ResourceEventHandler<BenchmarkExecution> {
    override fun onAdd(execution: BenchmarkExecution) {
        execution.name = execution.metadata.name
        logger.info { "Add new execution ${execution.metadata.name} to queue." }
        this.controller.executionsQueue.add(execution)
    }

    override fun onUpdate(oldExecution: BenchmarkExecution, newExecution: BenchmarkExecution) {
        logger.info { "Add updated execution to queue." }
        newExecution.name = newExecution.metadata.name
        this.controller.executionsQueue.removeIf { e -> e.name == newExecution.metadata.name }
        this.controller.executionsQueue.addFirst(newExecution)
        if (this.controller.isInitialized() &&  this.controller.executor.getExecution().name == newExecution.metadata.name) {
            this.controller.isUpdated.set(true)
            this.controller.executor.executor.run.compareAndSet(true, false)
        }
    }

    override fun onDelete(execution: BenchmarkExecution, b: Boolean) {
        logger.info { "Delete execution ${execution.metadata.name} from queue." }
        this.controller.executionsQueue.removeIf { e -> e.name == execution.metadata.name }
        if (this.controller.isInitialized() && this.controller.executor.getExecution().name == execution.metadata.name) {
            this.controller.isUpdated.set(true)
            this.controller.executor.executor.run.compareAndSet(true, false)
            logger.info { "Current benchmark stopped." }
        }
    }
}
