package theodolite.execution.operator

import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution

private val logger = KotlinLogging.logger {}

/**
 * Handles adding, updating and deleting BenchmarkExecutions.
 *
 * @param controller The TheodoliteController that handles the application state
 *
 * @see TheodoliteController
 * @see BenchmarkExecution
 */
class ExecutionHandler(private val controller: TheodoliteController) : ResourceEventHandler<BenchmarkExecution> {

    /**
     * Add an execution to the end of the queue of the TheodoliteController.
     *
     * @param execution the execution to add
     */
    override fun onAdd(execution: BenchmarkExecution) {
        execution.name = execution.metadata.name
        logger.info { "Add new execution ${execution.metadata.name} to queue." }
        this.controller.executionsQueue.add(execution)
    }

    /**
     * Updates an execution. If this execution is running at the time this function is called, it is stopped and
     * added to the beginning of the queue of the TheodoliteController.
     * Otherwise, it is just added to the beginning of the queue.
     *
     * @param oldExecution the old execution
     * @param newExecution the new execution
     */
    override fun onUpdate(oldExecution: BenchmarkExecution, newExecution: BenchmarkExecution) {
        logger.info { "Add updated execution to queue." }
        newExecution.name = newExecution.metadata.name
        try {
            this.controller.executionsQueue.removeIf { e -> e.name == newExecution.metadata.name }
        } catch (e: NullPointerException) {
            logger.warn { "No execution found for deletion" }
        }
        this.controller.executionsQueue.addFirst(newExecution)
        if (this.controller.isInitialized() && this.controller.executor.getExecution().name == newExecution.metadata.name) {
            this.controller.isUpdated.set(true)
            this.controller.executor.executor.run.compareAndSet(true, false)
        }
    }

    /**
     * Delete an execution from the queue of the TheodoliteController.
     *
     * @param execution the execution to delete
     */
    override fun onDelete(execution: BenchmarkExecution, b: Boolean) {
        try {
            this.controller.executionsQueue.removeIf { e -> e.name == execution.metadata.name }
            logger.info { "Delete execution ${execution.metadata.name} from queue." }
        } catch (e: NullPointerException) {
            logger.warn { "No execution found for deletion" }
        }
        if (this.controller.isInitialized() && this.controller.executor.getExecution().name == execution.metadata.name) {
            this.controller.isUpdated.set(true)
            this.controller.executor.executor.run.compareAndSet(true, false)
            logger.info { "Current benchmark stopped." }
        }
    }
}
