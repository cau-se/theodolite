package rocks.theodolite.kubernetes.operator

import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import mu.KotlinLogging
import rocks.theodolite.kubernetes.model.BenchmarkExecution
import rocks.theodolite.kubernetes.model.crd.ExecutionCRD
import rocks.theodolite.kubernetes.model.crd.ExecutionState

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
    private val mapper: ObjectMapper = ObjectMapper()

    /**
     * Adds an execution to the end of the queue of the TheodoliteController.
     *
     * @param execution the execution to add
     */
    @Synchronized
    override fun onAdd(execution: ExecutionCRD) {
        logger.info { "Add execution ${execution.metadata.name}." }
        execution.spec.name = execution.metadata.name
        when (val currentState = this.stateHandler.getExecutionState(execution.metadata.name)) {
            ExecutionState.NO_STATE -> this.stateHandler.setExecutionState(execution.spec.name, ExecutionState.PENDING)
            ExecutionState.RUNNING -> {
                this.stateHandler.setExecutionState(execution.spec.name, ExecutionState.RESTART)
                if (this.controller.isExecutionRunning(execution.spec.name)) {
                    this.controller.stop(restart = true)
                }
            }
            else -> {
                logger.info { "ExecutionState '$currentState' is not handled." }
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
        if (mapper.writeValueAsString(oldExecution.spec) != mapper.writeValueAsString(newExecution.spec)) {
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
