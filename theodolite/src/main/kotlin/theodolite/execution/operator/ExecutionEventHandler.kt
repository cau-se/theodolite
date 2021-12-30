package theodolite.execution.operator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import mu.KotlinLogging
import theodolite.benchmark.BenchmarkExecution
import theodolite.model.crd.*

private val logger = KotlinLogging.logger {}

/**
 * Handles adding, updating and deleting BenchmarkExecutions.
 *
 * @param controller The TheodoliteController that handles the application state
 *
 * @see TheodoliteController
 * @see BenchmarkExecution
 */
class ExecutionEventHandler(
    private val controller: TheodoliteController,
    private val stateHandler: ExecutionStateHandler
) : ResourceEventHandler<ExecutionCRD> {

    private val gson: Gson = GsonBuilder().enableComplexMapKeySerialization().create()

    /**
     * Adds an execution to the end of the queue of the TheodoliteController.
     *
     * @param execution the execution to add
     */
    @Synchronized
    override fun onAdd(execution: ExecutionCRD) {
        logger.info { "Add execution ${execution.metadata.name}." }
        execution.spec.name = execution.metadata.name
        when (this.stateHandler.getExecutionState(execution.metadata.name)) {
            ExecutionState.NO_STATE -> this.stateHandler.setExecutionState(execution.spec.name, ExecutionState.PENDING)
            ExecutionState.RUNNING -> {
                this.stateHandler.setExecutionState(execution.spec.name, ExecutionState.RESTART)
                if (this.controller.isExecutionRunning(execution.spec.name)) {
                    this.controller.stop(restart = true)
                }
            }
        }
    }

    /**
     * To be called on update of an execution. If this execution is running at the time this function is called, it is stopped and
     * added to the beginning of the queue of the TheodoliteController.
     * Otherwise, it is just added to the beginning of the queue.
     *
     * @param oldExecution the old execution
     * @param newExecution the new execution
     */
    @Synchronized
    override fun onUpdate(oldExecution: ExecutionCRD, newExecution: ExecutionCRD) {
        newExecution.spec.name = newExecution.metadata.name
        oldExecution.spec.name = oldExecution.metadata.name
        if (gson.toJson(oldExecution.spec) != gson.toJson(newExecution.spec)) {
            logger.info { "Receive update event for execution ${oldExecution.metadata.name}." }
            when (this.stateHandler.getExecutionState(newExecution.metadata.name)) {
                ExecutionState.RUNNING -> {
                    this.stateHandler.setExecutionState(newExecution.spec.name, ExecutionState.RESTART)
                    if (this.controller.isExecutionRunning(newExecution.spec.name)) {
                        this.controller.stop(restart = true)
                    }
                }
                ExecutionState.RESTART -> {
                } // should this set to pending?
                else -> this.stateHandler.setExecutionState(newExecution.spec.name, ExecutionState.PENDING)
            }
        }
    }

    /**
     * Delete an execution from the queue of the TheodoliteController.
     *
     * @param execution the execution to delete
     */
    @Synchronized
    override fun onDelete(execution: ExecutionCRD, deletedFinalStateUnknown: Boolean) {
        logger.info { "Delete execution ${execution.metadata.name}." }
        if (execution.status.executionState == ExecutionState.RUNNING
            && this.controller.isExecutionRunning(execution.metadata.name)
        ) {
            this.controller.stop()
        }
    }
}
